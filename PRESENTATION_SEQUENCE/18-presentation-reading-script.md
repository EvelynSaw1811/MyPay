# 18. Presentation Reading Script Based On PPT PDF

## How To Use This Script

This is a natural speaking script for the 16-slide MyPay presentation PDF.

Recommended delivery:

- Speak calmly and do not read too fast.
- Use this script as your base, then shorten sentences if needed.
- Replace `________` with the confirmed questionnaire response count before presenting.
- For the demo, pause longer on slides 10 and 11 because they explain the system design and implementation.
- If time is limited, keep each slide around 45 to 60 seconds.

## Slide 1: Title Slide

Good morning/afternoon. My name is Saw En Yi, TP073602, from the B.Sc. Honours Software Engineering course, intake APU3F2509SE.

Today I will be presenting my final year project, titled **Secure Distributed Microservice-Based E-Wallet with Split Payment Management System and Multi-Currency Support in Payment Using Spring Boot and React**.

In short, the project is called **MyPay**. It is a prototype e-wallet system that focuses on collaborative expense management. The system supports multi-currency wallets, group collections, flexible split-payment calculation, settlement, notifications, and reporting.

The main software engineering focus of this project is not only to build an e-wallet interface, but also to design a distributed backend using Spring Boot microservices, API Gateway, service discovery, centralised configuration, Redis, RabbitMQ, MySQL, and Docker Compose.

## Slide 2: Problem Statement

This project is motivated by four main gaps in existing solutions.

First, most e-wallets are designed mainly for individual payments, peer-to-peer transfers, or merchant payments. They are useful for paying someone, but they do not properly manage group expenses, shared obligations, or collaborative settlement.

Second, split-bill tools and spreadsheets can help users record expenses, but they are limited when the split becomes more complex. For example, users may need exact amount splitting, percentage splitting, hybrid splitting, tax calculation, or hierarchical splitting.

Third, multi-currency support is usually fragmented. Wallet balances, conversion rates, and settlement records are often handled separately, which can create confusion when a group expense involves different currencies.

Finally, from a system design perspective, a monolithic architecture can become difficult to scale and maintain when authentication, wallet, collection, transaction, notification, and reporting features grow together.

Therefore, MyPay is designed to address both the user problem and the software architecture problem.

## Slide 3: Aim and Objectives

The aim of this project is to design and develop a secure distributed microservice-based e-wallet prototype.

The system supports collaborative expense management, flexible split-payment calculation, multi-currency wallet operations, Saga-orchestrated settlement, event-driven notifications, and financial reporting. The frontend is built using React as a mobile-web application, while the backend is built using Spring Boot microservices.

The objectives are divided into several parts.

The first objective is to design the distributed architecture using Spring Boot, Spring Cloud Gateway, Eureka Discovery Server, Spring Cloud Config Server, MySQL, Redis, RabbitMQ, and Docker Compose.

The second objective is to implement a multi-currency wallet module supporting MYR, SGD, and USD.

The third objective is to implement a collaborative collection module for group expenses, members, invitations, and role-based access.

The fourth objective is to implement a flexible split-payment engine supporting equal, exact amount, percentage, hybrid, and hierarchical split strategies.

The fifth objective is to implement Saga-orchestrated transaction and settlement workflows with compensation handling.

Finally, the project is evaluated through functional verification, manual workflow testing, API testing, UI validation, and user acceptance testing.

## Slide 4: Target Users and Functionalities

The target users of MyPay are people who regularly manage shared expenses.

This includes students who share meals, trips, assignments, or rental expenses. It also includes working adults who manage office lunches, events, travel, or household payments. Household groups can also use it for rent, utilities, groceries, and recurring shared costs. Subscription groups are another possible target group because they often need to divide recurring payments.

The system has several user roles.

General users can register, log in, manage multi-currency wallets, add payees, receive notifications, and view reports.

Collection administrators can create and manage collections, invite members, assign roles, and manage expenses.

Editors can add and edit expenses inside a collection, depending on their permission.

Members can join collections, view their shares, and settle outstanding amounts.

The key idea is that the user should not need to manually calculate who owes whom. MyPay handles the collection structure, split calculation, settlement status, and financial summary.

## Slide 5: Domain Research

The domain research focuses on five areas.

First, e-wallets have evolved from simple digital payment tools into broader financial platforms. Modern users expect features such as peer-to-peer transfers, multi-currency support, transaction history, and financial visibility.

Second, collaborative finance is a common real-world need. Students, households, friends, and working adults often share expenses, but managing who paid first and who still owes money can become messy.

Third, fair division logic is important because not every group expense should be split equally. Some expenses require exact amounts, percentages, hybrid rules, tax calculation, or a hierarchical structure.

Fourth, multi-currency handling introduces additional complexity. A system must consider currency-specific wallet balances, exchange rates, and settlement accuracy.

Finally, fintech-style systems require secure and scalable architecture. This is why the project also researches distributed system design, microservices, fault isolation, and event-driven communication.

## Slide 6: Similar Systems

This slide compares MyPay with existing systems.

