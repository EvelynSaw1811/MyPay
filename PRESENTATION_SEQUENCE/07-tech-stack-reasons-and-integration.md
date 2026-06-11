# 07 - Tech Stack, Reasons, And Integration

## Frontend

### React

Used for component-based UI rendering. Chosen because it makes the application easier to split into pages, reusable UI components, context providers, and data-driven views.

### Vite

Used as the frontend build tool and dev server. Chosen for fast development startup and simple React integration.

### React Router

Used for page routing. It defines protected `/app` routes for dashboard, wallets, collections, settlement, invitations, notifications, reports, and profile.

### TanStack React Query

Used for server state, loading states, cache freshness, mutation handling, and invalidation after actions such as wallet updates, settlements, collection changes, and notifications.

### Axios

Used as the HTTP client. The shared client adds JWT authorization, `X-Request-Id`, automatic refresh-token handling, request retry after refresh, and redirect to login if refresh fails.

### Tailwind CSS

Used for utility-first styling. It supports consistent spacing, layout, colors, responsive constraints, sticky bars, scroll behavior, truncation, and mobile-first composition.

## Backend

### Java 17

Used as the backend language runtime. Chosen for stable enterprise support and compatibility with Spring Boot 3.

### Spring Boot 3.4.5

Used for building backend services. It provides web APIs, validation, JPA, security, actuator health checks, AMQP integration, Redis integration, and consistent service structure.

### Spring Cloud 2024.0.1

Used for distributed-system infrastructure:

- Config Server for centralized configuration.
- Eureka for service discovery.
- Spring Cloud Gateway for routing.
- OpenFeign for inter-service communication.
- Resilience4j circuit breaker support in selected service clients.

### Spring Cloud Gateway

Chosen as the API gateway because it integrates naturally with Spring Cloud, Eureka service discovery, JWT filtering, CORS, and Redis rate limiting.

### Eureka Discovery Server

Chosen so services can find each other by name, such as `lb://WALLET-SERVICE`, instead of relying on fixed service URLs.

### Spring Cloud Config Server

Chosen to centralize configuration files for Docker and local profiles.

### MySQL 8

Used as the relational database. Chosen because financial data needs structured transactions, relationships, indexes, and reliable persistence.

MyPay initializes separate schemas:

- `ewallet_auth_db`
- `ewallet_wallet_db`
- `ewallet_collection_db`
- `ewallet_transaction_db`
- `ewallet_currency_db`
- `ewallet_notification_db`

### Redis 7

Used in two major places:

- API gateway rate limiting.
- Currency exchange-rate caching.

Redis was chosen because it provides fast key-value access for high-frequency reads and counters.

### RabbitMQ

Used for asynchronous events:

- User registration events from auth to wallet and collection services.
- Notification events from collection and transaction services to notification service.

RabbitMQ was chosen because these workflows do not need to block the user's main request. It reduces coupling and lets consumers process events independently.

### Docker And Docker Compose

Used to run infrastructure and services consistently:

- MySQL
- Redis
- RabbitMQ with management UI
- Config Server
- Discovery Server
- API Gateway
- Auth, Wallet, Collection, Currency, Transaction, Notification, and Reporting services

Docker was chosen to make the system reproducible and easier to demonstrate.

## Integration Summary

1. React calls the API Gateway.
2. Gateway validates JWT, adds request context, rate-limits, and routes to services.
3. Services discover each other using Eureka.
4. Services load configuration from Config Server.
5. Services persist their own data in MySQL schemas.
6. Redis supports fast rate-limit and exchange-rate cache workflows.
7. RabbitMQ carries asynchronous events.
8. Reporting service aggregates data from other services for user-facing insights.
