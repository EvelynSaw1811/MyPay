# MyPay Frontend Development Plan
**System:** Secure Distributed Microservice-Based E-Wallet  
**Frontend:** React 18 (mobile-web, not native)  
**API Gateway:** `http://localhost:8080`  
**Report reference:** Chapter 1 Scope §1.5.1 – §1.5.4, Functionalities §1.6.4

---

## Requirements Coverage Audit

Cross-referencing FYP report §1.6.4 (all role capabilities) against the plan.

### General User Capabilities
| Requirement | Screen / Phase | Status |
|---|---|---|
| Register & authenticate secure account | Login / Register — Phase 2 | ✅ |
| Maintain multi-currency wallets (MYR, SGD, USD) | Wallet screen — Phase 3 | ✅ |
| View global financial statement (asset aggregation) | Reports: Dashboard — Phase 13 | ✅ |
| View real-time balances + consolidated transaction history | Wallet screen + History — Phase 3 | ✅ |
| Manage personal Payee registry | Payees screen — Phase 5 | ✅ |
| Receive global notifications (invitations, payment requests, confirmations) | Notifications screen + badge — Phase 12 | ✅ |
| Initialize / create new collections | Collections → New — Phase 6 | ✅ |

### Collection Administrator Capabilities
| Requirement | Screen / Phase | Status |
|---|---|---|
| Configure collection metadata (name, category: Trips / Expenses / Others) | Collection Settings — Phase 6 | ✅ |
| Invite users and assign roles (Editor or Member) | Invite form, role dropdown — Phase 11 | ✅ |
| Define master split-payment logic (all 5 strategies + tax) | SplitRuleBuilder — Phase 8 | ✅ |
| Record new expenses with currency tag | Add Expense form — Phase 8 | ✅ |
| Edit distribution logic or amount of any expense | Edit Expense form — Phase 8 | ✅ |
| Trigger payment prompts to members with outstanding debts | Remind button — Phase 11 ⚠️ | ⚠️ |
| View & generate Collection Statement | Reports: Collection Statement — Phase 13 | ✅ |
| Close collection formally | Collection Settings → Close — Phase 7 | ✅ |
| View full transparency details (receipts, split logic) | Expense Detail: ShareBreakdown — Phase 9 | ✅ |
| Execute payments for own share (same-currency & cross-currency) | Settlement flow — Phase 10 | ✅ |
| View personal settlement status | Collection detail: Balances tab — Phase 7 | ✅ |

### Collection Editor Capabilities
| Requirement | Screen / Phase | Status |
|---|---|---|
| Invite participants (Member role only, not Editor) | Invite form role-gated — Phase 11 | ✅ |
| Configure / modify master split logic | SplitRuleBuilder (role-gated) — Phase 8 | ✅ |
| Record new expenses | Add Expense (role-gated) — Phase 8 | ✅ |
| Edit expense amounts / distribution | Edit Expense (role-gated) — Phase 8 | ✅ |
| View & generate Collection Statement | Reports: Collection Statement — Phase 13 | ✅ |
| View transparency details | Expense Detail: ShareBreakdown — Phase 9 | ✅ |
| Execute payments + cross-currency | Settlement flow — Phase 10 | ✅ |
| View personal settlement status | Collection detail: Balances tab — Phase 7 | ✅ |

### Collection Member Capabilities
| Requirement | Screen / Phase | Status |
|---|---|---|
| Accept invitations to join a collection | Invitations screen — Phase 11 | ✅ |
| View Collection Statement (personal view only) | Reports: Collection Statement (role-filtered) — Phase 13 | ✅ |
| View full transparency details | Expense Detail: ShareBreakdown — Phase 9 | ✅ |
| Execute payments for own share | Settlement flow — Phase 10 | ✅ |
| Cross-currency settlement | SettlementModal currency picker — Phase 10 | ✅ |
| View personal settlement status | Collection detail: Balances tab — Phase 7 | ✅ |

