# MyPay — Project Analysis
**Date:** 2026-05-06  
**Scope:** Tech stack audit · Remaining work · Architecture validation

---

## 1. Tech Stack & Middleware — Full Inventory

### 1.1 Backend

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Language | Java | 17 | All microservices |
| Framework | Spring Boot | 3.4.5 | Service foundation, auto-configuration |
| Service discovery | Spring Cloud Netflix Eureka | Spring Cloud 2024.x | Services self-register at `:8761`; gateway routes by name |
| Centralised config | Spring Cloud Config Server | Spring Cloud 2024.x | Serves all `application.yml` configs from classpath; supports `native` and `docker` profiles |
| API Gateway | Spring Cloud Gateway | Spring Cloud 2024.x | Single ingress on `:8080`; JWT auth filter + rate limiting |
| Inter-service calls | OpenFeign (Spring Cloud OpenFeign) | Spring Cloud 2024.x | Declarative HTTP clients between services |
| Fault tolerance | Resilience4j | 2.x | Circuit breaker, retry, time limiter on every Feign client |
| Async messaging | RabbitMQ | 3-management-alpine | Event bus for settlement → notification pipeline |
| Database | MySQL | 8.0 | Persistent store; each service has its own isolated schema |
| ORM | Spring Data JPA / Hibernate | Bundled with Boot | Entity mapping, repository pattern |
| Authentication | Spring Security + JJWT | — | JWT access tokens (15 min TTL) + refresh tokens (7 days TTL) |
| Token revocation | Database blacklist (`REVOKED_TOKEN_T`) | — | SHA-256 hashed refresh tokens stored in MySQL; no Redis dependency for auth |
| Rate limiting | Spring Cloud Gateway + Redis | — | Token-bucket rate limiter applied at the gateway |
| Caching | Redis | 7-alpine | Exchange rate cache in currency-service |
| Shared library | `common-lib` Maven module | — | `ApiResponse<T>`, enums (`SplitType`, `CollectionRole`, `Currency`), exceptions, `NotificationEvent`, `RabbitMQConstants` |
| Build tool | Maven (multi-module) | — | Parent POM coordinates all service modules |
| Containerisation | Docker + Docker Compose | — | One-command startup of all infrastructure and 11 services with ordered health checks |

### 1.2 Frontend

| Concern | Technology | Version |
|---|---|---|
| Framework | React | 19 (Vite 8) |
| Routing | React Router | v7 |
| HTTP client | Axios | 1.x — with JWT interceptor, auto-refresh queue, redirect on failure |
| Server state | TanStack Query | v5 — caching, loading/error states, stale-time management |
| Client state | React Context API | built-in — AuthContext + NotificationContext (30 s polling) |
| Styling | Tailwind CSS | v3 — mobile-first, 480 px max-width root, custom blue/grayscale palette |
| Date formatting | day.js | 1.x — `formatDate`, `fromNow` via `relativeTime` plugin |
| Unique IDs | `crypto.randomUUID()` | built-in — idempotency keys for settlement mutations |

### 1.3 Infrastructure & DevOps

| Tool | Role |
|---|---|
| Docker Desktop | Runs MySQL, Redis, RabbitMQ and all 11 services as containers |
| Docker Compose | Defines startup order using `depends_on` + `healthcheck` (layered: infra → config → discovery → gateway → business services) |
| Eureka Dashboard (`:8761`) | Live service registry — shows all registered instances and their health |
| RabbitMQ Management UI (`:15672`) | Message queue monitoring — queues, consumers, message rates |
| Spring Boot Actuator | `/actuator/health` endpoint on every service; used by Docker health checks |

---

## 2. Remaining Components to Develop

### 2.1 Frontend — Incomplete / Missing

