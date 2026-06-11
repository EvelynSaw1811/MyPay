# 14 - FYP Examiner Preparation

## How I Would Want You To Present MyPay As Supervisor Or Second Marker

If I were your supervisor or second marker, I would not want you to present MyPay like a startup pitch only. I would want you to present it like a software engineering project:

1. What problem did you solve?
2. What requirements did you identify?
3. What architecture did you choose?
4. Why did you choose that architecture?
5. How did you implement the important parts?
6. How did you test, validate, and handle failure?
7. What did you learn?
8. What are the limitations and future improvements?

Your strongest presentation position:

"MyPay is a microservice-based e-wallet and group expense settlement system. The project demonstrates software engineering skills in system design, service separation, API gateway security, event-driven communication, transactional workflow design, database modeling, frontend state management, and mobile-first UI design."

## Recommended FYP Presentation Sequence

### 1. Introduction

Explain the real problem:

- People share expenses frequently.
- Existing payment tools move money, but do not manage the full shared-expense workflow.
- Existing split tools calculate debts, but often do not integrate wallet, settlement, notification, and reporting flow.

Keep this short. Do not spend too much time on market size.

### 2. Objectives And Scope

State clearly:

- Build a secure e-wallet prototype.
- Support multi-currency wallets.
- Support collections/groups.
- Support flexible expense splitting.
- Support settlement and net settlement.
- Support notifications and reports.
- Implement the system using microservices and supporting infrastructure.

Also state what is outside scope:

- Real bank integration.
- Real payment gateway settlement.
- Production KYC/compliance.
- Real push notification provider.

This shows maturity. Examiners like students who know their project boundary.

### 3. Requirements

Present functional requirements:

- Register/login/logout.
- Wallet account creation.
- Top up, debit, credit.
- Create collections and invite members.
- Add expenses.
- Split by equal, percentage, exact, hybrid, and hierarchical strategies.
- Settle individual share.
- Settle net amount.
- View notifications and reports.

Present non-functional requirements:

- Security through JWT.
- Traceable requests through request IDs and trace IDs.
- Reliability through saga compensation and idempotency.
- Scalability through service separation.
- Maintainability through layered code design.
- Responsiveness through mobile-first UI.

### 4. System Architecture

Show the architecture diagram.

Explain:

- Frontend talks only to API Gateway.
- API Gateway validates JWT and routes requests.
- Config Server centralizes configuration.
- Eureka Discovery Server supports service discovery.
- Services are separated by business domain.
- MySQL stores persistent service data.
- Redis supports rate limiting and exchange-rate caching.
- RabbitMQ supports asynchronous registration and notification events.

Do not just name technologies. Explain their role in the system.

### 5. Core Workflow Demo

Use one scenario:

"A group trip where one person pays for dinner, the bill has tax/service charge, members split differently, and then another user settles using wallet balance."

Demo:

1. Login.
2. Show dashboard.
3. Open wallet.
4. Open collection.
5. Add expense with non-equal split.
6. Show generated shares.
7. Settle one share.
8. Show notification/report update.
9. Show net settlement if data is ready.

### 6. Technical Highlights

Pick 4-5. Do not list everything.

Best choices:

- Split Strategy pattern.
- Saga orchestration for settlement.
- Idempotency key for duplicate prevention.
- RabbitMQ event-driven registration and notification.
- API Gateway JWT filtering and request context headers.
- React Query frontend cache invalidation.
- Redis exchange-rate caching.

### 7. Limitations And Future Work

Be honest:

- No real payment gateway.
- No real banking/KYC compliance.
- Current currency list is limited.
- Some cross-service references are soft IDs, not database-enforced foreign keys.
- More integration and load testing needed.
- More security hardening needed for production.

Then propose future work:

- Real payment provider integration.
- KYC workflow.
- Audit logs.
- Role/permission hardening.
- Observability dashboard.
- Automated integration tests.
- CI/CD pipeline.
- Mobile app version.

## Technical Questions I Would Ask

### General Architecture

1. Why did you choose microservices instead of a monolithic architecture?
2. What are the disadvantages of microservices in your project?
3. How does the frontend know which backend service to call?
4. What is the role of the API Gateway?
5. Why do you need a Config Server?
6. What problem does Eureka solve?
7. What happens if one microservice is down?
8. Which services are stateful and which are stateless?
9. How do services communicate with each other?
10. Why do you use both Feign clients and RabbitMQ?

Good answer pattern:

"I used Feign when the caller needs an immediate response, and RabbitMQ when another service can process the task asynchronously."

### Authentication And Security

1. How does JWT authentication work in your system?
2. Where is the JWT checked?
3. What information does the gateway add to downstream requests?
4. Why do you have refresh tokens?
5. How do you handle logout?
6. Why store revoked token hashes instead of raw tokens?
7. What happens when the access token expires?
8. How do you prevent users from accessing another user's resources?
9. How does collection role checking work?
10. What are the security weaknesses if this goes to production?

Strong point to mention:

The API Gateway validates JWT and adds `X-User-Id`. Services use request context from common-lib to know the authenticated user.

### Database Design

1. Why does each service have its own database/schema?
2. Why are many relationships soft references instead of foreign keys?
3. What are the advantages and disadvantages of soft cross-service IDs?
4. How do you maintain consistency between wallet and transaction data?
5. How do you handle deleted users?
6. Why do you have legacy user tables?
7. What indexes or constraints are important?
8. How do you prevent duplicate wallets?
9. How do you prevent duplicate idempotency keys?
10. What data would you audit in a real financial system?

Good answer pattern:

"Because this is microservice-based, each service owns its data. Cross-service relationships are represented by IDs. This improves independence but means consistency must be handled at the service/workflow level."

### Transaction And Settlement

1. Explain the settlement workflow step by step.
2. What is a saga?
3. Why did you use saga instead of one database transaction?
4. What happens if debit succeeds but credit fails?
5. What does compensation mean?
6. What does your `SagaState` record store?
7. What is idempotency?
8. How do you prevent duplicate payment if the user clicks twice?
9. How does net settlement work?
10. What are the limitations of your current netting algorithm?

Very strong answer:

"A local database transaction cannot cover multiple microservices and databases. Therefore, settlement is implemented as a saga. Each successful step is recorded, and if a later step fails, compensation reverses completed actions."

### Expense Splitting

1. Why did you use the Strategy pattern for split calculation?
2. How does `SplitStrategyFactory` work?
3. What split types are supported?
4. How do you handle rounding errors?
5. Why is `BigDecimal` used instead of `double`?
6. What is the role of `MoneyUtil`?
7. How is tax applied?
8. What happens if percentages do not total 100?
9. What happens if exact split amounts do not match the total?
10. How would you add a new split type?

Strong answer:

"I can add a new split algorithm by implementing `SplitStrategy`. The factory automatically maps strategy beans by split type, so the service logic does not need a large conditional block."

### Messaging And RabbitMQ

1. What events are published in MyPay?
2. What happens after user registration?
3. Why does wallet creation happen through an event?
4. What notification events exist?
5. What happens if a RabbitMQ consumer fails?
6. How would you retry failed messages?
7. What is the difference between a queue and an exchange?
8. Why use topic routing?
9. What data is included in a notification event?
10. What are the risks of asynchronous messaging?

### Redis And Caching

1. Where is Redis used?
2. Why is Redis suitable for rate limiting?
3. Why cache exchange rates?
4. What is cache TTL?
5. What happens if Redis is down?
6. What data should not be cached?
7. How do you avoid stale exchange rates?
8. How would you monitor Redis usage?

### Reporting

1. Why is reporting a separate service?
2. Does reporting own its own data?
3. How does reporting get wallet, collection, and transaction data?
4. Why use `CompletableFuture`?
5. What is the risk of reporting by calling many services?
6. How would you improve reporting for production?
7. What is the difference between read model and write model?

Strong answer:

"Reporting is currently an aggregation service. In production, I may create a separate read model updated through events to reduce cross-service calls."

## System Design Questions I Would Ask

1. Draw the complete request flow from frontend login to backend response.
2. Draw the service flow when a user registers.
3. Draw the service flow when a user settles an expense share.
4. Draw the difference between synchronous and asynchronous communication in MyPay.
5. Explain where data consistency can fail.
6. Explain where the system can scale horizontally.
7. Explain what happens if wallet service is unavailable during settlement.
8. Explain why API Gateway is important in microservices.
9. Explain how rate limiting protects the system.
10. Explain what would change if this became a real financial product.

## Code Design Questions I Would Ask