Splitwise is strong for expense tracking and group balances. However, it is not an internal e-wallet system. It does not focus on wallet debit, wallet credit, and settlement inside the same system.

Touch 'n Go eWallet and GrabPay are strong payment systems. They support wallet balance and merchant transactions, but collaborative expense management and advanced split logic are not their main focus.

Revolut is strong in multi-currency wallet and financial services. However, its group expense features are not focused on the local collaborative workflow that this project is targeting.

MyPay is different because it combines several areas in one prototype: group collections, flexible split strategies, multi-currency wallets, Saga-based settlement, event-driven notifications, and financial reporting.

So, the project is not trying to copy only an e-wallet or only a split-bill app. It combines both user-facing expense sharing and backend distributed system design.

## Slide 7: Technical Research

For the development approach, this project follows a research-through-design method with iterative and incremental development phases.

The backend is developed using Java 17 and Spring Boot 3.4.5. Java was chosen because it is strongly typed, mature, and widely used in enterprise backend systems.

The frontend uses JavaScript and JSX with React 19. React was selected because it supports component-based user interfaces and works well for building a mobile-web application.

MySQL 8.0 is used as the relational database management system. The project follows a database-per-service ownership approach, meaning each major service owns its own schema.

For backend infrastructure, the project uses Spring Cloud Gateway as the API entry point, Eureka Discovery Server for service discovery, Spring Cloud Config Server for centralised configuration, OpenFeign for synchronous service-to-service calls, RabbitMQ for asynchronous event-driven communication, Redis for caching and gateway-related support, Docker Compose for local deployment, and Maven multi-module structure for backend organisation.

For the frontend, the system uses Vite, Tailwind CSS, TanStack Query, and Axios.

## Slide 8: Primary Research

The primary research was conducted using a questionnaire, with responses received from `________` participants.

The findings show that users face difficulty when calculating shared bills manually. They also need a clear way to track who paid first and who still owes money.

Users also showed interest in event-based folders for group expenses. This influenced the collection module, where each collection can represent a trip, household, event, meal, or shared payment context.

The research also showed a need for flexible split rules, including fixed amount, percentage, tax, and mixed division. This directly influenced the split-payment engine.

Another finding is that users need reminders for payment obligations, which influenced the notification module.

Users also value wallet-based repayment, multi-currency support, consolidated statements, and transaction history. These findings shaped the wallet, currency, settlement, and reporting modules in MyPay.

## Slide 9: Research To Requirements

This slide connects the research findings to the system requirements.

The difficulty of manually calculating shared bills became the requirement for the split-payment engine.

The need to know who paid and who owes money became the requirement for expense shares, personal position, settlement status, and collection balances.

The desire for event-based expense folders became the requirement for the collection module.

The need to invite participants became the requirement for collection invitations and role-based access.

The need for reminders became the requirement for the notification module.

The need for multi-currency support became the requirement for wallet and currency services supporting MYR, SGD, and USD.

Finally, the need for financial summaries became the requirement for the reporting service.

In this way, the features of MyPay are not random. They are connected back to the user research and the problem statements.

## Slide 10: System Design

This slide shows the distributed microservice architecture of MyPay.

The React mobile-web frontend is the user-facing layer. The frontend does not directly call each backend service. Instead, it sends requests to the Spring Cloud Gateway.

The API Gateway acts as the single entry point. It handles routing, request filtering, security-related request information, and Redis-based rate limiting.

Behind the gateway, the system is separated into domain microservices. These include authentication, wallet, collection, transaction, currency, notification, and reporting services.

Each major service owns its own MySQL schema. This supports service independence and reduces tight coupling between modules.

RabbitMQ is used as the event bus for asynchronous communication. For example, notification workflows can react to events without blocking the main financial transaction.

The transaction service coordinates distributed settlement using Saga orchestration. This is important because wallet balance updates, collection share updates, currency conversion, and transaction records may involve different services.

Overall, the design separates frontend interaction, gateway routing, business services, data ownership, and asynchronous communication.

## Slide 11: Implementation

This slide summarises the implemented prototype.

The first part is login and registration, where users can create an account and authenticate into the system.

The second part is the wallet dashboard. Users can manage multi-currency wallets such as MYR, SGD, and USD, view balances, and perform simulated top-up.

The third part is collection details. Users can create group collections, invite members, view members, and manage shared expenses.

The fourth part is split calculation. The system supports multiple split strategies, including equal, exact, percentage, hybrid, and hierarchical splitting. This is one of the core features of MyPay because it reduces manual calculation effort.

The fifth part is settlement and reporting. Users can settle outstanding amounts and view financial summaries.

From the implementation side, the frontend is built using React mobile-web with React Router for page navigation. TanStack Query and Axios are used for API calls and caching. Tailwind CSS is used for responsive UI styling.

The backend is implemented using Spring Boot microservices, and Docker Compose is used to run the local infrastructure and services together.

## Slide 12: Evaluation

The system was evaluated through several testing approaches.

Unit testing was conducted by the developer to verify individual logic.

