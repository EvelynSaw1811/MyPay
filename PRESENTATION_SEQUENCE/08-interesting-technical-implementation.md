# 08 - Interesting Technical Implementation

## 1. Split Strategy Engine

The collection service uses the Strategy pattern for split calculations.

Implemented strategies:

- `EqualSplitStrategy`
- `PercentageSplitStrategy`
- `ExactSplitStrategy`
- `HybridSplitStrategy`
- `HierarchicalSplitStrategy`

The `SplitStrategyFactory` receives a list of strategy beans and maps them by `SplitType`. This makes the system extensible: adding a new split method can be done by adding a new strategy implementation.

## 2. Remainder Distribution

Money division creates rounding remainders. MyPay uses `RemainderDistributor` to adjust the first result so the computed shares still match the total amount.

Investor-friendly explanation:

"In payment systems, one cent of rounding error matters. MyPay includes logic to keep split totals consistent."

## 3. Money Utility

`MoneyUtil` standardizes money operations with `BigDecimal`, scale 4, and `HALF_UP` rounding.

Why this matters:

- Avoids floating point precision errors.
- Keeps calculations consistent across services.
- Makes expense, settlement, reporting, and currency conversion more reliable.

## 4. Settlement Saga

The transaction service implements settlement as a saga:

1. Validate the expense share.
2. Convert currency if needed.
3. Create pending transaction.
4. Debit payer wallet.
5. Credit payee wallet.
6. Save settlement record.
7. Mark share as settled.
8. Publish notifications.
9. Mark transaction completed.

If a later step fails, compensation reverses completed wallet actions and marks the saga failed.

## 5. Netting Saga

Netting calculates outstanding bilateral debt between users across collections, then transfers only the final net amount.

Why it matters:

- Reduces many small transfers into one settlement.
- Improves user convenience.
- Creates a strong differentiation from basic e-wallets.

## 6. Idempotency Keys

Settlement and netting requests include idempotency keys. If a user retries a request, the transaction service can return the existing transaction instead of charging again.

Investor-friendly explanation:

"Payment flows must handle double-clicks, refreshes, and network retries. Idempotency protects users from duplicate charges."

## 7. Event-Driven Registration

When a user registers:

- Auth service creates the user and credential.
- Auth service publishes `user.registered`.
- Wallet service consumes the event and creates the account or wallets.
- Collection service consumes the event and creates default collection types.

This keeps registration extensible without making auth service responsible for every downstream setup step.

## 8. Event-Driven Notifications

Collection and transaction services publish notification events to RabbitMQ. Notification service consumes them and stores notification records.

This means core business actions can complete without tightly coupling to notification storage.

## 9. Request Context And Traceability

The gateway creates or preserves `X-Trace-Id` and `X-Request-Id`. The common request filter stores these values in request context and logging MDC.

Why it matters:

- Easier debugging.
- Better production observability.
- Clearer audit trail across microservices.

## 10. Role-Based Collection Access

The collection service uses `@RequireCollectionRole` with an AOP aspect. The aspect checks whether the current user is a member of the collection and whether their role is sufficient.

Roles include admin, editor, and member style access.

## 11. Currency Rate Resolution

Currency service resolves rates in layers:

1. Redis cache.
2. External exchange-rate API.
3. Database fallback.
4. Hardcoded USD-based fallback for MYR, SGD, and USD.

This keeps the app usable even if the external exchange-rate API is unavailable.

## 12. Reporting Parallelization

Reporting service uses `CompletableFuture` to fetch wallet, collection, transaction, and balance data in parallel for certain reports.

Why it matters:

- Reduces wait time.
- Avoids purely sequential N+1 report loading.
- Shows thoughtfulness around user-facing performance.
