# MyPay — Project Structure & Component Reference

## Top-Level Layout

```
MyPay/
├── BACKEND/                   ← Spring Boot microservices (11 services)
├── FRONTEND/
│   └── mypay-frontend/        ← React + Vite single-page application
├── PROJECT_INFORMATION/       ← Documentation, requirements, seed reference
├── README.md                  ← Startup guide
└── .gitignore
```

---

## Backend Architecture

**Technology:** Java 17, Spring Boot 3.4.5, Spring Cloud 2024.0.1, Maven (multi-module)  
**Infrastructure:** MySQL, Redis, RabbitMQ, Docker Compose  
**Parent POM:** `BACKEND/pom.xml` — `com.mypay:mypay`

### Service Map

| Service | Port | Responsibility |
|---------|------|----------------|
| config-server | 8888 | Centralised configuration for all services |
| discovery-server | 8761 | Eureka service registry |
| api-gateway | 8080 | Single entry point, JWT auth, routing |
| auth-service | 8081 | Authentication, user accounts, JWT tokens |
| wallet-service | 8082 | Multi-currency wallets, balances, payees |
| collection-service | 8083 | Group expense collections, bill splitting |
| transaction-service | 8084 | Settlements, netting, Saga orchestration |
| currency-service | 8085 | Exchange rates, currency conversion |
| notification-service | 8086 | Event-driven notifications (RabbitMQ consumer) |
| reporting-service | 8087 | Aggregated reports and dashboard (no own DB) |
| common-lib | — | Shared constants, exceptions, DTOs, utilities |

---

## Service Details

### 1. config-server
**Purpose:** Spring Cloud Config Server — serves per-service YAML configuration files.  
**Key class:** `ConfigServerApplication` (`@EnableConfigServer`)  
**Config files:** `src/main/resources/configurations/` — one `<service>.yml` and `<service>-docker.yml` per service.

---

### 2. discovery-server
**Purpose:** Eureka service registry. All services register here; the API gateway resolves service names (e.g., `lb://WALLET-SERVICE`) at runtime.  
**Key class:** `DiscoveryServerApplication` (`@EnableEurekaServer`)

---

### 3. api-gateway
**Purpose:** Unified ingress. Validates JWT on every request, extracts the authenticated `userId` into the `X-User-Id` header, routes to the correct downstream service, and blocks external access to `/internal/` paths.  
**Key classes:**
- `JwtAuthenticationFilter` — global reactive filter; validates token, injects `X-User-Id`
- `JwtUtil` — token parsing and validation
- `RateLimiterConfig` — Redis-backed per-user rate limiting  
**Routes (from `api-gateway.yml`):**

| Path prefix | Downstream service |
|-------------|-------------------|
| `/api/auth/**` | AUTH-SERVICE |
| `/api/wallets/**` | WALLET-SERVICE |
| `/api/collections/**`, `/api/invitations/**` | COLLECTION-SERVICE |
| `/api/transactions/**` | TRANSACTION-SERVICE |
| `/api/currency/**` | CURRENCY-SERVICE |
| `/api/notifications/**` | NOTIFICATION-SERVICE |
| `/api/reports/**` | REPORTING-SERVICE |

---

### 4. auth-service
**Purpose:** User registration and login; issues JWT access + refresh tokens; revokes tokens on logout.  
**Package:** `com.mypay.auth`

| Layer | Classes |
|-------|---------|
| controller | `AuthController` |
| service | `AuthService` (interface), `AuthServiceImpl` |
| entity | `User`, `UserCredential`, `RevokedToken` |
| repository | `UserRepository`, `UserCredentialRepository`, `RevokedTokenRepository` |
| config | `JwtConfig`, `JwtUtil`, `SecurityConfig` |
| client | `WalletClient` (Feign) + fallback factory |
| dto | `LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserResponse`, `RefreshTokenRequest`, `LogoutRequest` |
| mapper | `UserMapper` |
| exception | `AuthExceptionHandler` |
| seed | `SeedDataInitializer` (inserts 6 test users via JdbcTemplate; `@Profile("dev")`) |

**Database tables:** `USER_T`, `USER_CREDENTIAL_T`, `REVOKED_TOKEN_T`  
**Cross-service call:** On registration, calls Wallet-Service `POST /api/wallets/internal/create` (Feign) to provision the user's wallet.

---

### 5. wallet-service
**Purpose:** Manages user wallets. Each wallet holds multiple currency accounts (MYR, SGD, USD). Supports top-up, internal debit/credit (called by Transaction-Service during settlement), and saved payees.  
**Package:** `com.mypay.wallet`