Functional testing was used to test core workflows, such as registration, login, wallet top-up, collection creation, invitation, expense creation, settlement, reporting, and notification review.

API testing was used to validate endpoints and responses.

Integration testing was used to check communication between microservices.

UI validation was performed through browser workflow checks and responsive design checks.

User Acceptance Testing was also conducted with three users: Ezra Koh Zhi Kai, Tan Pei Yi, and Chua Jing Cheng.

The UAT results showed that users were able to complete the core workflows successfully, and the feedback was generally positive in terms of usability and feature relevance.

However, I also recognise the limitations of the testing. Full automated regression testing, load testing, and penetration testing were not completed, so these are future improvements for a production-grade system.

## Slide 13: Challenges

This slide shows the problems faced and the limitations of the project.

One major challenge was coordinating communication between microservices while keeping data ownership clear. Because each service owns its own data, the system cannot simply rely on one shared database for all operations.

Another challenge was transaction consistency. For example, settlement may involve wallet debit, wallet credit, collection share update, and transaction record creation. This is why Saga orchestration and compensation logic are important.

The split-payment engine was also challenging because the system needs to maintain accurate rounding, tax calculation, and total amount consistency.

On the frontend side, the challenge was to keep the user workflow simple even though the backend logic is complex.

For limitations, MyPay is still a prototype. It does not integrate with real banks or payment gateways. Top-up, debit, and credit are simulated.

It is also not compliant yet with Bank Negara Malaysia e-money regulatory requirements. Exchange rates are manually seeded or fallback-based instead of using a live provider.

In addition, full automated testing, load testing, penetration testing, native mobile support, and report export are not included in the current scope.

## Slide 14: Future Enhancements

The future enhancements can be grouped into several areas.

The first area is payment and banking integration. MyPay could integrate with real payment gateways or banking APIs such as Stripe, Billplz, DuitNow Open API, or other financial service providers.

The second area is compliance and security. A production fintech system would require KYC, AML checks, fraud monitoring, stronger audit logs, and financial compliance controls.

The third area is currency and exchange. The system could integrate live exchange rate providers such as Open Exchange Rates or European Central Bank feeds. It could also support rate history, rate locking, and conversion audit records.

The fourth area is testing and quality assurance. The project could be improved with more automated backend testing using JUnit and Mockito, frontend testing using Vitest, end-to-end testing using Cypress or Playwright, and load testing using JMeter or k6.

Finally, the user experience could be improved through native mobile support, PDF or Excel report export, and more advanced analytics or budgeting insights.

## Slide 15: Conclusion

In conclusion, MyPay achieved the main aim of building a secure distributed microservice-based e-wallet prototype for collaborative expense management.

The project integrates multi-currency wallets, group collections, flexible split-payment strategies, Saga-based settlement, notifications, and reporting.

From a software engineering perspective, the project demonstrates practical use of microservices, database-per-service design, API gateway routing, service discovery, event-driven messaging, and containerised deployment.

From a user perspective, MyPay shows how e-wallet functionality can be extended beyond individual payment into collaborative financial management.

Through this project, I improved my programming skills in Java, JavaScript, and SQL. I also strengthened my understanding of system design, frontend development, testing, documentation, technical communication, time management, and project planning.

Overall, MyPay demonstrates both a functional prototype and the application of software engineering principles in a fintech-style system.

## Slide 16: Q & A

Thank you for listening to my presentation.

I am happy to answer any questions about the problem statement, research, system architecture, split-payment logic, Saga settlement, database design, implementation, testing, limitations, or future enhancements.

## Short Backup Answers For Q&A

### What Is MyPay?

MyPay is a secure distributed microservice-based e-wallet prototype that supports multi-currency wallets, collaborative group expense management, flexible split-payment calculation, Saga-based settlement, notifications, and reporting.

### Why Did You Choose Microservices?

Microservices were chosen to separate major responsibilities such as authentication, wallet, collection, transaction, currency, notification, and reporting. This improves modularity and demonstrates distributed system design, although it also increases integration complexity compared with a monolithic system.

### What Is The Most Technical Part?

The most technical part is the transaction and settlement flow because it coordinates wallet debit, wallet credit, collection share update, currency conversion, transaction records, idempotency, and compensation across multiple services.

### What Is Saga Orchestration?

Saga orchestration is a way to manage a business transaction across multiple services. Instead of using one database transaction, the transaction service coordinates several steps. If one step fails, compensation logic can be triggered to reduce inconsistency.

### Why Use OpenFeign And RabbitMQ?

OpenFeign is used for synchronous service-to-service calls when an immediate response is needed, such as settlement checking wallet or currency data. RabbitMQ is used for asynchronous event-driven communication, such as notification workflows.

### Is MyPay A Real E-Wallet?

MyPay implements e-wallet logic as a prototype, including wallet balances, debit, credit, settlement records, notifications, and reports. However, it does not integrate with real banks, payment gateways, KYC, AML, or regulatory compliance, so it is not production-ready as a real financial product.

### What Would You Improve First?

The first improvements would be stronger automated testing, real payment gateway integration, live exchange rates, audit logs, monitoring, and security testing.
