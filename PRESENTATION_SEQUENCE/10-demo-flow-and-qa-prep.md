# 10 - Demo Flow And Q&A Prep

## Recommended Demo Flow

### 1. Login

Show that MyPay starts with authenticated access.

Talking point:

"The gateway and frontend both participate in authentication. The frontend stores tokens and refreshes them automatically, while the gateway validates JWT before routing protected API calls."

### 2. Dashboard

Show:

- Wallet balances.
- You are owed.
- You owe.
- Net position.
- Quick actions.
- Masking/unmasking sensitive values.

Talking point:

"The dashboard gives users a financial command center instead of making them search through each group manually."

### 3. Wallet

Show:

- Multi-currency wallets.
- Wallet registration.
- Top-up.
- History.
- Exchange rates.

Talking point:

"MyPay models wallet balances directly, so settlement is connected to actual account state."

### 4. Collections

Show:

- Collection list.
- Collection detail.
- Members.
- Invitations.
- Expenses.
- Balances.

Talking point:

"Collections represent the social context of money: a trip, house, event, club, or recurring group."

### 5. Add Expense With Split Rule

Demonstrate a non-equal split if possible:

- Percentage.
- Exact.
- Hybrid.
- Hierarchical.
- Tax rate.

Talking point:

"The split engine is flexible enough for real-world expenses, where equal split is not always fair."

### 6. Settle Payment

Show:

- Amount owed.
- Pay with currency.
- Conversion preview if currency differs.
- Confirm payment.

Talking point:

"Behind this single button is a saga: debit, credit, settlement record, share update, and notifications."

### 7. Settle Net

Show:

- Total outstanding bilateral balance.
- Net settlement.

Talking point:

"Netting is one of the most investor-friendly features. It reduces multiple transfers into one final payment."

### 8. Notifications And Reports

Show:

- Notification page.
- Reports hub.
- Spending summary.
- Ledger.
- Position.
- Collection statement.

Talking point:

"Users get a record of what happened, not just a one-time payment screen."

## Likely Investor Questions

### How is MyPay different from an e-wallet?

Existing e-wallets move money. MyPay coordinates shared money obligations, calculates them, settles them, and records the result.

### How is MyPay different from Splitwise?

Splitwise-like tools track shared expenses. MyPay adds wallet modeling, settlement orchestration, multi-currency flow, event notifications, and a fintech-style microservice architecture.

### Why microservices for a student or prototype project?

Because the project demonstrates production-style service boundaries: auth, wallet, collection, transaction, currency, notification, and reporting can evolve independently. It also makes the system easier to scale by business capability.

### Why RabbitMQ?

RabbitMQ is used where actions should be asynchronous and decoupled, such as registration setup and notifications.

### Why Redis?

Redis supports fast gateway rate limiting and exchange-rate caching.

### Why MySQL?

Financial records are structured and relational: users, credentials, wallets, collections, members, expenses, transactions, settlements, rates, and notifications all benefit from relational persistence.

### How do you prevent double payment?

Settlement and netting use idempotency keys. If the same request is retried, the service can return the existing transaction instead of creating a duplicate.

### What happens if settlement fails halfway?

The transaction service tracks saga state and compensates completed wallet actions, such as reversing a debit or credit.

### Can this scale?

The architecture is designed for scaling by service:

- Gateway can scale at the edge.
- Business services can scale independently.
- Redis handles fast cache and rate-limit workloads.
- RabbitMQ buffers asynchronous work.
- MySQL schemas separate service data ownership.

### What would be built next?

Good next-step roadmap:

- Real payment rail or bank integration.
- Stronger KYC and compliance workflow.
- Merchant bill splitting.
- Push notifications.
- Production observability dashboard.
- More currencies.
- Recurring group bills.
- AI or rules-based spending insights.

## Closing Statement

"MyPay starts with a problem people feel immediately: shared expenses are messy. The product solves it with a complete workflow, and the backend proves it can grow beyond a prototype into a modular financial platform."
