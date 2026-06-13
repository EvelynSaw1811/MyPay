# MyPay E-Wallet System — Development Plan

## Project Summary

**System:** MyPay — Microservice-based E-Wallet with collaborative expense splitting  
**Group ID:** `com.mypay`  
**Stack:** Java 17 · Spring Boot 3.4.5 · Spring Cloud 2024.0.1 · MySQL 8.0 · Redis 7 · RabbitMQ 3 · Docker  
**Architecture:** 11 Maven modules — 1 shared lib, 2 infrastructure, 1 gateway, 7 business services

---

## Coding Standards (Non-Negotiable)

Apply to every file written in this project.

| Rule | Detail |
|---|---|
| Entity name | All UPPERCASE, singular (e.g., `USER`, `WALLET`) |
| Table name | All UPPERCASE + `_T` suffix (e.g., `USER_T`, `WALLET_ACCOUNT_T`) |
| Column prefix | First 3–4 letters of table name or initials (e.g., `user_id`, `wacc_balance`, `bs_name`) |
| Method name | lower camelCase |
| Class name | UpperCamelCase |
| ID type | `CHAR` (fixed length) or `INT` — no `VARCHAR` for IDs |
| Money type | `BigDecimal` only — **never** `float` or `double` |
| Principles | SOLID · DRY · YAGNI · Tell Don't Ask · Law of Demeter · Low Coupling |
| Must-have | Global exception handler · null checks · close resources after use |
| Response format | `ApiResponse<T>` envelope on every endpoint |

---

## Module Map & Build Order

```
Tier 0  common-lib              (shared JAR — built first, no Spring Boot)
Tier 1  config-server           (8888) — infrastructure only
        discovery-server        (8761) — depends on config-server
Tier 2  api-gateway             (8080) — depends on discovery + redis
Tier 3  auth-service            (8081)
        wallet-service          (8082)
        collection-service      (8083)
        transaction-service     (8084)
        currency-service        (8085)
        notification-service    (8086)
        reporting-service       (8087) — aggregator, no DB
```

Build every service in tier order. Do not start a tier before the previous one compiles cleanly.

---

## Phase 0 — Repository & Parent POM Setup

**Goal:** One Maven multi-module project that all services inherit from.

### 0.1 Root POM (`pom.xml`)

```xml
<groupId>com.mypay</groupId>
<artifactId>mypay</artifactId>
<packaging>pom</packaging>

<modules>
  common-lib, config-server, discovery-server, api-gateway,
  auth-service, wallet-service, collection-service,
  transaction-service, currency-service, notification-service,
  reporting-service
</modules>
```

Managed dependencies: Spring Boot 3.4.5, Spring Cloud 2024.0.1, jjwt 0.12.6, Lombok, Jakarta Validation.

### 0.2 Standard Service Package Layout

Every business service follows this exact layout:

```
src/main/java/com/mypay/<service>/
  <Service>Application.java
  client/          (Feign clients + fallback factories)
  config/          (Spring config beans)
  controller/      (REST controllers)
  dto/             (request + response POJOs)
  entity/          (JPA entities)
  exception/       (<Service>ExceptionHandler extends GlobalExceptionHandler)
  mapper/          (entity ↔ DTO converters)
  repository/      (Spring Data JPA interfaces)
  service/         (interfaces)
  service/impl/    (implementations)
```

### 0.3 Database Schemas

Six isolated databases — one per data-owning service:

| Database | Owner Service |
|---|---|
| `ewallet_auth_db` | auth-service |
| `ewallet_wallet_db` | wallet-service |
| `ewallet_collection_db` | collection-service |
| `ewallet_transaction_db` | transaction-service |
| `ewallet_currency_db` | currency-service |
| `ewallet_notification_db` | notification-service |

Create `init-schemas.sql` at project root that creates all six databases on MySQL startup.

---

## Phase 1 — common-lib (Shared Library)

**Build first. All other modules depend on this.**

### 1.1 Enums (`com.mypay.common.constant`)