### Split-Payment Logic Module
| Requirement | Component | Status |
|---|---|---|
| Equal division | SplitRuleBuilder — EQUAL mode | ✅ |
| Percentage-based division | SplitRuleBuilder — PERCENTAGE mode | ✅ |
| Exact amount division | SplitRuleBuilder — EXACT mode | ✅ |
| Mixed / Hybrid division | SplitRuleBuilder — HYBRID mode | ✅ |
| Hierarchical (primary / secondary) division | SplitRuleBuilder — HIERARCHICAL mode | ✅ |
| Tax-inclusive calculation (SST, Service Charge) | SplitRuleBuilder — taxRate + taxType fields | ✅ |

### Transaction & Notification Module
| Requirement | Screen / Phase | Status |
|---|---|---|
| Direct fund deductions from wallet | Settlement flow → POST /api/transactions/settle — Phase 10 | ✅ |
| Cross-currency settlement (pay SGD debt with MYR wallet) | SettlementModal with currency selector + live preview — Phase 10 | ✅ |
| Bilateral netting (consolidate multiple debts) | Netting flow → POST /api/transactions/settle-net — Phase 10 | ✅ |
| Automated notifications on settlement | Consumed from RabbitMQ by backend; shown in notifications list | ✅ |

### Financial Reporting & Statement Module
| Requirement | Screen / Phase | Status |
|---|---|---|
| Global statement: aggregated assets across all currencies | Reports: Dashboard — GET /api/reports/dashboard — Phase 13 | ✅ |
| Granular ledger per currency (inflow / outflow / net) | Reports: Currency Ledger — GET /api/reports/ledger/{currency} — Phase 13 | ✅ |
| Spending summary (spent / received / by type) | Reports: Spending — GET /api/reports/spending — Phase 13 | ✅ |
| Personal position (total owed / owing / by collection) | Reports: Position — GET /api/reports/position — Phase 13 | ✅ |
| Collection Statement (group expenditure + member balances) | Reports: Collection — GET /api/reports/collections/{id} — Phase 13 | ✅ |

### Multi-Currency Module
| Requirement | Screen / Phase | Status |
|---|---|---|
| Three currencies: MYR, SGD, USD | CurrencyBalanceCard — Phase 3 | ✅ |
| Real-time currency conversion display | Wallet: Rates + Converter widget — Phase 4 | ✅ |
| Exchange rate display | Wallet: Rates — GET /api/currency/rates — Phase 4 | ✅ |

### Out-of-Scope Confirmations (must NOT be implemented)
| Item | Status |
|---|---|
| Native iOS / Android app | ✅ Excluded — web only |
| Downloadable PDF / Excel / CSV reports | ✅ Excluded — on-screen views only |
| Predictive analytics / AI fraud detection | ✅ Excluded |
| Blockchain / cryptocurrency | ✅ Excluded |
| Physical POS / NFC integration | ✅ Excluded |

---

### ⚠️ Known Gap — "Trigger Payment Prompts" (Admin Feature)

**Requirement:** Admin can push payment reminder notifications to all members with outstanding debts.  
**Backend status:** No REST endpoint exists for this. Notifications are only emitted automatically as a side-effect of settlement events (via RabbitMQ).  
**Frontend handling:** The "Remind All" button will be rendered in the UI (Admin-only), but a backend endpoint `POST /api/collections/{collectionId}/remind` must be implemented before it is wired up. The button will be visually present but disabled with a tooltip until the endpoint is available.

---

## Tech Stack

| Concern | Library | Version |
|---|---|---|
| Framework | React | 18 |
| Build tool | Vite | 5.x |
| Styling | Tailwind CSS | 3.x |
| Routing | React Router | v6 |
| HTTP client | Axios | 1.x |
| Server state / caching | TanStack Query (React Query) | v5 |
| Client state | React Context API | built-in |
| Date formatting | day.js | 1.x |
| Number / currency formatting | Intl.NumberFormat | built-in |
| Unique IDs (idempotency keys) | crypto.randomUUID() | built-in |