1. Why do you use controller-service-repository layering?
2. Why do you use service interfaces?
3. Why do you use mappers?
4. How are DTOs different from entities?
5. Why should entities not be returned directly to the frontend?
6. Where is business logic located?
7. Where is validation located?
8. How are exceptions handled consistently?
9. What is the role of `common-lib`?
10. Which part of the code are you most proud of and why?
11. Which part would you refactor if you had more time?
12. How would you test `SettlementSagaOrchestrator`?
13. How would you test split strategies?
14. How would you test API Gateway authentication?
15. How do you ensure that code changes in one service do not break another?

## UI Design Questions I Would Ask

1. Why did you design the UI mobile-first?
2. How does the UI handle different screen sizes?
3. Why is the root layout constrained to `max-width: 480px`?
4. What is the purpose of sticky top and bottom navigation?
5. How do you protect sensitive information on screen?
6. How does `MaskedValue` improve privacy?
7. How does the frontend handle loading states?
8. How does the frontend handle failed API calls?
9. Why did you use React Query?
10. What is cache invalidation?
11. What happens in the UI after settlement succeeds?
12. How does the frontend refresh expired JWT tokens?
13. Why use route protection?
14. How would you improve accessibility?
15. How would you improve the UI for tablets or desktop?

Good answer:

"The target users are likely to use MyPay in real social situations on mobile phones, so the UI is designed like a wallet app: compact, touch-friendly, privacy-aware, and easy to navigate with sticky controls."

## Unexpected Strong Points For A Year 3 Software Engineering Student

These are the points that would make an examiner think, "This student understands beyond basic CRUD."

### 1. Saga Pattern With Compensation

Most Year 3 projects stop at CRUD. Explaining saga orchestration shows understanding of distributed transactions.

What to say:

"Because wallet debit, wallet credit, collection share update, and transaction record are across service boundaries, I cannot rely on a single database transaction. I use a saga-like workflow and compensation for failure recovery."

### 2. Idempotency Key

This is a real payment-system concept.

What to say:

"Idempotency protects against duplicate settlement if the user double-clicks, refreshes, or retries after network failure."

### 3. Strategy Pattern For Split Rules

This shows clean code design.

What to say:

"Instead of one large conditional block for all split types, each split method is its own class implementing the same interface."

### 4. Rounding And Money Precision

Using `BigDecimal`, `MoneyUtil`, and remainder distribution is more mature than using `double`.

What to say:

"In financial systems, small rounding errors accumulate. I use `BigDecimal` and a central money utility to make calculations consistent."

### 5. API Gateway Request Context

Trace ID and request ID are observability concepts.

What to say:

"The gateway adds context headers so downstream services can know the authenticated user and trace requests across services."

### 6. Event-Driven Onboarding

Publishing `user.registered` and letting wallet/collection services react is a strong microservice design point.

What to say:

"Auth service should not know every setup detail for other services. It announces the user registration event, and each service handles its own setup."

### 7. Redis In Two Different Roles

Redis is not just included for show. It supports gateway rate limiting and exchange-rate caching.

What to say:

"I use Redis for fast, temporary data: rate-limit counters and cached exchange rates."

### 8. Reporting Aggregation With Parallel Calls

Using `CompletableFuture` shows performance awareness.

What to say:

"Reports need data from multiple services. Parallel calls reduce wait time compared to calling each service sequentially."

### 9. Role-Based Collection Access With AOP

The `@RequireCollectionRole` annotation and aspect is a strong design point.

What to say:

"Instead of manually repeating role checks in every controller method, I use an annotation and aspect to centralize collection permission checks."

### 10. Honest Microservice Trade-Off Discussion

This may impress examiners more than pretending the design is perfect.

What to say:

"Microservices improve modularity and independent scaling, but they increase deployment, testing, debugging, and consistency complexity. For an FYP, it demonstrates architecture skill; for a small production team, a modular monolith may initially be simpler."

## Questions You Should Prepare Perfect Answers For

If you only prepare ten answers, prepare these:

1. Why microservices?
2. Why not monolith?
3. Explain settlement saga.
4. Explain idempotency.
5. Explain split strategy pattern.
6. Explain RabbitMQ usage.
7. Explain Redis usage.
8. Explain JWT flow.
9. Explain database ownership and soft references.
10. Explain what you would improve next.

## Suggested Final Defense Statement

"MyPay is not only a wallet interface. It is a full-stack software engineering project that demonstrates domain modeling, microservice architecture, secure API routing, event-driven workflows, financial calculation accuracy, distributed settlement handling, and mobile-first frontend design. The current implementation is a prototype, but the design decisions were made to reflect real software engineering concerns such as maintainability, reliability, scalability, and user experience."
