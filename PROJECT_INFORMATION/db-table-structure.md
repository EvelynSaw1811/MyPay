# Database Table Structure — MyPay

> Derived from JPA `@Entity` / `@Column` annotations.  
> All services use `spring.jpa.hibernate.ddl-auto: update` — Hibernate generates/updates table DDL at startup.  
> The `init-schemas.sql` only creates the six empty databases; table DDL is NOT in any SQL file.
>
> Column type mapping (Java → MySQL):
> - `CHAR(n)` → `CHAR(n)`
> - `VARCHAR(n)` → `VARCHAR(n)`
> - `TEXT` → `LONGTEXT`
> - `DECIMAL(p,s)` → `DECIMAL(p,s)`
> - `LocalDateTime` → `DATETIME(6)` (MySQL 8 default)
> - `boolean` / `Boolean` → `BIT(1)`
> - `int` / `Integer` → `INT`

---

## Database Inventory

| Database | Service | Port | Tables |
|----------|---------|------|--------|
| `ewallet_auth_db` | auth-service | 8081 | USER_T, USER_CREDENTIAL_T, REVOKED_TOKEN_T |
| `ewallet_wallet_db` | wallet-service | 8082 | ACCOUNT_T, WALLET_T, PAYEE_T |
| `ewallet_collection_db` | collection-service | 8083 | COLLECTION_T, COLLECTION_MEMBER_T, COLLECTION_TYPE_T, EXPENSE_T, EXPENSE_SHARE_T, INVITATION_T, SPLIT_RULE_T |
| `ewallet_transaction_db` | transaction-service | 8084 | TRANSACTION_T, SETTLEMENT_T, SAGA_STATE_T |
| `ewallet_currency_db` | currency-service | 8085 | CURRENCY_T, EXCHANGE_RATE_T |
| `ewallet_notification_db` | notification-service | 8086 | NOTIFICATION_T, USER_PREFERENCE_T |

> reporting-service (port 8087) has no dedicated database — it aggregates data from other services via HTTP.

---

## 1. ewallet_auth_db

### 1.1 USER_T

```sql
CREATE TABLE USER_T (
    user_id         CHAR(36)        NOT NULL,               -- PK, UUID (GenerationType.UUID)
    user_email      VARCHAR(255)    NOT NULL UNIQUE,
    user_phone      VARCHAR(20),
    user_fname      VARCHAR(100)    NOT NULL,
    user_lname      VARCHAR(100)    NOT NULL,
    user_nickname   VARCHAR(100),
    user_invitation_code VARCHAR(32) UNIQUE,
    user_status     VARCHAR(20)     DEFAULT 'ACTIVE',
    user_last_login DATETIME(6),
    user_created    DATETIME(6),                            -- set by @PrePersist, not updatable
    user_updated    DATETIME(6),                            -- set by @PrePersist/@PreUpdate
    PRIMARY KEY (user_id)
);
```

| Column | Java Field | Nullable | Default |
|--------|-----------|----------|---------|
| `user_id` | `userId` | NO | UUID auto |
| `user_email` | `userEmail` | NO | — |
| `user_phone` | `userPhone` | YES | NULL |
| `user_fname` | `userFirstName` | NO | — |
| `user_lname` | `userLastName` | NO | — |
| `user_nickname` | `userNickname` | YES | NULL |
| `user_invitation_code` | `userInvitationCode` | YES | NULL |
| `user_status` | `userStatus` | YES | `'ACTIVE'` |
| `user_last_login` | `userLastLogin` | YES | NULL |
| `user_created` | `userCreated` | YES | set on insert |
| `user_updated` | `userUpdated` | YES | set on insert/update |

---

### 1.2 USER_CREDENTIAL_T

