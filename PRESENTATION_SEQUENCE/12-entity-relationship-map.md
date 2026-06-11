# 12 - Entity Relationship Map

## Recommended Drawing Method

Use a business-level Mermaid relationship map for presentation, then keep the database ER diagram as backup for technical questions.

This entity map is easier for investors and lecturers because it uses domain names instead of table names. It shows how MyPay concepts connect: users own wallets, join collections, split expenses, settle shares, and receive notifications.

## Full Business Entity Relationship Diagram

```mermaid
erDiagram
  User {
    string userId
    string email
    string phone
    string name
    string nickname
    string invitationCode
    string status
  }

  UserCredential {
    string credentialId
    string userId
    string passwordHash
  }

  RevokedToken {
    string revokedTokenId
    string tokenHash
    string userId
    datetime expiresAt
  }

  Account {
    string accountId
    string userId
  }

  Wallet {
    string walletId
    string accountId
    string userId
    string currency
    decimal balance
    string status
  }

  Payee {
    string payeeId
    string accountId
    string payeeUserId
    string nickname
  }

  Collection {
    string collectionId
    string name
    string category
    string typeName
    string currency
    string status
    string ownerId
  }

  CollectionMember {
    string collectionMemberId
    string collectionId
    string userId
    string role
  }

  CollectionType {
    string collectionTypeId
    string userId
    string name
    boolean system
  }

  Invitation {
    string invitationId
    string collectionId
    string inviterId
    string inviteeId
    string role
    string status
  }

  Expense {
    string expenseId
    string collectionId
    string title
    decimal amount
    string currency
    string paidBy
    string splitType
    decimal taxRate
    string taxType
  }

  SplitRule {
    string splitRuleId
    string expenseId
    string userId
    decimal percentage
    decimal fixedAmount
    int weight
  }

  ExpenseShare {
    string expenseShareId
    string expenseId
    string userId
    decimal baseAmount
    decimal taxAmount
    decimal totalAmount
    boolean settled
  }

  Transaction {
    string transactionId
    string payerId
    string payeeId
    decimal amount
    string currency
    decimal convertedAmount
    string payeeCurrency
    string type
    string status
    string idempotencyKey
  }

  Settlement {
    string settlementId
    string transactionId
    string expenseShareId
    string collectionId
    string payerId
    string payeeId
    decimal amount
  }

  SagaState {
    string sagaStateId
    string transactionId
    int step
    string status
    int compensationStep
  }

  Currency {
    string currencyId
    string code
    string name
    string symbol
    boolean active
  }

  ExchangeRate {
    string exchangeRateId
    string baseCurrency
    string targetCurrency
    decimal rate
    datetime fetchedAt
  }

  Notification {
    string notificationId
    string userId
    string type
    string title
    string message
    string referenceId
    boolean read
  }

  UserPreference {
    string userPreferenceId
    string userId
    boolean emailEnabled
    boolean smsEnabled
    boolean pushEnabled
    boolean promoEnabled
  }

  User ||--|| UserCredential : logs_in_with
  User ||--o{ RevokedToken : can_revoke
  User ||--|| Account : owns
  Account ||--o{ Wallet : contains
  Account ||--o{ Payee : stores
  User ||--o{ Wallet : funds
  User ||--o{ Payee : can_be_saved_as

  User ||--o{ Collection : owns
  User ||--o{ CollectionMember : joins
  Collection ||--o{ CollectionMember : has
  User ||--o{ CollectionType : defines
  Collection ||--o{ Invitation : has
  User ||--o{ Invitation : sends_or_receives

  Collection ||--o{ Expense : contains
  User ||--o{ Expense : pays
  Expense ||--o{ SplitRule : calculated_from
  User ||--o{ SplitRule : assigned_rule
  Expense ||--o{ ExpenseShare : creates
  User ||--o{ ExpenseShare : owes_or_owns_share

  ExpenseShare ||--o{ Settlement : settled_by
  Collection ||--o{ Settlement : settlement_context
  User ||--o{ Transaction : payer_or_payee
  Transaction ||--o{ Settlement : records
  Transaction ||--o{ SagaState : tracked_by

  Currency ||--o{ Wallet : denominates
  Currency ||--o{ Collection : denominates
  Currency ||--o{ Expense : denominates
  Currency ||--o{ Transaction : denominates
  Currency ||--o{ ExchangeRate : base_or_target

  User ||--o{ Notification : receives
  User ||--|| UserPreference : configures
  Transaction ||..o{ Notification : can_trigger
  Expense ||..o{ Notification : can_trigger
  Invitation ||..o{ Notification : can_trigger
```

## Simplified Presentation Version

Use this smaller version when the audience is non-technical.

```mermaid
flowchart LR
  User["User"] --> Account["Account"]
  Account --> Wallet["Multi-Currency Wallets"]
  User --> Collection["Collection / Group"]
  Collection --> Member["Members"]
  Collection --> Expense["Expenses"]
  Expense --> SplitRule["Split Rules"]
  Expense --> Share["Expense Shares"]
  Share --> Settlement["Settlement"]
  Settlement --> Transaction["Transaction"]
  Transaction --> Saga["Saga State"]
  Currency["Currency + Exchange Rates"] --> Wallet
  Currency --> Expense
  Currency --> Transaction
  Transaction --> Notification["Notifications"]
  Expense --> Notification
  Collection --> Report["Reports"]
  Wallet --> Report
  Transaction --> Report
```

## How To Explain The Entity Map

Start from the user:

1. A user has credentials and one wallet account.
2. The wallet account can contain many currency wallets.
3. A user can create or join many collections.
4. A collection has members, invitations, and expenses.
5. Each expense is calculated using split rules.
6. Split rules produce expense shares.
7. Shares are settled through transactions.
8. Transactions are protected by saga state and idempotency.
9. Currency entities support wallet currencies, expense currencies, transaction currencies, and exchange rates.
10. Notifications and reports are generated from business events and aggregated data.

## Most Important Relationships To Mention

- `User -> Account -> Wallet`: this is the wallet ownership chain.
- `User -> CollectionMember -> Collection`: this is how groups and permissions work.
- `Collection -> Expense -> ExpenseShare`: this is the core split-expense flow.
- `ExpenseShare -> Settlement -> Transaction`: this is how debt becomes payment.
- `Transaction -> SagaState`: this is the technical reliability layer.
- `Currency -> Wallet/Expense/Transaction/ExchangeRate`: this is the multi-currency layer.
- `Notification.referenceId`: this links notifications to business events such as settlement, expense, or invitation.

## Entity Design Notes

- User data is owned by the auth service.
- Wallet data references users by ID, but does not physically join the auth database.
- Collection members and shares reference users by ID so the collection service owns group logic independently.
- Transaction records store payer and payee IDs, making transaction history queryable without joining user tables.
- Settlement records connect transactions back to expense shares and collections.
- Notification records use a flexible `referenceId` so different event types can reuse the same notification table.