---

## Project Structure

```
FRONTEND/
├── public/
├── src/
│   ├── api/
│   │   ├── client.js          # Axios instance + JWT interceptors
│   │   ├── auth.js
│   │   ├── wallet.js
│   │   ├── payee.js
│   │   ├── collection.js
│   │   ├── expense.js
│   │   ├── invitation.js
│   │   ├── transaction.js
│   │   ├── currency.js
│   │   ├── notification.js
│   │   └── reporting.js
│   ├── contexts/
│   │   ├── AuthContext.jsx        # tokens, user profile, login/logout/register
│   │   └── NotificationContext.jsx  # unread count, polling interval
│   ├── hooks/
│   │   ├── useAuth.js
│   │   ├── useWallet.js
│   │   ├── useCollections.js
│   │   ├── useNotifications.js
│   │   └── useCurrency.js
│   ├── components/
│   │   ├── layout/
│   │   │   ├── BottomNav.jsx
│   │   │   ├── TopBar.jsx
│   │   │   └── PageLayout.jsx
│   │   ├── wallet/
│   │   │   └── CurrencyBalanceCard.jsx
│   │   ├── collection/
│   │   │   ├── SplitRuleBuilder.jsx   # most complex component
│   │   │   └── ExpenseShareBreakdown.jsx
│   │   ├── transaction/
│   │   │   └── SettlementModal.jsx
│   │   └── ui/
│   │       ├── Button.jsx
│   │       ├── Input.jsx
│   │       ├── Select.jsx
│   │       ├── Modal.jsx
│   │       ├── Badge.jsx
│   │       ├── Card.jsx
│   │       ├── LoadingSpinner.jsx
│   │       └── EmptyState.jsx
│   ├── pages/
│   │   ├── auth/
│   │   │   ├── LoginPage.jsx
│   │   │   └── RegisterPage.jsx
│   │   ├── home/
│   │   │   └── DashboardPage.jsx
│   │   ├── wallet/
│   │   │   ├── WalletPage.jsx
│   │   │   ├── TopUpPage.jsx
│   │   │   ├── HistoryPage.jsx
│   │   │   └── RatesPage.jsx
│   │   ├── payees/
│   │   │   └── PayeesPage.jsx
│   │   ├── collections/
│   │   │   ├── CollectionsPage.jsx
│   │   │   ├── CreateCollectionPage.jsx
│   │   │   ├── CollectionDetailPage.jsx   # tabs: Expenses / Members / Balances
│   │   │   ├── CollectionSettingsPage.jsx
│   │   │   ├── AddExpensePage.jsx
│   │   │   └── ExpenseDetailPage.jsx
│   │   ├── settlement/
│   │   │   ├── SettlePage.jsx
│   │   │   └── NettingPage.jsx
│   │   ├── invitations/
│   │   │   └── InvitationsPage.jsx
│   │   ├── notifications/
│   │   │   └── NotificationsPage.jsx
│   │   ├── reports/
│   │   │   ├── ReportsHubPage.jsx
│   │   │   ├── SpendingReportPage.jsx
│   │   │   ├── LedgerReportPage.jsx        # tab per currency
│   │   │   ├── PositionReportPage.jsx
│   │   │   └── CollectionReportPage.jsx
│   │   └── profile/
│   │       └── ProfilePage.jsx
│   ├── routes/
│   │   ├── AppRoutes.jsx
│   │   └── ProtectedRoute.jsx
│   └── utils/
│       ├── currency.js       # formatAmount(value, currency)
│       └── date.js           # formatDate(isoString)
├── index.html
├── package.json
├── vite.config.js
└── tailwind.config.js
```

---

## Route Map