| Layer | Classes |
|-------|---------|
| controller | `WalletController`, `PayeeController` |
| service | `WalletService`, `PayeeService` (interfaces) + `impl/` |
| entity | `Wallet`, `WalletAccount`, `Payee` |
| repository | `WalletRepository`, `WalletAccountRepository`, `PayeeRepository` |
| dto | `WalletResponse`, `AccountResponse`, `TopUpRequest`, `WalletOperationRequest`, `PayeeRequest`, `PayeeResponse` |
| mapper | `WalletMapper` |
| exception | `WalletExceptionHandler` |
| seed | `SeedDataInitializer` (6 wallets × 3 currencies + payee contacts) |

**Database tables:** `WALLET_T`, `WALLET_ACCOUNT_T`, `PAYEE_T`  
**Internal endpoints (not exposed externally):** `/api/wallets/internal/create`, `/api/wallets/internal/debit/{userId}`, `/api/wallets/internal/credit/{userId}`

---

### 6. collection-service
**Purpose:** Group expense management. Users create collections (trips, shared expenses), invite members, and record expenses split by one of five strategies.  
**Package:** `com.mypay.collection`

| Layer | Classes |
|-------|---------|
| controller | `CollectionController`, `ExpenseController`, `InvitationController` |
| service | `CollectionService`, `ExpenseService`, `InvitationService` (interfaces) + `impl/` |
| entity | `Collection`, `CollectionMember`, `Expense`, `ExpenseShare`, `Invitation`, `SplitRule` |
| repository | 6 repositories (one per entity) |
| engine | `SplitStrategy` (interface), 5 implementations, `SplitStrategyFactory`, `TaxCalculator`, `RemainderDistributor`, `ShareResult` |
| config | `RequireCollectionRole` (annotation), `CollectionRoleAspect` (AOP enforcement), `RabbitMQConfig` |
| messaging | `NotificationEventPublisher` (publishes to RabbitMQ) |
| client | `NotificationClient` (Feign) + fallback |
| dto, mapper, exception | standard layers |
| seed | `SeedDataInitializer` (5 collections, 7 expenses covering all split types) |

**Database tables:** `COLLECTION_T`, `COLLECTION_MEMBER_T`, `EXPENSE_T`, `EXPENSE_SHARE_T`, `INVITATION_T`, `SPLIT_RULE_T`

**Split strategies:**

| Strategy | Class | Rule |
|----------|-------|------|
| EQUAL | `EqualSplitStrategy` | Total ÷ n, remainder to first member |
| EXACT | `ExactSplitStrategy` | Fixed amounts per member |
| PERCENTAGE | `PercentageSplitStrategy` | Percentage weights per member |
| HIERARCHICAL | `HierarchicalSplitStrategy` | Integer weights, ratio-based |
| HYBRID | `HybridSplitStrategy` | Fixed amounts first, equal split of remainder |

**Events published (RabbitMQ):**
- `notification.expense.created` — when a new expense is recorded
- `notification.invitation.sent` — when an invitation is dispatched

---

### 7. transaction-service
**Purpose:** Orchestrates money movement. Handles direct one-to-one settlements and multi-party netting (offsets mutual debts). Uses the Saga pattern to ensure consistency across Wallet-Service debit/credit operations.  
**Package:** `com.mypay.transaction`

| Layer | Classes |
|-------|---------|
| controller | `TransactionController` |
| service | `TransactionService` (interface), `TransactionServiceImpl` |
| entity | `Transaction`, `Settlement`, `SagaState` |
| repository | `TransactionRepository`, `SettlementRepository`, `SagaStateRepository` |
| saga | `SettlementSagaOrchestrator`, `NettingSagaOrchestrator` |
| client | `WalletClient`, `CollectionClient`, `CurrencyClient`, `NotificationClient` (Feign) + fallback factories |
| dto | `SettleRequest`, `NettingRequest`, `NettingResponse`, `SettlementResponse`, `TransactionResponse`, `ExpenseShareInfo`, `ShareResponse` |
| exception | `TransactionExceptionHandler` |
| seed | `SeedDataInitializer` (4 top-ups + 4 settlements) |

**Database tables:** `TRANSACTION_T`, `SETTLEMENT_T`, `SAGA_STATE_T`

---

### 8. currency-service
**Purpose:** Maintains a table of currency exchange rates and performs conversions. Rates are cached in Redis.  
**Package:** `com.mypay.currency`

| Layer | Classes |
|-------|---------|
| controller | `CurrencyController` |
| service | `CurrencyService` (interface), `CurrencyServiceImpl` |
| entity | `CurrencyEntity`, `ExchangeRate` |
| repository | `CurrencyRepository`, `ExchangeRateRepository` |
| config | `AppConfig`, `RedisConfig`, `DataInitializer` (seeds currency codes and rates on startup) |
| dto | `CurrencyResponse`, `ExchangeRateResponse`, `ConversionResponse` |
| exception | `CurrencyExceptionHandler` |