| Class | Values |
|---|---|
| `CollectionCategory` | TRIP, EXPENSE, OTHER |
| `CollectionRole` | ADMIN, EDITOR, MEMBER |
| `CollectionStatus` | ACTIVE, CLOSED |
| `Currency` | MYR, SGD, USD (+ `DEFAULT_CODE = "MYR"`) |
| `InvitationStatus` | PENDING, ACCEPTED, DECLINED |
| `SplitType` | EQUAL, PERCENTAGE, EXACT, HYBRID, HIERARCHICAL |
| `TransactionStatus` | PENDING, COMPLETED, FAILED, REVERSED |
| `TransactionType` | SETTLEMENT, TOP_UP, TRANSFER, NETTING |

### 1.2 ApiResponse DTO (`com.mypay.common.dto`)

```java
public class ApiResponse<T> {
    boolean success;
    String message;
    @JsonInclude(NON_NULL) T data;
    LocalDateTime timestamp;

    static <T> ApiResponse<T> success(T data)
    static <T> ApiResponse<T> success(String message, T data)
    static <T> ApiResponse<T> error(String message)
}
```

### 1.3 Exception Hierarchy (`com.mypay.common.exception`)

```
BaseException (abstract, holds HttpStatus)
  ├── BadRequestException          400
  ├── UnauthorizedException        401
  ├── ForbiddenException           403
  ├── ResourceNotFoundException    404
  ├── ConflictException            409
  ├── DuplicateResourceException   409
  └── InsufficientBalanceException 422

GlobalExceptionHandler (@RestControllerAdvice)
  — catches all BaseException subclasses → ApiResponse.error(message)
  — catches MethodArgumentNotValidException → validation error details
  — catches Exception (fallback) → 500 internal error
```

### 1.4 Events (`com.mypay.common.event`)

```java
// NotificationEvent.java
String userId, type, title, message, referenceId;
LocalDateTime timestamp;

// RabbitMQConstants.java
String EXCHANGE         = "mypay.notifications";
String QUEUE_SETTLEMENT = "mypay.notifications.settlement";
String QUEUE_EXPENSE    = "mypay.notifications.expense";
String QUEUE_INVITATION = "mypay.notifications.invitation";
String KEY_SETTLEMENT   = "notification.settlement.#";
String KEY_EXPENSE      = "notification.expense.#";
String KEY_INVITATION   = "notification.invitation.#";
```

### 1.5 Utilities (`com.mypay.common.util`)

**MoneyUtil** — all ops use `scale=4, HALF_UP`:
- `add(BigDecimal a, BigDecimal b)`
- `subtract(BigDecimal a, BigDecimal b)`
- `multiply(BigDecimal a, BigDecimal b)`
- `divide(BigDecimal a, BigDecimal b)`
- `round(BigDecimal a)`

**NotificationHelper** — fire-and-forget wrapper used by services that call the notification Feign client without blocking.

---

## Phase 2 — Infrastructure Services

### 2.1 config-server (port 8888)

- Enable with `@EnableConfigServer`
- Native file profile: reads config from `classpath:/configurations/`
- Config files to create per service: `<service-name>.yml` + `<service-name>-docker.yml`
- Shared `application.yml` (Eureka defaults, RabbitMQ, Actuator, Resilience4j defaults)
- Docker profile overrides swap `localhost` → Docker service names

**Resilience4j defaults (shared `application.yml`):**
```yaml
sliding-window-type: COUNT_BASED
sliding-window-size: 10
failure-rate-threshold: 50
wait-duration-in-open-state: 10s
permitted-number-of-calls-in-half-open-state: 3
minimum-number-of-calls: 5
retry.max-attempts: 3
retry.wait-duration: 500ms
retry.enable-exponential-backoff: true
timelimiter.timeout-duration: 3s
```

### 2.2 discovery-server (port 8761)

- Enable with `@EnableEurekaServer`
- Standalone mode: self-registration off, self-preservation off for dev
- Fetches config from config-server on startup

### 2.3 api-gateway (port 8080)

**Routes** (defined in `api-gateway.yml` from config-server):

| Path | Service ID |
|---|---|
| `/api/auth/**` | AUTH-SERVICE |
| `/api/wallets/**` | WALLET-SERVICE |
| `/api/collections/**` | COLLECTION-SERVICE |
| `/api/transactions/**` | TRANSACTION-SERVICE |
| `/api/currency/**` | CURRENCY-SERVICE |
| `/api/notifications/**` | NOTIFICATION-SERVICE |
| `/api/reports/**` | REPORTING-SERVICE |

