# Account And Wallet Structure Update Plan

## Goal

Update MyPay from the current structure:

```text
User -> Wallet -> WalletAccount
```

to the requested structure:

```text
User -> Account -> Wallet
```

Target business rules:

- Each user has exactly one account.
- Each account can have at most 3 wallets.
- Supported wallet currencies are MYR, USD, and SGD.
- Sign-up always creates an account and one MYR wallet.
- During sign-up, the user can optionally open USD and/or SGD wallets.
- After sign-up, the user can open missing USD and/or SGD wallets from the profile page.
- IDs must stay explicit and stable: `userId`, `accountId`, and `walletId`.

## 1. Current Structure Summary

From `PROJECT_INFORMATION/do-dto-dao-reference.md` and the current code:

- `auth-service` owns `User`.
- `wallet-service` currently owns:
  - `Wallet`: persisted in `WALLET_T`, has `walletId` and `walletUserId`.
  - `WalletAccount`: persisted in `WALLET_ACCOUNT_T`, has `walletAccountId`, currency, balance, and a many-to-one link to `Wallet`.
  - `Payee`: links saved payees to `payeeWalletId`.
- The existing `Wallet` entity is actually the user's top-level financial container.
- The existing `WalletAccount` entity is actually the per-currency wallet/balance.
- `WalletServiceImpl.createWallet(userId)` currently creates one `Wallet` and one `WalletAccount` for every value in `CurrencyCode` (`MYR`, `SGD`, `USD`).
- `auth-service` publishes a `user.registered` event containing only `userId`; `wallet-service` consumes it and creates all currency accounts.
- The frontend assumes all currencies exist and renders `MYR`, `SGD`, and `USD` from the fixed `CURRENCIES` list.

## 2. Data Structure And DO Updates

### Recommended Naming Model

To match the new business language cleanly:

| Requested Concept | Current Equivalent | Recommended Entity |
|---|---|---|
| User | `auth-service.User` | keep `User` |
| Account | current `wallet-service.Wallet` | rename/replace with `Account` |
| Wallet | current `wallet-service.WalletAccount` | rename/replace with `Wallet` |

This avoids keeping misleading names where an entity called `WalletAccount` is actually the new wallet.

### New/Updated Entities

#### `Account`

Create in `wallet-service`, for example `com.mypay.wallet.entity.Account`.

Fields:

| Field | Type | Notes |
|---|---|---|
| `accountId` | `String` | UUID primary key |
| `accountUserId` | `String` | unique, one account per user |
| `accountCreated` | `LocalDateTime` | created timestamp |
| `accountUpdated` | `LocalDateTime` | updated timestamp |
| `wallets` | `List<Wallet>` | one-to-many, max 3 enforced in service |

Database table suggestion: `ACCOUNT_T`.

Recommended constraints:

- Unique constraint on `acct_user_id`.
- `acct_user_id` should be non-null.

#### `Wallet`

Create/update in `wallet-service`, for example `com.mypay.wallet.entity.Wallet`.

Fields:

| Field | Type | Notes |
|---|---|---|
| `walletId` | `String` | UUID primary key |
| `account` | `Account` | many-to-one parent |
| `walletCurrency` | `String` | `MYR`, `USD`, or `SGD` |
| `walletBalance` | `BigDecimal` | default `0.0000` |
| `walletStatus` | `String` or enum | `ACTIVE`; optional but useful for future close/freeze logic |
| `walletCreated` | `LocalDateTime` | created timestamp |
| `walletUpdated` | `LocalDateTime` | updated timestamp |

Database table suggestion: `WALLET_T`.

Recommended constraints:

- Unique constraint on `(wallet_account_id, wallet_currency)`.
- Currency whitelist should be validated in service code and optionally with an enum.
- Enforce max 3 wallets per account in service logic.

### DTO Updates

Current `WalletResponse` and `AccountResponse` names are inverted relative to the new requirements. Update DTOs to make API payloads clear.

Recommended DTOs:

| DTO | Fields |
|---|---|
| `AccountResponse` | `accountId`, `userId`, `wallets`, `createdAt`, `updatedAt` |
| `WalletResponse` | `walletId`, `accountId`, `currency`, `balance`, `status`, `createdAt`, `updatedAt` |
| `CreateAccountRequest` or event payload | `userId`, `walletCurrencies` |
| `OpenWalletRequest` | `currency` |
| `TopUpRequest` | `currency`, `amount` |
| `WalletOperationRequest` | `currency`, `amount` |

For backward compatibility, either:

- keep `/api/wallets/me` returning the existing shape temporarily but add `accountId`, or
- introduce `/api/accounts/me` and migrate frontend callers in one pass.

