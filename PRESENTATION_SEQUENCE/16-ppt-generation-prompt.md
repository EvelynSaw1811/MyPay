# 16. PPT Generation Prompt for MyPay FYP Presentation

## Recommended Tool

Use **Gamma.app** if you want the easiest AI-generated presentation draft from a long prompt, then export to PowerPoint for editing.

Alternative tools:

- **Microsoft PowerPoint Copilot**: best if you want a native PPTX workflow and already have Copilot access.
- **Canva Magic Design**: good for quick visual styling and manual polishing.
- **Beautiful.ai**: good for clean academic/business layouts, but less flexible for technical diagrams.

Recommended workflow:

1. Paste the prompt below into Gamma or PowerPoint Copilot.
2. Generate a first draft with speaker notes.
3. Replace screenshot placeholders with your actual MyPay screenshots.
4. Simplify any slide that becomes too text-heavy.
5. Export to PPTX and polish fonts, diagrams, and spacing manually.

## Recommended Theme

Use a **professional academic fintech defense** theme.

Visual direction:

- Background: white or very light gray.
- Primary color: deep navy blue.
- Secondary color: black or charcoal text.
- Accent color: calm cyan or steel blue.
- Supporting colors: light gray dividers, pale blue section bands.
- Style: minimalist, logical, calm, structured, and technical.
- Avoid: decorative gradients, overly playful colors, crowded icons, and marketing-style slides.

Suggested palette:

- Background: `#F8FAFC`
- Primary navy: `#0F2A44`
- Main blue: `#2563EB`
- Accent cyan: `#0891B2`
- Text charcoal: `#111827`
- Muted gray: `#64748B`
- Divider gray: `#E5E7EB`

Suggested typography:

- Title font: Aptos Display, Inter, or Calibri Light.
- Body font: Aptos, Inter, or Calibri.
- Keep each slide to 3 to 5 main points.
- Use diagrams and screenshots instead of long paragraphs.

## AI Prompt to Generate the Presentation

Copy and paste the following prompt into Gamma, PowerPoint Copilot, Canva Magic Design, or another AI slide generator.

