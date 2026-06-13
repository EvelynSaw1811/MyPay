# Edge, Boundary & Scenario Test Plan — MyPay

> Derived from full source code inspection (entities, DTOs, repositories, controllers, frontend API).  
> Last updated: 2026-05-19  
>
> **Legend**
> - ✅ Happy path  
> - ❌ Error / rejection expected  
> - ⚠️ Edge case — may pass or silently misbehave  
> - 🔁 Idempotency / concurrency concern  
> - 🌐 Cross-service dependency  

---

## Table of Contents

1. [Auth Service](#1-auth-service)
2. [Wallet Service](#2-wallet-service)
3. [Collection Service](#3-collection-service)
4. [Expense & Share Service](#4-expense--share-service)
5. [Invitation Service](#5-invitation-service)
6. [Collection Type Service](#6-collection-type-service)
7. [Transaction Service](#7-transaction-service)
8. [Currency Service](#8-currency-service)
9. [Notification Service](#9-notification-service)
10. [Reporting Service](#10-reporting-service)
11. [Cross-Service Integration Flows](#11-cross-service-integration-flows)
12. [Security & Authorization Scenarios](#12-security--authorization-scenarios)
13. [Concurrency & Race Conditions](#13-concurrency--race-conditions)
14. [Frontend ↔ Backend Contract Scenarios](#14-frontend--backend-contract-scenarios)
15. [Global Boundary Values Reference](#15-global-boundary-values-reference)

---

## 1. Auth Service

### 1.1 POST `/api/auth/register`

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| A-R-01 | Standard registration | Valid email, ≥8-char password, firstName, lastName, nickname, phone, walletCurrencies=["MYR"] | 201 AuthResponse with accessToken, refreshToken, userId, invitationCode; wallet account created | ✅ |
| A-R-02 | Registration without phone | phone omitted | 201; `USER_T.user_phone` = NULL | ✅ |
| A-R-03 | Registration without walletCurrencies | walletCurrencies omitted or null | 201; account created with no wallets OR default wallet per service impl | ⚠️ |
| A-R-04 | Registration with multiple walletCurrencies | walletCurrencies=["MYR","USD","SGD"] | 201; 3 wallets created in wallet-service | ✅ |
| A-R-05 | Duplicate email | email already in USER_T | 409 Conflict | ❌ |
| A-R-06 | Email case sensitivity | "User@Example.com" when "user@example.com" exists | Should 409 (email is stored as-is; uniqueness is DB-level case-insensitive in MySQL utf8mb4) | ⚠️ |
| A-R-07 | Invalid email format | "notanemail", "a@", "@b.com" | 400 Validation error | ❌ |
| A-R-08 | Password exactly 8 chars | password="12345678" | 201 (boundary — min=8 allowed) | ✅ |
| A-R-09 | Password 7 chars | password="1234567" | 400 `min=8` violation | ❌ |
| A-R-10 | Password empty string | password="" | 400 NotBlank violation | ❌ |
| A-R-11 | Empty firstName | firstName="" | 400 NotBlank violation | ❌ |
| A-R-12 | firstName = 100 chars | 100-char string | 201 (column VARCHAR(100) — boundary max) | ✅ |
| A-R-13 | firstName = 101 chars | 101-char string | DB truncation or exception (no `@Size(max=100)` on RegisterRequest.firstName) | ⚠️ |
| A-R-14 | Phone exceeds 20 chars | phone="123456789012345678901" (21 chars) | No @Size on RegisterRequest.phone — DB truncation or persistence error | ⚠️ |
| A-R-15 | Invalid phone format | phone="abc-xyz" | Accepted (no phone format validation on RegisterRequest, only on UpdateUserRequest) | ⚠️ |
| A-R-16 | walletCurrencies contains invalid code | walletCurrencies=["XYZ"] | Depends on wallet-service validation; auth returns 201 but wallet may not be created | ⚠️ |
| A-R-17 | Duplicate nickname | nickname already in USER_T | UserRepository.findByUserNickname exists but there is no UNIQUE constraint on `user_nickname` column; duplicates possible | ⚠️ |
| A-R-18 | Wallet-service unavailable during register | Network failure to wallet-service | Auth user is created but no wallet account — orphaned user state | 🌐 |
| A-R-19 | Unicode in name fields | firstName="王小明", lastName="Müller" | Should persist correctly (MySQL utf8mb4) | ✅ |
| A-R-20 | All fields empty/null | {} | 400 multiple validation failures | ❌ |

---

### 1.2 POST `/api/auth/login`

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| A-L-01 | Valid credentials | Correct email + password | 200 AuthResponse with accessToken (15 min), refreshToken (7 days) | ✅ |
| A-L-02 | Wrong password | Correct email, wrong password | 401 Unauthorized | ❌ |
| A-L-03 | Email not registered | Unknown email | 401/404 Unauthorized or Not Found | ❌ |
| A-L-04 | Email case mismatch | Login with "USER@example.com" when registered as "user@example.com" | UserRepository.findByUserEmail does exact match; may 401 | ⚠️ |
| A-L-05 | INACTIVE user | user_status = "INACTIVE" | Depends on service impl — should 401 Forbidden | ⚠️ |
| A-L-06 | Password is empty string | password="" | 400 NotBlank | ❌ |
| A-L-07 | Login with phone instead of email | identifier = phone number | 400 (LoginRequest only accepts email field) | ❌ |
| A-L-08 | Concurrent logins | Two simultaneous logins with same user | Both should succeed; multiple valid refresh tokens possible | ⚠️ |
| A-L-09 | Login after account deletion | Deleted user tries to login | 401/404 | ❌ |

---

### 1.3 POST `/api/auth/refresh`

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| A-RF-01 | Valid refresh token | Live refreshToken | 200 new accessToken + refreshToken | ✅ |
| A-RF-02 | Expired refresh token | refreshToken older than 7 days | 401 | ❌ |
| A-RF-03 | Revoked refresh token | refreshToken already in REVOKED_TOKEN_T | 401 | ❌ |
| A-RF-04 | Tampered JWT | Signature changed | 401 | ❌ |
| A-RF-05 | Empty refresh token string | refreshToken="" | 400 NotBlank | ❌ |
| A-RF-06 | Using access token as refresh token | Send accessToken in refreshToken field | 401 (wrong token type/claims) | ❌ |
| A-RF-07 | Refresh token for deleted user | User deleted after token issued | Should 401/404 | ⚠️ |
| A-RF-08 | Token close to expiry (< 1 min remaining) | Near-expiry refreshToken | 200 new tokens issued | ✅ |

---

### 1.4 POST `/api/auth/logout`

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| A-LO-01 | Valid logout | Live refreshToken | 200; token hash added to REVOKED_TOKEN_T | ✅ |
| A-LO-02 | Already logged out | Same refreshToken sent twice | Second call: token hash already in REVOKED_TOKEN_T — should return 200 (idempotent) or 400 | 🔁 |
| A-LO-03 | Expired token logout | Expired but otherwise valid token | Should still revoke (or simply 200 since it's already invalid) | ⚠️ |
| A-LO-04 | Empty refresh token | refreshToken="" | 400 NotBlank | ❌ |

---

### 1.5 GET `/api/auth/users/{userId}` / Profile Operations

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| A-U-01 | Get own profile | userId = authenticated user | 200 UserResponse | ✅ |
| A-U-02 | Get another user's profile | userId ≠ X-User-Id | Controller calls service without ownership check — depends on service impl; may return 200 or 403 | ⚠️ |
| A-U-03 | User not found | Non-existent UUID | 404 | ❌ |
| A-U-04 | Malformed UUID | userId = "not-a-uuid" | 404 or 400 depending on JPA | ❌ |
| A-U-05 | Update email to existing email | UpdateUserRequest.email = another user's email | 409 Conflict | ❌ |
| A-U-06 | Update only one field | { firstName: "NewName" }, others null | Only firstName updated; other fields unchanged | ✅ |
| A-U-07 | Update email to same value | Same email as current | No conflict; should succeed | ✅ |
| A-U-08 | Clear phone with empty string | phone="" | `applyUpdate` sets phone to null | ✅ |
| A-U-09 | Update another user's profile | PUT /api/auth/users/{otherId} with own token | 403 (authUserId check in service) | ❌ |
| A-U-10 | Change to same password | currentPassword=X, newPassword=X | Should reject (same password) or pass — depends on service impl | ⚠️ |
| A-U-11 | Change password, wrong current | currentPassword wrong | 401/400 | ❌ |
| A-U-12 | New password < 8 chars | newPassword="abc123" | 400 @Size(min=8) | ❌ |
| A-U-13 | Delete account, wrong password | password mismatch | 401/400 | ❌ |
| A-U-14 | Delete account, correct password | Correct password | 200; user + credentials archived to legacy tables | ✅ |
| A-U-15 | Resolve user by email | GET /internal/users/resolve?identifier=email | 200 UserResponse | ✅ |
| A-U-16 | Resolve user by phone | identifier=phone number | 200 UserResponse | ✅ |
| A-U-17 | Resolve user by nickname | identifier=nickname | 200 UserResponse | ✅ |
| A-U-18 | Resolve user not found | Unknown identifier | 404 | ❌ |

---

## 2. Wallet Service

### 2.1 Account & Wallet Creation

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| W-C-01 | Create account (internal) | Valid userId | 201 AccountResponse; ACCOUNT_T row created | ✅ |
| W-C-02 | Create account when already exists | userId already in ACCOUNT_T | Should return existing or 409 | ⚠️ |
| W-C-03 | Open new wallet for existing account | currency="USD" | 201 WalletResponse; WALLET_T row created | ✅ |
| W-C-04 | Open duplicate wallet (same currency) | currency already exists for account | 409 Conflict (UNIQUE constraint `wllt_acct_id + wllt_currency`) | ❌ |
| W-C-05 | Open wallet with 3-char currency code | currency="MYR" | 201 | ✅ |
| W-C-06 | Open wallet with 4-char code | currency="USDT" | Stored as 4-char (CHAR(3) will truncate to 3 in MySQL) or service rejects | ⚠️ |
| W-C-07 | Open wallet with lowercase code | currency="myr" | Depends on service — no normalization forced in DTO | ⚠️ |
| W-C-08 | Open wallet with unsupported currency | currency="XYZ" | May succeed (no currency validation in wallet-service against currency-service) | ⚠️ |
| W-C-09 | Close wallet with zero balance | DELETE /api/accounts/wallets/MYR | 200 WalletResponse | ✅ |
| W-C-10 | Close wallet with positive balance | balance > 0 | Depends on service impl — should reject or allow | ⚠️ |
| W-C-11 | Close wallet that doesn't exist | currency not in user's wallets | 404 | ❌ |
| W-C-12 | Get wallet for user with no account | New user, no wallet created yet | 404 | ❌ |

---

### 2.2 Top-Up

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| W-T-01 | Standard top-up | currency="MYR", amount=100.00 | 200; wllt_balance += 100.0000 | ✅ |
| W-T-02 | Minimum amount | amount=0.01 | 200 (boundary — @DecimalMin("0.01") passes) | ✅ |
| W-T-03 | Below minimum | amount=0.009 | 400 @DecimalMin violation | ❌ |
| W-T-04 | Zero amount | amount=0.00 | 400 @DecimalMin violation | ❌ |
| W-T-05 | Negative amount | amount=-1.00 | 400 @DecimalMin violation | ❌ |
| W-T-06 | Maximum safe decimal | amount=999999999999999.9999 (DECIMAL(19,4) max ≈ 10^15) | Should persist if within DB bounds | ✅ |
| W-T-07 | More than 4 decimal places | amount=100.00005 | BigDecimal precision: stored as 100.0001 (ROUND_HALF_UP) or truncated — depends on DB rounding mode | ⚠️ |
| W-T-08 | Top-up wallet that doesn't exist | currency="EUR", wallet not opened | 404 | ❌ |
| W-T-09 | Concurrent top-ups to same wallet | Two simultaneous +100 requests | Balance should be +200; race condition if not locked | 🔁 |
| W-T-10 | Top-up with null amount | amount=null | 400 @NotNull | ❌ |

---

### 2.3 Debit / Credit (Internal)

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| W-D-01 | Standard debit | userId, currency, amount ≤ balance | 200; balance reduced | ✅ |
| W-D-02 | Debit exact balance | amount = current balance | 200; balance = 0.0000 | ✅ |
| W-D-03 | Debit more than balance | amount > current balance | 400/422 Insufficient funds | ❌ |
| W-D-04 | Debit minimum amount | amount=0.0001 (@DecimalMin) | 200 (boundary) | ✅ |
| W-D-05 | Debit below minimum | amount=0.00009 | 400 @DecimalMin violation | ❌ |
| W-D-06 | Debit wallet not found | Unknown userId or currency | 404 | ❌ |
| W-D-07 | Standard credit | Valid userId, currency | 200; balance increased | ✅ |
| W-D-08 | Credit wallet not found | Unknown userId or currency | 404 | ❌ |
| W-D-09 | Concurrent debit of same wallet | Two simultaneous debits totalling > balance | One should fail with insufficient funds | 🔁 |

---

### 2.4 Payees

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| W-P-01 | Add payee by email identifier | identifier = valid email | 201 PayeeResponse | ✅ |
| W-P-02 | Add payee by phone identifier | identifier = phone number | 201 (resolved via auth /internal/users/resolve) | ✅ |
| W-P-03 | Add payee by nickname | identifier = nickname | 201 | ✅ |
| W-P-04 | Add payee with direct userId | payeeUserId = valid UUID | 201 | ✅ |
| W-P-05 | Add payee — identifier not found | Unknown email/phone/nickname | 404 from auth-service | ❌ |
| W-P-06 | Add self as payee | identifier = own email | Should reject (self-payee makes no sense) — depends on service impl | ⚠️ |
| W-P-07 | Duplicate payee | Same user already in PAYEE_T for this account | 409 (UNIQUE constraint `paye_acct_id + paye_user_id`) | ❌ |
| W-P-08 | Add payee with no identifier and no userId | Both null | Should reject — validation unclear (no @NotNull on PayeeRequest fields) | ⚠️ |
| W-P-09 | Add payee with nickname only (no identifier) | nickname="Friend", no identifier | Depends on service — may try to resolve null identifier | ⚠️ |
| W-P-10 | Remove existing payee | Valid payeeId belonging to user | 200 | ✅ |
| W-P-11 | Remove payee not owned by user | payeeId belongs to another account | 403 or 404 | ❌ |
| W-P-12 | Remove non-existent payeeId | Unknown UUID | 404 | ❌ |
| W-P-13 | List payees — empty | No payees added | 200 empty list [] | ✅ |
| W-P-14 | Auth-service unavailable when adding payee | Network failure | 502/503 from wallet-service | 🌐 |

---

## 3. Collection Service

### 3.1 Create Collection

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| C-C-01 | Standard creation | name, category=TRIP, currency=MYR | 201 CollectionResponse; creator added as ADMIN member | ✅ |
| C-C-02 | Missing name | name="" or null | 400 @NotBlank | ❌ |
| C-C-03 | Missing category | category=null | 400 @NotNull | ❌ |
| C-C-04 | Invalid category value | category="VACATION" | 400 (not a valid CollectionCategory enum) | ❌ |
| C-C-05 | Missing currency | currency=null or "" | 400 @NotBlank | ❌ |
| C-C-06 | Optional description omitted | description=null | 201; `coll_desc` = NULL | ✅ |
| C-C-07 | typeName provided | typeName="Roommates" | 201; stored in `coll_type_name` | ✅ |
| C-C-08 | Name = 255 chars | 255-char string | 201 (boundary — VARCHAR(255)) | ✅ |
| C-C-09 | Name = 256 chars | 256-char string | DB truncation or persistence exception | ⚠️ |
| C-C-10 | Description = 500 chars | 500-char string | 201 (boundary — VARCHAR(500)) | ✅ |
| C-C-11 | Description = 501 chars | 501-char string | DB truncation or persistence exception | ⚠️ |
| C-C-12 | Same name as existing collection | Duplicate name for same user | 201 (no uniqueness constraint on name) | ✅ |

---

### 3.2 Get / Update / Close Collection

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| C-G-01 | List my collections | User is ADMIN/MEMBER of collections | 200 list with `myRole` and `myNetBalance` populated | ✅ |
| C-G-02 | List when no collections | New user with zero collections | 200 empty list [] | ✅ |
| C-G-03 | Get collection as member | User is MEMBER | 200 with myRole=MEMBER | ✅ |
| C-G-04 | Get collection not a member | User has no CollectionMember row | 403 Forbidden (@RequireCollectionRole check) | ❌ |
| C-G-05 | Get non-existent collection | Unknown collectionId | 404 | ❌ |
| C-G-06 | Update collection as ADMIN | Valid payload | 200 CollectionResponse updated | ✅ |
| C-G-07 | Update collection as MEMBER | User is MEMBER, not ADMIN | 403 (@RequireCollectionRole(ADMIN)) | ❌ |
| C-G-08 | Update collection as EDITOR | User is EDITOR | 403 (@RequireCollectionRole(ADMIN) — only ADMIN can update) | ❌ |
| C-G-09 | Close collection | Owner closes ACTIVE collection | 200; `coll_status` = CLOSED | ✅ |
| C-G-10 | Close already CLOSED collection | Repeat close | Should 400 or idempotent 200 | ⚠️ |
| C-G-11 | Close collection with unsettled expenses | Has EXPENSE_SHARE_T rows with es_settled=false | Depends on service impl — may allow or reject | ⚠️ |
| C-G-12 | Close as non-owner member | EDITOR tries to close | Depends on service impl (close endpoint has no @RequireCollectionRole) | ⚠️ |
| C-G-13 | Get collection after closure | collection closed | 200 with status=CLOSED | ✅ |

---

### 3.3 Member Management

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| C-M-01 | Get members of collection | User is member | 200 list with enriched userNickname + invitationCode | ✅ |
| C-M-02 | Get members — auth-service down | Cannot resolve userNickname | userNickname/invitationCode fields null in response | 🌐 |
| C-M-03 | Remove member as ADMIN | targetUserId is MEMBER | 200; COLLECTION_MEMBER_T row deleted | ✅ |
| C-M-04 | Remove member who is ADMIN | Target is another ADMIN | Depends on service impl — should reject or allow | ⚠️ |
| C-M-05 | Remove collection owner | targetUserId = owner | Should reject — owner cannot be removed | ⚠️ |
| C-M-06 | Remove self from collection | targetUserId = own userId | Depends on impl (leave collection) | ⚠️ |
| C-M-07 | Remove non-member | User not in collection | 404 | ❌ |
| C-M-08 | Remove member as MEMBER (not ADMIN) | @RequireCollectionRole(ADMIN) on endpoint | 403 | ❌ |
| C-M-09 | Remove last member | Only 1 member remains | Depends on impl — may leave collection empty | ⚠️ |
| C-M-10 | Get balances — no expenses | Empty collection | 200; all netBalance = 0.0000 | ✅ |
| C-M-11 | Get balances — all settled | All es_settled=true | 200; all netBalance = 0.0000 | ✅ |
| C-M-12 | Get balances — mixed settled/unsettled | Some shares settled | 200; partial balances | ✅ |

---

## 4. Expense & Share Service

### 4.1 Create Expense — Split Type Scenarios

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| E-C-01 | EQUAL split — 2 members | amount=100, splitType=EQUAL, 2 participants | 201; each ExpenseShare.es_total_amt=50.0000 | ✅ |
| E-C-02 | EQUAL split — amount not divisible | amount=100, 3 participants | 201; shares ≈ 33.3333 each; rounding depends on impl (total may be 99.9999 or 100.0001) | ⚠️ |
| E-C-03 | EQUAL split — 1 participant | 1 participant | 201; share = full amount | ✅ |
| E-C-04 | PERCENTAGE split — sums to 100% | 50%+50% for 2 participants | 201 | ✅ |
| E-C-05 | PERCENTAGE split — sums to 99% | Missing 1% | Should reject (sum ≠ 100%) or allow with rounding | ⚠️ |
| E-C-06 | PERCENTAGE split — sums to 101% | Over 100% | Should reject | ⚠️ |
| E-C-07 | PERCENTAGE split — one participant at 100% | 1 participant, 100% | 201 | ✅ |
| E-C-08 | EXACT split — amounts sum to expense amount | 60+40=100 for amount=100 | 201 | ✅ |
| E-C-09 | EXACT split — amounts don't sum to total | 60+30=90 for amount=100 | Should reject (mismatch) | ⚠️ |
| E-C-10 | HYBRID split | Mixed percentage and fixed amounts | 201 if impl handles correctly | ✅ |
| E-C-11 | HIERARCHICAL split | Weighted distribution | 201 if impl handles weights | ✅ |
| E-C-12 | paidBy not a member | paidBy = non-member userId | Should reject | ⚠️ |
| E-C-13 | Participant not a member | participants includes non-member userId | Should reject | ⚠️ |
| E-C-14 | Empty participants list | participants=[] | 400 @NotEmpty | ❌ |
| E-C-15 | Amount = 0.01 (min boundary) | amount=0.01 | 201 | ✅ |
| E-C-16 | Amount = 0.009 | Below minimum | 400 @DecimalMin("0.01") | ❌ |
| E-C-17 | Amount = 0 | amount=0.00 | 400 @DecimalMin violation | ❌ |
| E-C-18 | Amount negative | amount=-50 | 400 | ❌ |
| E-C-19 | Title exactly 30 chars | 30-char string | 201 (boundary) | ✅ |
| E-C-20 | Title 31 chars | 31-char string | 400 @Size(max=30) | ❌ |
| E-C-21 | Title empty | title="" | 400 @NotBlank | ❌ |
| E-C-22 | Description exactly 100 chars | 100-char string | 201 (boundary) | ✅ |
| E-C-23 | Description 101 chars | 101-char string | 400 @Size(max=100) | ❌ |
| E-C-24 | With tax rate and tax type | taxRate=0.06, taxType="SST" | 201; es_tax_amt computed per share | ✅ |
| E-C-25 | taxRate = 0.0000 (zero) | taxRate=0.0000 | 201; es_tax_amt = 0 | ✅ |
| E-C-26 | taxRate = 0.9999 (boundary for DECIMAL(5,4)) | taxRate=0.9999 | 201 (99.99% tax — unusual but valid by type) | ✅ |
| E-C-27 | taxRate = 1.0000 (overflow DECIMAL(5,4)) | taxRate=1.0000 | DECIMAL(5,4) max value is 9.9999; 1.0000 is valid | ✅ |
| E-C-28 | Expense in different currency from collection | expenseCurrency="USD" while collection.currency="MYR" | No currency match validation visible — may store | ⚠️ |
| E-C-29 | Create expense in CLOSED collection | collectionStatus=CLOSED | Should reject — depends on impl | ⚠️ |
| E-C-30 | paidBy = one of the participants | Payer is also sharing | 201 (payer's share reduces their net) | ✅ |
| E-C-31 | paidBy not in participants | Payer paid but is not listed as participant | 201; payer has 0 share, others owe full amount | ✅ |

---

### 4.2 Update / Delete Expense

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| E-U-01 | Update expense details | Valid new title, amount, participants | 200; existing shares replaced | ✅ |
| E-U-02 | Update expense with some settled shares | Some es_settled=true | Should reject update (settled shares can't be changed) or allow with caveat | ⚠️ |
| E-U-03 | Update expense as MEMBER | Not ADMIN/EDITOR | 403 | ❌ |
| E-U-04 | Delete expense with no settled shares | All shares unsettled | 200; EXPENSE_T + shares deleted | ✅ |
| E-U-05 | Delete expense with settled shares | Some shares settled | Should reject (data integrity) or cascade | ⚠️ |
| E-U-06 | Delete expense as MEMBER | Not ADMIN/EDITOR | 403 | ❌ |
| E-U-07 | Delete non-existent expense | Unknown expenseId | 404 | ❌ |

---

### 4.3 Share Settlement

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| E-S-01 | Settle share | shareId, collectionId, expenseId | 200; es_settled=true, es_settled_at=now | ✅ |
| E-S-02 | Settle already-settled share | es_settled already true | Should return 200 (idempotent) or 409 | 🔁 |
| E-S-03 | Unsettle a settled share | PUT .../unsettle | 200; es_settled=false, es_settled_at=NULL | ✅ |
| E-S-04 | Unsettle a non-settled share | es_settled = false | Should return 200 (idempotent) or 400 | ⚠️ |
| E-S-05 | Get share — member access | Valid shareId | 200 ShareResponse | ✅ |
| E-S-06 | Get share not belonging to expense | shareId from different expense | 404 (findByExpenseShareIdAndExpense_ExpenseId checks both) | ❌ |
| E-S-07 | Settle share as non-member | Not in COLLECTION_MEMBER_T | 403 | ❌ |
| E-S-08 | Settle share — self-settling own share | es_user_id = own userId | 200 (no restriction) | ✅ |
| E-S-09 | Settle share of payer (zero-amount share) | Payer's share where base=0 | 200; es_settled=true on 0-amount record | ✅ |

---

## 5. Invitation Service

### 5.1 Send Invitation

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| I-S-01 | Invite by userId | inviteeUserId = valid UUID | 201 InvitationResponse; INVITATION_T created | ✅ |
| I-S-02 | Invite by email identifier | identifier = email | 201; invitee resolved via auth /internal/users/resolve | ✅ |
| I-S-03 | Invite by phone identifier | identifier = phone | 201 | ✅ |
| I-S-04 | Invite by nickname/invitationCode | identifier = nickname | 201 | ✅ |
| I-S-05 | Invite unknown identifier | identifier = nonexistent email | 404 from auth-service | ❌ |
| I-S-06 | Invite existing member | invitee already in COLLECTION_MEMBER_T | Should 409 (already a member) | ❌ |
| I-S-07 | Invite user with pending invitation | Same invitee + same collection already PENDING | Should 409 (InvitationRepository.existsByCollectionAndInviteeAndStatus check) | ❌ |
| I-S-08 | Re-invite after DECLINED | Previous invitation DECLINED, new invite sent | Should allow new invitation | ✅ |
| I-S-09 | Invite yourself | inviteeUserId = own userId | Should reject (self-invitation) — depends on impl | ⚠️ |
| I-S-10 | Invite as MEMBER (not ADMIN/EDITOR) | @RequireCollectionRole(ADMIN, EDITOR) | 403 | ❌ |
| I-S-11 | Both inviteeUserId and identifier provided | Both fields non-null | Depends on impl — which takes priority? | ⚠️ |
| I-S-12 | Neither inviteeUserId nor identifier | Both null | Should reject — no @NotNull/NotBlank on either field | ⚠️ |
| I-S-13 | Missing role | role = null | 400 @NotNull | ❌ |
| I-S-14 | Invite to CLOSED collection | coll_status = CLOSED | Should reject — depends on impl | ⚠️ |
| I-S-15 | Auth-service down during invite | Cannot resolve identifier | 502/503 | 🌐 |

---

### 5.2 Respond to Invitation

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| I-R-01 | Accept invitation | action="ACCEPT" | 200; COLLECTION_MEMBER_T row created; invitation status→ACCEPTED | ✅ |
| I-R-02 | Decline invitation | action="DECLINE" | 200; invitation status→DECLINED; no member row | ✅ |
| I-R-03 | Invalid action | action="IGNORE" | 400 @Pattern(ACCEPT\|DECLINE) | ❌ |
| I-R-04 | Respond to already-ACCEPTED invitation | action="ACCEPT" on ACCEPTED invite | 409 or idempotent 200 | ⚠️ |
| I-R-05 | Respond to already-DECLINED invitation | action="ACCEPT" after DECLINED | Should reject | ❌ |
| I-R-06 | Respond as wrong user | invitee A's invitation, but user B responds | 403 | ❌ |
| I-R-07 | Invitation not found | Unknown invitationId | 404 | ❌ |
| I-R-08 | Accept when collection is CLOSED | Collection closed between invite and response | Should reject | ⚠️ |
| I-R-09 | Accept when already a member | Already joined via another path | 409 (UNIQUE constraint on member table) | ❌ |
| I-R-10 | Get my invitations — none pending | No PENDING invitations | 200 empty list | ✅ |

---

## 6. Collection Type Service

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| CT-01 | Create custom type | name="Work Expenses" | 201 CollectionTypeResponse | ✅ |
| CT-02 | Create duplicate type for same user | Same name again | 409 (existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase) | ❌ |
| CT-03 | Create duplicate — different case | "work expenses" when "Work Expenses" exists | 409 (IgnoreCase check) | ❌ |
| CT-04 | Create type for different user — same name | Two users with same type name | 201 for both (unique per user) | ✅ |
| CT-05 | Create system type via endpoint | Any user creates via API | 201 with system=false (system flag hardcoded to false in controller) | ✅ |
| CT-06 | Delete own type | Valid collectionTypeId owned by user | 204 No Content | ✅ |
| CT-07 | Delete another user's type | Owned by different user | 403 ForbiddenException | ❌ |
| CT-08 | Delete non-existent type | Unknown collectionTypeId | 404 ResourceNotFoundException | ❌ |
| CT-09 | Delete system type | collectionTypeSystem=true | No guard in controller — may allow deletion | ⚠️ |
| CT-10 | List types | User has 3 custom types | 200 list of 3 | ✅ |
| CT-11 | List types — none | New user, no types created | 200 empty list | ✅ |
| CT-12 | Type name exactly 100 chars | 100-char string | Depends — no @Size on name in entity (VARCHAR(100)) | ⚠️ |
| CT-13 | Type name empty string | name="" | 400 @NotBlank | ❌ |
| CT-14 | NULL ctyp_user_id uniqueness | Two system types same name | MySQL treats NULL≠NULL; duplicate system types possible | ⚠️ |

---

## 7. Transaction Service

### 7.1 POST `/api/transactions/settle` — Single Share Settlement

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| T-S-01 | Standard settlement — same currency | payer+payee both MYR wallets | 200; TRANSACTION_T + SETTLEMENT_T created; wallets debited/credited; share marked settled | ✅ |
| T-S-02 | Settlement — cross-currency | payer MYR, payee USD wallet, payeeCurrency="USD" | 200; txn_converted_amt set; FX via currency-service | ✅ |
| T-S-03 | Settlement — payeeCurrency not specified | payeeCurrency omitted | Service uses payer's currency or collection's currency | ⚠️ |
| T-S-04 | Insufficient balance | payer balance < share amount | 400/422 Insufficient funds; saga compensation runs | ❌ |
| T-S-05 | Share already settled | es_settled = true | Should reject (duplicate settlement) | ❌ |
| T-S-06 | Share not found | Unknown shareId | 404 from collection-service | ❌ |
| T-S-07 | Collection not found | Unknown collectionId | 404 from collection-service | ❌ |
| T-S-08 | Payer = payee (self-settlement) | payerId = payeeId | Depends on impl — no guard visible; debit and credit same wallet | ⚠️ |
| T-S-09 | payeeCurrency same as payer currency | payeeCurrency = payer's currency | 200; txn_converted_amt = txn_amount, no conversion needed | ✅ |
| T-S-10 | Idempotency — duplicate request same key | Same idempotencyKey sent twice | Second call returns same response (existing transaction) | 🔁 |
| T-S-11 | Idempotency — different content, same key | Different shareId but same key | Should return cached result for first request | 🔁 |
| T-S-12 | Settlement amount = 0.0001 (min debit) | es_total_amt = 0.0001 | 200 (boundary — WalletOperationRequest @DecimalMin("0.0001")) | ✅ |
| T-S-13 | Currency-service unavailable | FX conversion needed but currency-service down | 502; saga compensation; wallet not debited | 🌐 |
| T-S-14 | Wallet-service debit fails mid-saga | Debit succeeds, credit fails | Saga should compensate — re-credit payer | 🔁 |
| T-S-15 | Settle share from closed collection | coll_status=CLOSED | Depends on service impl | ⚠️ |
| T-S-16 | Payer has no wallet in the required currency | No wallet row for payer + currency | 404 from wallet-service | ❌ |
| T-S-17 | Payee has no wallet in payeeCurrency | Payee wallet doesn't exist | 404 from wallet-service; saga compensation | ❌ |

---

### 7.2 POST `/api/transactions/settle-net` — Netting

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| T-N-01 | Standard netting — A owes B net | counterpartyId=B, collectionIds=[c1,c2] | 200; net calculated; single transfer transaction created | ✅ |
| T-N-02 | Netting — balanced (net=0) | A and B owe each other equal amounts | 200; net=0; no actual transfer? Or zero-amount transaction | ⚠️ |
| T-N-03 | Netting — B owes A net | After netting, counterparty is actually the payer | 200; roles reversed | ✅ |
| T-N-04 | Netting — no unsettled shares | No shares between user and counterparty | 200 or 404 (no debts to net) | ⚠️ |
| T-N-05 | Netting across single collection | collectionIds=[c1] only | 200 | ✅ |
| T-N-06 | Empty collectionIds | collectionIds=[] | 400 @NotEmpty | ❌ |
| T-N-07 | Unknown counterpartyId | Not a valid user | 404 | ❌ |
| T-N-08 | Netting with insufficient balance | Net payer has insufficient balance | 400/422; compensation | ❌ |
| T-N-09 | Idempotency — same netting request twice | Same idempotencyKey | Second call returns cached result | 🔁 |
| T-N-10 | Netting with cross-currency collections | Collections in MYR and USD | Netting in specified currency; conversion applied | ⚠️ |

---

### 7.3 GET `/api/transactions/history`

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| T-H-01 | Get history — has transactions | User has sent/received transactions | 200 list ordered by createdAt DESC | ✅ |
| T-H-02 | Get history — no transactions | New user | 200 empty list | ✅ |
| T-H-03 | Get history by currency | currency="MYR" | 200 filtered list (JPQL query) | ✅ |
| T-H-04 | Get history — unknown currency | currency="XYZ" | 200 empty list (no matching records) | ✅ |
| T-H-05 | TransactionResponse missing FX fields | History shows txn with cross-currency | convertedAmount + payeeCurrency NOT in TransactionResponse — data loss | ⚠️ |
| T-H-06 | Large transaction history | User with 10,000+ transactions | No pagination — may cause memory/timeout issues | ⚠️ |

---

## 8. Currency Service

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| CY-01 | List active currencies | — | 200 list of currencies where curr_active=true | ✅ |
| CY-02 | Get all rates, base=MYR | base=MYR | 200 ExchangeRateTableResponse | ✅ |
| CY-03 | Get rates, base=USD | base=USD | 200; rates computed relative to USD | ✅ |
| CY-04 | Get rates — unsupported base | base=XYZ | 404 or empty rates map | ⚠️ |
| CY-05 | Get rates — no data for base | Rates not yet fetched | 404 or empty map | ⚠️ |
| CY-06 | Get rate MYR→USD | from=MYR, to=USD | 200 ExchangeRateResponse | ✅ |
| CY-07 | Get rate — same currency | from=MYR, to=MYR | Rate = 1.0 or 404 (no row in DB for same pair) | ⚠️ |
| CY-08 | Get rate — reverse pair not in DB | from=USD, to=MYR when only MYR→USD stored | 404 or computed as 1/rate | ⚠️ |
| CY-09 | Get rate — unsupported pair | from=JPY, to=BTC | 404 | ❌ |
| CY-10 | Convert amount=0 | amount=0.00 | 200; convertedAmount=0 | ✅ |
| CY-11 | Convert amount negative | amount=-10 | 200 but returns negative converted amount (no validation on amount in service) | ⚠️ |
| CY-12 | Convert same currency | from=MYR, to=MYR | convertedAmount = originalAmount | ✅ |
| CY-13 | Convert very small amount | amount=0.0001 | BigDecimal precision maintained | ✅ |
| CY-14 | Convert very large amount | amount=999999999.9999 | DECIMAL(20,6) rate — precision maintained | ✅ |
| CY-15 | No rates in DB | ExchangeRate table empty | 404 from repository | ❌ |
| CY-16 | Multiple historical rates for same pair | 10 rows of MYR→USD | Latest by `exrt_fetched` is used (findTop...OrderByDesc) | ✅ |
| CY-17 | Frontend fallback rates used | Currency-service down | Frontend uses hardcoded { USD:1, MYR:4.47, SGD:1.35 } | 🌐 |
| CY-18 | Frontend fallback for unsupported pair | from=EUR when not in fallback map | `FALLBACK_USD_RATES[to] / FALLBACK_USD_RATES[from]` → undefined / undefined = NaN | ⚠️ |

---

## 9. Notification Service

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| N-01 | Create notification (internal) | userId, type, title, message | 201 NotificationResponse | ✅ |
| N-02 | Duplicate notification | Same referenceId + type (existsByRefIdAndType = true) | Service may skip creation (deduplicate) | ⚠️ |
| N-03 | Create without message | message=null | 201; notf_message=NULL | ✅ |
| N-04 | Create without referenceId | referenceId=null | 201; notf_ref_id=NULL | ✅ |
| N-05 | Create missing userId | userId="" | 400 @NotBlank | ❌ |
| N-06 | Get all notifications | User has mix of read/unread | 200 ordered by notf_created DESC | ✅ |
| N-07 | Get all — none | No notifications | 200 empty list | ✅ |
| N-08 | Get unread only | Some read, some unread | 200 only unread ones | ✅ |
| N-09 | Unread count | 5 unread | 200 { count: 5 } | ✅ |
| N-10 | Unread count — none | All read | 200 { count: 0 } | ✅ |
| N-11 | Mark as read | Valid notificationId | 200; notf_read=true, notf_read_at=now | ✅ |
| N-12 | Mark already-read notification | notf_read already true | Idempotent 200 or 400 | 🔁 |
| N-13 | Mark read — wrong user | Notification belongs to user B | 403 | ❌ |
| N-14 | Mark read — not found | Unknown notificationId | 404 | ❌ |
| N-15 | Mark all as read | 10 unread notifications | 200; all notf_read=true | ✅ |
| N-16 | Mark all as read — nothing to mark | All already read | 200 (no-op) | ✅ |
| N-17 | Get preferences — first time | No USER_PREFERENCE_T row | 201 row created with defaults (email=true, push=true, sms=false, promo=false) | ✅ |
| N-18 | Get preferences — existing | Row exists | 200 | ✅ |
| N-19 | Update single preference | { pushEnabled: false }, others null | 200; only push_enabled changed (patch semantics) | ✅ |
| N-20 | Update all preferences | All four fields set | 200; all updated | ✅ |
| N-21 | Update — all null | {} empty body | 200 no-op (all fields null → no changes applied) | ✅ |

---

## 10. Reporting Service

| # | Scenario | Input | Expected Outcome | Type |
|---|----------|-------|-----------------|------|
| R-01 | Dashboard — new user | No wallets, no collections, no transactions | 200; zeros across all fields | ✅ |
| R-02 | Dashboard — active user | Wallets with balance, collections, transactions | 200; populated report | ✅ |
| R-03 | Dashboard — wallet-service down | Cannot fetch wallet data | Partial report or 502 | 🌐 |
| R-04 | Spending summary — no transactions | Empty TRANSACTION_T | 200; totalSpent=0, totalReceived=0, netBalance=0 | ✅ |
| R-05 | Spending summary — mixed types | SETTLEMENT + TOP_UP + TRANSFER | 200; byType map populated | ✅ |
| R-06 | Currency ledger — MYR | User has MYR wallet transactions | 200 CurrencyLedgerReport | ✅ |
| R-07 | Currency ledger — no transactions | No txns for currency | 200; zeros | ✅ |
| R-08 | Currency ledger — invalid currency | currency="XYZ" | 200 with zeros or 404 | ⚠️ |
| R-09 | Collection statement — as member | Valid collectionId, user is member | 200 CollectionStatementReport | ✅ |
| R-10 | Collection statement — not member | User not in collection | 403 or 404 | ❌ |
| R-11 | Collection statement — no expenses | Collection with zero expenses | 200; totalAmount=0, expenses=[] | ✅ |
| R-12 | Personal position — all settled | No outstanding shares | 200; all zeros | ✅ |
| R-13 | Personal position — has debts | Unsettled shares | 200; positive totalOwing/totalPayable | ✅ |
| R-14 | Personal position — has receivables | Others owe the user | 200; positive totalOwed/totalReceivable | ✅ |
| R-15 | PersonalPositionReport redundant fields | byCollection vs positionsByCollection | Both present in response — duplicate data; client must know which to use | ⚠️ |
| R-16 | Reporting with multiple downstream failures | All services down | Cascade 502 errors | 🌐 |

---

## 11. Cross-Service Integration Flows

### 11.1 Registration → Wallet Creation

| # | Scenario | Expected Outcome | Type |
|---|----------|-----------------|------|
| X-01 | Full registration flow | User → UserCredential → wallet account → wallets for each currency | ✅ |
| X-02 | Wallet-service down at registration | User created in auth-service; wallet NOT created; orphaned user (no ACCOUNT_T row) | 🌐 |
| X-03 | Register with walletCurrencies=["MYR","USD"] then list wallets | Two wallets returned | ✅ |
| X-04 | Re-call wallet create after previous failure | POST /api/wallets/internal/create for existing userId | Should return existing or create safely | 🔁 |

---

### 11.2 Expense → Settlement → Wallet Debit/Credit

| # | Scenario | Expected Outcome | Type |
|---|----------|-----------------|------|
| X-10 | Full settle flow (same currency) | Share → SettleRequest → collection lookup → wallet debit → wallet credit → share marked settled | ✅ |
| X-11 | Full settle flow (cross-currency) | FX rate fetched → debit in MYR → credit in USD at converted amount | ✅ |
| X-12 | Collection-service down during settlement | Cannot look up share | 502; transaction not created | 🌐 |
| X-13 | Wallet debit succeeds, credit fails | Saga compensates — re-credits payer; transaction marked FAILED | 🔁 |
| X-14 | Collection share-mark-settled fails | Wallets moved; share still shows unsettled | Inconsistent state | 🔁 |
| X-15 | Settle already-settled share (concurrent race) | Two users trigger settlement simultaneously | One should fail (share already settled check) | 🔁 |
| X-16 | User deletes account after settling | Settlement history references deleted user | TransactionResponse.payerId/payeeId returns deleted userId | ⚠️ |

---

### 11.3 Invitation → Member Join → Collection Access

| # | Scenario | Expected Outcome | Type |
|---|----------|-----------------|------|
| X-20 | Full invitation flow | Invite → respond ACCEPT → become MEMBER → can view expenses | ✅ |
| X-21 | Auth-service down during invite | Cannot resolve invitee identifier | 502 | 🌐 |
| X-22 | Collection closed between invite and ACCEPT | Invite is PENDING; collection gets closed | Accept should be rejected (collection closed) | ⚠️ |
| X-23 | User deleted after being invited | Invite still in INVITATION_T | Invitation remains; invitee user no longer exists | ⚠️ |
| X-24 | Invite user who is already an ADMIN | Attempt to invite current ADMIN | Should 409 (already member) | ❌ |

---

### 11.4 Frontend Token Refresh Flow

| # | Scenario | Expected Outcome | Type |
|---|----------|-----------------|------|
| X-30 | Access token expires mid-session | Axios interceptor catches 401; auto-refreshes; retries original request | ✅ |
| X-31 | Multiple concurrent requests on 401 | Only one refresh call issued (queue mechanism in client.js) | ✅ |
| X-32 | Refresh token also expired | Interceptor catches refresh 401; clears localStorage; redirects to /login | ✅ |
| X-33 | Refresh token stolen and revoked server-side | Next use returns 401; user forced to login | ✅ |
| X-34 | No refresh token in localStorage | Interceptor checks `if (!refreshToken) throw`; redirect to /login | ✅ |

---

## 12. Security & Authorization Scenarios

| # | Scenario | Expected Outcome | Type |
|---|----------|-----------------|------|
| S-01 | Access authenticated endpoint without JWT | No Authorization header | 401 Unauthorized (gateway filter) | ❌ |
| S-02 | Access with expired access token | Token past 15-min expiry | 401 | ❌ |
| S-03 | Access with tampered token payload | Base64-decoded, modified, re-encoded | 401 (signature invalid) | ❌ |
| S-04 | Access internal endpoint from frontend | POST /api/wallets/internal/create | Should be blocked at gateway or service level | ⚠️ |
| S-05 | Access another user's wallet | userId in path ≠ X-User-Id from token | Wallet endpoints use RequestContextHolder (JWT user); path userId ignored | ⚠️ |
| S-06 | MEMBER attempts ADMIN action in collection | @RequireCollectionRole(ADMIN) | 403 | ❌ |
| S-07 | EDITOR attempts update collection | @RequireCollectionRole(ADMIN) on PUT | 403 | ❌ |
| S-08 | Token with wrong user's collectionId | User not in collection | 403 (@RequireCollectionRole checks membership) | ❌ |
| S-09 | SQL injection in query params | identifier="'; DROP TABLE USER_T;--" | JPA parameterized queries prevent injection | ✅ |
| S-10 | XSS payload in name fields | name="<script>alert(1)</script>" | Stored as-is; frontend must sanitize output | ⚠️ |
| S-11 | Very long JWT token | Malformed or crafted token > 4KB | Gateway/service should reject | ⚠️ |
| S-12 | Invitation responded by third party | invitationId guessed; wrong user responds | 403 (invitee ownership check) | ❌ |

---

## 13. Concurrency & Race Conditions

| # | Scenario | Risk | Mitigation |
|---|----------|------|-----------|
| CC-01 | Two concurrent settlements of same share | Both read es_settled=false; both proceed; double wallet debit | Share settlement should use optimistic/pessimistic lock or unique idempotency check |
| CC-02 | Concurrent top-ups to same wallet | Both read balance; both write balance+amount; one update lost | Wallet balance update needs SELECT FOR UPDATE or @Lock |
| CC-03 | Concurrent debit — balance at limit | Both read balance > 0; both debit; final balance < 0 | Need atomic check-and-debit (SELECT FOR UPDATE) |
| CC-04 | Duplicate registration with same email | Two concurrent POSTs; both pass `existsByEmail` check; one fails at DB UNIQUE | DB unique constraint ensures only one succeeds; both service-layer checks may pass |
| CC-05 | Concurrent invitation acceptance | Same invitation accepted twice | Second accept: COLLECTION_MEMBER_T UNIQUE constraint rejects; or saga-style dedup |
| CC-06 | Duplicate idempotency key collision | Different transactions sharing same key | One stored; second returns same transaction ID |
| CC-07 | Token revocation race | Logout and refresh sent simultaneously | One wins; the other either succeeds then revokedToken blocks it, or refresh after revoke |
| CC-08 | Collection type creation — duplicate check | Two concurrent creates with same name | Both pass `existsBy...` check; DB UNIQUE constraint rejects one |

---

## 14. Frontend ↔ Backend Contract Scenarios

| # | Scenario | Observation | Risk |
|---|----------|-------------|------|
| FE-01 | `AuthResponse.name` always null | Frontend reads `authResponse.name` but it's never populated by mapper | If UI renders `name`, it shows nothing |
| FE-02 | `AccountResponse.accounts` vs `wallets` | Frontend uses both `.wallets` and (some code may use) `.accounts` | Inconsistency; both are identical — safe but redundant |
| FE-03 | `settleShare(body)` sends body to no-body endpoint | Backend ignores body silently | Harmless but misleading API contract |
| FE-04 | `expenseApi.settleShare` signature: `(colId, expId, shareId, body)` | body param passed but unused on backend | If frontend sends `{ payeeCurrency }` inside body, it is silently dropped — currency NOT applied |
| FE-05 | `unreadCount` response shape | Backend returns `Map { count: N }`; frontend calls `.then(r => r.data.data)` | `data.data` = `{ count: 5 }` — frontend must access `.count` |
| FE-06 | `TransactionResponse` missing `convertedAmount` | History page cannot show FX conversion details | Data loss in history display |
| FE-07 | Frontend fallback rate for non-USD/MYR/SGD | `FALLBACK_USD_RATES[cur]` undefined → division by undefined → NaN | Conversion shows NaN if unusual currency used when currency-service down |
| FE-08 | `auth.js` uses raw axios (no auth header) | register, login, logout sent without JWT | Correct — these are public endpoints |
| FE-09 | `preference.js` calls `client.get` (with auth) for preferences | Preferences are user-scoped | Correct |
| FE-10 | `payee.js addPayee({ identifier, nickname })` | Backend receives `{ identifier, nickname, payeeUserId: undefined }` | Service resolves payeeUserId from identifier — correct |
| FE-11 | `collectionApi.close(id)` returns raw response, not `.data.data` | Close returns full axios response | Frontend code after close cannot chain `.data` on result |
| FE-12 | `respond(id, action)` wraps action: `{ action }` | Backend expects `InvitationActionRequest.action` field | Correct |

---

## 15. Global Boundary Values Reference

### String Lengths

| Field | Location | Min | Max | What happens at max+1 |
|-------|----------|-----|-----|----------------------|
| `user_email` | USER_T | 1 | 255 | DB error or truncation |
| `user_fname` / `user_lname` | USER_T | 1 | 100 | DB error (no @Size on RegisterRequest) |
| `user_nickname` | USER_T | 1 (UpdateUserRequest) | 100 | 400 @Size violation |
| `user_phone` | USER_T | — | 20 | Possible truncation (no @Size on RegisterRequest) |
| `user_invitation_code` | USER_T | — | 32 | DB error |
| `coll_name` | COLLECTION_T | 1 | 255 | DB error or truncation |
| `coll_desc` | COLLECTION_T | 0 | 500 | DB error or truncation |
| `exp_title` | EXPENSE_T | 1 | 30 | 400 @Size(max=30) |
| `exp_desc` | EXPENSE_T | 0 | 100 | 400 @Size(max=100) |
| `notf_message` | NOTIFICATION_T | — | TEXT (unlimited) | — |
| `curr_code` | CURRENCY_T | 3 | 3 | CHAR(3) truncates silently |

---

### Numeric Boundaries

| Field | Type | Min (application) | Min (DB) | Max (DB) | Decimal Places |
|-------|------|------------------|----------|----------|----------------|
| `wllt_balance` | DECIMAL(19,4) | 0 | 0 | 999,999,999,999,999.9999 | 4 |
| `exp_amount` | DECIMAL(19,4) | 0.01 (@DecimalMin) | 0.0001 | 999,999,999,999,999.9999 | 4 |
| `es_total_amt` | DECIMAL(19,4) | — | 0 | 999,999,999,999,999.9999 | 4 |
| `txn_amount` | DECIMAL(19,4) | 0.0001 (@DecimalMin) | 0.0001 | 999,999,999,999,999.9999 | 4 |
| `exrt_rate` | DECIMAL(20,6) | — | 0.000001 | 99,999,999,999,999.999999 | 6 |
| `exp_tax_rate` | DECIMAL(5,4) | 0 | 0 | 9.9999 | 4 |
| `sr_percentage` | DECIMAL(8,4) | 0 | 0 | 9999.9999 | 4 |
| `topup_amount` | Application | 0.01 | 0.01 | Uncapped | 2+ |
| `topup_amount` | DB | — | 0.01 | DECIMAL(19,4) max | 4 |

---

### ID / UUID Format

| Concern | Behaviour |
|---------|-----------|
| Valid UUID v4 | Accepted everywhere |
| Malformed UUID (e.g. "abc-123") | JPA may throw DataIntegrityViolationException or 400; not validated at controller layer |
| Empty string UUID | 400 or 404 depending on mapping |
| UUID from different entity type (e.g. walletId used as userId) | 404 (no record found) |

---

### DateTime Boundaries

| Concern | Behaviour |
|---------|-----------|
| userLastLogin never set | NULL in USER_T — frontend must handle null `lastLogin` |
| expenseShareSettledDateTime before createdAt | Possible if system clock changes; no guard |
| exchangeRateFetchedDateTime far in past | findTop...OrderByDesc will still use latest available |
| JWT expiry: access token = 900,000 ms (15 min) | After 15 min, any request returns 401 |
| JWT expiry: refresh token = 604,800,000 ms (7 days) | After 7 days, refresh returns 401 |

---

### Enum Boundary Cases

| Enum | Edge Case |
|------|-----------|
| `CollectionCategory` | JSON with unknown value → 400 (Jackson deserialization fails) |
| `CollectionRole` | Sending "SUPERADMIN" → 400 |
| `SplitType` | "HYBRID" / "HIERARCHICAL" — may not be fully implemented in service |
| `TransactionType` | "TOP_UP" transactions in history — no top-up via transaction endpoint; created internally |
| `TransactionStatus` | "REVERSED" — no reversal endpoint exists; status only set internally |
| `InvitationStatus` | "PENDING" invitations are never auto-expired; accumulate indefinitely |