| Item | Status | Detail |
|---|---|---|
| "Remind All" button (Admin) | UI rendered, wired to nothing | Backend has no `POST /api/collections/:id/remind` endpoint. Button is disabled with tooltip until endpoint is available. |
| Expense edit pre-fill | Route exists, partial | `AddExpensePage` loads existing expense via `getExpense`; pre-fill works only if the backend returns `splitRule` embedded in the expense response. Needs verification. |
| Invite from Collection Detail | Functional via query param | `CollectionDetailPage` navigates to `/app/invitations?colId=…`; the modal pre-fills from query param. Minor UX — no inline invite sheet on the collection page itself. |
| `EmptyState` icon prop | Removed in minimalist retheme | Callers that still pass `icon=` compile fine (prop silently ignored). No visual breakage but callers could be cleaned up. |

### 2.2 Backend — Incomplete / Missing

| Item | Status | What is needed |
|---|---|---|
| `POST /api/collections/:id/remind` | **Not implemented** | Controller endpoint, service method, RabbitMQ event publication, and notification consumer handler. Needed to fulfil "Admin triggers payment reminder" requirement. |
| Revoked token cleanup scheduler | Written but not wired | `RevokedTokenRepository.deleteExpiredTokens()` exists but no `@Scheduled` task calls it. Expired tokens accumulate in `REVOKED_TOKEN_T` indefinitely. Add `@Scheduled(cron = "0 0 3 * * *")` in `AuthServiceImpl` or a dedicated `TokenCleanupScheduler` component. |
| `GET /api/collections/:colId/expenses/:expId` (transaction-service Feign) | Added as Feign method in transaction-service | Need to verify collection-service actually exposes this exact path and returns the `paidBy` field in the response body. |
| Reporting service end-to-end | Aggregation logic written | `ReportingServiceImpl` makes Feign calls to wallet, transaction, collection, and currency services. Not tested end-to-end under real data. Edge cases (empty collections, multi-currency positions) need validation. |

### 2.3 Middleware Integration — Not Yet Tested

| Integration | Risk / Gap |
|---|---|
| RabbitMQ settlement → notification pipeline | `NotificationEventPublisher` (transaction-service) publishes; `NotificationEventConsumer` (notification-service) consumes. Written but not tested end-to-end with real data. |
| Feign circuit breaker behaviour | Fallback factories return meaningful errors. Circuit breaker thresholds (50% failure rate, 10-call window) are default values — need tuning based on observed latency. |
| Cross-currency settlement accuracy | `SettlementSagaOrchestrator` fetches live rate from currency-service at settlement time. Redis TTL on rates (not specified in currency-service config) may cause stale rate usage. |
| Frontend ↔ Backend field naming | Backend uses snake_case in some DTOs; Axios receives camelCase from Spring (Jackson default). Any inconsistency will silently produce `undefined` values in the UI. Needs end-to-end smoke test on each API response. |

---

## 3. Backend Architecture Validation

### Does the structure fulfil Distributed System + Microservice + Spring Boot concepts?

**Verdict: Yes — fully compliant.** Below is the mapping.

### 3.1 Microservice Principles

| Principle | Implementation | Evidence |
|---|---|---|
| **Single Responsibility** | Each service owns exactly one business domain | `auth-service` = identity only; `wallet-service` = balances only; `collection-service` = groups + expenses + splits |
| **Database per Service** | Each service has its own MySQL schema | `ewallet_auth_db`, `ewallet_wallet_db`, `ewallet_collection_db`, `ewallet_transaction_db`, `ewallet_currency_db`, `ewallet_notification_db` — no cross-schema joins |
| **Independent deployability** | Each service is a self-contained Spring Boot JAR with its own Dockerfile | Stopping or redeploying one service does not require restarting others |
| **Loose coupling** | Services communicate only via REST (Feign) or RabbitMQ events | No shared JPA entities or in-process calls between services |
| **Shared kernel via library** | `common-lib` module provides shared types without coupling implementations | DTOs, enums, and exceptions are shared; business logic is not |

### 3.2 Distributed System Patterns

