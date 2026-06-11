import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'

import LoginPage from '../pages/auth/LoginPage'
import RegisterPage from '../pages/auth/RegisterPage'
import DashboardPage from '../pages/home/DashboardPage'
import MoneyListPage from '../pages/home/MoneyListPage'
import WalletPage from '../pages/wallet/WalletPage'
import WalletInfoPage from '../pages/wallet/WalletInfoPage'
import TopUpPage from '../pages/wallet/TopUpPage'
import HistoryPage from '../pages/wallet/HistoryPage'
import RegisterWalletPage from '../pages/wallet/RegisterWalletPage'
import RegisterWalletHubPage from '../pages/wallet/RegisterWalletHubPage'
import CancelWalletPage from '../pages/wallet/CancelWalletPage'
import RatesPage from '../pages/wallet/RatesPage'
import PayeesPage from '../pages/payees/PayeesPage'
import CollectionsPage from '../pages/collections/CollectionsPage'
import CreateCollectionPage from '../pages/collections/CreateCollectionPage'
import CollectionDetailPage from '../pages/collections/CollectionDetailPage'
import CollectionSettingsPage from '../pages/collections/CollectionSettingsPage'
import AddExpensePage from '../pages/collections/AddExpensePage'
import ExpenseDetailPage from '../pages/collections/ExpenseDetailPage'
import SettlePage from '../pages/settlement/SettlePage'
import NettingPage from '../pages/settlement/NettingPage'
import InvitationsPage from '../pages/invitations/InvitationsPage'
import NotificationsPage from '../pages/notifications/NotificationsPage'
import ReportsHubPage from '../pages/reports/ReportsHubPage'
import SpendingReportPage from '../pages/reports/SpendingReportPage'
import LedgerReportPage from '../pages/reports/LedgerReportPage'
import PositionReportPage from '../pages/reports/PositionReportPage'
import CollectionReportPage from '../pages/reports/CollectionReportPage'
import ProfilePage from '../pages/profile/ProfilePage'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route path="/app" element={<ProtectedRoute />}>
        <Route index element={<Navigate to="/app/home" replace />} />
        <Route path="home" element={<DashboardPage />} />
        <Route path="home/money/:kind" element={<MoneyListPage />} />

        <Route path="wallet" element={<WalletPage />} />
        <Route path="wallet/info/:currency" element={<WalletInfoPage />} />
        <Route path="wallet/topup" element={<TopUpPage />} />
        <Route path="wallet/history" element={<HistoryPage />} />
        <Route path="wallet/history/:currency" element={<HistoryPage />} />
        <Route path="wallet/register" element={<RegisterWalletHubPage />} />
        <Route path="wallet/register/:currency" element={<RegisterWalletPage />} />
        <Route path="wallet/cancel" element={<CancelWalletPage />} />
        <Route path="wallet/rates" element={<RatesPage />} />

        <Route path="payees" element={<PayeesPage />} />

        <Route path="collections" element={<CollectionsPage />} />
        <Route path="collections/new" element={<CreateCollectionPage />} />
        <Route path="collections/:id" element={<CollectionDetailPage />} />
        <Route path="collections/:id/settings" element={<CollectionSettingsPage />} />
        <Route path="collections/:id/expenses/new" element={<AddExpensePage />} />
        <Route path="collections/:id/expenses/:eid" element={<ExpenseDetailPage />} />
        <Route path="collections/:id/expenses/:eid/edit" element={<AddExpensePage />} />

        <Route path="settle/:colId/:eid/:shareId" element={<SettlePage />} />
        <Route path="settle-net" element={<NettingPage />} />

        <Route path="invitations" element={<InvitationsPage />} />
        <Route path="notifications" element={<NotificationsPage />} />

        <Route path="reports" element={<ReportsHubPage />} />
        <Route path="reports/spending" element={<SpendingReportPage />} />
        <Route path="reports/ledger/:currency" element={<LedgerReportPage />} />
        <Route path="reports/position" element={<PositionReportPage />} />
        <Route path="reports/collection/:id" element={<CollectionReportPage />} />

        <Route path="profile" element={<ProfilePage />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