```text
Create a professional, minimalist final year project defense presentation for a software engineering student.

Presentation style:
- Academic, technical, calm, and logical.
- Main colors: white, deep navy blue, black/charcoal, and muted cyan accent.
- Use clean section headings, concise bullet points, and simple technical diagrams.
- Avoid decorative gradients and crowded layouts.
- Make the presentation suitable for a university supervisor and second marker.
- Include speaker notes for each slide.
- Leave any unknown information blank using "________".
- Keep each slide clear enough to present in a viva/demo setting.

Project:
Secure Distributed Microservice-Based E-Wallet with Split Payment Management System and Multi-Currency Support in Payment Using Spring Boot and React

Student:
SAW EN YI (TP073602)

Intake:
APU3F2509SE

Course:
B.Sc. (Hons) Software Engineering

Supervisor:
Ms. Sathiapriya A/P Ramiah

Second Marker:
Ms. Vasugi Jayamangalam Rajoo

Create the following slides:

SLIDE 1: Title Slide
Content:
- Project Title: Secure Distributed Microservice-Based E-Wallet with Split Payment Management System and Multi-Currency Support in Payment Using Spring Boot and React
- Student: SAW EN YI (TP073602)
- Intake: APU3F2509SE
- Course: B.Sc. (Hons) Software Engineering
- Supervisor: Ms. Sathiapriya A/P Ramiah
- 2nd Marker: Ms. Vasugi Jayamangalam Rajoo

Speaker note:
Introduce MyPay as a secure distributed e-wallet prototype focused on collaborative expense management, split payment calculation, multi-currency wallet operations, and microservice-based system design.

SLIDE 2: Problem Statements
Use a clean 4-part layout.

Problem 1:
Existing e-wallets mainly support individual peer-to-peer or merchant payments, but they do not natively manage group expenses, shared obligations, or collaborative settlement.

Problem 2:
Existing split-bill tools and manual spreadsheets are limited when users need customised split rules such as exact amount, percentage, hybrid, tax, and hierarchical splitting.

Problem 3:
Multi-currency support is fragmented in group expense situations because wallet balances, conversion rates, and settlement records are usually handled separately.

Problem 4:
Monolithic system designs can become difficult to scale and maintain when authentication, wallet, collection, transaction, notification, and reporting features grow together.

Speaker note:
Explain that MyPay addresses both user-level financial pain points and software architecture problems.

SLIDE 3: Aim and Objectives

Aim:
To design and develop a secure distributed microservice-based e-wallet prototype that supports collaborative expense management, flexible split-payment calculation, multi-currency wallet operations, Saga-orchestrated settlement, event-driven notifications, and financial reporting through a React mobile-web frontend and Spring Boot backend microservices.

Objectives:
- To design and implement a distributed microservice architecture using Spring Boot, Spring Cloud Gateway, Eureka Discovery Server, Spring Cloud Config Server, MySQL, Redis, RabbitMQ, and Docker Compose.
- To implement a multi-currency wallet module supporting MYR, SGD, and USD accounts.
- To implement a collaborative collection module for group expenses, members, invitations, and role-based access.
- To implement a flexible split-payment engine supporting equal, exact amount, percentage, hybrid, and hierarchical split strategies.
- To implement Saga-orchestrated transaction and settlement workflows with compensation handling.
- To design a React mobile-web frontend for wallet, collection, expense, settlement, notification, and reporting workflows.
- To evaluate the prototype through functional verification, manual workflow testing, API testing, UI validation, and user acceptance testing.

SLIDE 4: Target Users and Functionalities

Target users:
- Students managing shared meals, trips, assignments, or rental expenses.
- Working adults managing office lunches, shared events, travel, or household payments.
- Household groups managing rent, utilities, groceries, and recurring shared expenses.
- Collection administrators, editors, and members within a group expense context.

Functionalities:
General users can:
- Register, log in, refresh session, log out, edit profile, change password, and delete account.
- Open and manage MYR, SGD, and USD wallets.
- Simulate wallet top-up and view balances.
- Add and manage payees.
- View notifications and financial reports.

Collection administrators can:
- Create, update, close, and manage collections.
- Invite members and assign roles.
- Add, edit, and manage expenses.
- View collection balances and settlement status.

Collection editors can:
- Add and edit expenses based on their permission level.
- Help manage collection financial records.

Collection members can:
- Join collections through invitations.
- View expenses, outstanding shares, and settlement status.
- Settle outstanding amounts.

SLIDE 5: Domain Research

Cover these research areas:
- Evolution of e-wallets from simple digital payment tools into broader financial platforms.
- Social and collaborative finance needs, especially shared expenses among students, friends, households, and working adults.
- Fair division and split-payment logic, including equal, exact, percentage, hybrid, and hierarchical splitting.
- Multi-currency wallet challenges, including conversion rates, currency-specific balances, and settlement accuracy.
- Secure distributed system design for fintech-style applications.

Key message:
MyPay combines e-wallet operation, group expense management, flexible split calculation, and microservice architecture in one prototype.

SLIDE 6: Similar Systems

Compare MyPay with existing solutions:

Splitwise:
- Strong at expense tracking and group balances.
- Does not function as a full e-wallet with internal wallet debit and credit.
- Limited integration with multi-currency wallet settlement.

Touch 'n Go eWallet / GrabPay / common e-wallets:
- Strong at payments, wallet balance, and merchant transactions.
- Weak at native collaborative expense management and detailed split logic.

Revolut:
- Strong at multi-currency wallet and financial services.
- Group expense features are not the main focus for local collaborative expense workflows.

MyPay difference:
- Combines group collections, flexible split strategies, multi-currency wallets, Saga-based settlement, event-driven notifications, and reporting in a distributed microservice architecture.

SLIDE 7: Technical Research

System development methodology chosen:
Research-through-design with iterative and incremental development phases.

Programming languages chosen:
- Java 17 for backend microservices.
- JavaScript / JSX for the React frontend.
- SQL for relational database operations.

Database Management System chosen:
- MySQL 8.0, using database-per-service ownership.

Tools and technologies used:
- Spring Boot 3.4.5 for backend microservices.
- Spring Cloud Gateway for API routing and request filtering.
- Eureka Discovery Server for service discovery.
- Spring Cloud Config Server for centralised configuration.
- OpenFeign for inter-service communication.
- RabbitMQ for event-driven messaging and notification workflows.
- Redis for caching, token/session support, and gateway rate limiting.
- Docker Compose for local containerised deployment.
- React 19 and Vite 8 for frontend development.
- Tailwind CSS for responsive mobile-first UI styling.
- TanStack Query and Axios for API state management and HTTP communication.
- Maven multi-module structure for backend project organisation.

SLIDE 8: Primary Research Done

Questionnaire:
- Received responses from: ________

Main findings to present:
- Users face difficulty calculating shared bills manually.
- Users need to record who paid first and who owes whom.
- Users want event-based folders or collections for group expenses.
- Users need flexible split rules, including fixed amount, percentage, tax, and mixed division.
- Users want reminders or notifications for payment obligations.
- Users need wallet-based repayment and multi-currency support.
- Users value consolidated statements and transaction history.

How findings influenced MyPay:
- Collection module was created for group-based expense folders.
- Split engine was created for equal, exact, percentage, hybrid, and hierarchical strategies.
- Notification module was created for invitations, expenses, and settlement reminders.
- Reporting module was created for financial summaries and personal positions.
- Multi-currency wallet design was created for MYR, SGD, and USD usage.

SLIDE 9: Primary Research to Requirement Mapping

If Slide 9 is needed, use this slide. If not needed, leave it blank.

Suggested content:
- Manual bill calculation difficulty maps to split-payment engine.
- Need to know who owes whom maps to expense shares, personal position, and settlement status.
- Event-based expense management maps to collection module.
- Participant invitation needs map to collection invitation and role-based access.
- Reminder needs map to notification module.
- Multi-currency needs map to wallet and currency services.
- Financial summary needs map to reporting service.

SLIDE 10: Design

Use one main architecture diagram plus small supporting labels.

Recommended diagram:
React Frontend -> API Gateway -> Microservices -> Databases / Redis / RabbitMQ

System design points:
- API Gateway acts as the single entry point for the frontend.
- Authentication service handles login, registration, JWT tokens, refresh tokens, and user identity.
- Wallet service manages multi-currency wallet accounts, balances, top-up simulation, payees, debit, and credit.
- Collection service manages collections, members, invitations, roles, expenses, and split shares.
- Transaction service coordinates settlement and netting using Saga orchestration.
- Currency service provides currency rates through cache, database, and fallback values.
- Notification service consumes RabbitMQ events and stores user notifications.
- Reporting service aggregates data from other services without owning a database.
- Each major service owns its own database schema.

Design artifacts to mention:
- Use case diagram.
- Context diagram.
- Entity relationship diagram.
- Class diagram.
- Sequence diagrams for registration, expense creation, settlement, and invitation flow.

SLIDE 11: Implementation

Use a maximum of 5 screenshots. Replace placeholders with actual screenshots.

Screenshot 1:
Login / registration / mobile entry screen.

Screenshot 2:
Wallet dashboard showing multi-currency balances.

Screenshot 3:
Collection detail page with members and expenses.

Screenshot 4:
Add expense or split calculation screen showing split strategy.

Screenshot 5:
Settlement, notification, or reporting screen.

Technical implementation points:
- Frontend is rendered as a React mobile-web application.
- Pages are routed using React Router.
- API data is fetched and cached using TanStack Query and Axios.
- UI is styled using Tailwind CSS with responsive layouts.
- Backend is implemented as Spring Boot microservices.
- Docker Compose starts the infrastructure and services together for local development.

SLIDE 12: Testing

Testing performed:
- Unit testing done by developer.
- Functional workflow testing.
- API testing.
- Integration testing across microservices.
- UI validation through browser workflow checks.
- User Acceptance Testing with 3 users.

User Acceptance Testing:
- Users involved: Ezra Koh Zhi Kai, Tan Pei Yi, Chua Jing Cheng.

Outcomes of User Acceptance Testing:
- Users could complete core workflows such as registration, login, wallet top-up, collection creation, invitation, expense creation, settlement, reporting, and notification review.
- Feedback was generally positive for usability and feature relevance.
- The system demonstrated that the main objectives were functionally achieved.

Testing caution:
- Full automated regression, load testing, and penetration testing were not completed, so these remain future improvements.

SLIDE 13: Problems and Limitations

Limitations:
- No real banking or payment gateway integration; top-up, debit, and credit are simulated.
- The prototype is not compliant with Bank Negara Malaysia e-money regulatory requirements.
- Exchange rates are manually seeded or fallback-based instead of using a live foreign exchange provider.
- Automated test coverage is limited compared with production financial systems.
- No full load testing, performance benchmarking, or penetration testing was completed.
- The frontend is a mobile-web prototype, not a native mobile application.
- Report export to PDF, Excel, or CSV is not included.

Problems faced:
- Coordinating microservice communication and data ownership.
- Handling transaction consistency across services.
- Designing flexible split logic while maintaining accurate rounding and tax calculation.
- Keeping the frontend workflow simple despite complex backend logic.

SLIDE 14: Future Enhancements

Future enhancements:
- Integrate real payment gateways or banking APIs such as Stripe, Billplz, DuitNow Open API, or other financial service providers.
- Add KYC, AML checks, fraud monitoring, and stronger financial compliance controls.
- Integrate live exchange rate providers such as Open Exchange Rates or European Central Bank feeds.
- Add exchange rate history, rate locking, and conversion audit records.
- Expand automated testing using JUnit 5, Mockito, Vitest, Cypress or Playwright, and JMeter or k6.
- Add native mobile application support.
- Add PDF, Excel, and CSV report export.
- Improve analytics and budgeting insights.

SLIDE 15: Conclusion

Conclusion:
- MyPay achieved the main aim of building a secure distributed microservice-based e-wallet prototype for collaborative expense management.
- The project integrates multi-currency wallets, group collections, flexible split-payment strategies, Saga-based settlement, notifications, and reporting.
- The architecture demonstrates practical use of microservices, database-per-service design, API gateway routing, service discovery, event-driven messaging, and containerised deployment.
- The project helped improve programming skills, system design understanding, frontend development ability, testing discipline, documentation skill, and time management.
- MyPay shows how e-wallet functionality can be extended beyond individual payments into collaborative financial management.

SLIDE 16: Q & A

Design:
- Minimal closing slide.
- Include the project title, student name, and "Thank You".
- Add "Questions?" as the main text.

Speaker note:
Invite questions from the supervisor and second marker. Be prepared to explain the architecture, split logic, Saga settlement flow, database design, frontend rendering, testing limitations, and future enhancements.
```

