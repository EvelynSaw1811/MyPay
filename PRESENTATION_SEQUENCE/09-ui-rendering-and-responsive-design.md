# 09 - UI Rendering And Responsive Design

## How The UI Renders

The frontend starts in `main.jsx`:

1. React creates the root with `createRoot`.
2. `BrowserRouter` enables route-based pages.
3. `QueryClientProvider` supplies React Query cache and server-state handling.
4. `AuthProvider` supplies authenticated user state.
5. `NotificationProvider` supplies notification-related state.
6. `App` renders `AppRoutes`.

## Routing Structure

Public routes:

- `/login`
- `/register`

Protected app routes:

- `/app/home`
- `/app/wallet`
- `/app/collections`
- `/app/settle-net`
- `/app/invitations`
- `/app/notifications`
- `/app/reports`
- `/app/profile`

This gives the app a clear authenticated shell.

## Data Rendering

The UI uses React Query:

- `useQuery` fetches wallet, collection, expense, report, notification, and currency data.
- `useMutation` handles actions such as settlement, top-up, invitation response, profile update, and wallet registration.
- `invalidateQueries` refreshes affected screens after mutations.
- Default query settings include retry and stale-time behavior.

## API Handling

The shared Axios client:

- Uses `VITE_API_BASE_URL`, defaulting to `http://localhost:8080`.
- Adds `Authorization: Bearer <token>`.
- Adds an `X-Request-Id`.
- Automatically refreshes the token after a `401`.
- Queues concurrent failed requests while refresh is in progress.
- Redirects to login if refresh fails.

## Mobile-First Layout

The root UI is constrained to `max-width: 480px`, centered on desktop, and fills mobile height with `min-height: 100dvh`.

Why this matters:

- The app feels like a real mobile wallet.
- Desktop demos still show a clean phone-sized interface.
- Mobile viewport height is handled better through `100dvh`.

## Responsive And Screen-Size Handling

Important UI techniques:

- `max-width: 480px` keeps the wallet interface readable.
- `min-h-[100dvh]` adapts to mobile browser viewport changes.
- Sticky top bar and sticky bottom navigation keep primary controls reachable.
- `safe-bottom` uses `env(safe-area-inset-bottom, 16px)` for phone bottom insets.
- `overflow-y-auto` supports long pages.
- `overflow-x-auto` plus `.no-scrollbar` supports tab strips and filter chips.
- `truncate`, `min-w-0`, and fixed grid columns prevent long names or IDs from breaking layouts.
- Grids such as `grid-cols-2`, `grid-cols-3`, and `grid-cols-5` organize dense mobile content.

## Interesting UI Handling Features

### 1. Sensitive Value Masking

`MaskedValue` is used for balances, wallet IDs, and profile codes. Dashboard sections also persist mask state in local storage.

Investor message:

"Finance UI needs privacy by default, especially in public or social settings."

### 2. Bottom Navigation

The bottom navigation exposes the main app areas:

- Home
- Collection
- Wallet
- Reports
- Profile

It stays sticky at the bottom for mobile ergonomics.

### 3. Sticky Top Bar

The top bar holds page title, back action, metadata, and page actions while the content scrolls.

### 4. Modal Handling

The modal locks body scrolling while open and uses a mobile-friendly bottom-sheet style with max viewport height.

### 5. Settlement Preview

Settlement UI can show a converted amount before payment. React Query fetches conversion only when needed, and the confirmation button disables while conversion is loading.

### 6. Dashboard Summary

The dashboard combines wallet balances, money owed, money owing, and net position. It uses compact cards and grid sections to fit financial summaries on a small screen.

### 7. Collection Detail Tabs

Collection detail uses horizontally scrollable tabs and sticky settlement actions so users can work through expenses, members, balances, and invitations without leaving the collection context.

## Design Language

The UI uses:

- Clean white and light gray surfaces.
- Blue primary action color.
- Green for positive amounts.
- Red for owing or danger states.
- Compact typography suitable for finance dashboards.
- Mobile wallet-style cards.

## Presentation Message

"The UI is intentionally mobile-first because the target use case happens in real social moments: at a restaurant, during travel, after a purchase, or while settling with friends. MyPay keeps the interface compact, touch-friendly, private, and focused on fast financial decisions."