```sql
CREATE TABLE USER_CREDENTIAL_T (
    ucrd_id         CHAR(36)        NOT NULL,               -- PK, UUID
    ucrd_user_id    CHAR(36)        NOT NULL UNIQUE,        -- FK to USER_T.user_id (logical)
    ucrd_pwd_hash   VARCHAR(255)    NOT NULL,
    ucrd_created    DATETIME(6),                            -- set by @PrePersist, not updatable
    PRIMARY KEY (ucrd_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `ucrd_id` | `userCredentialId` | NO | UUID PK |
| `ucrd_user_id` | `userCredentialUserId` | NO | Logical FK to USER_T.user_id; no JPA FK constraint |
| `ucrd_pwd_hash` | `userCredentialPwdHash` | NO | BCrypt hash |
| `ucrd_created` | `userCredentialCreated` | YES | Set on insert only |

---

### 1.3 REVOKED_TOKEN_T

```sql
CREATE TABLE REVOKED_TOKEN_T (
    rtkn_id         CHAR(36)        NOT NULL,               -- PK, UUID
    rtkn_token_hash VARCHAR(64)     NOT NULL UNIQUE,        -- SHA-256 hash of JWT
    rtkn_user_id    CHAR(36)        NOT NULL,
    rtkn_expires_at DATETIME(6)     NOT NULL,               -- token's original expiry
    rtkn_revoked_at DATETIME(6),                            -- set by @PrePersist
    PRIMARY KEY (rtkn_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `rtkn_id` | `revokedToken` | NO | UUID PK |
| `rtkn_token_hash` | `revokedTokenHash` | NO | Used for fast blacklist lookup |
| `rtkn_user_id` | `revokedTokenUserId` | NO | |
| `rtkn_expires_at` | `revokedTokenExpireDateTime` | NO | Allows cleanup of expired entries |
| `rtkn_revoked_at` | `revokedTokenRevokeDateTime` | YES | Set on insert |

---

## 2. ewallet_wallet_db

### 2.1 ACCOUNT_T

```sql
CREATE TABLE ACCOUNT_T (
    acct_id         CHAR(36)        NOT NULL,               -- PK, UUID
    acct_user_id    CHAR(36)        NOT NULL UNIQUE,        -- logical FK to auth USER_T.user_id
    acct_created    DATETIME(6),
    acct_updated    DATETIME(6),
    PRIMARY KEY (acct_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `acct_id` | `accountId` | NO | |
| `acct_user_id` | `accountUserId` | NO | 1:1 with auth user |
| `acct_created` | `accountCreated` | YES | |
| `acct_updated` | `accountUpdated` | YES | |

---

### 2.2 WALLET_T

```sql
CREATE TABLE WALLET_T (
    wllt_id         CHAR(36)        NOT NULL,               -- PK, UUID
    wllt_acct_id    CHAR(36)        NOT NULL,               -- FK → ACCOUNT_T.acct_id
    wllt_user_id    CHAR(36)        NOT NULL,               -- denormalized user ID
    wllt_currency   CHAR(3)         NOT NULL,
    wllt_balance    DECIMAL(19,4)   NOT NULL DEFAULT 0.0000,
    wllt_status     VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    wllt_created    DATETIME(6),
    wllt_updated    DATETIME(6),
    PRIMARY KEY (wllt_id),
    UNIQUE KEY uq_wallet_acct_currency (wllt_acct_id, wllt_currency),
    CONSTRAINT fk_wallet_account FOREIGN KEY (wllt_acct_id) REFERENCES ACCOUNT_T(acct_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `wllt_id` | `walletId` | NO | |
| `wllt_acct_id` | `account` (relationship) | NO | JPA FK |
| `wllt_user_id` | `walletUserId` | NO | Denormalized for query shortcuts |
| `wllt_currency` | `walletCurrency` | NO | ISO 4217 code |
| `wllt_balance` | `walletBalance` | NO | Starts at 0 |
| `wllt_status` | `walletStatus` | NO | `ACTIVE` / `CLOSED` |
| `wllt_created` | `walletCreated` | YES | |
| `wllt_updated` | `walletUpdated` | YES | |

---

### 2.3 PAYEE_T

```sql
CREATE TABLE PAYEE_T (
    paye_id         CHAR(36)        NOT NULL,               -- PK, UUID
    paye_acct_id    CHAR(36)        NOT NULL,               -- owner's account ID
    paye_user_id    CHAR(36)        NOT NULL,               -- target user ID
    paye_nickname   VARCHAR(100),
    paye_created    DATETIME(6),
    PRIMARY KEY (paye_id),
    UNIQUE KEY uq_payee_acct_user (paye_acct_id, paye_user_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `paye_id` | `payeeId` | NO | |
| `paye_acct_id` | `payeeAccountId` | NO | Logical FK to ACCOUNT_T |
| `paye_user_id` | `payeeUserId` | NO | Logical FK to auth USER_T |
| `paye_nickname` | `payeeNickname` | YES | |
| `paye_created` | `payeeCreated` | YES | |

---

## 3. ewallet_collection_db

### 3.1 COLLECTION_T

```sql
CREATE TABLE COLLECTION_T (
    coll_id         CHAR(36)        NOT NULL,               -- PK, set by @PrePersist UUID
    coll_name       VARCHAR(255)    NOT NULL,
    coll_desc       VARCHAR(500),
    coll_category   VARCHAR(20),                            -- enum: TRIP|EXPENSE|MONTHLY|OTHER
    coll_type_name  VARCHAR(100),
    coll_currency   CHAR(3)         NOT NULL,
    coll_status     VARCHAR(20)     DEFAULT 'ACTIVE',       -- enum: ACTIVE|CLOSED
    coll_owner_id   CHAR(36)        NOT NULL,               -- logical FK to auth USER_T
    coll_created    DATETIME(6),
    coll_updated    DATETIME(6),
    PRIMARY KEY (coll_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `coll_id` | `collectionId` | NO | UUID assigned in `@PrePersist` |
| `coll_name` | `collectionName` | NO | |
| `coll_desc` | `collectionDescription` | YES | |
| `coll_category` | `collectionCategory` | YES | Stored as string enum |
| `coll_type_name` | `collectionTypeName` | YES | Denormalized from COLLECTION_TYPE_T |
| `coll_currency` | `collectionCurrency` | NO | |
| `coll_status` | `collectionStatus` | YES | Default `ACTIVE` |
| `coll_owner_id` | `collectionOwnerId` | NO | |
| `coll_created` | `collectionCreated` | YES | |
| `coll_updated` | `collectionUpdated` | YES | |

---

### 3.2 COLLECTION_MEMBER_T

```sql
CREATE TABLE COLLECTION_MEMBER_T (
    cm_id           CHAR(36)        NOT NULL,               -- PK, UUID
    cm_coll_id      CHAR(36)        NOT NULL,               -- FK → COLLECTION_T.coll_id
    cm_user_id      CHAR(36)        NOT NULL,               -- logical FK to auth USER_T
    cm_role         VARCHAR(20)     NOT NULL,               -- enum: ADMIN|EDITOR|MEMBER
    cm_joined_at    DATETIME(6),
    PRIMARY KEY (cm_id),
    UNIQUE KEY uq_member_coll_user (cm_coll_id, cm_user_id),
    CONSTRAINT fk_member_collection FOREIGN KEY (cm_coll_id) REFERENCES COLLECTION_T(coll_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `cm_id` | `collectionMemberId` | NO | |
| `cm_coll_id` | `collection` (relationship) | NO | JPA FK |
| `cm_user_id` | `collectionMemberUserId` | NO | |
| `cm_role` | `collectionMemberRole` | NO | |
| `cm_joined_at` | `collectionMemberJoinedDateTime` | YES | Set on insert |

---

### 3.3 COLLECTION_TYPE_T

```sql
CREATE TABLE COLLECTION_TYPE_T (
    ctyp_id         CHAR(36)        NOT NULL,               -- PK, UUID
    ctyp_user_id    CHAR(36),                               -- NULL for system types
    ctyp_name       VARCHAR(100)    NOT NULL,
    ctyp_system     BIT(1)          DEFAULT b'0',
    ctyp_created    DATETIME(6),
    PRIMARY KEY (ctyp_id),
    UNIQUE KEY uq_type_user_name (ctyp_user_id, ctyp_name)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `ctyp_id` | `collectionTypeId` | NO | |
| `ctyp_user_id` | `collectionTypeUserId` | YES | NULL = system/global type |
| `ctyp_name` | `collectionTypeName` | NO | |
| `ctyp_system` | `collectionTypeSystem` | YES | Default false |
| `ctyp_created` | `collectionTypeCreated` | YES | |

---

### 3.4 EXPENSE_T

```sql
CREATE TABLE EXPENSE_T (
    exp_id          CHAR(36)        NOT NULL,               -- PK, UUID
    exp_coll_id     CHAR(36)        NOT NULL,               -- FK → COLLECTION_T.coll_id
    exp_title       VARCHAR(30)     NOT NULL,
    exp_desc        VARCHAR(100),
    exp_amount      DECIMAL(19,4)   NOT NULL,
    exp_currency    CHAR(3)         NOT NULL,
    exp_paid_by     CHAR(36)        NOT NULL,               -- logical FK to auth USER_T
    exp_split_type  VARCHAR(20)     NOT NULL,               -- enum: EQUAL|PERCENTAGE|EXACT|HYBRID|HIERARCHICAL
    exp_tax_rate    DECIMAL(5,4),
    exp_tax_type    VARCHAR(20),
    exp_created     DATETIME(6),
    exp_updated     DATETIME(6),
    PRIMARY KEY (exp_id),
    CONSTRAINT fk_expense_collection FOREIGN KEY (exp_coll_id) REFERENCES COLLECTION_T(coll_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `exp_id` | `expenseId` | NO | |
| `exp_coll_id` | `collection` (relationship) | NO | JPA FK |
| `exp_title` | `expenseTitle` | NO | Max 30 chars |
| `exp_desc` | `expenseDescription` | YES | |
| `exp_amount` | `expenseAmount` | NO | |
| `exp_currency` | `expenseCurrency` | NO | |
| `exp_paid_by` | `expensePaidBy` | NO | User ID who paid |
| `exp_split_type` | `expenseSplitType` | NO | |
| `exp_tax_rate` | `expenseTaxRate` | YES | Scale 4 decimal |
| `exp_tax_type` | `expenseTaxType` | YES | |
| `exp_created` | `expenseCreated` | YES | |
| `exp_updated` | `expenseUpdated` | YES | |

---

### 3.5 EXPENSE_SHARE_T

```sql
CREATE TABLE EXPENSE_SHARE_T (
    es_id           CHAR(36)        NOT NULL,               -- PK, UUID
    es_exp_id       CHAR(36)        NOT NULL,               -- FK → EXPENSE_T.exp_id
    es_user_id      CHAR(36)        NOT NULL,               -- logical FK to auth USER_T
    es_base_amt     DECIMAL(19,4),
    es_tax_amt      DECIMAL(19,4)   DEFAULT 0.0000,
    es_total_amt    DECIMAL(19,4)   NOT NULL,
    es_settled      BIT(1)          DEFAULT b'0',
    es_settled_at   DATETIME(6),
    PRIMARY KEY (es_id),
    CONSTRAINT fk_share_expense FOREIGN KEY (es_exp_id) REFERENCES EXPENSE_T(exp_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `es_id` | `expenseShareId` | NO | |
| `es_exp_id` | `expense` (relationship) | NO | JPA FK |
| `es_user_id` | `expenseShareUserId` | NO | |
| `es_base_amt` | `expenseShareBaseAmount` | YES | Amount before tax |
| `es_tax_amt` | `expenseShareTaxAmount` | YES | Default 0 |
| `es_total_amt` | `expenseShareTotalAmount` | NO | Base + tax |
| `es_settled` | `expenseShareSettled` | YES | Default false |
| `es_settled_at` | `expenseShareSettledDateTime` | YES | Set when settled |

---

### 3.6 INVITATION_T

```sql
CREATE TABLE INVITATION_T (
    inv_id          CHAR(36)        NOT NULL,               -- PK, UUID
    inv_coll_id     CHAR(36)        NOT NULL,               -- FK → COLLECTION_T.coll_id
    inv_inviter     CHAR(36)        NOT NULL,               -- user ID of sender
    inv_invitee     CHAR(36)        NOT NULL,               -- user ID of recipient
    inv_role        VARCHAR(20)     NOT NULL,               -- enum: ADMIN|EDITOR|MEMBER
    inv_status      VARCHAR(20)     DEFAULT 'PENDING',      -- enum: PENDING|ACCEPTED|DECLINED
    inv_created     DATETIME(6),
    inv_updated     DATETIME(6),
    PRIMARY KEY (inv_id),
    CONSTRAINT fk_invitation_collection FOREIGN KEY (inv_coll_id) REFERENCES COLLECTION_T(coll_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `inv_id` | `invitationId` | NO | |
| `inv_coll_id` | `collection` (relationship) | NO | JPA FK |
| `inv_inviter` | `invitationInviter` | NO | |
| `inv_invitee` | `invitationInvitee` | NO | |
| `inv_role` | `invitationRole` | NO | |
| `inv_status` | `invitationStatus` | YES | Default `PENDING` |
| `inv_created` | `invitationCreated` | YES | |
| `inv_updated` | `invitationUpdated` | YES | |

---

### 3.7 SPLIT_RULE_T

```sql
CREATE TABLE SPLIT_RULE_T (
    sr_id           CHAR(36)        NOT NULL,               -- PK, UUID
    sr_exp_id       CHAR(36)        NOT NULL,               -- FK → EXPENSE_T.exp_id
    sr_user_id      CHAR(36)        NOT NULL,               -- logical FK to auth USER_T
    sr_percentage   DECIMAL(8,4),                           -- used for PERCENTAGE split
    sr_fixed_amt    DECIMAL(19,4),                          -- used for EXACT split
    sr_weight       INT,                                    -- used for HYBRID/HIERARCHICAL split
    PRIMARY KEY (sr_id),
    CONSTRAINT fk_splitrule_expense FOREIGN KEY (sr_exp_id) REFERENCES EXPENSE_T(exp_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `sr_id` | `splitRuleId` | NO | |
| `sr_exp_id` | `expense` (relationship) | NO | JPA FK |
| `sr_user_id` | `splitRuleUserId` | NO | |
| `sr_percentage` | `splitRulePercentage` | YES | For PERCENTAGE type |
| `sr_fixed_amt` | `splitRuleFixedAmount` | YES | For EXACT type |
| `sr_weight` | `splitRuleWeight` | YES | For HYBRID/HIERARCHICAL |

---

## 4. ewallet_transaction_db

### 4.1 TRANSACTION_T

```sql
CREATE TABLE TRANSACTION_T (
    txn_id              CHAR(36)        NOT NULL,           -- PK, UUID
    txn_payer_id        CHAR(36)        NOT NULL,           -- logical FK to auth USER_T
    txn_payee_id        CHAR(36)        NOT NULL,           -- logical FK to auth USER_T
    txn_amount          DECIMAL(19,4)   NOT NULL,
    txn_currency        CHAR(3),                            -- payer's currency
    txn_converted_amt   DECIMAL(19,4),                      -- amount in payee's currency
    txn_payee_curr      CHAR(3),                            -- payee's currency
    txn_type            VARCHAR(20)     NOT NULL,           -- enum: SETTLEMENT|TOP_UP|TRANSFER|NETTING
    txn_status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING', -- enum: PENDING|COMPLETED|FAILED|REVERSED
    txn_idem_key        VARCHAR(255)    UNIQUE,             -- idempotency key
    txn_created         DATETIME(6),
    txn_updated         DATETIME(6),
    PRIMARY KEY (txn_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `txn_id` | `transactionId` | NO | |
| `txn_payer_id` | `transactionPayerId` | NO | |
| `txn_payee_id` | `transactionPayeeId` | NO | |
| `txn_amount` | `transactionAmount` | NO | |
| `txn_currency` | `transactionCurrency` | YES | Payer's wallet currency |
| `txn_converted_amt` | `transactionConvertedAmount` | YES | NULL if same currency |
| `txn_payee_curr` | `transactionPayeeCurrency` | YES | NULL if same currency |
| `txn_type` | `transactionType` | NO | |
| `txn_status` | `transactionStatus` | NO | Default `PENDING` |
| `txn_idem_key` | `transactionIdempotencyKey` | YES | Prevents duplicate processing |
| `txn_created` | `transactionCreated` | YES | |
| `txn_updated` | `transactionUpdated` | YES | |

---

### 4.2 SETTLEMENT_T

```sql
CREATE TABLE SETTLEMENT_T (
    stl_id          CHAR(36)        NOT NULL,               -- PK, UUID
    stl_txn_id      CHAR(36)        NOT NULL,               -- FK → TRANSACTION_T.txn_id
    stl_share_id    CHAR(36),                               -- logical FK to collection EXPENSE_SHARE_T
    stl_coll_id     CHAR(36),                               -- logical FK to collection COLLECTION_T
    stl_payer_id    CHAR(36)        NOT NULL,
    stl_payee_id    CHAR(36)        NOT NULL,
    stl_amount      DECIMAL(19,4)   NOT NULL,
    stl_created     DATETIME(6),
    PRIMARY KEY (stl_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `stl_id` | `settlementId` | NO | |
| `stl_txn_id` | `settlementTransactionId` | NO | Links to TRANSACTION_T |
| `stl_share_id` | `settlementExpenseShareId` | YES | Cross-DB reference to collection |
| `stl_coll_id` | `settlementCollectionId` | YES | Cross-DB reference to collection |
| `stl_payer_id` | `settlementPayerId` | NO | |
| `stl_payee_id` | `settlementPayeeId` | NO | |
| `stl_amount` | `settlementAmount` | NO | |
| `stl_created` | `settlementCreated` | YES | |

---

### 4.3 SAGA_STATE_T

```sql
CREATE TABLE SAGA_STATE_T (
    saga_id         CHAR(36)        NOT NULL,               -- PK, UUID
    saga_txn_id     CHAR(36)        NOT NULL,               -- FK → TRANSACTION_T.txn_id
    saga_step       INT             NOT NULL,               -- current saga step number
    saga_status     VARCHAR(20),                            -- e.g. STARTED, COMPENSATING, DONE
    saga_comp_step  INT,                                    -- compensation step if rolling back
    saga_updated    DATETIME(6),                            -- set by @PrePersist/@PreUpdate
    PRIMARY KEY (saga_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `saga_id` | `sagaStateId` | NO | |
| `saga_txn_id` | `sagaStateTransactionId` | NO | |
| `saga_step` | `sagaStateStep` | NO | |
| `saga_status` | `sagaStateStatus` | YES | |
| `saga_comp_step` | `sagaStateCompensationStep` | YES | |
| `saga_updated` | `sagaStateUpdated` | YES | Updated on persist AND update |

---

## 5. ewallet_currency_db

### 5.1 CURRENCY_T

```sql
CREATE TABLE CURRENCY_T (
    curr_id         CHAR(36)        NOT NULL,               -- PK, UUID
    curr_code       CHAR(3)         NOT NULL UNIQUE,        -- ISO 4217 (e.g. MYR, USD, SGD)
    curr_name       VARCHAR(100),
    curr_symbol     VARCHAR(5),
    curr_active     BIT(1)          DEFAULT b'1',
    PRIMARY KEY (curr_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `curr_id` | `currencyId` | NO | |
| `curr_code` | `currencyCode` | NO | UNIQUE |
| `curr_name` | `currencyName` | YES | |
| `curr_symbol` | `currencySymbol` | YES | |
| `curr_active` | `currencyActive` | YES | Default true |

---

### 5.2 EXCHANGE_RATE_T

```sql
CREATE TABLE EXCHANGE_RATE_T (
    exrt_id         CHAR(36)        NOT NULL,               -- PK, UUID
    exrt_base       CHAR(3)         NOT NULL,               -- base currency code
    exrt_target     CHAR(3)         NOT NULL,               -- target currency code
    exrt_rate       DECIMAL(20,6)   NOT NULL,
    exrt_fetched    DATETIME(6)     NOT NULL,               -- when rate was fetched from source
    PRIMARY KEY (exrt_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `exrt_id` | `exchangeRateId` | NO | |
| `exrt_base` | `exchangeRateBaseCurrency` | NO | |
| `exrt_target` | `exchangeRateTargetCurrency` | NO | |
| `exrt_rate` | `exchangeRateValue` | NO | High precision (6 decimal places) |
| `exrt_fetched` | `exchangeRateFetchedDateTime` | NO | |

> Note: No unique constraint on `(exrt_base, exrt_target)` — the repository fetches the latest entry using `findTop...OrderByFetchedDateTimeDesc`. Multiple historical rates are kept.

---

## 6. ewallet_notification_db

### 6.1 NOTIFICATION_T

```sql
CREATE TABLE NOTIFICATION_T (
    notf_id         CHAR(36)        NOT NULL,               -- PK, UUID
    notf_user_id    CHAR(36)        NOT NULL,               -- logical FK to auth USER_T
    notf_type       VARCHAR(50),
    notf_title      VARCHAR(255),
    notf_message    LONGTEXT,                               -- TEXT in entity maps to LONGTEXT in MySQL
    notf_ref_id     CHAR(36),                               -- reference to related entity (e.g. collection ID)
    notf_read       BIT(1)          DEFAULT b'0',
    notf_read_at    DATETIME(6),
    notf_created    DATETIME(6),
    PRIMARY KEY (notf_id)
);
```

| Column | Java Field | Nullable | Notes |
|--------|-----------|----------|-------|
| `notf_id` | `notificationId` | NO | |
| `notf_user_id` | `notificationUserId` | NO | |
| `notf_type` | `notificationType` | YES | e.g. `INVITATION`, `SETTLEMENT` |
| `notf_title` | `notificationTitle` | YES | |
| `notf_message` | `notificationMessage` | YES | Full text body |
| `notf_ref_id` | `notificationReferenceId` | YES | Entity ID for deep-link |
| `notf_read` | `notificationRead` | YES | Default false |
| `notf_read_at` | `notificationReadDateTime` | YES | Set when `markAsRead` called |
| `notf_created` | `notificationCreated` | YES | |

---

### 6.2 USER_PREFERENCE_T

```sql
CREATE TABLE USER_PREFERENCE_T (
    uprf_id             CHAR(36)    NOT NULL,               -- PK, UUID
    uprf_user_id        CHAR(36)    NOT NULL UNIQUE,        -- logical FK to auth USER_T
    uprf_email_enabled  BIT(1)      NOT NULL DEFAULT b'1',
    uprf_sms_enabled    BIT(1)      NOT NULL DEFAULT b'0',
    uprf_push_enabled   BIT(1)      NOT NULL DEFAULT b'1',
    uprf_promo_enabled  BIT(1)      NOT NULL DEFAULT b'0',
    uprf_created        DATETIME(6),
    uprf_updated        DATETIME(6),
    PRIMARY KEY (uprf_id)
);
```

| Column | Java Field | Nullable | Default |
|--------|-----------|----------|---------|
| `uprf_id` | `userPreferenceId` | NO | UUID |
| `uprf_user_id` | `userPreferenceUserId` | NO | — |
| `uprf_email_enabled` | `userPreferenceEmailEnabled` | NO | `true` |
| `uprf_sms_enabled` | `userPreferenceSmsEnabled` | NO | `false` |
| `uprf_push_enabled` | `userPreferencePushEnabled` | NO | `true` |
| `uprf_promo_enabled` | `userPreferencePromoEnabled` | NO | `false` |
| `uprf_created` | `userPreferenceCreated` | YES | Set on insert |
| `uprf_updated` | `userPreferenceUpdated` | YES | Set on insert/update |

---

## 7. Cross-Database References (Logical FKs)

Because each microservice owns its own database, cross-service relationships are stored as plain ID columns — no database-level foreign key constraints enforced across databases.

| Referencing Table / Column | Referenced DB / Table / Column | Resolved By |
|---------------------------|-------------------------------|-------------|
| `ACCOUNT_T.acct_user_id` | `auth_db.USER_T.user_id` | auth-service API |
| `WALLET_T.wllt_user_id` | `auth_db.USER_T.user_id` | auth-service API |
| `PAYEE_T.paye_user_id` | `auth_db.USER_T.user_id` | auth-service `/internal/users/resolve` |
| `COLLECTION_T.coll_owner_id` | `auth_db.USER_T.user_id` | auth-service API |
| `COLLECTION_MEMBER_T.cm_user_id` | `auth_db.USER_T.user_id` | auth-service API |
| `EXPENSE_T.exp_paid_by` | `auth_db.USER_T.user_id` | auth-service API |
| `EXPENSE_SHARE_T.es_user_id` | `auth_db.USER_T.user_id` | auth-service API |
| `INVITATION_T.inv_inviter / inv_invitee` | `auth_db.USER_T.user_id` | auth-service API |
| `TRANSACTION_T.txn_payer_id / txn_payee_id` | `auth_db.USER_T.user_id` | auth-service API |
| `SETTLEMENT_T.stl_share_id` | `collection_db.EXPENSE_SHARE_T.es_id` | collection-service API |
| `SETTLEMENT_T.stl_coll_id` | `collection_db.COLLECTION_T.coll_id` | collection-service API |
| `NOTIFICATION_T.notf_user_id` | `auth_db.USER_T.user_id` | auth-service API |
| `USER_PREFERENCE_T.uprf_user_id` | `auth_db.USER_T.user_id` | auth-service API |

---

## 8. Discrepancies: Prepared Queries vs. Actual Schema

### 8.1 Confirmed Matches
All entity column names, types, and constraints defined via `@Column` annotations are fully consistent with what Hibernate will generate. There are no external SQL migration files that could conflict.

### 8.2 Issues Found

| # | Table | Issue |
|---|-------|-------|
| 1 | `EXCHANGE_RATE_T` | No unique constraint on `(exrt_base, exrt_target)`. Multiple rows for the same currency pair accumulate over time. Only the latest row is used (via `findTop...OrderByFetchedDateTimeDesc`). Table will grow unbounded unless pruned externally. |
| 2 | `COLLECTION_TYPE_T` | Unique constraint is on `(ctyp_user_id, ctyp_name)`. System types have `ctyp_user_id = NULL`. MySQL treats NULL ≠ NULL, so multiple system types with the same name could be inserted — `existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase` cannot guard against NULL-keyed duplicates. |
| 3 | `SAGA_STATE_T` | No unique constraint on `saga_txn_id` — multiple saga state rows could exist per transaction if not managed carefully by the service layer. |
| 4 | `SETTLEMENT_T` | `stl_share_id` and `stl_coll_id` are cross-database references with no DB-level integrity constraint. Orphaned settlements are possible if collection records are deleted. |
| 5 | `TRANSACTION_T` | `txn_currency` is nullable (no `nullable = false`) even though currency is always set by the service. Risk of NULL if the service fails mid-transaction before setting the currency. |
| 6 | All tables | Hibernate `ddl-auto: update` does NOT drop unused columns. If a column is removed from an entity, the old column stays in the DB until manually dropped — schema drift risk over time. |