**Open paths (bypass JWT):** `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`

**JwtAuthenticationFilter logic:**
1. Check if path is open → pass through
2. Extract `Authorization: Bearer <token>` → return 401 if missing
3. Validate JWT signature + expiry with `JwtUtil` → return 401 if invalid
4. Extract `userId` from token subject
5. Inject `X-User-Id: <userId>` header → forward to downstream

**RateLimiterConfig:** Redis-backed, key by `X-User-Id` or client IP. Rate: 5 req/s, burst: 10.

No service performs its own JWT validation — all trust the `X-User-Id` header injected by the gateway.

---

## Phase 3 — auth-service (port 8081)

**Database:** `ewallet_auth_db`

### 3.1 Entities

**USER_T**
```
user_id       CHAR(36) PK    (UUID)
user_email    VARCHAR(255)   UNIQUE NOT NULL
user_phone    VARCHAR(20)
user_fname    VARCHAR(100)   NOT NULL
user_lname    VARCHAR(100)   NOT NULL
user_status   VARCHAR(20)    DEFAULT 'ACTIVE'
user_created  DATETIME
user_updated  DATETIME
```

**USER_CREDENTIAL_T**
```
ucrd_id       CHAR(36) PK    (UUID)
ucrd_user_id  CHAR(36)       FK → USER_T.user_id
ucrd_pwd_hash VARCHAR(255)   NOT NULL
ucrd_created  DATETIME
```

### 3.2 JWT Config

- Access token: 15 minutes
- Refresh token: 7 days
- Secret: 256-bit key (from config-server, not hardcoded)
- Library: `io.jsonwebtoken:jjwt` v0.12.6

### 3.3 API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Open | Register + auto-create wallet |
| POST | `/api/auth/login` | Open | Login → JWT pair |
| POST | `/api/auth/refresh` | Open | Refresh access token |
| POST | `/api/auth/logout` | JWT | Invalidate refresh token |
| GET | `/api/auth/users/{userId}` | JWT | Get user profile |

### 3.4 Registration Flow

1. Validate email uniqueness (`UserRepository.existsByUserEmail`)
2. Save `USER_T` entity (UUID generated)
3. BCrypt hash password → save `USER_CREDENTIAL_T`
4. **[Feign]** `POST wallet-service/api/wallets/internal/create` — create wallet with 3 accounts (MYR, SGD, USD)
   - Fallback: log warning, continue — registration must not fail if wallet service is down
5. Generate access + refresh JWT
6. Return `AuthResponse`

### 3.5 Feign Client

```java
// WalletClient.java — target: WALLET-SERVICE
@PostMapping("/api/wallets/internal/create")
ApiResponse<Void> createWallet(@RequestHeader("X-User-Id") String userId);

// WalletClientFallbackFactory.java
// Returns error response, logs warning — non-blocking fallback
```

---

## Phase 4 — wallet-service (port 8082)

**Database:** `ewallet_wallet_db`

### 4.1 Entities

**WALLET_T**
```
wllt_id       CHAR(36) PK
wllt_user_id  CHAR(36)    NOT NULL UNIQUE
wllt_created  DATETIME
wllt_updated  DATETIME
```

**WALLET_ACCOUNT_T**
```
wacc_id       CHAR(36) PK
wacc_wllt_id  CHAR(36)    FK → WALLET_T.wllt_id
wacc_currency VARCHAR(3)  NOT NULL
wacc_balance  DECIMAL(19,4) DEFAULT 0.0000
wacc_updated  DATETIME
UNIQUE(wacc_wllt_id, wacc_currency)
```

**PAYEE_T**
```
paye_id       CHAR(36) PK
paye_wllt_id  CHAR(36)    FK → WALLET_T.wllt_id
paye_user_id  CHAR(36)    NOT NULL
paye_nickname VARCHAR(100)
paye_created  DATETIME
UNIQUE(paye_wllt_id, paye_user_id)
```