```
/login
/register

/app                          (ProtectedRoute — redirects to /login if no token)
  /app/home                   DashboardPage
  /app/wallet                 WalletPage
    /app/wallet/topup         TopUpPage
    /app/wallet/history       HistoryPage (all currencies)
    /app/wallet/history/:currency   HistoryPage (filtered)
    /app/wallet/rates         RatesPage (exchange rates + converter)
  /app/payees                 PayeesPage
  /app/collections            CollectionsPage
    /app/collections/new      CreateCollectionPage
    /app/collections/:id      CollectionDetailPage
      /app/collections/:id/expenses/new      AddExpensePage
      /app/collections/:id/expenses/:eid     ExpenseDetailPage
      /app/collections/:id/members           (tab inside CollectionDetailPage)
      /app/collections/:id/settings          CollectionSettingsPage
  /app/settle/:colId/:eid/:shareId    SettlePage
  /app/settle-net             NettingPage
  /app/invitations            InvitationsPage
  /app/notifications          NotificationsPage
  /app/reports                ReportsHubPage
    /app/reports/spending     SpendingReportPage
    /app/reports/ledger/:currency   LedgerReportPage
    /app/reports/position     PositionReportPage
    /app/reports/collection/:id     CollectionReportPage
  /app/profile                ProfilePage
```

**Bottom navigation tabs (5):** Home · Collections · Wallet · Reports · Profile

---

## API to Screen Mapping

| Screen | Method | Endpoint |
|---|---|---|
| DashboardPage | GET | /api/reports/dashboard |
| DashboardPage (badge) | GET | /api/notifications/unread/count |
| WalletPage | GET | /api/wallets/me |
| WalletPage (per currency) | GET | /api/wallets/balance/:currency |
| TopUpPage | POST | /api/wallets/topup |
| HistoryPage | GET | /api/transactions/history |
| HistoryPage (filtered) | GET | /api/transactions/history/:currency |
| RatesPage | GET | /api/currency/rates?base=MYR |
| RatesPage (convert) | GET | /api/currency/convert?from=&to=&amount= |
| PayeesPage | GET / POST / DELETE | /api/wallets/payees, /api/wallets/payees/:id |
| CollectionsPage | GET | /api/collections |
| CreateCollectionPage | POST | /api/collections |
| CollectionDetailPage | GET | /api/collections/:id, /members, /balances |
| CollectionDetailPage (expenses) | GET | /api/collections/:id/expenses |
| CollectionSettingsPage (update) | PUT | /api/collections/:id |
| CollectionSettingsPage (close) | POST | /api/collections/:id/close |
| CollectionSettingsPage (remove member) | DELETE | /api/collections/:id/members/:uid |
| AddExpensePage | POST | /api/collections/:id/expenses |
| AddExpensePage (edit) | PUT | /api/collections/:id/expenses/:eid |
| ExpenseDetailPage | GET | /api/collections/:id/expenses/:eid |
| ExpenseDetailPage (share) | GET | /api/collections/:id/expenses/:eid/shares/:sid |
| SettlePage | POST | /api/transactions/settle |
| SettlePage (currency preview) | GET | /api/currency/convert |
| NettingPage | POST | /api/transactions/settle-net |
| InvitationsPage (send) | POST | /api/collections/:id/invitations |
| InvitationsPage (my inbox) | GET | /api/invitations |
| InvitationsPage (respond) | POST | /api/invitations/:id/respond |
| NotificationsPage | GET | /api/notifications |
| NotificationsPage (mark read) | PUT | /api/notifications/:id/read |
| NotificationsPage (mark all) | PUT | /api/notifications/read-all |
| SpendingReportPage | GET | /api/reports/spending |
| LedgerReportPage | GET | /api/reports/ledger/:currency |
| PositionReportPage | GET | /api/reports/position |
| CollectionReportPage | GET | /api/reports/collections/:id |
| ProfilePage (user info) | GET | /api/auth/users/:userId |
| ProfilePage (logout) | POST | /api/auth/logout |