**Database tables:** `CURRENCY_T`, `EXCHANGE_RATE_T`  
**Cache:** Redis (exchange rates)

---

### 9. notification-service
**Purpose:** Consumes RabbitMQ events from collection-service and transaction-service and persists notification records for users to retrieve.  
**Package:** `com.mypay.notification`

| Layer | Classes |
|-------|---------|
| controller | `NotificationController` |
| service | `NotificationService` (interface), `NotificationServiceImpl` |
| entity | `Notification` |
| repository | `NotificationRepository` |
| messaging | `NotificationEventConsumer` (RabbitMQ `@RabbitListener`) |
| config | `RabbitMQConfig` |
| dto | `CreateNotificationRequest`, `NotificationResponse` |
| exception | `NotificationExceptionHandler` |

**Database table:** `NOTIFICATION_T`  
**Queues consumed:**
- `mypay.notifications.settlement` (routing key: `notification.settlement.#`)
- `mypay.notifications.expense` (routing key: `notification.expense.#`)
- `mypay.notifications.invitation` (routing key: `notification.invitation.#`)

---

### 10. reporting-service
**Purpose:** Read-only aggregation service. No own database. Calls four other services via Feign and assembles composite reports.  
**Package:** `com.mypay.reporting`

| Layer | Classes |
|-------|---------|
| controller | `ReportingController` |
| service | `ReportingService` (interface), `ReportingServiceImpl` |
| client | `WalletClient`, `CollectionClient`, `TransactionClient`, `CurrencyClient` (Feign) + fallback factories |
| dto | `UserDashboardReport`, `SpendingSummaryReport`, `CurrencyLedgerReport`, `CollectionStatementReport`, `PersonalPositionReport` |
| exception | `ReportingExceptionHandler` |

**Reports available:**

| Endpoint | Report |
|----------|--------|
| `GET /api/reports/dashboard` | Aggregated wallet + balance overview |
| `GET /api/reports/spending` | Spending summary by category/currency |
| `GET /api/reports/ledger/{currency}` | Currency-specific transaction ledger |
| `GET /api/reports/collections/{id}` | Full collection statement |
| `GET /api/reports/position` | Who owes whom (net position) |

---

### 11. common-lib
**Purpose:** Shared library imported by all services. Contains no runnable application — compiled into a JAR and referenced as a Maven dependency.  
**Package:** `com.mypay.common`

| Sub-package | Contents |
|-------------|----------|
| `constant` | `CollectionCategory`, `CollectionRole` (ADMIN/EDITOR/MEMBER), `CollectionStatus`, `SplitType` (5 types), `TransactionStatus`, `TransactionType` |
| `dto` | `ApiResponse<T>` (standard response envelope), `NotificationEvent` (RabbitMQ payload) |
| `event` | `RabbitMQConstants` (exchange name, queue names, routing keys) |
| `exception` | `BaseException`, `BadRequestException`, `ConflictException`, `DuplicateResourceException`, `ForbiddenException`, `GlobalExceptionHandler`, `InsufficientBalanceException`, `ResourceNotFoundException`, `UnauthorizedException` |
| `util` | `MoneyUtil` (rounding, money arithmetic), `NotificationHelper` |

---

## Communication Patterns

### Synchronous — REST via Feign + Eureka

```
Frontend ──HTTPS──► API Gateway ──HTTP──► Target Service
                                               │ (internal only)
                                         Feign ─────► Other Services
```

Internal cross-service paths:
- `auth-service → wallet-service`: create wallet on registration
- `transaction-service → wallet-service`: debit/credit during settlement
- `transaction-service → collection-service`: read expense share data
- `transaction-service → currency-service`: convert amounts
- `reporting-service → wallet, collection, transaction, currency`: aggregate data

All Feign clients have a `*FallbackFactory` for circuit-breaker resilience.

### Asynchronous — RabbitMQ Topic Exchange

```
collection-service ──publish──► mypay.notifications (Topic Exchange) ──► notification-service
transaction-service ──publish──►                                        └─ (consumed by queues)
```

| Routing key pattern | Queue | Source |
|--------------------|-------|--------|
| `notification.expense.#` | `mypay.notifications.expense` | collection-service |
| `notification.invitation.#` | `mypay.notifications.invitation` | collection-service |
| `notification.settlement.#` | `mypay.notifications.settlement` | transaction-service |

### Service Discovery — Eureka

All services register with discovery-server. The API gateway resolves downstream services by logical name (e.g., `lb://WALLET-SERVICE`) — no hardcoded IPs.

### Centralised Configuration — Spring Cloud Config