### 4.2 API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/wallets/internal/create` | X-User-Id | Create wallet + 3 accounts |
| GET | `/api/wallets/me` | X-User-Id | Get my wallet + balances |
| GET | `/api/wallets/balance/{currency}` | X-User-Id | Get balance for currency |
| POST | `/api/wallets/topup` | X-User-Id | Add funds |
| POST | `/api/wallets/internal/debit/{userId}` | Internal | Deduct funds (settlement) |
| POST | `/api/wallets/internal/credit/{userId}` | Internal | Add funds (settlement) |
| GET | `/api/wallets/payees` | X-User-Id | List payees |
| POST | `/api/wallets/payees` | X-User-Id | Add payee |
| DELETE | `/api/wallets/payees/{payeeId}` | X-User-Id | Remove payee |

### 4.3 Key Business Rules

- On wallet creation: always create 3 `WALLET_ACCOUNT_T` rows for MYR, SGD, USD
- All balance operations use `MoneyUtil` — never raw arithmetic
- `debit` must throw `InsufficientBalanceException` if `wacc_balance < amount`
- `credit` and `debit` are internal-only; do not expose through gateway to clients directly (only transaction-service calls them)

---

## Phase 5 — collection-service (port 8083)

**Database:** `ewallet_collection_db`  
This is the largest service. Build the split engine before the API layer.

### 5.1 Entities

**COLLECTION_T**
```
coll_id       CHAR(36) PK
coll_name     VARCHAR(255) NOT NULL
coll_desc     VARCHAR(500)
coll_category VARCHAR(20)
coll_currency VARCHAR(3)   NOT NULL
coll_status   VARCHAR(20)  DEFAULT 'ACTIVE'
coll_owner_id CHAR(36)     NOT NULL
coll_created  DATETIME
coll_updated  DATETIME
```

**COLLECTION_MEMBER_T**
```
cm_id         CHAR(36) PK
cm_coll_id    CHAR(36)  FK → COLLECTION_T.coll_id
cm_user_id    CHAR(36)  NOT NULL
cm_role       VARCHAR(20) NOT NULL
cm_joined_at  DATETIME
UNIQUE(cm_coll_id, cm_user_id)
```

**EXPENSE_T**
```
exp_id        CHAR(36) PK
exp_coll_id   CHAR(36)  FK → COLLECTION_T.coll_id
exp_desc      VARCHAR(255)
exp_amount    DECIMAL(19,4) NOT NULL
exp_currency  VARCHAR(3)    NOT NULL
exp_paid_by   CHAR(36)      NOT NULL
exp_split_type VARCHAR(20)  NOT NULL
exp_tax_rate  DECIMAL(5,4)  DEFAULT NULL
exp_tax_type  VARCHAR(20)   DEFAULT NULL
exp_created   DATETIME
exp_updated   DATETIME
```

**SPLIT_RULE_T**
```
sr_id         CHAR(36) PK
sr_exp_id     CHAR(36)  FK → EXPENSE_T.exp_id
sr_user_id    CHAR(36)  NOT NULL
sr_percentage DECIMAL(8,4) DEFAULT NULL
sr_fixed_amt  DECIMAL(19,4) DEFAULT NULL
sr_weight     INT DEFAULT NULL
```

**EXPENSE_SHARE_T**
```
es_id         CHAR(36) PK
es_exp_id     CHAR(36)  FK → EXPENSE_T.exp_id
es_user_id    CHAR(36)  NOT NULL
es_base_amt   DECIMAL(19,4)
es_tax_amt    DECIMAL(19,4) DEFAULT 0.0000
es_total_amt  DECIMAL(19,4) NOT NULL
es_settled    BOOLEAN   DEFAULT FALSE
es_settled_at DATETIME  DEFAULT NULL
```

**INVITATION_T**
```
inv_id        CHAR(36) PK
inv_coll_id   CHAR(36)  FK → COLLECTION_T.coll_id
inv_inviter   CHAR(36)  NOT NULL
inv_invitee   CHAR(36)  NOT NULL
inv_role      VARCHAR(20) NOT NULL
inv_status    VARCHAR(20) DEFAULT 'PENDING'
inv_created   DATETIME
inv_updated   DATETIME
```

### 5.2 Split Engine (`engine/` package)

Build and unit-test this before connecting to the service layer.

**Interface:**
```java
public interface SplitStrategy {
    List<ShareResult> calculate(BigDecimal totalAmount, List<ParticipantShare> participants);
}
```

**ShareResult:**
```java
String userId;
BigDecimal baseAmount;
BigDecimal taxAmount;
BigDecimal totalAmount;
```