---

## JWT Interceptor Logic (api/client.js)

```
1. Request interceptor
   → Read accessToken from localStorage
   → Attach header: Authorization: Bearer <accessToken>

2. Response interceptor — on HTTP 401:
   a. Read refreshToken from localStorage
   b. POST /api/auth/refresh  { refreshToken }
   c. If success → save new accessToken + refreshToken → retry original request
   d. If fail (refresh also 401) → clear localStorage → redirect to /login
   e. While refresh is in flight → queue all other 401 requests, replay after refresh
```

---

## Key Complex Components

### 1. SplitRuleBuilder (src/components/collection/SplitRuleBuilder.jsx)
Most complex UI component. Renders different input fields based on `splitType`:

| splitType | UI |
|---|---|
| EQUAL | Participant checkboxes only |
| PERCENTAGE | Per-participant % input — live validation: must sum to 100 |
| EXACT | Per-participant amount input — live validation: must sum to total |
| HYBRID | Some participants get a fixed-amount input; remainder split equally among others |
| HIERARCHICAL | Primary group (select strategy) + secondary group + optional override fields |

Common to all: optional `taxRate` (%) + `taxType` dropdown (`INCLUSIVE` / `EXCLUSIVE`).

Request body built from this component maps directly to `CreateExpenseRequest`:
```json
{
  "description": "...",
  "amount": 100.00,
  "currency": "MYR",
  "splitType": "HYBRID",
  "taxRate": 6.0,
  "taxType": "EXCLUSIVE",
  "participants": [
    { "userId": "...", "amount": 50.00 },
    { "userId": "...", "amount": null }
  ]
}
```

### 2. ExpenseShareBreakdown (src/components/collection/ExpenseShareBreakdown.jsx)
Transparency view on ExpenseDetailPage. For each share shows:
- Participant name
- Base amount (before tax)
- Tax amount
- Total amount
- Settled / Unsettled badge
- "Pay" button (only if `shareUserId === currentUserId` and `!settled`)

### 3. SettlementModal (src/components/transaction/SettlementModal.jsx)
Opened from the "Pay" button in ExpenseShareBreakdown:
1. Shows: debt amount in original currency
2. Dropdown: select payee currency (MYR / SGD / USD)
3. If different from debt currency → calls GET /api/currency/convert live → shows preview "You will pay X SGD"
4. Generates `idempotencyKey = crypto.randomUUID()` on open
5. Confirm → POST /api/transactions/settle

### 4. CurrencyBalanceCard (src/components/wallet/CurrencyBalanceCard.jsx)
Shown on WalletPage and DashboardPage:
- Currency code + flag emoji
- Balance formatted with `Intl.NumberFormat`
- Equivalent in MYR shown below (from exchange rate)

### 5. NotificationBadge
Inside TopBar. Polls `GET /api/notifications/unread/count` every 30 seconds via `setInterval` inside `NotificationContext`. Shows red dot with count when > 0.

---

## Role-Gated UI Rules

After fetching `/api/collections/:id/members`, find the entry where `userId === currentUserId` and store `myRole`.

| UI Element | Visible when |
|---|---|
| "Add Expense" button | myRole === ADMIN or EDITOR |
| "Edit Expense" option | myRole === ADMIN or EDITOR |
| "Invite Member" button | myRole === ADMIN or EDITOR |
| Invite role dropdown includes "EDITOR" | myRole === ADMIN only |
| "Close Collection" button | myRole === ADMIN only |
| "Edit Collection" settings | myRole === ADMIN only |
| "Remove Member" button | myRole === ADMIN only |
| "Remind All" button (pending backend) | myRole === ADMIN only — disabled |
| "Pay" button on a share | share.userId === currentUserId and !share.settled |
| Collection Statement — full audit view | myRole === ADMIN or EDITOR |
| Collection Statement — personal view only | myRole === MEMBER |

---