Recommended final response shape:

```json
{
  "accountId": "account-uuid",
  "userId": "user-uuid",
  "wallets": [
    {
      "walletId": "wallet-uuid",
      "accountId": "account-uuid",
      "currency": "MYR",
      "balance": 0.0000,
      "status": "ACTIVE"
    }
  ]
}
```

### DAO Updates

Replace or refactor current repositories:

| Current DAO | Recommended DAO |
|---|---|
| `WalletRepository` | `AccountRepository` |
| `WalletAccountRepository` | `WalletRepository` |
| `PayeeRepository` using `payeeWalletId` | update to use `payeeAccountId` |

Recommended repository methods:

```java
Optional<Account> findByAccountUserId(String userId);
boolean existsByAccountUserId(String userId);

List<Wallet> findByAccount_AccountId(String accountId);
long countByAccount_AccountId(String accountId);
Optional<Wallet> findByAccount_AccountUserIdAndWalletCurrency(String userId, String currency);
boolean existsByAccount_AccountIdAndWalletCurrency(String accountId, String currency);
```

## 3. Backend Flow And Logic Updates

### Auth Registration Flow

Current flow:

```text
POST /api/auth/register
  -> save User
  -> save UserCredential
  -> publish user.registered with userId only
  -> wallet-service creates all currency accounts
```

Required flow:

```text
POST /api/auth/register
  -> save User
  -> save UserCredential
  -> publish user.registered with userId + requested wallet currencies
  -> wallet-service creates Account
  -> wallet-service creates MYR wallet always
  -> wallet-service creates USD/SGD only when selected
```

Backend changes:

- Add optional fields to `RegisterRequest`, such as:
  - `Boolean openUsdWallet`
  - `Boolean openSgdWallet`
  - or `List<String> walletCurrencies`
- Normalize requested currencies in `auth-service`.
- Ensure `MYR` is always included server-side even if the frontend omits it.
- Replace the raw string RabbitMQ message with a structured event class, for example `UserRegisteredEvent`.
- Update `UserEventPublisher.publishUserRegistered(...)` to send `userId` and selected currencies.
- Update `UserEventConsumer.onUserRegistered(...)` to consume the structured event.

### Wallet/Account Service Flow

Current service method:

```java
WalletResponse createWallet(String userId)
```

Recommended service methods:

```java
AccountResponse createAccount(String userId, Set<String> walletCurrencies);
AccountResponse getMyAccount(String userId);
WalletResponse openWallet(String userId, OpenWalletRequest request);
WalletResponse getBalance(String userId, String currency);
WalletResponse topUp(String userId, TopUpRequest request);
void debit(String userId, WalletOperationRequest request);
void credit(String userId, WalletOperationRequest request);
```

Rules to enforce:

- `createAccount` must be idempotent-safe around duplicate registration events.
- `MYR` wallet must always exist for new accounts.
- `USD` and `SGD` wallets are optional.
- Reject unsupported currencies.
- Reject opening a duplicate wallet for the same account/currency.
- Reject opening more than 3 wallets.
- Debit, credit, top-up, settlement, and netting must fail clearly if the target currency wallet does not exist.
- Consider whether `credit` should auto-open a receiving wallet. Recommended: do not auto-open; require the user to explicitly open the wallet so the product behavior remains predictable.

### Controller/API Updates

Current endpoints under `/api/wallets` can remain, but naming should be clarified.

