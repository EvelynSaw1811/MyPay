# 17. Supervisor and Second Marker Question Prep

## Examiner Mindset

If I were the supervisor or second marker, I would not only check whether MyPay works. I would check whether the student understands **why the system was designed this way**, what trade-offs were made, what limitations exist, and whether the implementation matches the FYP documentation.

The strongest presentation should show three things:

- The student understands the problem domain, not only the code.
- The system design decisions are intentional and defensible.
- The student can honestly explain limitations without weakening the project.

## Technical Questions I Would Ask

### System Architecture

1. Why did you choose microservices instead of a monolithic architecture?
2. What are the disadvantages of using microservices for an FYP prototype?
3. How does the API Gateway improve the system design?
4. What is the role of Eureka Discovery Server in MyPay?
5. Why do you need a Config Server?
6. What would happen if one microservice is down?
7. Which services are most critical to the system?
8. How does MyPay separate responsibilities between services?
9. Why does each service own its own database schema?
10. How would you scale this system if the number of users increased?

### Authentication and Security

1. How does JWT authentication work in your system?
2. What is the difference between an access token and a refresh token?
3. How do you handle logout if JWT tokens are stateless?
4. How does the gateway know which user is sending the request?
5. How do you prevent users from accessing collections they do not belong to?
6. How are passwords stored?
7. What sensitive information should not be stored in frontend local storage?
8. What security features are missing before this can become a real fintech product?
9. How would you protect the system from brute-force login attacks?
10. How would you improve the system for compliance with financial regulations?

### Wallet and Transaction Logic

1. How does the wallet service manage balances?
2. What is the difference between top-up simulation and real payment integration?
3. How does the system prevent a user from spending more than their wallet balance?
4. Why is money calculation more difficult than normal decimal calculation?
5. Why should financial values use `BigDecimal` instead of floating-point numbers?
6. What happens when a settlement fails halfway?
7. How does Saga orchestration help with distributed transaction consistency?
8. What is compensation in the Saga pattern?
9. How is netting different from direct settlement?
10. What would happen if the wallet debit succeeds but the collection update fails?

### Split Payment Engine

1. What split strategies does MyPay support?
2. How does equal split handle rounding remainders?
3. How does percentage split ensure the total equals 100 percent?
4. How does exact amount split validate that all shares match the total?
5. Why did you implement hybrid split?
6. What is hierarchical split, and what user problem does it solve?
7. How do tax and service charge affect split calculation?
8. How does the system decide who owes whom?
9. How do you make sure the final split total matches the expense total?
10. Which split strategy was the hardest to implement, and why?

### Database Design

1. Why did you use MySQL?
2. How are users, wallets, collections, expenses, and settlements related?
3. Why does the reporting service not own its own database?
4. What is the benefit of database-per-service design?
5. What is the drawback of database-per-service design?
6. How do you handle data that needs to be shown across multiple services?
7. What tables are most important in the collection service?
8. How do you store expense shares?
9. How do you prevent duplicate settlement processing?
10. How would you audit financial transactions in a production version?

### Redis, RabbitMQ, and Integration

1. Why did you use Redis?
2. What data is suitable for Redis, and what data should not be stored only in Redis?
3. Why did you use RabbitMQ?
4. What is the difference between synchronous and asynchronous communication?
5. Which parts of MyPay use event-driven communication?
6. What happens if RabbitMQ is temporarily unavailable?
7. What happens if a notification event is consumed twice?
8. Why use OpenFeign instead of manually calling REST APIs?
9. How does Docker Compose help during development?
10. Which infrastructure component was most difficult to integrate?

### Frontend and UI

1. Why did you choose React?
2. Why is the frontend designed as mobile-web instead of desktop-first?
3. How does React render the UI?
4. How does React Router manage page navigation?
5. Why did you use TanStack Query?
6. How do you handle loading, success, and error states?
7. How does the UI adapt to different screen sizes?
8. What did you do to avoid overcrowding on mobile screens?
9. How does the frontend communicate with the backend API Gateway?
10. What frontend improvements would you make with more time?