**Implementations (strict algorithm order):**

| Strategy | Algorithm |
|---|---|
| `EqualSplitStrategy` | `totalAmount / n` for each participant; remainder penny given to first participant |
| `ExactSplitStrategy` | Each participant has `sr_fixed_amt`; validate sum == totalAmount |
| `PercentageSplitStrategy` | Each gets `totalAmount * (percentage/100)`; validate percentages sum to 100 |
| `HybridSplitStrategy` | 1. Sum all fixed amounts → 2. Remainder = total − fixed sum → 3. Split remainder equally among non-fixed participants |
| `HierarchicalSplitStrategy` | Primary group shares a larger portion (by weight), secondary group shares the rest |

**Pipeline order for every strategy:**
1. Deduct fixed amounts (HYBRID/HIERARCHICAL)
2. Compute shared base
3. Apply division rule
4. Apply `TaxCalculator` individually per participant if `exp_tax_rate` is set
5. Apply `RemainderDistributor` — ensure `sum(es_total_amt) == totalAmount` exactly (no money loss)

**SplitStrategyFactory:**
```java
SplitStrategy create(SplitType type) {
    return switch (type) {
        case EQUAL -> new EqualSplitStrategy();
        case EXACT -> new ExactSplitStrategy();
        case PERCENTAGE -> new PercentageSplitStrategy();
        case HYBRID -> new HybridSplitStrategy();
        case HIERARCHICAL -> new HierarchicalSplitStrategy();
    };
}
```

**RemainderDistributor:** After computing all shares, subtract computed sum from `totalAmount`. If a remainder (positive or negative) exists due to rounding, add/subtract it from the first participant's share. This guarantees zero-sum.

