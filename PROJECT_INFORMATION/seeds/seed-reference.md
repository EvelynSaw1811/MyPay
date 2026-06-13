# MyPay - Seed Data Reference

Seed data is loaded automatically when services start with `spring.profiles.active=dev`.
Each initializer is idempotent. The newer deterministic collection/transaction/notification seeds backfill when older dev data exists and skip only when their deterministic scenario rows already exist.

## Accounts

All users use password `Test@1234`.

| Name | Email | User ID | Nickname | Invitation Code
|---|---|---|---|---|
| Alice Tan | alice.tan@mypay.test | 00000001-0000-0000-0000-000000000001 | Alice | MP-00000001 |
| Bob Lim | bob.lim@mypay.test | 00000002-0000-0000-0000-000000000002 | Bob | MP-00000002 |
| Carol Wong | carol.wong@mypay.test | 00000003-0000-0000-0000-000000000003 | Carol | MP-00000003 |
| David Chen | david.chen@mypay.test | 00000004-0000-0000-0000-000000000004 | David | MP-00000004 |
| Emma Lee | emma.lee@mypay.test | 00000005-0000-0000-0000-000000000005 | Emma | MP-00000005 |
| Frank Ng | frank.ng@mypay.test | 00000006-0000-0000-0000-000000000006 | Frank | MP-00000006 |
| Grace Ong | grace.inactive@mypay.test | 00000007-0000-0000-0000-000000000007 | Grace, INACTIVE | MP-00000007 |
| Henry Low | henry.nowallet@mypay.test | 00000008-0000-0000-0000-000000000008 | Henry, no wallet | MP-00000008 |
| Ivy Teoh | ivy.nonickname@mypay.test | 00000009-0000-0000-0000-000000000009 | null nickname | MP-00000009 |

## Account Wallet Balances

Each listed user has one account. MYR is mandatory for every account; USD and SGD are only present when explicitly opened.

| User | MYR Wallet | SGD Wallet | USD Wallet |
|---|---:|---:|---:|
| Alice | 2500 | 500 | 100 |
| Bob | 1800 | 1200 | not opened |
| Carol | 3200 | not opened | 50 |
| David | 2000 | not opened | not opened |
| Emma | 1500 | 100 | 0 |
| Frank | 500 | 2000 | not opened |
| Ivy | 25 | not opened | not opened |

Grace is inactive and intentionally has no account. Henry is active and intentionally has no account. Ivy has an account, one MYR wallet, and null nickname for display fallback tests.

Wallet registration scenarios are covered by existing seeded accounts:

| Scenario | Seed User |
|---|---|
| Already owns every supported wallet | Alice, Emma |
| Can register exactly one missing wallet | Bob (USD), Carol (SGD), Frank (USD) |
| Can register two missing wallets | David, Ivy |
| Has no account, so registration lookup should fail | Henry |

Saved payees: Alice -> Bob/Carol/Ivy, Bob -> Alice/Frank, Carol -> Emma, David -> Emma/Alice, Emma -> Bob, Frank -> David, Ivy -> Alice. Alice's Ivy payee has a null custom nickname for fallback display testing.

## Collections

Collection IDs are deterministic so transactions, settlements, notifications, and reports can reference the same scenario records.

| ID | Name | Category | Currency | Owner | Status |
|---|---|---|---|---|---|
| 10000001-0000-0000-0000-000000000001 | Bali Trip 2025 | TRIP | MYR | Alice | ACTIVE |
| 10000002-0000-0000-0000-000000000002 | Office Lunch Pool | EXPENSE | MYR | Bob | ACTIVE |
| 10000003-0000-0000-0000-000000000003 | SG Weekend Getaway | TRIP | SGD | Bob | ACTIVE |
| 10000004-0000-0000-0000-000000000004 | Team Building Retreat | OTHER | MYR | Carol | ACTIVE |
| 10000005-0000-0000-0000-000000000005 | Holiday Dinner Party | EXPENSE | USD | David | ACTIVE |
| 10000006-0000-0000-0000-000000000006 | Archived Movie Night | OTHER | MYR | Emma | CLOSED |
| 10000007-0000-0000-0000-000000000007 | Empty Planning Collection | MONTHLY | MYR | Henry | ACTIVE |
| 10000008-0000-0000-0000-000000000008 | Solo Coffee Run | EXPENSE | MYR | Ivy | ACTIVE |
| 10000009-0000-0000-0000-000000000009 | Quarterly Regional Product Operations And Settlement Reconciliation Workshop | OTHER | MYR | Alice | ACTIVE |

## Collection Types

Collection types are stored per user. The development seed gives each active collection user the default set `Expense`, `Trip`, `Monthly`, and `Other`, matching the registration-event behavior for newly created users.

| User | Seeded Types |
|---|---|
| Alice | Expense, Trip, Monthly, Other, Roommates |
| Bob | Expense, Trip, Monthly, Other |
| Carol | Expense, Trip, Monthly, Other |
| David | Expense, Trip, Monthly, Other |
| Emma | Expense, Trip, Monthly, Other |
| Frank | Expense, Trip, Monthly, Other |
| Henry | Expense, Trip, Monthly, Other |
| Ivy | Expense, Trip, Monthly, Other |

## Expenses And Coverage