### Testing

1. What types of testing did you perform?
2. What did your User Acceptance Testing prove?
3. Why is manual testing not enough for a production financial system?
4. What unit tests are most important in MyPay?
5. How would you test the split calculation engine automatically?
6. How would you test the Saga settlement flow?
7. How would you test the frontend?
8. How would you perform load testing?
9. What security testing would be needed before real deployment?
10. Which part of the system has the highest testing risk?

## Non-Technical Questions I Would Ask

1. Why did you choose this project topic?
2. Who is the main target user of MyPay?
3. What real-world problem does MyPay solve?
4. How is MyPay different from Splitwise?
5. How is MyPay different from normal e-wallets?
6. What is the most valuable feature for users?
7. What is the most valuable feature from a business or investor perspective?
8. What did you learn from your research?
9. How did your questionnaire influence the system requirements?
10. What was the hardest part of the project?
11. What would you do differently if you restarted the project?
12. What feature are you most proud of?
13. What is the biggest limitation of MyPay?
14. Is MyPay ready for real users? Why or why not?
15. What did you learn as a software engineering student?

## What I Expect The Student To Prepare In Advance

### Demo Preparation

- A working local environment or recorded backup demo.
- Docker containers started before the presentation.
- Test accounts already created.
- Wallet balances already topped up.
- At least one existing collection with members.
- At least one expense using a simple split strategy.
- At least one expense using a more advanced strategy such as hybrid or hierarchical split.
- At least one pending settlement.
- At least one completed settlement.
- Notifications already available to show.
- Reports already populated with meaningful data.

### Explanation Preparation

- A 30-second project summary.
- A 1-minute problem explanation.
- A 1-minute architecture explanation.
- A clear explanation of why microservices were chosen.
- A clear explanation of the split-payment engine.
- A clear explanation of Saga settlement and compensation.
- A clear explanation of database-per-service design.
- A clear explanation of React frontend rendering and responsive layout.
- Honest explanation of limitations.
- A short future enhancement plan.

### Evidence Preparation

- Final FYP document.
- Presentation slides.
- Architecture diagram.
- ERD or database relationship diagram.
- Class relationship diagram.
- Use case diagram.
- Sequence diagram for settlement or expense creation.
- Screenshots of key screens.
- Testing evidence.
- UAT feedback summary.
- GitHub or project folder ready if the examiner asks to see code.

### Backup Preparation

- Backup screenshots in case the live demo fails.
- Backup screen recording of the main demo flow.
- A short list of known limitations.
- A short list of common examiner questions and answers.
- Database already seeded with presentable data.
- Browser tabs and terminal windows arranged before the presentation.

## Unexpected Points That Would Impress Me

### 1. Explain The Trade-Off Of Microservices Honestly

Do not only say microservices are scalable. A stronger answer is:

> Microservices improve modularity, independent scaling, and fault isolation, but they also increase complexity. For MyPay, microservices are justified because the project studies distributed architecture and separates wallet, collection, transaction, currency, notification, and reporting responsibilities. However, for a small real startup prototype, a modular monolith may be simpler at the beginning.

This answer sounds mature because it recognises both benefits and costs.

### 2. Explain Why Distributed Transactions Are Difficult

It would be impressive if the student explains that MyPay cannot simply use one database transaction across all services because each service owns its own data.

Strong explanation:

> In a monolith, one database transaction can update several tables together. In MyPay, wallet balance, collection share status, and transaction record may belong to different services. Saga orchestration is used so each step can either complete or trigger compensation if a later step fails.

### 3. Explain Rounding In Money Calculation

Many students forget this. It would be impressive to explain that money calculation needs exact decimal handling.

Strong explanation:

> Financial values should not use floating-point calculation because small precision errors can occur. MyPay uses decimal-based calculation and remainder distribution so that split shares still add up to the original expense total.