All services pull their configuration from config-server on startup. Config files live in `config-server/src/main/resources/configurations/`. Docker profiles (`-docker.yml`) override host names (e.g., `localhost` → `mypay-mysql`) for containerised deployment.

---

## Frontend Architecture

**Path:** `FRONTEND/mypay-frontend/`  
**Technology:** React 19, Vite 8, Axios, React Router, TanStack Query v5

### Directory Structure

```
src/
├── api/               ← Axios call functions (one file per service)
│   ├── client.js      ← Axios instance: JWT headers, auto-refresh on 401
│   ├── auth.js        ← login, register, logout, refresh
│   ├── wallet.js      ← wallet balance, top-up
│   ├── collection.js  ← collection CRUD
│   ├── expense.js     ← expense management
│   ├── invitation.js  ← invitation accept/reject
│   ├── transaction.js ← settle, settle-net
│   ├── currency.js    ← rates, convert
│   ├── notification.js
│   ├── payee.js
│   └── reporting.js
│
├── components/
│   ├── layout/        ← PageLayout, TopBar, BottomNav (mobile nav)
│   ├── ui/            ← Button, Card, Input, Select, Modal, Badge, EmptyState, LoadingSpinner
│   ├── collection/    ← ExpenseShareBreakdown, SplitRuleBuilder
│   ├── transaction/   ← SettlementModal
│   └── wallet/        ← CurrencyBalanceCard
│
├── contexts/
│   ├── AuthContext.jsx        ← Login state, token storage, user profile
│   └── NotificationContext.jsx
│
├── pages/
│   ├── auth/          ← LoginPage, RegisterPage
│   ├── home/          ← DashboardPage (balance hero, wallet grid, quick actions)
│   ├── wallet/        ← WalletPage, TopUpPage, HistoryPage, RatesPage
│   ├── collections/   ← CollectionsPage, CreateCollectionPage, CollectionDetailPage,
│   │                     CollectionSettingsPage, AddExpensePage, ExpenseDetailPage
│   ├── settlement/    ← SettlePage, NettingPage
│   ├── reports/       ← ReportsHubPage, SpendingReportPage, LedgerReportPage,
│   │                     CollectionReportPage, PositionReportPage
│   ├── invitations/   ← InvitationsPage
│   ├── notifications/ ← NotificationsPage
│   ├── payees/        ← PayeesPage
│   └── profile/       ← ProfilePage
│
├── routes/
│   ├── AppRoutes.jsx      ← Route definitions
│   └── ProtectedRoute.jsx ← Guards pages behind authentication
│
└── utils/
    ├── currency.js    ← formatAmount, currency helpers
    └── date.js        ← date formatting
```

### Key Frontend Behaviours

- **JWT auto-attach:** `client.js` request interceptor adds `Authorization: Bearer <token>` to every request.
- **Auto token refresh:** On `401`, `client.js` pauses the failed request, calls `/api/auth/refresh`, retries. Concurrent requests are queued and replayed after refresh. On refresh failure, clears storage and redirects to `/login`.
- **TanStack Query v5:** All data fetching uses `useQuery` / `useMutation`. Named API exports are used; response data accessed as `data?.data` (Axios wraps in `.data`, backend wraps in `ApiResponse.data`).
- **Mobile-first layout:** 480 px max-width, bottom navigation bar, Ant Design colour tokens (`#1677FF` primary, `#52C41A` success, `#FF4D4F` danger).

---

## Infrastructure Components (Docker Compose)

| Container | Image | Purpose |
|-----------|-------|---------|
| mypay-mysql | mysql:8 | Single MySQL instance; each service gets its own schema |
| mypay-redis | redis:7 | Cache for currency exchange rates; rate-limiter store |
| mypay-rabbitmq | rabbitmq:3-management | Async event bus (topic exchange) |
| mypay-config-server | built from source | Config-server boots first |
| mypay-discovery-server | built from source | Eureka boots second |
| mypay-api-gateway | built from source | Boots after discovery |
| mypay-* (8 services) | built from source | Start after gateway |

All services run with `SPRING_PROFILES_ACTIVE=docker,dev` in local development; the `dev` profile activates seed data.

---

## Database Tables Summary

| Service | Tables |
|---------|--------|
| auth | USER_T, USER_CREDENTIAL_T, REVOKED_TOKEN_T |
| wallet | WALLET_T, WALLET_ACCOUNT_T, PAYEE_T |
| collection | COLLECTION_T, COLLECTION_MEMBER_T, EXPENSE_T, EXPENSE_SHARE_T, INVITATION_T, SPLIT_RULE_T |
| transaction | TRANSACTION_T, SETTLEMENT_T, SAGA_STATE_T |
| currency | CURRENCY_T, EXCHANGE_RATE_T |
| notification | NOTIFICATION_T |