| Expense | Collection | Split | Amount | Coverage |
|---|---|---|---:|---|
| Hotel Booking | Bali Trip 2025 | EQUAL | MYR 1200 | Open and settled shares |
| Flight Tickets | Bali Trip 2025 | PERCENTAGE | MYR 2400 | Percentage rules and settled history |
| Activity Passes | Bali Trip 2025 | PERCENTAGE | MYR 600 | Open shares for netting/settlement |
| Team Lunch | Office Lunch Pool | EQUAL | MYR 300 | Small group settlement |
| Sentosa Island Entry | SG Weekend Getaway | EXACT | SGD 120 | Exact split plus cross-currency settlement |
| Equipment Rental | Team Building Retreat | HYBRID | MYR 450 | Fixed amount plus weighted remainder |
| Catered Dinner | Team Building Retreat | EQUAL | MYR 300 | SIMPLE 6% tax, base MYR 50 + tax MYR 3 per share |
| Snacks and Drinks | Team Building Retreat | EQUAL | MYR 100 | Rounding case: 33.34 + 33.33 + 33.33 |
| Christmas Dinner | Holiday Dinner Party | HIERARCHICAL | USD 180 | Weighted hierarchy |
| Movie Tickets | Archived Movie Night | EQUAL | MYR 90 | Closed collection read-only scenario |
| Empty Planning Collection | Empty Planning Collection | none | MYR 0 | Empty collection display and report zeros |
| Coffee | Solo Coffee Run | EQUAL | MYR 12.50 | Single-member collection with fully settled owner share |
| Workshop Supplies With A Deliberately Long Expense Description | Long-name collection | EQUAL | MYR 99.99 | Long text wrapping and layout boundary |

## Invitations

| Invitation | Collection | Inviter | Invitee | Status |
|---|---|---|---|---|
| 40000001-0000-0000-0000-000000000001 | Bali Trip 2025 | Alice | Frank | PENDING |
| 40000002-0000-0000-0000-000000000002 | SG Weekend Getaway | Bob | Alice | ACCEPTED |
| 40000003-0000-0000-0000-000000000003 | Holiday Dinner Party | David | Carol | DECLINED |

## Transactions

| Type | From | To | Amount | Payee Amount | Status | Linked Data |
|---|---|---|---:|---:|---|---|
| TOP_UP | Alice | Alice | MYR 2000 | MYR 2000 | COMPLETED | Wallet funding |
| TOP_UP | Bob | Bob | SGD 1500 | SGD 1500 | COMPLETED | Wallet funding |
| TOP_UP | Carol | Carol | MYR 1000 | MYR 1000 | COMPLETED | Wallet funding |
| TOP_UP | Frank | Frank | SGD 500 | SGD 500 | COMPLETED | Wallet funding |
| SETTLEMENT | Carol | Alice | MYR 300 | MYR 300 | COMPLETED | Hotel Booking share |
| SETTLEMENT | Carol | Bob | MYR 720 | MYR 720 | COMPLETED | Flight Tickets share |
| SETTLEMENT | Emma | Bob | MYR 100 | MYR 100 | COMPLETED | Team Lunch share |
| SETTLEMENT | David | Alice | MYR 300 | MYR 300 | PENDING | Hotel Booking open share |
| SETTLEMENT | David | Frank | MYR 170 | SGD 50 | COMPLETED | Sentosa cross-currency share |
| NETTING | Bob | Alice | MYR 150 | MYR 150 | COMPLETED | Bali Trip netting scenario |
| SETTLEMENT | Frank | David | USD 40 | USD 40 | FAILED | Christmas Dinner failed saga |

## Currency

Currencies: MYR, SGD, USD. JPY is seeded as inactive for currency filtering tests.

Seeded exchange rates:

| Base | Target | Rate |
|---|---|---:|
| MYR | SGD | 0.285000 |
| MYR | SGD | 0.270000 stale historical row |
| SGD | MYR | 3.508772 |
| MYR | USD | 0.210000 |
| USD | MYR | 4.761905 |
| SGD | USD | 0.736842 |
| USD | SGD | 1.357143 |

## Notifications

Notification seed data covers invitation received, invitation accepted, invitation declined, expense created, settlement sent, settlement received, settlement failed, read notifications, and unread notifications.

## Scenario Coverage

| Scenario | Covered |
|---|---|
| Login and nickname display | Yes |
| Inactive-user login blocking | Yes |
| Missing-account user | Yes |
| MYR-only account | Yes |
| Optional wallet opening | Yes |
| Wallet registration redirect for owned currency | Yes |
| Wallet registration offer for missing currency | Yes |
| Wallet registration blocked when account is missing | Yes |
| Null-nickname display fallback | Yes |
| Multi-currency account wallet display | Yes |
| Saved payees | Yes |
| ACTIVE and CLOSED collections | Yes |
| Per-user collection type defaults | Yes |
| Custom collection type seed | Yes |
| Empty collection | Yes |
| Single-member collection | Yes |
| Long-name layout boundary | Yes |
| ADMIN, EDITOR, MEMBER roles | Yes |
| EQUAL, PERCENTAGE, EXACT, HYBRID, HIERARCHICAL splits | Yes |
| Tax calculation display | Yes |
| Rounding/remainder display | Yes |
| Pending, accepted, declined invitations | Yes |
| Completed, pending, and failed settlements | Yes |
| Cross-currency settlement | Yes |
| Netting transaction | Yes |
| Saga completed and failed states | Yes |
| Notification inbox read/unread states | Yes |
| Exchange-rate table/fallback data | Yes |
| Stale exchange-rate latest-wins behavior | Yes |
| Inactive currency filtering | Yes |