### 4. Explain Why Reporting Service Does Not Own A Database

This is a good architectural talking point.

Strong explanation:

> Reporting is an aggregation service. It gathers wallet, collection, transaction, and currency data from other services. It does not own a separate database because it should not duplicate source-of-truth financial records in this prototype.

### 5. Explain The Difference Between User Features And Architecture Features

This shows presentation maturity.

Example:

- User feature: create group expense and split payment.
- Architecture feature: collection service stores expenses while transaction service coordinates settlement.
- User feature: receive notification.
- Architecture feature: RabbitMQ decouples event generation from notification delivery.

### 6. Explain What Is Simulated And What Is Real

This is very important for a fintech-style project.

Strong explanation:

> The wallet logic, internal debit and credit, settlement records, split calculation, notifications, and reports are implemented in the prototype. However, actual banking integration, real e-money compliance, KYC, AML, and payment gateway settlement are not implemented.

This prevents the examiner from thinking the student is overclaiming.

### 7. Explain How The UI Simplifies Complex Backend Logic

This connects engineering and user experience.

Strong explanation:

> The backend has many services and workflows, but the user should only see simple actions: create a collection, add an expense, choose a split method, and settle. The UI hides architectural complexity behind task-based screens.

### 8. Explain Failure Scenarios

Unexpected and impressive question handling:

- What if wallet debit succeeds but credit fails?
- What if notification fails?
- What if currency rate is unavailable?
- What if a settlement request is sent twice?

Strong answer:

> Critical financial actions need state tracking, idempotency, and compensation. Non-critical actions like notifications should not block the main financial workflow.

### 9. Explain Why Some Features Were Not Implemented

This shows scope control.

Example:

> Real bank integration and regulatory compliance are outside the FYP prototype scope because they require third-party approval, legal compliance, identity verification, and production security controls. Instead, the prototype focuses on the software engineering design of wallet, split, settlement, and microservice coordination.

### 10. Explain How MyPay Could Become Production-Ready

A strong future plan should mention:

- Real payment gateway integration.
- KYC and AML checks.
- Live exchange rate provider.
- Audit logs.
- Stronger automated testing.
- Load testing.
- Security testing.
- Monitoring and observability.
- CI/CD deployment.
- Cloud infrastructure.

## Short Answers The Student Should Memorise

### What Is MyPay?

MyPay is a secure distributed microservice-based e-wallet prototype that supports multi-currency wallets, collaborative group expense management, flexible split-payment calculation, Saga-based settlement, notifications, and reporting.

### Why Is MyPay Different?

MyPay combines features that are usually separated across different systems: e-wallet balance management, group expense tracking, advanced split rules, multi-currency settlement, and microservice-based backend architecture.

### Why Microservices?

Microservices were chosen to separate major financial responsibilities into independent services, including authentication, wallet, collection, transaction, currency, notification, and reporting. This improves modularity and shows distributed system design, but it also increases integration complexity.

### What Is The Most Technical Part?

The most technical part is the transaction and settlement flow because it coordinates wallet debit, wallet credit, collection share update, currency conversion, idempotency, and failure compensation across multiple services.

### What Is The Biggest Limitation?

The biggest limitation is that MyPay is a prototype. It does not integrate with real banks or payment gateways, and it does not include full financial compliance, KYC, AML, penetration testing, or production-level monitoring.

### What Are You Most Proud Of?

The strongest achievement is integrating user-facing expense sharing with backend distributed system design, especially the split-payment engine, multi-currency wallet flow, and Saga-based settlement.

## Final Examiner Advice

During the presentation, do not present MyPay only as a list of screens. Present it as a complete software engineering project:

1. A real user problem.
2. A researched domain.
3. A justified architecture.
4. A working implementation.
5. A tested prototype.
6. A clear understanding of limitations.
7. A credible future improvement path.

The student should sound confident, but not exaggerated. A good FYP defense is strongest when the student can explain both **what works** and **what still needs improvement**.