| Pattern | Implementation |
|---|---|
| **API Gateway** | Spring Cloud Gateway on `:8080` — single entry point, JWT verification, rate limiting, route resolution by service name |
| **Service Registry / Discovery** | Eureka Server (`:8761`) — all 7 business services and the gateway self-register; no hard-coded host addresses |
| **Centralised Configuration** | Config Server (`:8888`) — all `application.yml` configs served centrally; `docker` profile overrides host names for container networking |
| **Circuit Breaker** | Resilience4j on all Feign clients — prevents cascading failures; fallback factories return structured errors instead of exceptions bubbling up |
| **Saga Pattern (Orchestration)** | `SettlementSagaOrchestrator` and `NettingSagaOrchestrator` — distributed transactions coordinated with `SagaState` entity for compensating rollback |
| **Event-Driven Architecture** | RabbitMQ — settlement events published asynchronously; notification-service consumes without tight coupling to transaction-service |
| **CQRS read-side (API Composition)** | `reporting-service` has no database — it aggregates data from other services via Feign and composes reports at query time |
| **Fault tolerance** | Resilience4j circuit breaker + retry + time limiter; all Feign calls have a FallbackFactory |
| **Health monitoring** | Spring Boot Actuator `/actuator/health` on all services; Docker Compose `healthcheck` blocks dependent service startup until health passes |

### 3.3 Spring Boot Conventions

| Convention | Applied |
|---|---|
| `@SpringBootApplication` entry point | All 9 applications |
| Layered architecture: Controller → Service (interface + impl) → Repository | All 7 business services |
| `@RestController` + `@RequestMapping` | All controllers |
| Spring Data JPA `JpaRepository` | All entity repositories |
| `@ExceptionHandler` / `@ControllerAdvice` | Per-service exception handlers + `GlobalExceptionHandler` in `common-lib` |
| `spring-boot-actuator` | All services — health, info, metrics endpoints |
| Spring Security `SecurityFilterChain` | `auth-service` (login/register) and API Gateway (JWT filter) |
| `@FeignClient` with `fallbackFactory` | All inter-service Feign clients |

### 3.4 Architecture Diagram (Textual)

```
Browser / Mobile Web
        │
        ▼
  [API Gateway :8080]  ←── JWT auth filter, rate limiter
        │
        ├──► [auth-service :8081]         ── MySQL: ewallet_auth_db
        ├──► [wallet-service :8082]       ── MySQL: ewallet_wallet_db
        ├──► [collection-service :8083]   ── MySQL: ewallet_collection_db ──► RabbitMQ
        ├──► [transaction-service :8084]  ── MySQL: ewallet_transaction_db ──► RabbitMQ
        ├──► [currency-service :8085]     ── MySQL: ewallet_currency_db, Redis (rate cache)
        ├──► [notification-service :8086] ── MySQL: ewallet_notification_db ◄── RabbitMQ
        └──► [reporting-service :8087]    ── No DB (API composition via Feign)

  All services ──► [Eureka :8761]        (service registry)
  All services ──► [Config Server :8888] (centralised config)
```

---

## 4. Frontend Theme Change Log

**Date:** 2026-05-06  
**Change:** Minimalistic, pragmatic redesign

| Before | After |
|---|---|
| Primary colour: `#6C63FF` (violet/purple) | Primary colour: `#2563EB` (blue-600) |
| Colourful gradients on currency cards (violet → indigo, emerald → teal, amber → orange) | Flat solid backgrounds: blue (MYR), gray-800 (SGD), gray-950 (USD) |
| Gradient headers on expense detail and settle pages | Flat `bg-primary` header |
| Emoji icons in BottomNav tabs and quick-action cards | Text-only labels; SVG notification bell icon in TopBar |
| `EmptyState` with large emoji | Minimal horizontal rule + text |
| Login/Register with large emoji hero | Clean wordmark + left-aligned heading |
| Profile and Reports hub with emoji menu items | Bordered list rows with chevron |
| Dashboard quick-action cards with emoji | Text-only bordered buttons |