## Extra Prompt for Speaker Notes

Use this if the slide generator does not create strong speaker notes:

```text
For each slide, write short speaker notes for a final year software engineering viva presentation. The notes should explain the slide naturally in 45 to 75 seconds. The tone should be confident, academic, and easy to understand. Include technical explanation only where it helps the supervisor and second marker understand the design decisions.
```

## Extra Prompt for Diagram Generation

Use this if the slide generator can create diagrams:

```text
Create a clean architecture diagram for MyPay:
React mobile-web frontend sends requests to Spring Cloud Gateway. The gateway routes requests to Auth Service, Wallet Service, Collection Service, Transaction Service, Currency Service, Notification Service, and Reporting Service. Eureka Discovery Server provides service discovery. Config Server provides centralised configuration. Each major service owns its own MySQL schema. Redis is used for caching, token/session support, and rate limiting. RabbitMQ is used for event-driven notifications. Transaction Service coordinates settlement using Saga orchestration and communicates with Wallet, Collection, and Currency services.
```

## Information Still To Fill Manually

- Confirm exact questionnaire response count if it is required by the marker.
- Add actual implementation screenshots.
- Add exact testing evidence screenshots or test result tables if required.
- Add final presentation timing per slide.
- Add any lecturer-specific formatting requirement.