### 5.3 Role Authorization (AOP)

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireCollectionRole {
    CollectionRole[] value();
}
```

`CollectionRoleAspect` (Around advice):
1. Extract `collectionId` from method parameter
2. Extract `userId` from `X-User-Id` request header
3. Query `CollectionMemberRepository`
4. If role insufficient → throw `ForbiddenException`

### 5.4 API Endpoints

**Collections:**
```
POST   /api/collections                          Create collection (caller becomes ADMIN)
GET    /api/collections                          My collections
GET    /api/collections/{cid}                    Collection detail
PUT    /api/collections/{cid}                    Update [ADMIN]
POST   /api/collections/{cid}/close              Close collection [ADMIN]
GET    /api/collections/{cid}/members            List members
DELETE /api/collections/{cid}/members/{userId}   Remove member [ADMIN]
GET    /api/collections/{cid}/balances           Net balance summary
```

**Expenses:**
```
POST   /api/collections/{cid}/expenses                        Create expense [EDITOR+]
GET    /api/collections/{cid}/expenses                        List expenses
GET    /api/collections/{cid}/expenses/{eid}                  Get expense
PUT    /api/collections/{cid}/expenses/{eid}                  Update expense [EDITOR+]
DELETE /api/collections/{cid}/expenses/{eid}                  Delete expense [EDITOR+]
GET    /api/collections/{cid}/expenses/{eid}/shares/{sid}     Get share (internal use by transaction-service)
PUT    /api/collections/{cid}/expenses/{eid}/shares/{sid}/settle    Mark settled (internal)
PUT    /api/collections/{cid}/expenses/{eid}/shares/{sid}/unsettle  Reverse settlement (internal)
```

**Invitations:**
```
POST   /api/collections/{cid}/invitations           Invite user [ADMIN]
GET    /api/collections/{cid}/invitations           List collection invitations [ADMIN]
GET    /api/invitations                             My pending invitations
POST   /api/invitations/{invId}/respond             Accept or decline
```

### 5.5 Expense Creation Flow

1. Validate user is ACTIVE collection member
2. Validate collection status is ACTIVE
3. Save `EXPENSE_T`
4. Save `SPLIT_RULE_T` rows (one per participant from request)
5. `SplitStrategyFactory.create(splitType)` → `strategy.calculate(totalAmount, participants)`
6. If `exp_tax_rate` set: `TaxCalculator.apply(results)` per participant
7. `RemainderDistributor.distribute(results, totalAmount)` — fix rounding
8. Save `EXPENSE_SHARE_T` rows
9. **[RabbitMQ]** publish `notification.expense.created` event
10. Return `ExpenseResponse` with full share breakdown

### 5.6 Messaging (RabbitMQ)

Producers: `ExpenseService` (expense.created), `InvitationService` (invitation.received)

`NotificationEventPublisher` wraps `RabbitTemplate.convertAndSend()`.

---

## Phase 6 — currency-service (port 8085)

**Database:** `ewallet_currency_db`  
Build this before transaction-service (which depends on it).

### 6.1 Entities

**CURRENCY_T**
```
curr_id      CHAR(36) PK
curr_code    CHAR(3)  NOT NULL UNIQUE
curr_name    VARCHAR(100)
curr_symbol  VARCHAR(5)
curr_active  BOOLEAN DEFAULT TRUE
```

**EXCHANGE_RATE_T**
```
exrt_id      CHAR(36) PK
exrt_base    CHAR(3)  NOT NULL
exrt_target  CHAR(3)  NOT NULL
exrt_rate    DECIMAL(20,6) NOT NULL
exrt_fetched DATETIME NOT NULL
```

### 6.2 Rate Resolution Strategy (cache-first)

1. **Redis cache** → return if TTL not expired (TTL from config, e.g., 1 hour)
2. **External API call** (mock/real) → store in Redis + persist to `EXCHANGE_RATE_T`
3. **DB fallback** → latest row for currency pair if external API fails
4. **Hardcoded defaults** → last resort, log warning

All rates stored as base = USD. Cross-rates computed via: `rate(A→B) = rate(USD→B) / rate(USD→A)`.

`DataInitializer`: seeds MYR, SGD, USD rows in `CURRENCY_T` on first startup if empty.

### 6.3 API Endpoints

```
GET /api/currency/rates                   All rates from base currency
GET /api/currency/rates/{from}/{to}       Specific pair rate
GET /api/currency/convert?from&to&amount  Convert amount
GET /api/currency/currencies             List supported currencies
```

---

## Phase 7 — transaction-service (port 8084)

**Database:** `ewallet_transaction_db`  
Most complex service. Implement saga pattern carefully.

### 7.1 Entities

**TRANSACTION_T**
```
txn_id            CHAR(36) PK
txn_payer_id      CHAR(36) NOT NULL
txn_payee_id      CHAR(36) NOT NULL
txn_amount        DECIMAL(19,4) NOT NULL
txn_currency      CHAR(3)
txn_converted_amt DECIMAL(19,4) DEFAULT NULL
txn_payee_curr    CHAR(3) DEFAULT NULL
txn_type          VARCHAR(20) NOT NULL
txn_status        VARCHAR(20) NOT NULL
txn_idem_key      VARCHAR(255) UNIQUE
txn_created       DATETIME
txn_updated       DATETIME
```

**SETTLEMENT_T**
```
stl_id        CHAR(36) PK
stl_txn_id    CHAR(36) FK → TRANSACTION_T.txn_id
stl_share_id  CHAR(36)
stl_coll_id   CHAR(36)
stl_payer_id  CHAR(36)
stl_payee_id  CHAR(36)
stl_amount    DECIMAL(19,4)
stl_created   DATETIME
```

**SAGA_STATE_T**
```
saga_id    CHAR(36) PK
saga_txn_id CHAR(36) FK → TRANSACTION_T.txn_id
saga_step  INT NOT NULL
saga_status VARCHAR(20)
saga_comp_step INT DEFAULT NULL
saga_updated  DATETIME
```

### 7.2 Settlement Saga (7 steps)

`SettlementSagaOrchestrator.execute(SettleRequest, userId)`:

```
Step 1: Validate share (Feign → collection-service: GET share, assert not settled)
Step 2: Currency conversion (Feign → currency-service: convert if currencies differ)
Step 3: Debit payer   (Feign → wallet-service: POST /internal/debit/{payerId})   ← CRITICAL
Step 4: Credit payee  (Feign → wallet-service: POST /internal/credit/{payeeId})  ← CRITICAL
Step 5: Create Settlement record
Step 6: Mark share settled (Feign → collection-service: PUT .../settle)
Step 7: Publish notification events (RabbitMQ: settlement.received + settlement.confirmed)
```

**Compensation (on any step failure — execute in reverse):**
- Reverse Step 6: unsettle share
- Reverse Step 4: debit back from payee
- Reverse Step 3: credit back to payer
- Mark Transaction as FAILED
- Update SagaState

### 7.3 Netting Saga (5 steps)

`NettingSagaOrchestrator.execute(NettingRequest, userId)`:

```
Step 1: Fetch all expenses for specified collections (Feign → collection-service)
Step 2: Calculate net balance between user and counterparty
Step 3: Debit net payer (Feign → wallet-service)
Step 4: Credit net payee (Feign → wallet-service)
Step 5: Mark all relevant shares settled (Feign → collection-service, batch)
```

Compensation reverses all wallet operations and unsettles all shares on failure.

### 7.4 Idempotency

Before any transaction: check `TRANSACTION_T.txn_idem_key`.  
If duplicate found → return existing transaction response instead of reprocessing.  
Client must supply idempotency key in request.

### 7.5 Circuit Breaker Config (transaction-service specific)

| Instance | Failure Threshold | Wait Duration |
|---|---|---|
| `walletServiceCB` | 30% | 5s (more sensitive — critical path) |
| `currencyServiceCB` | 50% | 10s |
| `collectionServiceCB` | 50% | 10s |

### 7.6 API Endpoints

```
POST /api/transactions/settle       Settle a single expense share
POST /api/transactions/settle-net   Bilateral netting across collections
GET  /api/transactions/history      Full transaction history
GET  /api/transactions/history/{currency}  Filtered by currency
```

---

## Phase 8 — notification-service (port 8086)

**Database:** `ewallet_notification_db`

### 8.1 Entity

**NOTIFICATION_T**
```
notf_id       CHAR(36) PK
notf_user_id  CHAR(36) NOT NULL
notf_type     VARCHAR(50)
notf_title    VARCHAR(255)
notf_message  TEXT
notf_ref_id   CHAR(36) DEFAULT NULL
notf_read     BOOLEAN DEFAULT FALSE
notf_read_at  DATETIME DEFAULT NULL
notf_created  DATETIME
```

### 8.2 RabbitMQ Consumer

`NotificationEventConsumer` — three `@RabbitListener` methods:
- Queue `mypay.notifications.settlement` → `handleSettlement(NotificationEvent)`
- Queue `mypay.notifications.expense` → `handleExpense(NotificationEvent)`
- Queue `mypay.notifications.invitation` → `handleInvitation(NotificationEvent)`

Each listener: deserialize event → save `NOTIFICATION_T` row. Do not throw — log errors and ack the message to prevent requeue loops.

### 8.3 API Endpoints

```
POST   /api/notifications/internal/create   Create notification (called by other services via Feign)
GET    /api/notifications                   All my notifications
GET    /api/notifications/unread            Unread only
GET    /api/notifications/unread/count      Unread count
PUT    /api/notifications/{id}/read         Mark one as read
PUT    /api/notifications/read-all          Mark all as read
```

---

## Phase 9 — reporting-service (port 8087)

**No database** — pure aggregation via Feign.  
Exclude `DataSource` and JPA auto-configuration.

### 9.1 Data Sources (all via Feign, all have fallback)

| Feign Client | Operations |
|---|---|
| `WalletClient` | `GET /api/wallets/me` |
| `CollectionClient` | `GET /api/collections`, `GET /api/collections/{id}/expenses`, `GET /api/collections/{id}/balances` |
| `TransactionClient` | `GET /api/transactions/history`, `GET /api/transactions/history/{currency}` |
| `CurrencyClient` | `GET /api/currency/convert` |

All fallback factories return empty lists / null maps — partial data is acceptable.

### 9.2 Reports & Endpoints

```
GET /api/reports/dashboard             UserDashboardReport — wallet summary, collection count, transaction totals
GET /api/reports/spending              SpendingSummaryReport — total spent/received, net, by type
GET /api/reports/ledger/{currency}     CurrencyLedgerReport — inflow/outflow per currency
GET /api/reports/collections/{cid}     CollectionStatementReport — expenses + member positions
GET /api/reports/position              PersonalPositionReport — owed/owing per collection
```

Circuit breaker timeout on all Feign calls: 5s (extended vs. default 3s to allow aggregation time).

---

## Phase 10 — Testing Strategy

### 10.1 Unit Tests (per service)

| Target | Test Cases |
|---|---|
| `EqualSplitStrategy` | 3 participants, odd amount → remainder goes to first |
| `PercentageSplitStrategy` | Percentages sum to 100 · percentages don't sum to 100 (BadRequest) |
| `ExactSplitStrategy` | Fixed amounts sum != total → BadRequest |
| `HybridSplitStrategy` | Fixed + equal remainder distribution |
| `HierarchicalSplitStrategy` | Primary/secondary tier weighting |
| `RemainderDistributor` | Zero-sum guarantee after rounding |
| `TaxCalculator` | Simple tax · compound tax · zero tax |
| `MoneyUtil` | Scale/rounding correctness with BigDecimal edge cases |
| `WalletServiceImpl` | Insufficient balance exception · currency not found |
| `AuthServiceImpl` | Duplicate email registration · wrong password |

### 10.2 Integration Tests (per service)

- Auth: register → wallet auto-created (mock `WalletClient`)
- Wallet: top-up → balance updated; debit beyond balance → exception
- Collection: create expense → shares sum equals total; split engine E2E
- Transaction: settlement saga with mock Feign clients; compensation on step 4 failure

### 10.3 Scenario Tests (full system)

Run against Docker Compose stack:

| Scenario | Steps |
|---|---|
| Full user journey | Register → Login → TopUp → CreateCollection → CreateExpense → Settle |
| Multi-currency split | Expense in SGD, payer wallet MYR → currency conversion via currency-service |
| Tax + hybrid split | Expense with 10% tax, 2 fixed + 2 equal remainder participants |
| Concurrent payments | Two users settle same share simultaneously → idempotency key prevents double settlement |
| Circuit breaker | Kill wallet-service → transaction-service CB opens → error returned, no partial state |

### 10.4 Postman Collections

- Organize collections in `postman/collections/` per service
- Use environment variables for tokens, base URL, user IDs
- Document test cases matching integration scenarios above

---

## Phase 11 — Docker Compose Deployment

### 11.1 Dockerfile (all services, same pattern)

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE <port>
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
```