Recommended endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/accounts/me` | current user's account and wallets |
| `POST` | `/api/accounts/internal/create` | internal account creation from registration event |
| `POST` | `/api/accounts/wallets` | open USD/SGD wallet after sign-up |
| `GET` | `/api/accounts/wallets/{currency}` | get one wallet balance |
| `POST` | `/api/accounts/wallets/topup` | top up an existing wallet |
| `POST` | `/api/accounts/internal/debit/{userId}` | internal debit by user/currency |
| `POST` | `/api/accounts/internal/credit/{userId}` | internal credit by user/currency |

If changing routes is too large for the current frontend, keep `/api/wallets/*` as compatibility aliases and migrate UI calls later.

### Payee Logic

Current `Payee` stores `payeeWalletId`, which points to the current top-level `Wallet`.

With the new model:

- Rename to `payeeAccountId` because payees belong to a user's account/contact list, not a single currency wallet.
- Update unique constraint from `(paye_wllt_id, paye_user_id)` to `(paye_acct_id, paye_user_id)`.
- Update `PayeeServiceImpl.findWallet(...)` to find the new `Account`.
- Update payee repository methods accordingly.

### Transaction And Reporting Services

Transaction service currently calls wallet-service by `userId + currency`, which can continue to work if wallet-service maps that to `Account -> Wallet`.

Required updates:

- Update Feign client paths if endpoint names change.
- Update error handling/message expectations for missing optional wallets.
- Settlement and netting screens must only offer payment currencies for wallets the payer owns.
- Cross-currency settlement must validate that the payee owns the target currency wallet.
- Reporting service `WalletClient` and dashboard wallet summary parsing must consume `wallets` instead of old `accounts`, or support both during migration.

### Mapper Updates

Replace `WalletMapper` methods:

- `toResponse(Account account, List<Wallet> wallets)` returns `AccountResponse`.
- `toWalletResponse(Wallet wallet)` returns `WalletResponse`.
- `toPayeeResponse(Payee payee)` should expose payee fields without leaking account internals.

### Migration Strategy

Because the current entity names conflict with the desired names, use one of these approaches:

1. Direct refactor for dev-only data:
   - Replace entities/tables and reset local dev databases.
   - Fastest for FYP/dev if no production data exists.

2. Additive migration:
   - Create `ACCOUNT_T`.
   - Rename/transform `WALLET_ACCOUNT_T` rows into new `WALLET_T` rows.
   - Migrate current `WALLET_T.wllt_user_id` to `ACCOUNT_T.acct_user_id`.
   - Update payee foreign key columns.

Given this project uses dev seed initializers and reset scripts, direct refactor is likely acceptable unless production-like migration is required.

## 4. Frontend Flow And Logic Updates

### Registration Page

Current `RegisterPage.jsx` only collects profile credentials.

Required updates:

- Add wallet selection controls:
  - MYR shown as included and disabled/mandatory.
  - USD optional checkbox/toggle.
  - SGD optional checkbox/toggle.
- Submit selected optional currencies to `authApi.register`.
- Keep backend responsible for forcing MYR even if the frontend payload is manipulated.

Recommended request body:

```json
{
  "email": "user@example.com",
  "password": "Test@1234",
  "firstName": "Evelyn",
  "lastName": "Tan",
  "userNickname": "Eve",
  "phone": "60123456789",
  "walletCurrencies": ["MYR", "USD"]
}
```

### Auth Context/API

- `AuthContext.register(body)` can pass the new fields through unchanged.
- No token/profile changes are required unless registration response starts including account data.

### Wallet Page

Current `WalletPage.jsx` maps fixed `CURRENCIES` and shows unavailable currencies as zero.

Required updates:

- Render only wallets returned by the account payload as active balances.
- Show missing optional wallets as explicit "not opened" actions only where useful.
- Prefer `account.wallets` over `wallet.accounts`.
- Update labels from "Currency wallets" to language that matches the product model.
- Make top-up buttons/flows available only for opened wallets.

### Top-Up Page

Current `TopUpPage.jsx` offers all currencies from the fixed `CURRENCIES` array.

Required updates:

- Fetch account/wallets first.
- Populate the currency select from opened wallets only.
- If only MYR exists, only MYR should be selectable.
- If no wallet exists for a selected currency, block submission before API call.

### Profile Page

The requirements specifically say USD and SGD wallets can be opened later from profile.

Required updates in `ProfilePage.jsx` / `WalletSummarySection.jsx`:

- Display account ID and wallet IDs where appropriate.
- Show active wallets with balances.
- Show missing USD/SGD wallets with an "Open wallet" action.
- Call a new API method such as `walletApi.openWallet({ currency })`.
- Invalidate/refetch `['wallet']` or the new `['account']` query after opening a wallet.
- Keep MYR displayed as mandatory and already active.

### Settlement, Netting, Collections, Reports

Current settlement and netting pages use the fixed `CURRENCIES` array.

Required updates:

- Payment currency selectors should use currencies from the user's opened wallets.
- Collection and expense creation can still allow MYR/USD/SGD as collection currencies, but settlement must validate user wallet availability.
- Reports and dashboard should handle partial wallet sets, not assume all 3 balances exist.
- History filters can still show all supported currencies, but should gracefully show empty state for unopened currencies.

### Frontend API Module Updates

Current `src/api/wallet.js`:

```js
getWallet:  ()       => client.get('/api/wallets/me')
getBalance: currency => client.get(`/api/wallets/balance/${currency}`)
topUp:      body     => client.post('/api/wallets/topup', body)
```

Recommended additions:

```js
getAccount: () => client.get('/api/accounts/me').then(r => r.data.data)
openWallet: body => client.post('/api/accounts/wallets', body).then(r => r.data.data)
```

During migration, keep aliases so existing pages can be updated incrementally.

## 5. Seed Data Updates

### Current Seeds

`wallet-service` seed currently creates:

- One top-level wallet per seeded user.
- MYR, SGD, and USD wallet accounts for every seeded user with a wallet.
- Grace and Henry intentionally have no wallet.
- Payees point to the current top-level wallet ID.

### Required Seed Changes

Update seed language and data to:

- Create one account per seeded user that should have financial access.
- Create wallets under each account.
- Always create MYR wallet for accounts.
- Create USD/SGD wallets only for users/scenarios that need them.
- Keep at least one seed user with only MYR to test optional wallet opening.
- Keep at least one seed user with MYR + USD.
- Keep at least one seed user with MYR + SGD.
- Keep at least one seed user with all three wallets.
- Keep Henry as "active user with no account" only if the app still needs a missing-account boundary case; otherwise update Henry to "MYR-only account" and create a different explicit no-account test user.
- Update payees to store account IDs, not wallet IDs.

Suggested seed coverage:

| User | Account? | MYR Wallet | USD Wallet | SGD Wallet | Purpose |
|---|---:|---:|---:|---:|---|
| Alice | yes | yes | yes | yes | full multi-currency user |
| Bob | yes | yes | no | yes | missing USD wallet scenario |
| Carol | yes | yes | yes | no | missing SGD wallet scenario |
| David | yes | yes | no | no | MYR-only profile open-wallet test |
| Emma | yes | yes | yes | yes | cross-currency settlement coverage |
| Frank | yes | yes | no | yes | SGD-heavy netting/settlement coverage |
| Grace | no | no | no | no | inactive user boundary |
| Henry | optional | optional | no | no | decide whether to keep no-account boundary |
| Ivy | yes | yes | no | no | null nickname and MYR-only display fallback |

### Seed Reference Documentation

Update `PROJECT_INFORMATION/seeds/seed-reference.md`:

- Rename "Wallet Balances" to "Account Wallet Balances".
- Include `Account ID` and each `Wallet ID` if deterministic IDs are introduced.
- Mark missing optional wallets as `not opened`, not `0`.
- Distinguish between zero balance and absent wallet.
- Update scenario coverage from "Missing-wallet user" to more precise cases:
  - Missing account.
  - MYR-only account.
  - Optional wallet opening.
  - Attempted payment from unopened wallet.

## 6. Implementation Order

1. Refactor wallet-service domain model:
   - Add `Account`.
   - Convert current `WalletAccount` concept into the new `Wallet`.
   - Update repositories, mapper, DTOs, service methods, and controller routes.

2. Update registration event contract:
   - Add wallet selection fields to `RegisterRequest`.
   - Add structured `UserRegisteredEvent` in `common-lib` or a shared event package.
   - Update auth publisher and wallet consumer.

3. Update wallet/account business rules:
   - Always create MYR.
   - Optionally create USD/SGD.
   - Add open-wallet endpoint.
   - Enforce max 3 wallets and duplicate prevention.

4. Update dependent backend services:
   - Transaction Feign client paths if changed.
   - Reporting wallet client and dashboard wallet summary parsing.
   - Payee ownership from account ID instead of old wallet ID.

5. Update frontend:
   - Registration optional wallet selection.
   - Wallet/profile account payload rendering.
   - Profile open-wallet action.
   - Top-up/settlement/netting currency choices from opened wallets.

6. Update seeds and documentation:
   - Wallet-service seed initializer.
   - Seed reference.
   - DO/DTO/DAO reference after implementation.

7. Verify:
   - Register with MYR only.
   - Register with MYR + USD.
   - Register with MYR + SGD.
   - Register with all three.
   - Open USD/SGD later from profile.
   - Attempt duplicate open-wallet.
   - Attempt top-up/debit/credit on unopened wallet.
   - Settlement where payer lacks selected currency.
   - Cross-currency settlement where payee lacks target wallet.
   - Dashboard/report display for partial wallet sets.

## 7. Key Risks And Decisions

- Naming risk: keeping current class names will confuse future development. Prefer a real `Account`/`Wallet` rename.
- API compatibility risk: frontend currently assumes `/api/wallets/me` and `accounts`. Either migrate all callers together or support old/new response fields temporarily.
- Event contract risk: raw `userId` registration events cannot carry optional wallet choices. A structured event is required.
- Missing wallet semantics: decide whether incoming credits should fail or auto-open wallets. Recommended behavior is fail with a clear message.
- Seed semantics: `0` balance is not the same as "wallet not opened"; seed docs and UI must show the difference.
