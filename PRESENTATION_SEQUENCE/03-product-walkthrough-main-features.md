# 03 - Product Walkthrough And Main Features

## Product Overview

MyPay is a mobile-first web application with a Spring Cloud backend. The current frontend routes show a complete user journey from authentication to wallets, collections, settlement, notifications, reports, and profile management.

## Main Features

### 1. Authentication And Profile

- Register, login, refresh token, logout.
- Password hashing through Spring Security.
- JWT access and refresh tokens.
- Revoked refresh token tracking on logout.
- User profile update, password change, and account deletion with legacy archive tables.
- Invitation code generation for user discovery.

### 2. Multi-Currency Wallet

- User account with wallets in supported currencies.
- Supported current currencies in the UI: MYR, SGD, USD.
- Open/register wallet by currency.
- Top up wallet.
- Wallet information, balance display, and transaction history.
- Sensitive values can be masked in the UI.

### 3. Collections

- Users create group collections.
- Collection types include defaults such as Expense, Trip, Monthly, and Other.
- Members can be invited and assigned roles.
- Collections contain expenses, members, balances, invitations, and settings.

### 4. Expense Splitting

The split engine supports:

- Equal split.
- Percentage split.
- Exact amount split.
- Hybrid split, where fixed amounts are assigned first and the remainder is shared.
- Hierarchical split using share weights.
- Optional tax handling.
- Remainder distribution to keep money totals consistent after rounding.

### 5. Settlement

- A user can settle an individual expense share.
- Settlement uses an idempotency key to avoid duplicate payments.
- The transaction service debits payer wallet, credits payee wallet, records settlement, marks share settled, and publishes notifications.
- Cross-currency settlement can preview conversion before payment.

### 6. Net Settlement

- MyPay can calculate net bilateral debt.
- Instead of many small transfers, users can settle the final net amount.
- This is a strong product differentiator because it reduces payment count and improves convenience.

### 7. Notifications

- Expense, invitation, settlement, and netting events are sent through RabbitMQ.
- Notification service consumes events and stores user notifications.
- Notification pages let users view and act on updates.

### 8. Reports

- Dashboard report.
- Spending summary.
- Currency ledger.
- Personal position.
- Collection statement.
- Net debt reporting.

## Demo Priority

If time is limited, demonstrate:

1. Login.
2. Dashboard showing wallets, owed, owe, and net position.
3. Create or open a collection.
4. Add an expense with a non-trivial split.
5. Settle a share or use net settlement.
6. Show notification and report updates.
