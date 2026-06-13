# DO / DTO / DAO Reference — MyPay

> Auto-generated from source code inspection on 2026-05-19.  
> Tables are derived from `@Entity` / `@Column` JPA annotations (Hibernate `ddl-auto: update`).  
> "⚠️ Mismatch" flags real discrepancies between layers.

---

## Table of Contents
1. [Common Library](#1-common-library)
2. [Auth Service](#2-auth-service)
3. [Wallet Service](#3-wallet-service)
4. [Collection Service](#4-collection-service)
5. [Transaction Service](#5-transaction-service)
6. [Currency Service](#6-currency-service)
7. [Notification Service](#7-notification-service)
8. [Reporting Service](#8-reporting-service)
9. [Cross-Layer Data Flow Summary](#9-cross-layer-data-flow-summary)
10. [Identified Mismatches & Issues](#10-identified-mismatches--issues)

---

## 1. Common Library

### 1.1 Enums (`com.mypay.common.constant`)

| Enum | Values |
|------|--------|
| `CollectionCategory` | `TRIP`, `EXPENSE`, `MONTHLY`, `OTHER` |
| `CollectionStatus` | `ACTIVE`, `CLOSED` |
| `CollectionRole` | `ADMIN`, `EDITOR`, `MEMBER` |
| `InvitationStatus` | `PENDING`, `ACCEPTED`, `DECLINED` |
| `SplitType` | `EQUAL`, `PERCENTAGE`, `EXACT`, `HYBRID`, `HIERARCHICAL` |
| `TransactionStatus` | `PENDING`, `COMPLETED`, `FAILED`, `REVERSED` |
| `TransactionType` | `SETTLEMENT`, `TOP_UP`, `TRANSFER`, `NETTING` |

### 1.2 ApiResponse DTO

| Field | Type | Notes |
|-------|------|-------|
| `success` | `boolean` | Always present |
| `message` | `String` | Always present |
| `data` | `T` | `@JsonInclude NON_NULL` |
| `errorCode` | `String` | `@JsonInclude NON_NULL` |
| `module` | `String` | `@JsonInclude NON_NULL` |
| `traceId` | `String` | `@JsonInclude NON_NULL` |
| `timestamp` | `LocalDateTime` | Default: `LocalDateTime.now()` |

All backend responses are wrapped: `{ success, message, data, timestamp }`.  
Frontend unwraps via `.then(r => r.data.data)`.

---

## 2. Auth Service

**Database:** `ewallet_auth_db` | **Port:** 8081

### 2.1 Domain Objects (Entities)

#### `User` → table `USER_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `user_id` | `userId` | `CHAR(36)` | PK, UUID, not updatable |
| `user_email` | `userEmail` | `VARCHAR(255)` | NOT NULL, UNIQUE |
| `user_phone` | `userPhone` | `VARCHAR(20)` | nullable |
| `user_fname` | `userFirstName` | `VARCHAR(100)` | NOT NULL |
| `user_lname` | `userLastName` | `VARCHAR(100)` | NOT NULL |
| `user_nickname` | `userNickname` | `VARCHAR(100)` | nullable |
| `user_invitation_code` | `userInvitationCode` | `VARCHAR(32)` | UNIQUE, nullable |
| `user_status` | `userStatus` | `VARCHAR(20)` | default `'ACTIVE'` |
| `user_last_login` | `userLastLogin` | `DATETIME` | nullable |
| `user_created` | `userCreated` | `DATETIME` | not updatable, set by `@PrePersist` |
| `user_updated` | `userUpdated` | `DATETIME` | set by `@PrePersist`/`@PreUpdate` |

#### `UserCredential` → table `USER_CREDENTIAL_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `ucrd_id` | `userCredentialId` | `CHAR(36)` | PK, UUID |
| `ucrd_user_id` | `userCredentialUserId` | `CHAR(36)` | NOT NULL, UNIQUE |
| `ucrd_pwd_hash` | `userCredentialPwdHash` | `VARCHAR(255)` | NOT NULL |
| `ucrd_created` | `userCredentialCreated` | `DATETIME` | not updatable |

#### `RevokedToken` → table `REVOKED_TOKEN_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `rtkn_id` | `revokedToken` | `CHAR(36)` | PK, UUID |
| `rtkn_token_hash` | `revokedTokenHash` | `VARCHAR(64)` | NOT NULL, UNIQUE |
| `rtkn_user_id` | `revokedTokenUserId` | `CHAR(36)` | NOT NULL |
| `rtkn_expires_at` | `revokedTokenExpireDateTime` | `DATETIME` | NOT NULL |
| `rtkn_revoked_at` | `revokedTokenRevokeDateTime` | `DATETIME` | not updatable, `@PrePersist` |

### 2.2 DTOs

#### Request DTOs

| DTO | Fields |
|-----|--------|
| `LoginRequest` | `email` (NotBlank, Email), `password` (NotBlank) |
| `RegisterRequest` | `email` (NotBlank, Email), `password` (NotBlank, min 8), `firstName` (NotBlank), `lastName` (NotBlank), `userNickname` (NotBlank), `phone` (optional), `walletCurrencies` (Set\<String\>, optional) |
| `RefreshTokenRequest` | `refreshToken` (NotBlank) |
| `LogoutRequest` | `refreshToken` (NotBlank) |
| `UpdateUserRequest` | `email` (Email, max 255, optional), `firstName` (1-100, optional), `lastName` (1-100, optional), `userNickname` (1-100, optional), `phone` (max 20, optional) |
| `ChangePasswordRequest` | `currentPassword` (NotBlank), `newPassword` (NotBlank, min 8) |
| `DeleteAccountRequest` | `password` (NotBlank) |

#### Response DTOs

| DTO | Fields |
|-----|--------|
| `AuthResponse` | `accessToken`, `refreshToken`, `userId`, `email`, `firstName`, `lastName`, `userNickname`, `invitationCode`, `name` ⚠️ |
| `UserResponse` | `userId`, `email`, `firstName`, `lastName`, `userNickname`, `invitationCode`, `phone`, `status`, `lastLogin`, `createdAt`, `updatedAt` |

> ⚠️ `AuthResponse.name` — no corresponding field in `User` entity; not set by `UserMapper.toResponse()`. Will always be `null` unless explicitly set in the service impl.

### 2.3 DAO (Repositories)

#### `UserRepository`
```
findByUserEmail(String)         → Optional<User>
findByUserPhone(String)         → Optional<User>
findByUserNickname(String)      → Optional<User>
findByUserInvitationCode(String)→ Optional<User>
existsByUserEmail(String)       → boolean
existsByUserInvitationCode(String) → boolean
```

#### `UserCredentialRepository`
```
findByUserCredentialUserId(String) → Optional<UserCredential>
```

#### `RevokedTokenRepository`
```
existsByRevokedTokenHash(String)                         → boolean
deleteExpiredTokens(LocalDateTime now)  [JPQL @Modifying]→ void
```

### 2.4 Mapper: `UserMapper`

| Mapping | Source → Target |
|---------|----------------|
| `toEntity(RegisterRequest)` | `email→userEmail`, `firstName→userFirstName`, `lastName→userLastName`, `userNickname→userNickname`, `phone→userPhone` |
| `toResponse(User)` | `userId`, `userEmail→email`, `userFirstName→firstName`, `userLastName→lastName`, `userNickname→userNickname`, `userInvitationCode→invitationCode`, `userPhone→phone`, `userStatus→status`, `userLastLogin→lastLogin`, `userCreated→createdAt`, `userUpdated→updatedAt` |
| `applyUpdate(User, UpdateUserRequest)` | Applies non-null/non-blank fields only |

### 2.5 API Endpoints

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/api/auth/register` | `RegisterRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/login` | `LoginRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/refresh` | `RefreshTokenRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/logout` | `LogoutRequest` | `ApiResponse<Void>` |
| GET | `/api/auth/users/{userId}` | — | `ApiResponse<UserResponse>` |
| PUT | `/api/auth/users/{userId}` | `UpdateUserRequest` | `ApiResponse<UserResponse>` |
| PUT | `/api/auth/users/{userId}/password` | `ChangePasswordRequest` | `ApiResponse<Void>` |
| DELETE | `/api/auth/users/{userId}` | `DeleteAccountRequest` | `ApiResponse<Void>` |
| GET | `/api/auth/internal/users/resolve?identifier=` | — | `ApiResponse<UserResponse>` |

### 2.6 Frontend API (`src/api/auth.js`, `src/api/profile.js`)

| Function | Method + Path | Payload Sent | Response Expected |
|----------|--------------|--------------|-------------------|
| `register(body)` | POST `/api/auth/register` | `{ email, password, firstName, lastName, userNickname, phone, walletCurrencies }` | `AuthResponse` data |
| `login(body)` | POST `/api/auth/login` | `{ email, password }` | `AuthResponse` data |
| `logout(body)` | POST `/api/auth/logout` | `{ refreshToken }` | — |
| `getUser(id)` | GET `/api/auth/users/{id}` | — | `UserResponse` data |
| `getProfile(userId)` | GET `/api/auth/users/{userId}` | — | `UserResponse` data |
| `updateProfile(userId, body)` | PUT `/api/auth/users/{userId}` | `{ email?, firstName?, lastName?, userNickname?, phone? }` | `UserResponse` data |
| `changePassword(userId, body)` | PUT `/api/auth/users/{userId}/password` | `{ currentPassword, newPassword }` | full `ApiResponse` |
| `deleteAccount(userId, body)` | DELETE `/api/auth/users/{userId}` | `{ password }` | full `ApiResponse` |

**Token refresh (client.js):** POST `/api/auth/refresh` → `{ refreshToken }` → `{ accessToken, refreshToken }`.  
Uses `axios` directly (not the authenticated `client`) for `register`, `login`, `logout`.

---

## 3. Wallet Service

**Database:** `ewallet_wallet_db` | **Port:** 8082

### 3.1 Domain Objects (Entities)

#### `Account` → table `ACCOUNT_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `acct_id` | `accountId` | `CHAR(36)` | PK, UUID |
| `acct_user_id` | `accountUserId` | `CHAR(36)` | NOT NULL, UNIQUE |
| `acct_created` | `accountCreated` | `DATETIME` | not updatable |
| `acct_updated` | `accountUpdated` | `DATETIME` | |
| *(relationship)* | `wallets` | `List<Wallet>` | `@OneToMany(mappedBy="account", CASCADE ALL, LAZY)` |

#### `Wallet` → table `WALLET_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `wllt_id` | `walletId` | `CHAR(36)` | PK, UUID |
| `wllt_acct_id` | `account` | `CHAR(36)` | FK → `ACCOUNT_T.acct_id`, NOT NULL |
| `wllt_user_id` | `walletUserId` | `CHAR(36)` | NOT NULL |
| `wllt_currency` | `walletCurrency` | `CHAR(3)` | NOT NULL |
| `wllt_balance` | `walletBalance` | `DECIMAL(19,4)` | NOT NULL, default 0 |
| `wllt_status` | `walletStatus` | `VARCHAR(20)` | NOT NULL, default `'ACTIVE'` |
| `wllt_created` | `walletCreated` | `DATETIME` | not updatable |
| `wllt_updated` | `walletUpdated` | `DATETIME` | |

Unique constraint: `(wllt_acct_id, wllt_currency)` — one wallet per currency per account.

#### `Payee` → table `PAYEE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `paye_id` | `payeeId` | `CHAR(36)` | PK, UUID |
| `paye_acct_id` | `payeeAccountId` | `CHAR(36)` | NOT NULL |
| `paye_user_id` | `payeeUserId` | `CHAR(36)` | NOT NULL |
| `paye_nickname` | `payeeNickname` | `VARCHAR(100)` | nullable |
| `paye_created` | `payeeCreated` | `DATETIME` | not updatable |

Unique constraint: `(paye_acct_id, paye_user_id)`.

### 3.2 DTOs

#### Request DTOs

| DTO | Fields |
|-----|--------|
| `OpenWalletRequest` | `currency` (NotBlank) |
| `TopUpRequest` | `currency` (NotBlank), `amount` (NotNull, min 0.01) |
| `WalletOperationRequest` | `currency` (NotBlank), `amount` (NotNull, min 0.0001) — internal use only |
| `PayeeRequest` | `payeeUserId` (optional), `identifier` (optional), `nickname` (optional) |

#### Response DTOs

| DTO | Fields |
|-----|--------|
| `WalletResponse` | `walletId`, `accountId`, `currency`, `balance` (BigDecimal), `status`, `createdAt`, `updatedAt` |
| `AccountResponse` | `accountId`, `userId`, `wallets` (List\<WalletResponse\>), `accounts` (List\<WalletResponse\>) ⚠️, `createdAt`, `updatedAt` |
| `PayeeResponse` | `payeeId`, `userId`, `nickname`, `createdAt` |
| `WalletRegistrationStatusResponse` | `currency`, `walletExists` (boolean), `canRegister` (boolean) |

> ⚠️ `AccountResponse.accounts` is a backward-compatibility alias for `accounts` — always set to same value as `wallets`. Frontend should migrate to using `wallets`.

### 3.3 DAO (Repositories)

#### `AccountRepository`
```
findByAccountUserId(String)      → Optional<Account>
existsByAccountUserId(String)    → boolean
```

#### `WalletRepository`
```
findByAccount_AccountId(String)                                         → List<Wallet>
countByAccount_AccountId(String)                                        → long
findByAccount_AccountUserIdAndWalletCurrency(String, String)            → Optional<Wallet>
existsByAccount_AccountIdAndWalletCurrency(String, String)              → boolean
```

#### `PayeeRepository`
```
findByPayeeAccountId(String)                              → List<Payee>
existsByPayeeAccountIdAndPayeeUserId(String, String)      → boolean
findByPayeeIdAndPayeeAccountId(String, String)             → Optional<Payee>
```

### 3.4 Mapper: `WalletMapper`

| Mapping | Source → Target |
|---------|----------------|
| `toAccountResponse(Account, List<Wallet>)` | `accountId`, `accountUserId→userId`, `wallets` (mapped list), `accounts` (same mapped list), `accountCreated→createdAt`, `accountUpdated→updatedAt` |
| `toWalletResponse(Wallet)` | `walletId`, `account.accountId→accountId`, `walletCurrency→currency`, `walletBalance→balance`, `walletStatus→status`, `walletCreated→createdAt`, `walletUpdated→updatedAt` |
| `toPayeeResponse(Payee)` | `payeeId`, `payeeUserId→userId`, `payeeNickname→nickname`, `payeeCreated→createdAt` |

> Note: `PayeeResponse.userId` is set from `payeeUserId` (the payee's user ID, not the owner's). This is the stored target user ID.

### 3.5 API Endpoints

| Method | Path | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/api/wallets/internal/create` | X-User-Id header | — | `ApiResponse<AccountResponse>` |
| GET | `/api/wallets/me` | JWT | — | `ApiResponse<AccountResponse>` |
| GET | `/api/accounts/me` | JWT | — | `ApiResponse<AccountResponse>` |
| POST | `/api/wallets` | JWT | `OpenWalletRequest` | `ApiResponse<WalletResponse>` |
| POST | `/api/accounts/wallets` | JWT | `OpenWalletRequest` | `ApiResponse<WalletResponse>` |
| DELETE | `/api/accounts/wallets/{currency}` | JWT | — | `ApiResponse<WalletResponse>` |
| GET | `/api/wallets/registration/{currency}` | JWT | — | `ApiResponse<WalletRegistrationStatusResponse>` |
| GET | `/api/wallets/balance/{currency}` | JWT | — | `ApiResponse<WalletResponse>` |
| POST | `/api/wallets/topup` | JWT | `TopUpRequest` | `ApiResponse<WalletResponse>` |
| POST | `/api/wallets/internal/debit/{userId}` | Internal | `WalletOperationRequest` | `ApiResponse<Void>` |
| POST | `/api/wallets/internal/credit/{userId}` | Internal | `WalletOperationRequest` | `ApiResponse<Void>` |
| GET | `/api/wallets/payees` | JWT | — | `ApiResponse<List<PayeeResponse>>` |
| POST | `/api/wallets/payees` | JWT | `PayeeRequest` | `ApiResponse<PayeeResponse>` |
| DELETE | `/api/wallets/payees/{payeeId}` | JWT | — | `ApiResponse<Void>` |

### 3.6 Frontend API (`src/api/wallet.js`, `src/api/payee.js`)

| Function | Method + Path | Payload Sent | Response Expected |
|----------|--------------|--------------|-------------------|
| `getWallet()` | GET `/api/wallets/me` | — | `AccountResponse` |
| `getAccount()` | GET `/api/accounts/me` | — | `AccountResponse` |
| `getBalance(currency)` | GET `/api/wallets/balance/{currency}` | — | `WalletResponse` |
| `getRegistrationStatus(currency)` | GET `/api/wallets/registration/{currency}` | — | `WalletRegistrationStatusResponse` |
| `openWallet(body)` | POST `/api/accounts/wallets` | `{ currency }` | `WalletResponse` |
| `closeWallet(currency)` | DELETE `/api/accounts/wallets/{currency}` | — | `WalletResponse` |
| `topUp(body)` | POST `/api/wallets/topup` | `{ currency, amount }` | `WalletResponse` |
| `getPayees()` | GET `/api/wallets/payees` | — | `List<PayeeResponse>` |
| `addPayee({ identifier, nickname })` | POST `/api/wallets/payees` | `{ identifier, nickname }` | `PayeeResponse` |
| `removePayee(id)` | DELETE `/api/wallets/payees/{id}` | — | — |

> ⚠️ `PayeeRequest` has three optional fields: `payeeUserId`, `identifier`, `nickname`. Frontend sends only `{ identifier, nickname }` — `payeeUserId` is resolved server-side from the identifier. Consistent by design.

---

## 4. Collection Service

**Database:** `ewallet_collection_db` | **Port:** 8083

### 4.1 Domain Objects (Entities)

#### `Collection` → table `COLLECTION_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `coll_id` | `collectionId` | `CHAR(36)` | PK, set by `@PrePersist` |
| `coll_name` | `collectionName` | `VARCHAR(255)` | NOT NULL |
| `coll_desc` | `collectionDescription` | `VARCHAR(500)` | nullable |
| `coll_category` | `collectionCategory` | `VARCHAR(20)` | enum `CollectionCategory` |
| `coll_type_name` | `collectionTypeName` | `VARCHAR(100)` | nullable |
| `coll_currency` | `collectionCurrency` | `CHAR(3)` | NOT NULL |
| `coll_status` | `collectionStatus` | `VARCHAR(20)` | enum `CollectionStatus`, default `ACTIVE` |
| `coll_owner_id` | `collectionOwnerId` | `CHAR(36)` | NOT NULL |
| `coll_created` | `collectionCreated` | `DATETIME` | not updatable |
| `coll_updated` | `collectionUpdated` | `DATETIME` | |

#### `CollectionMember` → table `COLLECTION_MEMBER_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `cm_id` | `collectionMemberId` | `CHAR(36)` | PK, set by `@PrePersist` |
| `cm_coll_id` | `collection` | `CHAR(36)` | FK → `COLLECTION_T.coll_id`, NOT NULL |
| `cm_user_id` | `collectionMemberUserId` | `CHAR(36)` | NOT NULL |
| `cm_role` | `collectionMemberRole` | `VARCHAR(20)` | NOT NULL, enum `CollectionRole` |
| `cm_joined_at` | `collectionMemberJoinedDateTime` | `DATETIME` | not updatable |

Unique constraint: `(cm_coll_id, cm_user_id)`.

#### `CollectionType` → table `COLLECTION_TYPE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `ctyp_id` | `collectionTypeId` | `CHAR(36)` | PK, set by `@PrePersist` |
| `ctyp_user_id` | `collectionTypeUserId` | `CHAR(36)` | nullable (system types have null) |
| `ctyp_name` | `collectionTypeName` | `VARCHAR(100)` | NOT NULL |
| `ctyp_system` | `collectionTypeSystem` | `BIT(1)` | default false |
| `ctyp_created` | `collectionTypeCreated` | `DATETIME` | not updatable |

Unique constraint: `(ctyp_user_id, ctyp_name)`.

#### `Expense` → table `EXPENSE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `exp_id` | `expenseId` | `CHAR(36)` | PK, set by `@PrePersist` |
| `exp_coll_id` | `collection` | `CHAR(36)` | FK → `COLLECTION_T.coll_id`, NOT NULL |
| `exp_title` | `expenseTitle` | `VARCHAR(30)` | NOT NULL |
| `exp_desc` | `expenseDescription` | `VARCHAR(100)` | nullable |
| `exp_amount` | `expenseAmount` | `DECIMAL(19,4)` | NOT NULL |
| `exp_currency` | `expenseCurrency` | `CHAR(3)` | NOT NULL |
| `exp_paid_by` | `expensePaidBy` | `CHAR(36)` | NOT NULL (userId) |
| `exp_split_type` | `expenseSplitType` | `VARCHAR(20)` | NOT NULL, enum `SplitType` |
| `exp_tax_rate` | `expenseTaxRate` | `DECIMAL(5,4)` | nullable |
| `exp_tax_type` | `expenseTaxType` | `VARCHAR(20)` | nullable |
| `exp_created` | `expenseCreated` | `DATETIME` | not updatable |
| `exp_updated` | `expenseUpdated` | `DATETIME` | |

#### `ExpenseShare` → table `EXPENSE_SHARE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `es_id` | `expenseShareId` | `CHAR(36)` | PK, set by `@PrePersist` |
| `es_exp_id` | `expense` | `CHAR(36)` | FK → `EXPENSE_T.exp_id`, NOT NULL |
| `es_user_id` | `expenseShareUserId` | `CHAR(36)` | NOT NULL |
| `es_base_amt` | `expenseShareBaseAmount` | `DECIMAL(19,4)` | nullable |
| `es_tax_amt` | `expenseShareTaxAmount` | `DECIMAL(19,4)` | default 0 |
| `es_total_amt` | `expenseShareTotalAmount` | `DECIMAL(19,4)` | NOT NULL |
| `es_settled` | `expenseShareSettled` | `BIT(1)` | default false |
| `es_settled_at` | `expenseShareSettledDateTime` | `DATETIME` | nullable |

#### `Invitation` → table `INVITATION_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `inv_id` | `invitationId` | `CHAR(36)` | PK, set by `@PrePersist` |
| `inv_coll_id` | `collection` | `CHAR(36)` | FK → `COLLECTION_T.coll_id`, NOT NULL |
| `inv_inviter` | `invitationInviter` | `CHAR(36)` | NOT NULL (userId) |
| `inv_invitee` | `invitationInvitee` | `CHAR(36)` | NOT NULL (userId) |
| `inv_role` | `invitationRole` | `VARCHAR(20)` | NOT NULL, enum `CollectionRole` |
| `inv_status` | `invitationStatus` | `VARCHAR(20)` | enum `InvitationStatus`, default `PENDING` |
| `inv_created` | `invitationCreated` | `DATETIME` | not updatable |
| `inv_updated` | `invitationUpdated` | `DATETIME` | |

#### `SplitRule` → table `SPLIT_RULE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `sr_id` | `splitRuleId` | `CHAR(36)` | PK, set by `@PrePersist` |
| `sr_exp_id` | `expense` | `CHAR(36)` | FK → `EXPENSE_T.exp_id`, NOT NULL |
| `sr_user_id` | `splitRuleUserId` | `CHAR(36)` | NOT NULL |
| `sr_percentage` | `splitRulePercentage` | `DECIMAL(8,4)` | nullable |
| `sr_fixed_amt` | `splitRuleFixedAmount` | `DECIMAL(19,4)` | nullable |
| `sr_weight` | `splitRuleWeight` | `INT` | nullable |

### 4.2 DTOs

#### Request DTOs

| DTO | Fields |
|-----|--------|
| `CreateCollectionRequest` | `name` (NotBlank), `description` (optional), `category` (NotNull, `CollectionCategory`), `typeName` (optional), `currency` (NotBlank) |
| `CreateExpenseRequest` | `title` (NotBlank, max 30), `description` (max 100, optional), `amount` (NotNull, min 0.01), `currency` (NotBlank), `paidBy` (NotBlank, userId), `splitType` (NotNull, `SplitType`), `participants` (NotEmpty, `List<ParticipantShare>`) ⚠️, `taxRate` (optional), `taxType` (optional) |
| `InviteRequest` | `inviteeUserId` (optional), `identifier` (optional), `role` (NotNull, `CollectionRole`) |
| `InvitationActionRequest` | `action` (NotBlank, pattern `ACCEPT\|DECLINE`) |
| `CollectionTypeRequest` | `name` (NotBlank) |

> ⚠️ `ParticipantShare` — inner/separate class referenced in `CreateExpenseRequest` but not scanned. Expected fields: `userId`, and one or more of `percentage`, `amount`, `weight` depending on `splitType`.

#### Response DTOs

| DTO | Fields |
|-----|--------|
| `CollectionResponse` | `collectionId`, `name`, `description`, `category` (enum), `typeName`, `currency`, `status` (enum), `ownerId`, `createdAt`, `updatedAt`, `lastSettledAt`, `myRole` (enum, nullable), `myNetBalance` (BigDecimal, nullable) |
| `MemberResponse` | `memberId`, `userId`, `role` (enum), `joinedAt`, `userNickname` *, `invitationCode` * |
| `ExpenseResponse` | `expenseId`, `collectionId`, `title`, `description`, `amount`, `currency`, `paidBy`, `splitType` (enum), `taxRate`, `taxType`, `shares` (List\<ShareResponse\>), `createdAt` ⚠️ |
| `ShareResponse` (collection pkg) | `shareId`, `expenseId`, `userId`, `baseAmount`, `taxAmount`, `totalAmount`, `settled` (boolean), `settledAt` |
| `InvitationResponse` | `invitationId`, `collectionId`, `collectionName`, `inviterId`, `inviteeId`, `role` (enum), `status` (enum), `createdAt` |
| `CollectionTypeResponse` | `collectionTypeId`, `name`, `system` (boolean) |
| `BalanceSummaryResponse` | `userId`, `netBalance`, `totalOwed`, `totalOwing`, `userNickname` *, `invitationCode` * |

> \* `userNickname` and `invitationCode` in `MemberResponse` / `BalanceSummaryResponse` are populated by inter-service calls to auth-service. They are NOT stored in the collection DB.  
> ⚠️ `ExpenseResponse` missing `updatedAt` — entity has `expenseUpdated` but it's not mapped to the response.

### 4.3 DAO (Repositories)

#### `CollectionRepository`
```
findByCollectionOwnerId(String)  → List<Collection>
```

#### `CollectionMemberRepository`
```
findByCollection(Collection)                                          → List<CollectionMember>
findByCollectionMemberUserId(String)                                  → List<CollectionMember>
findByCollectionAndCollectionMemberUserId(Collection, String)         → Optional<CollectionMember>
existsByCollectionAndCollectionMemberUserId(Collection, String)       → boolean
```

#### `ExpenseRepository`
```
findByCollection(Collection)                                          → List<Expense>
findByCollectionAndExpensePaidBy(Collection, String)                  → List<Expense>
countByCollection(Collection)                                         → long
```

#### `ExpenseShareRepository`
```
findByExpense(Expense)                                                → List<ExpenseShare>
findByExpenseShareIdAndExpense_ExpenseId(String, String)              → Optional<ExpenseShare>
findByExpenseShareUserIdAndExpense_Collection_CollectionId(String, String) → List<ExpenseShare>
findLastSettledDateTimeByCollection(Collection) [JPQL]                → Optional<LocalDateTime>
```

#### `InvitationRepository`
```
findByInvitationInviteeAndInvitationStatus(String, InvitationStatus) → List<Invitation>
findByCollection(Collection)                                          → List<Invitation>
existsByCollectionAndInvitationInviteeAndInvitationStatus(Collection, String, InvitationStatus) → boolean
```

#### `SplitRuleRepository`
```
findByExpense(Expense) → List<SplitRule>
```

#### `CollectionTypeRepository`
```
findByCollectionTypeUserId(String)                                    → List<CollectionType>
existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase(String, String) → boolean
```

### 4.4 Mappers

#### `CollectionMapper`
| Mapping | Notes |
|---------|-------|
| `toResponse(Collection)` | Maps all entity fields; `myRole`, `myNetBalance`, `lastSettledAt` default to null |
| `toResponse(Collection, CollectionRole)` | Adds myRole |
| `toResponse(Collection, CollectionRole, BigDecimal)` | Adds myNetBalance |
| `toResponse(Collection, CollectionRole, BigDecimal, LocalDateTime)` | Full mapping |
| `toMemberResponse(CollectionMember)` | `userNickname`/`invitationCode` null |
| `toMemberResponse(CollectionMember, String, String)` | Full mapping with enriched user data |

#### `ExpenseMapper`
| Mapping | Notes |
|---------|-------|
| `toResponse(Expense, List<ExpenseShare>)` | Maps all expense fields + share list |
| `toShareResponse(ExpenseShare)` | Maps all share fields |

#### `InvitationMapper`
| Mapping | Notes |
|---------|-------|
| `toResponse(Invitation)` | Maps all invitation fields; `collectionName` from relationship |

### 4.5 API Endpoints

| Method | Path | Role Required | Request | Response |
|--------|------|---------------|---------|----------|
| POST | `/api/collections` | — | `CreateCollectionRequest` | `ApiResponse<CollectionResponse>` |
| GET | `/api/collections` | — | — | `ApiResponse<List<CollectionResponse>>` |
| GET | `/api/collections/{collectionId}` | MEMBER | — | `ApiResponse<CollectionResponse>` |
| PUT | `/api/collections/{collectionId}` | ADMIN | `CreateCollectionRequest` | `ApiResponse<CollectionResponse>` |
| POST | `/api/collections/{collectionId}/close` | — | — | `ApiResponse<Void>` |
| GET | `/api/collections/{collectionId}/members` | MEMBER | — | `ApiResponse<List<MemberResponse>>` |
| DELETE | `/api/collections/{collectionId}/members/{userId}` | ADMIN | — | `ApiResponse<Void>` |
| GET | `/api/collections/{collectionId}/balances` | MEMBER | — | `ApiResponse<List<BalanceSummaryResponse>>` |
| POST | `/api/collections/{collectionId}/expenses` | ADMIN/EDITOR | `CreateExpenseRequest` | `ApiResponse<ExpenseResponse>` |
| GET | `/api/collections/{collectionId}/expenses` | MEMBER | — | `ApiResponse<List<ExpenseResponse>>` |
| GET | `/api/collections/{collectionId}/expenses/{expenseId}` | MEMBER | — | `ApiResponse<ExpenseResponse>` |
| PUT | `/api/collections/{collectionId}/expenses/{expenseId}` | ADMIN/EDITOR | `CreateExpenseRequest` | `ApiResponse<ExpenseResponse>` |
| DELETE | `/api/collections/{collectionId}/expenses/{expenseId}` | ADMIN/EDITOR | — | `ApiResponse<Void>` |
| GET | `/api/collections/{collectionId}/expenses/{expenseId}/shares/{shareId}` | MEMBER | — | `ApiResponse<ShareResponse>` |
| PUT | `/api/collections/{collectionId}/expenses/{expenseId}/shares/{shareId}/settle` | MEMBER | — | `ApiResponse<Void>` |
| PUT | `/api/collections/{collectionId}/expenses/{expenseId}/shares/{shareId}/unsettle` | MEMBER | — | `ApiResponse<Void>` |
| POST | `/api/collections/{collectionId}/invitations` | ADMIN/EDITOR | `InviteRequest` | `ApiResponse<InvitationResponse>` |
| GET | `/api/collections/{collectionId}/invitations` | MEMBER | — | `ApiResponse<List<InvitationResponse>>` |
| GET | `/api/invitations` | — | — | `ApiResponse<List<InvitationResponse>>` |
| POST | `/api/invitations/{invitationId}/respond` | — | `InvitationActionRequest` | `ApiResponse<InvitationResponse>` |
| GET | `/api/collections/types` | — | — | `ApiResponse<List<CollectionTypeResponse>>` |
| POST | `/api/collections/types` | — | `CollectionTypeRequest` | `ApiResponse<CollectionTypeResponse>` |
| DELETE | `/api/collections/types/{collectionTypeId}` | — | — | 204 No Content |

### 4.6 Frontend API

| Function | Method + Path | Payload Sent | Response Expected |
|----------|--------------|--------------|-------------------|
| `list()` | GET `/api/collections` | — | `List<CollectionResponse>` |
| `get(id)` | GET `/api/collections/{id}` | — | `CollectionResponse` |
| `create(body)` | POST `/api/collections` | `{ name, description, category, typeName, currency }` | `CollectionResponse` |
| `update(id, body)` | PUT `/api/collections/{id}` | `{ name, description, category, typeName, currency }` | `CollectionResponse` |
| `close(id)` | POST `/api/collections/{id}/close` | — | — |
| `getMembers(id)` | GET `/api/collections/{id}/members` | — | `List<MemberResponse>` |
| `removeMember(id, userId)` | DELETE `/api/collections/{id}/members/{userId}` | — | — |
| `getBalances(id)` | GET `/api/collections/{id}/balances` | — | `List<BalanceSummaryResponse>` |
| `listTypes()` | GET `/api/collections/types` | — | `List<CollectionTypeResponse>` |
| `createType(body)` | POST `/api/collections/types` | `{ name }` | `CollectionTypeResponse` |
| `deleteType(id)` | DELETE `/api/collections/types/{id}` | — | — |
| `list(colId)` (expense) | GET `/api/collections/{colId}/expenses` | — | `List<ExpenseResponse>` |
| `create(colId, body)` (expense) | POST `/api/collections/{colId}/expenses` | `{ title, description, amount, currency, paidBy, splitType, participants, taxRate, taxType }` | `ExpenseResponse` |
| `settleShare(colId, expId, shareId, body)` (expense) | PUT `.../shares/{shareId}/settle` | `body` ⚠️ | — |
| `myInvitations()` | GET `/api/invitations` | — | `List<InvitationResponse>` |
| `respond(id, action)` | POST `/api/invitations/{id}/respond` | `{ action }` | `InvitationResponse` |

> ⚠️ `expenseApi.settleShare` sends a `body` parameter but the backend endpoint is a plain PUT with no `@RequestBody` — body is silently ignored.

---

## 5. Transaction Service

**Database:** `ewallet_transaction_db` | **Port:** 8084

### 5.1 Domain Objects (Entities)

#### `Transaction` → table `TRANSACTION_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `txn_id` | `transactionId` | `CHAR(36)` | PK, UUID |
| `txn_payer_id` | `transactionPayerId` | `CHAR(36)` | NOT NULL |
| `txn_payee_id` | `transactionPayeeId` | `CHAR(36)` | NOT NULL |
| `txn_amount` | `transactionAmount` | `DECIMAL(19,4)` | NOT NULL |
| `txn_currency` | `transactionCurrency` | `CHAR(3)` | nullable |
| `txn_converted_amt` | `transactionConvertedAmount` | `DECIMAL(19,4)` | nullable |
| `txn_payee_curr` | `transactionPayeeCurrency` | `CHAR(3)` | nullable |
| `txn_type` | `transactionType` | `VARCHAR(20)` | NOT NULL, enum `TransactionType` |
| `txn_status` | `transactionStatus` | `VARCHAR(20)` | NOT NULL, enum `TransactionStatus`, default `PENDING` |
| `txn_idem_key` | `transactionIdempotencyKey` | `VARCHAR(255)` | UNIQUE, nullable |
| `txn_created` | `transactionCreated` | `DATETIME` | not updatable |
| `txn_updated` | `transactionUpdated` | `DATETIME` | |

#### `Settlement` → table `SETTLEMENT_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `stl_id` | `settlementId` | `CHAR(36)` | PK, UUID |
| `stl_txn_id` | `settlementTransactionId` | `CHAR(36)` | NOT NULL |
| `stl_share_id` | `settlementExpenseShareId` | `CHAR(36)` | nullable |
| `stl_coll_id` | `settlementCollectionId` | `CHAR(36)` | nullable |
| `stl_payer_id` | `settlementPayerId` | `CHAR(36)` | NOT NULL |
| `stl_payee_id` | `settlementPayeeId` | `CHAR(36)` | NOT NULL |
| `stl_amount` | `settlementAmount` | `DECIMAL(19,4)` | NOT NULL |
| `stl_created` | `settlementCreated` | `DATETIME` | not updatable |

#### `SagaState` → table `SAGA_STATE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `saga_id` | `sagaStateId` | `CHAR(36)` | PK, UUID |
| `saga_txn_id` | `sagaStateTransactionId` | `CHAR(36)` | NOT NULL |
| `saga_step` | `sagaStateStep` | `INT` | NOT NULL |
| `saga_status` | `sagaStateStatus` | `VARCHAR(20)` | nullable |
| `saga_comp_step` | `sagaStateCompensationStep` | `INT` | nullable |
| `saga_updated` | `sagaStateUpdated` | `DATETIME` | set by `@PrePersist`/`@PreUpdate` |

### 5.2 DTOs

#### Request DTOs

| DTO | Fields |
|-----|--------|
| `SettleRequest` | `shareId` (NotBlank), `collectionId` (NotBlank), `expenseId` (NotBlank), `payeeCurrency` (optional), `idempotencyKey` (optional) |
| `NettingRequest` | `counterpartyId` (NotBlank), `collectionIds` (NotEmpty, List\<String\>), `currency` (optional), `idempotencyKey` (optional) |

#### Response DTOs

| DTO | Fields |
|-----|--------|
| `TransactionResponse` | `transactionId`, `payerId`, `payeeId`, `amount`, `currency`, `type` (enum), `status` (enum), `createdAt` ⚠️ |
| `SettlementResponse` | `transactionId`, `settlementId`, `payerId`, `payeeId`, `amount`, `currency`, `convertedAmount`, `payeeCurrency`, `status` (enum), `createdAt` |
| `NettingResponse` | `transactionId`, `netPayerId`, `netPayeeId`, `netAmount`, `currency`, `status` (enum), `createdAt` |
| `ShareResponse` (txn pkg) | `shareId`, `expenseId`, `userId`, `baseAmount`, `taxAmount`, `totalAmount`, `settled` (boolean), `settledAt` |

> ⚠️ `TransactionResponse` is missing `convertedAmount` and `payeeCurrency` — these are present in the entity and in `SettlementResponse` but not exposed in the history endpoint response. Callers of `/api/transactions/history` cannot see FX conversion details.

### 5.3 DAO (Repositories)

#### `TransactionRepository`
```
findByTransactionIdempotencyKey(String)                                                        → Optional<Transaction>
existsByTransactionIdempotencyKey(String)                                                      → boolean
findByTransactionPayerIdOrTransactionPayeeIdOrderByTransactionCreatedDesc(String, String)      → List<Transaction>
findHistoryByUserIdAndCurrency(String userId, String currency) [JPQL]                         → List<Transaction>
```

#### `SettlementRepository`
```
findBySettlementTransactionId(String) → List<Settlement>
```

#### `SagaStateRepository`
```
findBySagaStateTransactionId(String) → Optional<SagaState>
```

### 5.4 API Endpoints

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/api/transactions/settle` | `SettleRequest` | `ApiResponse<SettlementResponse>` |
| POST | `/api/transactions/settle-net` | `NettingRequest` | `ApiResponse<NettingResponse>` |
| GET | `/api/transactions/history` | — | `ApiResponse<List<TransactionResponse>>` |
| GET | `/api/transactions/history/{currency}` | — | `ApiResponse<List<TransactionResponse>>` |

### 5.5 Frontend API (`src/api/transaction.js`)

| Function | Method + Path | Payload Sent | Response Expected |
|----------|--------------|--------------|-------------------|
| `settle(body)` | POST `/api/transactions/settle` | `{ shareId, collectionId, expenseId, payeeCurrency?, idempotencyKey? }` | `SettlementResponse` |
| `settleNet(body)` | POST `/api/transactions/settle-net` | `{ counterpartyId, collectionIds, currency?, idempotencyKey? }` | `NettingResponse` |
| `history()` | GET `/api/transactions/history` | — | `List<TransactionResponse>` |
| `historyByCurrency(cur)` | GET `/api/transactions/history/{cur}` | — | `List<TransactionResponse>` |

---

## 6. Currency Service

**Database:** `ewallet_currency_db` | **Port:** 8085

### 6.1 Domain Objects (Entities)

#### `Currency` → table `CURRENCY_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `curr_id` | `currencyId` | `CHAR(36)` | PK, UUID |
| `curr_code` | `currencyCode` | `CHAR(3)` | NOT NULL, UNIQUE |
| `curr_name` | `currencyName` | `VARCHAR(100)` | nullable |
| `curr_symbol` | `currencySymbol` | `VARCHAR(5)` | nullable |
| `curr_active` | `currencyActive` | `BIT(1)` | default true |

#### `ExchangeRate` → table `EXCHANGE_RATE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `exrt_id` | `exchangeRateId` | `CHAR(36)` | PK, UUID |
| `exrt_base` | `exchangeRateBaseCurrency` | `CHAR(3)` | NOT NULL |
| `exrt_target` | `exchangeRateTargetCurrency` | `CHAR(3)` | NOT NULL |
| `exrt_rate` | `exchangeRateValue` | `DECIMAL(20,6)` | NOT NULL |
| `exrt_fetched` | `exchangeRateFetchedDateTime` | `DATETIME` | NOT NULL |

### 6.2 DTOs

| DTO | Fields |
|-----|--------|
| `CurrencyResponse` | `code`, `name`, `symbol`, `active` (boolean) |
| `ExchangeRateResponse` | `baseCurrency`, `targetCurrency`, `rate` (BigDecimal), `fetchedAt` |
| `ConversionResponse` | `fromCurrency`, `toCurrency`, `originalAmount`, `convertedAmount`, `rate` |
| `ExchangeRateTableResponse` | `baseCurrency`, `rates` (Map\<String, BigDecimal\>), `fetchedAt` |

### 6.3 DAO (Repositories)

#### `CurrencyRepository`
```
findByCurrencyActiveTrue()                  → List<Currency>
findByCurrencyCode(String)                  → Optional<Currency>
existsByCurrencyCode(String)               → boolean
```

#### `ExchangeRateRepository`
```
findTopByExchangeRateBaseCurrencyAndExchangeRateTargetCurrencyOrderByExchangeRateFetchedDateTimeDesc(String, String) → Optional<ExchangeRate>
```

### 6.4 API Endpoints

| Method | Path | Query Params | Response |
|--------|------|--------------|----------|
| GET | `/api/currency/currencies` | — | `ApiResponse<List<CurrencyResponse>>` |
| GET | `/api/currency/rates` | `base` (default: MYR) | `ApiResponse<ExchangeRateTableResponse>` |
| GET | `/api/currency/rates/{from}/{to}` | — | `ApiResponse<ExchangeRateResponse>` |
| GET | `/api/currency/convert` | `from`, `to`, `amount` | `ApiResponse<ConversionResponse>` |

### 6.5 Frontend API (`src/api/currency.js`)

| Function | Method + Path | Notes |
|----------|--------------|-------|
| `list()` | GET `/api/currency/currencies` | Returns `List<CurrencyResponse>` |
| `rates(base)` | GET `/api/currency/rates?base={base}` | Returns `ExchangeRateTableResponse` |
| `rate(from, to)` | GET `/api/currency/rates/{from}/{to}` | Returns `ExchangeRateResponse` |
| `convert(from, to, amount)` | GET `/api/currency/convert?from&to&amount` | Returns `ConversionResponse` |
| `getRates(base)` | Wrapper for `rates()` | Has hardcoded fallback: `{ USD:1, MYR:4.47, SGD:1.35 }` on error |
| `convertCurrency({from,to,amount})` | Wrapper for `convert()` | Has fallback calculation using hardcoded rates |

---

## 7. Notification Service

**Database:** `ewallet_notification_db` | **Port:** 8086

### 7.1 Domain Objects (Entities)

#### `Notification` → table `NOTIFICATION_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `notf_id` | `notificationId` | `CHAR(36)` | PK, UUID |
| `notf_user_id` | `notificationUserId` | `CHAR(36)` | NOT NULL |
| `notf_type` | `notificationType` | `VARCHAR(50)` | nullable |
| `notf_title` | `notificationTitle` | `VARCHAR(255)` | nullable |
| `notf_message` | `notificationMessage` | `TEXT` | nullable |
| `notf_ref_id` | `notificationReferenceId` | `CHAR(36)` | nullable |
| `notf_read` | `notificationRead` | `BIT(1)` | default false |
| `notf_read_at` | `notificationReadDateTime` | `DATETIME` | nullable |
| `notf_created` | `notificationCreated` | `DATETIME` | not updatable |

#### `UserPreference` → table `USER_PREFERENCE_T`

| Column | Java Field | Type | Constraints |
|--------|-----------|------|-------------|
| `uprf_id` | `userPreferenceId` | `CHAR(36)` | PK, UUID |
| `uprf_user_id` | `userPreferenceUserId` | `CHAR(36)` | NOT NULL, UNIQUE |
| `uprf_email_enabled` | `userPreferenceEmailEnabled` | `BIT(1)` | NOT NULL, default true |
| `uprf_sms_enabled` | `userPreferenceSmsEnabled` | `BIT(1)` | NOT NULL, default false |
| `uprf_push_enabled` | `userPreferencePushEnabled` | `BIT(1)` | NOT NULL, default true |
| `uprf_promo_enabled` | `userPreferencePromoEnabled` | `BIT(1)` | NOT NULL, default false |
| `uprf_created` | `userPreferenceCreated` | `DATETIME` | not updatable |
| `uprf_updated` | `userPreferenceUpdated` | `DATETIME` | |

### 7.2 DTOs

#### Request DTOs

| DTO | Fields |
|-----|--------|
| `CreateNotificationRequest` | `userId` (NotBlank), `type` (NotBlank), `title` (NotBlank), `message` (optional), `referenceId` (optional) |
| `UpdateUserPreferenceRequest` | `emailEnabled` (Boolean, nullable), `smsEnabled` (Boolean, nullable), `pushEnabled` (Boolean, nullable), `promoEnabled` (Boolean, nullable) |

#### Response DTOs

| DTO | Fields |
|-----|--------|
| `NotificationResponse` | `notificationId`, `userId`, `type`, `title`, `message`, `referenceId`, `read` (boolean), `readAt`, `createdAt` |
| `UserPreferenceResponse` | `userPreferenceId`, `userId`, `emailEnabled`, `smsEnabled`, `pushEnabled`, `promoEnabled`, `createdAt`, `updatedAt` |

### 7.3 DAO (Repositories)

#### `NotificationRepository`
```
findByNotificationUserIdOrderByNotificationCreatedDesc(String)                                    → List<Notification>
findByNotificationUserIdAndNotificationReadFalseOrderByNotificationCreatedDesc(String)            → List<Notification>
countByNotificationUserIdAndNotificationReadFalse(String)                                         → long
existsByNotificationReferenceIdAndNotificationType(String, String)                               → boolean
```

#### `UserPreferenceRepository`
```
findByUserPreferenceUserId(String) → Optional<UserPreference>
```

### 7.4 Mappers

#### `UserPreferenceMapper`
| Mapping | Source → Target |
|---------|----------------|
| `toResponse(UserPreference)` | `userPreferenceId`, `userPreferenceUserId→userId`, all boolean flags, `createdAt`, `updatedAt` |
| `applyUpdate(UserPreference, UpdateUserPreferenceRequest)` | Applies non-null Boolean fields only (patch semantics) |

### 7.5 API Endpoints

| Method | Path | Request | Response |
|--------|------|---------|----------|
| POST | `/api/notifications/internal/create` | `CreateNotificationRequest` | `ApiResponse<NotificationResponse>` |
| GET | `/api/notifications` | — | `ApiResponse<List<NotificationResponse>>` |
| GET | `/api/notifications/unread` | — | `ApiResponse<List<NotificationResponse>>` |
| GET | `/api/notifications/unread/count` | — | `ApiResponse<Map<String,Long>>` → `{ count: N }` |
| PUT | `/api/notifications/{id}/read` | — | `ApiResponse<Void>` |
| PUT | `/api/notifications/read-all` | — | `ApiResponse<Void>` |
| GET | `/api/notifications/preferences` | — | `ApiResponse<UserPreferenceResponse>` |
| PUT | `/api/notifications/preferences` | `UpdateUserPreferenceRequest` | `ApiResponse<UserPreferenceResponse>` |

### 7.6 Frontend API (`src/api/notification.js`, `src/api/preference.js`)

| Function | Method + Path | Response Expected |
|----------|--------------|-------------------|
| `list()` | GET `/api/notifications` | `List<NotificationResponse>` |
| `unread()` | GET `/api/notifications/unread` | `List<NotificationResponse>` |
| `unreadCount()` | GET `/api/notifications/unread/count` | `{ count: N }` (Map) |
| `markRead(id)` | PUT `/api/notifications/{id}/read` | — |
| `markAllRead()` | PUT `/api/notifications/read-all` | — |
| `getPreferences()` | GET `/api/notifications/preferences` | `UserPreferenceResponse` |
| `updatePreferences(body)` | PUT `/api/notifications/preferences` | `{ emailEnabled?, smsEnabled?, pushEnabled?, promoEnabled? }` → `UserPreferenceResponse` |

---

## 8. Reporting Service

**Port:** 8087 | **No dedicated database** — aggregates data by calling other services.

### 8.1 DTOs (Response only — no persisted entities)

| DTO | Fields |
|-----|--------|
| `UserDashboardReport` | `walletSummary` (Map), `walletBalances` (Map), `collectionCount` (int), `totalTransacted`, `transactionCount` (long), `totalReceivable`, `totalPayable`, `netPosition` |
| `SpendingSummaryReport` | `totalSpent`, `totalReceived`, `netBalance`, `byType` (Map\<String, BigDecimal\>) |
| `CurrencyLedgerReport` | `currency`, `totalInflow`, `totalOutflow`, `net` |
| `CollectionStatementReport` | `collectionId`, `collectionName`, `category`, `currency`, `status`, `totalAmount`, `expenses` (List\<Map\>), `memberBalances` (List\<Map\>) |
| `PersonalPositionReport` | `totalOwed`, `totalOwing`, `totalReceivable`, `totalPayable`, `netPosition`, `positionByCollection` (Map), `positionsByCollection` (List\<Map\>) ⚠️, `byCollection` (List\<Map\>) ⚠️, `netDebts` (List\<Map\>) |

> ⚠️ `PersonalPositionReport` has overlapping/redundant fields: `positionByCollection` (Map), `positionsByCollection` (List), `byCollection` (List) — likely evolution artefacts. Also `totalOwed`/`totalOwing` vs `totalReceivable`/`totalPayable` express the same concepts with different names.

### 8.2 API Endpoints

| Method | Path | Response |
|--------|------|----------|
| GET | `/api/reports/dashboard` | `ApiResponse<UserDashboardReport>` |
| GET | `/api/reports/spending` | `ApiResponse<SpendingSummaryReport>` |
| GET | `/api/reports/ledger/{currency}` | `ApiResponse<CurrencyLedgerReport>` |
| GET | `/api/reports/collections/{collectionId}` | `ApiResponse<CollectionStatementReport>` |
| GET | `/api/reports/position` | `ApiResponse<PersonalPositionReport>` |

### 8.3 Frontend API (`src/api/reporting.js`)

| Function | Method + Path |
|----------|--------------|
| `getDashboard()` | GET `/api/reports/dashboard` |
| `getSpending()` | GET `/api/reports/spending` |
| `getLedger(cur)` | GET `/api/reports/ledger/{cur}` |
| `getPosition()` | GET `/api/reports/position` |
| `getCollectionStatement(id)` | GET `/api/reports/collections/{id}` |

---

## 9. Cross-Layer Data Flow Summary

### Authentication Flow
```
Frontend register({ email, password, firstName, lastName, userNickname, phone, walletCurrencies })
  → POST /api/auth/register (RegisterRequest)
  → AuthService.register()
  → User entity (USER_T) + UserCredential entity (USER_CREDENTIAL_T)
  → wallet-service /api/wallets/internal/create (via inter-service)
  → returns AuthResponse { accessToken, refreshToken, userId, email, firstName, lastName, userNickname, invitationCode }
```

### Wallet Topup Flow
```
Frontend topUp({ currency, amount })
  → POST /api/wallets/topup (TopUpRequest)
  → WalletService.topUp()
  → WALLET_T.wllt_balance += amount
  → returns WalletResponse { walletId, accountId, currency, balance, status, createdAt, updatedAt }
```

### Expense Settlement Flow
```
Frontend settle({ shareId, collectionId, expenseId, payeeCurrency })
  → POST /api/transactions/settle (SettleRequest)
  → TransactionService.settle()
  → calls collection-service to get ExpenseShare (collection DB)
  → calls wallet-service /internal/debit (payer) + /internal/credit (payee)
  → creates TRANSACTION_T + SETTLEMENT_T records
  → calls collection-service to mark ExpenseShare as settled (es_settled = true)
  → returns SettlementResponse { transactionId, settlementId, payerId, payeeId, amount, currency, convertedAmount, payeeCurrency, status, createdAt }
```

### Collection Invitation Flow
```
Frontend sendInvitation(colId, { inviteeUserId, identifier, role })
  → POST /api/collections/{colId}/invitations (InviteRequest)
  → InvitationService.invite()
  → resolves invitee via auth-service /internal/users/resolve if identifier provided
  → creates INVITATION_T record (status=PENDING)
  → Frontend respond(id, 'ACCEPT') → POST /api/invitations/{id}/respond
  → creates COLLECTION_MEMBER_T record
```

---

## 10. Identified Mismatches & Issues

| # | Severity | Service | Issue | Location |
|---|----------|---------|-------|----------|
| 1 | Low | Auth | `AuthResponse.name` field has no source in `User` entity and is never set by `UserMapper` — always `null` in responses | `AuthResponse.java`, `UserMapper.java` |
| 2 | Low | Wallet | `AccountResponse.accounts` is a duplicate of `wallets` (backward-compat alias) — frontend may use either field inconsistently | `AccountResponse.java`, `WalletMapper.java` |
| 3 | Low | Collection | `ExpenseResponse` missing `updatedAt` — entity has `expenseUpdated` but mapper does not include it | `ExpenseMapper.java`, `ExpenseResponse.java` |
| 4 | Low | Collection | `expenseApi.settleShare(colId, expId, shareId, body)` sends a request body but backend `settleShare` endpoint accepts no `@RequestBody` — body is silently ignored | `expense.js`, `ExpenseController.java` |
| 5 | Medium | Collection | `ParticipantShare` class referenced in `CreateExpenseRequest` but not found in scanned sources — may be an inner class or missing from dto package | `CreateExpenseRequest.java` |
| 6 | Medium | Transaction | `TransactionResponse` (used for history endpoints) is missing `convertedAmount` and `payeeCurrency` that exist in `Transaction` entity — FX conversion details unavailable in history | `TransactionResponse.java`, `Transaction.java` |
| 7 | Low | Reporting | `PersonalPositionReport` has three overlapping collection-position fields: `positionByCollection` (Map), `positionsByCollection` (List\<Map\>), `byCollection` (List\<Map\>) — naming and type inconsistency | `PersonalPositionReport.java` |
| 8 | Low | Reporting | `PersonalPositionReport` has dual naming: `totalOwed`/`totalOwing` and `totalReceivable`/`totalPayable` — same concepts expressed twice | `PersonalPositionReport.java` |
| 9 | Info | Collection | `MemberResponse.userNickname` and `invitationCode` are populated via inter-service call to auth-service — not available if auth-service is down | `CollectionMapper.java`, `CollectionService` impl |
| 10 | Info | Currency | Frontend has hardcoded fallback rates `{ USD:1, MYR:4.47, SGD:1.35 }` — used when currency-service is unreachable | `currency.js` |
| 11 | Info | Auth | `walletCurrencies` in `RegisterRequest` is passed through to wallet-service to create initial wallets during registration — not stored in `USER_T` | `RegisterRequest.java`, `AuthService` impl |