### 11.2 docker-compose.yml Startup Order

```
Layer 1 (parallel): mysql, redis, rabbitmq
Layer 2:            config-server (healthcheck: /actuator/health)
Layer 3:            discovery-server
Layer 4:            api-gateway
Layer 5 (parallel): auth, wallet, collection, transaction, currency, notification, reporting
```

Use `depends_on: condition: service_healthy` for config-server and discovery-server.

### 11.3 Build & Run

```bash
# Build all jars
./mvnw clean package -DskipTests

# Start full stack
docker-compose up --build

# Verify
curl http://localhost:8761          # Eureka dashboard
curl http://localhost:8080/api/auth/register  # Gateway reachable
```

---

## Development Sequence (Recommended Order)

| Order | Module | Reason |
|---|---|---|
| 1 | common-lib | Everything depends on it |
| 2 | config-server | Services bootstrap from it |
| 3 | discovery-server | Services register here |
| 4 | api-gateway | Gateway needed for end-to-end testing |
| 5 | auth-service | All scenarios start with login |
| 6 | wallet-service | Auth-service calls it on registration |
| 7 | currency-service | Transaction-service depends on it |
| 8 | collection-service | Transaction-service validates shares here |
| 9 | transaction-service | Depends on wallet, collection, currency |
| 10 | notification-service | Async — can be added anytime after RabbitMQ is up |
| 11 | reporting-service | Depends on all others being stable |
| 12 | Docker Compose | Final integration |

---

## Key Invariants to Enforce Everywhere

1. **Zero-sum splits** — `sum(EXPENSE_SHARE_T.es_total_amt)` must equal `EXPENSE_T.exp_amount` exactly
2. **No double settlement** — idempotency key on every transaction; saga must be atomic
3. **BigDecimal only** — never `float` or `double` for any monetary value
4. **Internal endpoints** — `/internal/` paths are inter-service only; document this clearly; no gateway route exposes them directly to clients
5. **No shared databases** — services must not query another service's database directly; all cross-service data goes through API
6. **Circular dependencies** — the dependency graph must remain acyclic: `auth→wallet`, `collection→notification`, `transaction→{wallet,collection,currency,notification}`, `reporting→{wallet,collection,transaction,currency}`