## Implementation Phases

| Phase | Deliverable | Key files |
|---|---|---|
| **1 — Foundation** | Vite scaffold, Tailwind config, Axios client with JWT interceptors, AuthContext, React Router, ProtectedRoute, BottomNav + TopBar shell | client.js, AuthContext.jsx, AppRoutes.jsx, ProtectedRoute.jsx, BottomNav.jsx |
| **2 — Auth** | Login page, Register page, token persistence in localStorage | LoginPage.jsx, RegisterPage.jsx, api/auth.js |
| **3 — Wallet** | Wallet home (3 currency balance cards), Top-up form, Transaction history (all + per currency tabs) | WalletPage.jsx, TopUpPage.jsx, HistoryPage.jsx, CurrencyBalanceCard.jsx, api/wallet.js |
| **4 — Rates** | Exchange rates table (MYR/SGD/USD), currency converter widget | RatesPage.jsx, api/currency.js |
| **5 — Payees** | Payees list, add payee form (search by email), remove payee | PayeesPage.jsx, api/payee.js |
| **6 — Collections list** | Collections list with category filter, Create collection form (name, description, category, currency) | CollectionsPage.jsx, CreateCollectionPage.jsx, api/collection.js |
| **7 — Collection detail** | CollectionDetailPage with 3 tabs — Expenses list / Members list / Balances summary, Collection settings (edit metadata, close, remove member) | CollectionDetailPage.jsx, CollectionSettingsPage.jsx |
| **8 — Expenses** | Add Expense form with full SplitRuleBuilder (all 5 strategies + tax), Edit expense, Delete expense | AddExpensePage.jsx, SplitRuleBuilder.jsx, api/expense.js |
| **9 — Transparency** | Expense detail page with ExpenseShareBreakdown showing base/tax/total per participant, settled/unsettled badges | ExpenseDetailPage.jsx, ExpenseShareBreakdown.jsx |
| **10 — Settlement** | SettlementModal with live currency conversion preview + idempotency key, Settle share flow, Netting flow (select counterparty + collections) | SettlePage.jsx, NettingPage.jsx, SettlementModal.jsx, api/transaction.js |
| **11 — Invitations** | Send invitation (select from payees, assign role), My invitations inbox (accept / decline), Role gating for invite role options | InvitationsPage.jsx, api/invitation.js |
| **12 — Notifications** | Notifications list, Mark read / mark all read, Unread count polling badge in TopBar | NotificationsPage.jsx, NotificationContext.jsx, api/notification.js |
| **13 — Reports** | Reports hub, Spending summary, Currency ledger (MYR/SGD/USD tabs), Personal position (per-collection breakdown), Collection statement | ReportsHubPage.jsx, SpendingReportPage.jsx, LedgerReportPage.jsx, PositionReportPage.jsx, CollectionReportPage.jsx, api/reporting.js |
| **14 — Profile** | User profile view, logout button, payees shortcut link | ProfilePage.jsx |

---

## Development Constraints (from report §1.5.3 – §1.5.4)

- **Mobile-first layout** — target viewport ~390px wide; test in Chrome DevTools mobile emulator
- **3 currencies only** — MYR, SGD, USD; no dynamic currency list beyond these three
- **No PDF/CSV export** — all reports are on-screen views only (explicitly out of scope)
- **No offline mode** — no service workers; React Query provides in-memory cache only
- **No native app** — web-based React only; no Capacitor or React Native
- **No analytics / AI** — historical data display only; no trend predictions
- **Simulated banking** — wallet top-up and deductions are simulated; no real bank API

---

## Summary

All 31 functional requirements from §1.6.4 are covered across 14 implementation phases.  
One item — Admin "Trigger payment prompts" — requires a backend endpoint (`POST /api/collections/{id}/remind`) that does not currently exist. The UI button will be present but disabled until the endpoint is implemented.  
All 5 out-of-scope exclusions are respected and will not be built.
