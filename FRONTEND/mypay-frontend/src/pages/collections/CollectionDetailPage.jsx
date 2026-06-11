import { useMemo, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../../contexts/AuthContext'
import PageLayout from '../../components/layout/PageLayout'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import Card from '../../components/ui/Card'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import Modal from '../../components/ui/Modal'
import ExpenseForm from '../../components/collection/ExpenseForm'
import { formatAmount } from '../../utils/currency'
import { formatDate } from '../../utils/date'
import { getCollection, getMembers, getBalances, leaveCollection } from '../../api/collection'
import { listExpenses, settleShare } from '../../api/expense'
import { listForCollection } from '../../api/invitation'
import { getWallet } from '../../api/wallet'

const TABS = ['Expenses', 'Members', 'Balances', 'Details', 'Me']
const MEMBER_TABS = [
  { value: 'ALL', label: 'ALL' },
  { value: 'ADMIN', label: 'ADMIN' },
  { value: 'EDITOR', label: 'EDITOR' },
  { value: 'MEMBER', label: 'MEMBER' },
  { value: 'PENDING', label: 'PENDING' },
  { value: 'DECLINED', label: 'REJECTED' },
]

function expensePosition(expense, userId) {
  const shares = expense.shares ?? []
  if (expense.paidBy === userId) {
    return -shares
      .filter((s) => s.userId !== userId && !s.settled)
      .reduce((sum, s) => sum + Number(s.totalAmount ?? 0), 0)
  }
  const ownShare = shares.find((s) => s.userId === userId)
  return ownShare && !ownShare.settled ? Number(ownShare.totalAmount ?? 0) : 0
}

function expenseDisplayState(expense, userId) {
  const shares = expense.shares ?? []
  if (!userId) return { state: 'UNRELATED', amount: 0, prefix: '' }

  if (expense.paidBy === userId) {
    const receivableShares = shares.filter((s) => s.userId !== userId && Number(s.totalAmount ?? 0) > 0)
    const pendingAmount = receivableShares
      .filter((s) => !s.settled)
      .reduce((sum, s) => sum + Number(s.totalAmount ?? 0), 0)
    if (pendingAmount > 0) return { state: 'RECEIVABLE', amount: pendingAmount, prefix: '+' }

    const settledAmount = receivableShares
      .filter((s) => s.settled)
      .reduce((sum, s) => sum + Number(s.totalAmount ?? 0), 0)
    if (settledAmount > 0) return { state: 'SETTLED', amount: settledAmount, prefix: '' }
    return { state: 'UNRELATED', amount: 0, prefix: '' }
  }

  const ownShare = shares.find((s) => s.userId === userId)
  const amount = Number(ownShare?.totalAmount ?? 0)
  if (!ownShare || amount <= 0) return { state: 'UNRELATED', amount: 0, prefix: '' }
  return ownShare.settled
    ? { state: 'SETTLED', amount, prefix: '' }
    : { state: 'PAYABLE', amount, prefix: '-' }
}

function MeSection({ title, rows, currency, mode }) {
  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wide text-gray-400 pb-1">{title}</p>
      <div className="space-y-2">
        {rows.map((row) => {
          const nickname = row.member?.userNickname ?? row.member?.name ?? row.userId
          const initial = (nickname ?? '?')[0]?.toUpperCase() ?? '?'
          const amount = Number(row.amount ?? 0)
          const signedAmount = mode === 'payable' ? -Math.abs(amount)
            : mode === 'receivable' ? Math.abs(amount)
            : amount
          const valueClass = signedAmount > 0
            ? 'text-success'
            : signedAmount < 0
            ? 'text-danger'
            : 'text-gray-400'
          const prefix = signedAmount > 0 ? '+' : signedAmount < 0 ? '-' : ''
          return (
            <div key={`${title}-${row.userId}`} className="flex items-center gap-3 border-b border-gray-50 py-2 last:border-0">
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">{initial}</div>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-gray-800">{nickname}</p>
              </div>
              <span className={`shrink-0 text-sm font-normal ${valueClass}`}>
                {prefix}{formatAmount(Math.abs(signedAmount), currency)}
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default function CollectionDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { user } = useAuth()

  const [tab, setTab] = useState('Expenses')
  const [showExpenseModal, setShowExpenseModal] = useState(false)
  const [expenseSearch, setExpenseSearch] = useState('')
  const [expenseSortKey, setExpenseSortKey] = useState('date_asc')
  const [memberSearch, setMemberSearch] = useState('')
  const [memberRole, setMemberRole] = useState('ALL')
  const [balanceSearch, setBalanceSearch] = useState('')
  const [balanceSortDirection, setBalanceSortDirection] = useState('desc')

  // Settle flow
  const [settleMode, setSettleMode] = useState(false)
  const [selectedSettleIds, setSelectedSettleIds] = useState(new Set())
  const [showSettleModal, setShowSettleModal] = useState(false)
  const [settleLoading, setSettleLoading] = useState(false)
  const [settleBarHidden, setSettleBarHidden] = useState(false)
  const [showSettleHistory, setShowSettleHistory] = useState(false)
  const [leaveError, setLeaveError] = useState('')

  const { data: colData, isLoading: colLoading } = useQuery({
    queryKey: ['collection', id],
    queryFn: () => getCollection(id),
  })
  const { data: membersData } = useQuery({
    queryKey: ['collection', id, 'members'],
    queryFn: () => getMembers(id),
  })
  const { data: expData } = useQuery({
    queryKey: ['collection', id, 'expenses'],
    queryFn: () => listExpenses(id),
  })
  const { data: balData } = useQuery({
    queryKey: ['collection', id, 'balances'],
    queryFn: () => getBalances(id),
  })
  const { data: invData } = useQuery({
    queryKey: ['collection', id, 'invitations'],
    queryFn: () => listForCollection(id),
    enabled: !!id,
  })
  const { data: walletData } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })

  const col       = colData?.data
  const members   = membersData?.data ?? []
  const expenses  = expData?.data ?? []
  const balances  = balData?.data ?? []
  const invitations = invData?.data ?? []
  const currentUserId  = user?.userId || user?.sub
  const memberByUserId = Object.fromEntries(members.map((m) => [m.userId, m]))
  const myMembership   = members.find((m) => m.userId === currentUserId)
  const myRole    = col?.myRole ?? myMembership?.role
  const canEdit   = myRole === 'ADMIN' || myRole === 'EDITOR'
  const isAdmin   = myRole === 'ADMIN'
  const owner     = memberByUserId[col?.ownerId]
  const adminCount  = members.filter((m) => m.role === 'ADMIN').length
  const editorCount = members.filter((m) => m.role === 'EDITOR').length
  const currency  = col?.currency ?? 'MYR'
  const activeWallets = (walletData?.wallets ?? walletData?.accounts ?? [])
    .filter((wallet) => (wallet.status ?? wallet.walletStatus ?? 'ACTIVE').toUpperCase() !== 'CLOSED')
  const hasSettlementWallet = activeWallets.some((wallet) => wallet.currency === currency)

  // ── Expense rows ──────────────────────────────────────────────────────────
  const expenseRows = useMemo(() => expenses.map((e) => ({
    ...e,
    myPosition: expensePosition(e, currentUserId),
    myDisplay: expenseDisplayState(e, currentUserId),
  })), [currentUserId, expenses])

  const filteredExpenses = useMemo(() => {
    const rows = expenseRows.filter((e) => {
      const q = expenseSearch.trim().toLowerCase()
      return (e.title ?? '').toLowerCase().includes(q) || (e.description ?? '').toLowerCase().includes(q)
    })
    return [...rows].sort((a, b) => {
      switch (expenseSortKey) {
        case 'date_asc':   return new Date(a.createdAt) - new Date(b.createdAt)
        case 'date_desc':  return new Date(b.createdAt) - new Date(a.createdAt)
        case 'title_asc':  return (a.title ?? a.description ?? '').localeCompare(b.title ?? b.description ?? '')
        case 'me_asc':     return (a.myPosition ?? 0) - (b.myPosition ?? 0)
        case 'me_desc':    return (b.myPosition ?? 0) - (a.myPosition ?? 0)
        case 'total_asc':  return Number(a.amount) - Number(b.amount)
        case 'total_desc': return Number(b.amount) - Number(a.amount)
        default:           return 0
      }
    })
  }, [expenseRows, expenseSearch, expenseSortKey])

  // ── Pending summary ───────────────────────────────────────────────────────
  const myExpenseSummary = useMemo(() => expenseRows.reduce((s, e) => {
    if (e.myPosition > 0) s.payable  += e.myPosition
    if (e.myPosition < 0) s.receivable += Math.abs(e.myPosition)
    return s
  }, { payable: 0, receivable: 0 }), [expenseRows])

  // ── Settled summary ───────────────────────────────────────────────────────
  const mySettledSummary = useMemo(() => {
    let paid = 0      // I settled my debt out
    let received = 0  // Others settled their debt to me
    for (const expense of expenses) {
      for (const share of expense.shares ?? []) {
        if (!share.settled) continue
        if (share.userId === currentUserId && expense.paidBy !== currentUserId) {
          paid += Number(share.totalAmount ?? 0)
        }
        if (expense.paidBy === currentUserId && share.userId !== currentUserId) {
          received += Number(share.totalAmount ?? 0)
        }
      }
    }
    return { paid, received }
  }, [expenses, currentUserId])

  // ── Settlement history (settled shares involving current user) ────────────
  const settledItems = useMemo(() => {
    const items = []
    for (const expense of expenses) {
      for (const share of expense.shares ?? []) {
        if (!share.settled) continue
        if (share.userId === expense.paidBy) continue
        if (share.userId !== currentUserId && expense.paidBy !== currentUserId) continue
        const iMadePayment = share.userId === currentUserId && expense.paidBy !== currentUserId
        const counterpartyId = iMadePayment ? expense.paidBy : share.userId
        items.push({
          shareId: share.shareId,
          expenseTitle: expense.title ?? expense.description,
          settledAt: share.settledAt,
          amount: Number(share.totalAmount ?? 0),
          iMadePayment,
          counterpartyId,
          counterpartyMember: memberByUserId[counterpartyId],
        })
      }
    }
    return items.sort((a, b) => new Date(b.settledAt ?? 0) - new Date(a.settledAt ?? 0))
  }, [expenses, currentUserId, memberByUserId])

  // ── Settlement history grouped by calendar date ───────────────────────────
  const settledByDate = useMemo(() => {
    const groups = []
    const map = new Map()
    for (const item of settledItems) {
      const d = item.settledAt ? new Date(item.settledAt) : null
      const key = d
        ? `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
        : 'unknown'
      const label = d
        ? d.toLocaleDateString('en-MY', { year: 'numeric', month: 'short', day: 'numeric' })
        : 'Unknown date'
      if (!map.has(key)) {
        const group = { key, label, items: [] }
        map.set(key, group)
        groups.push(group)
      }
      map.get(key).items.push(item)
    }
    return groups
  }, [settledItems])

  // ── Settle actions ────────────────────────────────────────────────────────
  const settleSummary = useMemo(() => {
    const byPayer = new Map()
    for (const expId of selectedSettleIds) {
      const exp = expenseRows.find((e) => e.expenseId === expId)
      if (!exp) continue
      byPayer.set(exp.paidBy, (byPayer.get(exp.paidBy) ?? 0) + exp.myPosition)
    }
    return [...byPayer.entries()].map(([uid, amount]) => ({
      userId: uid, amount, member: memberByUserId[uid],
    }))
  }, [selectedSettleIds, expenseRows, memberByUserId])

  const totalSettleAmount = settleSummary.reduce((s, r) => s + r.amount, 0)

  const myReceivableRows = useMemo(() => {
    const amounts = new Map()
    for (const expense of expenses) {
      if (expense.paidBy !== currentUserId) continue
      for (const share of expense.shares ?? []) {
        if (share.userId === currentUserId || share.settled) continue
        amounts.set(share.userId, (amounts.get(share.userId) ?? 0) + Number(share.totalAmount ?? 0))
      }
    }
    return [...amounts.entries()]
      .filter(([, amt]) => amt > 0)
      .map(([uid, amount]) => ({ userId: uid, amount, member: memberByUserId[uid] }))
  }, [currentUserId, expenses, memberByUserId])

  const overallRows = useMemo(() => {
    const amounts = new Map()
    for (const expense of expenses) {
      for (const share of expense.shares ?? []) {
        if (share.settled || Number(share.totalAmount ?? 0) <= 0) continue
        const amount = Number(share.totalAmount ?? 0)
        if (expense.paidBy === currentUserId && share.userId !== currentUserId) {
          amounts.set(share.userId, (amounts.get(share.userId) ?? 0) + amount)
        } else if (share.userId === currentUserId && expense.paidBy !== currentUserId) {
          amounts.set(expense.paidBy, (amounts.get(expense.paidBy) ?? 0) - amount)
        }
      }
    }
    return [...amounts.entries()]
      .filter(([, amount]) => amount !== 0)
      .map(([userId, amount]) => ({ userId, amount, member: memberByUserId[userId] }))
  }, [currentUserId, expenses, memberByUserId])

  const selfShareRows = useMemo(() => expenses
    .filter((expense) => expense.paidBy === currentUserId)
    .flatMap((expense) => (expense.shares ?? [])
      .filter((share) => share.userId === currentUserId && Number(share.totalAmount ?? 0) > 0)
      .map((share) => ({ expense, share }))),
  [currentUserId, expenses])

  function toggleSettle(expenseId) {
    if (!hasSettlementWallet) return
    setSelectedSettleIds((prev) => {
      const next = new Set(prev)
      if (next.has(expenseId)) next.delete(expenseId)
      else next.add(expenseId)
      return next
    })
  }

  function exitSettleMode() {
    setSettleMode(false)
    setSelectedSettleIds(new Set())
    setShowSettleModal(false)
    setSettleBarHidden(false)
  }

  async function handleConfirmSettle() {
    if (!hasSettlementWallet) return
    setSettleLoading(true)
    try {
      for (const expId of selectedSettleIds) {
        const exp = expenses.find((e) => e.expenseId === expId)
        const myShare = exp?.shares?.find((s) => s.userId === currentUserId && !s.settled)
        if (myShare) await settleShare(id, expId, myShare.shareId)
      }
      qc.invalidateQueries({ queryKey: ['collection', id, 'expenses'] })
      qc.invalidateQueries({ queryKey: ['collection', id, 'balances'] })
      exitSettleMode()
    } finally {
      setSettleLoading(false)
    }
  }

  async function handleLeaveCollection() {
    setLeaveError('')
    const hasUnsettled = expenses.some((expense) => (expense.shares ?? []).some((share) => {
      if (share.settled || Number(share.totalAmount ?? 0) <= 0) return false
      return share.userId === currentUserId || expense.paidBy === currentUserId
    }))
    if (hasUnsettled) {
      setLeaveError('Please settle all expenses in this collection before deleting it from your list.')
      return
    }
    if (!window.confirm('Remove this collection from your account?')) return
    await leaveCollection(id)
    qc.invalidateQueries({ queryKey: ['collections'] })
    navigate('/app/collections')
  }

  // ── Members / Balances lists ──────────────────────────────────────────────
  const filteredMembers = useMemo(() => members.filter((m) => {
    const nick = m.userNickname ?? m.name ?? m.userId
    return nick.toLowerCase().includes(memberSearch.trim().toLowerCase()) &&
      (memberRole === 'ALL' || m.role === memberRole)
  }), [memberRole, memberSearch, members])

  const filteredInvitationMembers = useMemo(() => invitations.filter((inv) => {
    if (memberRole !== 'PENDING' && memberRole !== 'DECLINED') return false
    const code = inv.inviteeInvitationCode ?? inv.invitationCode ?? inv.inviteeId ?? ''
    return inv.status === memberRole &&
      code.toLowerCase().includes(memberSearch.trim().toLowerCase())
  }), [invitations, memberRole, memberSearch])

  const filteredBalances = useMemo(() => balances
    .filter((b) => (b.userNickname ?? b.name ?? b.userId).toLowerCase().includes(balanceSearch.trim().toLowerCase()))
    .sort((a, b) => (Number(a.netBalance ?? 0) - Number(b.netBalance ?? 0)) * (balanceSortDirection === 'asc' ? 1 : -1)),
  [balanceSearch, balanceSortDirection, balances])

  // ── Me tab ────────────────────────────────────────────────────────────────
  const meRows = useMemo(() => {
    const amounts = new Map()
    for (const expense of expenses.filter((e) => e.paidBy === currentUserId)) {
      for (const share of expense.shares ?? []) {
        if (share.userId === currentUserId || share.settled) continue
        amounts.set(share.userId, (amounts.get(share.userId) ?? 0) + Number(share.totalAmount ?? 0))
      }
    }
    return [...amounts.entries()].filter(([, a]) => a > 0)
      .map(([userId, amount]) => ({ userId, amount, member: memberByUserId[userId] }))
  }, [currentUserId, expenses, memberByUserId])

  const whoIOweRows = useMemo(() => {
    const amounts = new Map()
    for (const expense of expenses) {
      if (expense.paidBy === currentUserId) continue
      const myShare = expense.shares?.find((s) => s.userId === currentUserId && !s.settled)
      if (!myShare) continue
      const amt = Number(myShare.totalAmount ?? 0)
      if (amt <= 0) continue
      amounts.set(expense.paidBy, (amounts.get(expense.paidBy) ?? 0) + amt)
    }
    return [...amounts.entries()].map(([uid, amount]) => ({ userId: uid, amount, member: memberByUserId[uid] }))
  }, [currentUserId, expenses, memberByUserId])

  // ── Sticky bar visibility ─────────────────────────────────────────────────
  const showSettleBar = tab === 'Expenses' && !!myMembership && col?.status === 'ACTIVE' &&
    (myExpenseSummary.payable > 0 || settleMode)

  if (colLoading) return <PageLayout title="Collection" back><LoadingSpinner fullPage /></PageLayout>

  return (
    <PageLayout
      title={col?.name ?? 'Collection'}
      back
      titleMeta={(
        <div className="flex min-w-0 items-center justify-end gap-1.5">
          <span className={`inline-flex h-[18px] items-center rounded-full px-1.5 text-[10px] font-semibold leading-none ${
            col?.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
          }`}>
            {col?.status}
          </span>
          {myRole && (
            <span className="inline-flex h-[18px] items-center text-[10px] font-semibold leading-none tracking-wide text-gray-500">{myRole}</span>
          )}
          <span className="inline-flex h-[18px] items-center text-[10px] font-semibold leading-none tracking-wide text-gray-400">{(col?.typeName ?? col?.category)?.toString().toUpperCase()}</span>
        </div>
      )}
      actions={myRole === 'ADMIN' && (
        <button onClick={() => navigate(`/app/collections/${id}/settings`)} className="text-sm font-medium text-primary">
          Settings
        </button>
      )}
    >
      {/* ── Tab bar ─────────────────────────────────────────────────────── */}
      <div className="flex gap-1 overflow-x-auto no-scrollbar border-b border-gray-100 px-4 pt-3">
        {TABS.map((item) => (
          <button
            key={item}
            onClick={() => setTab(item)}
            className={`h-8 -mb-px whitespace-nowrap border-b-2 px-3 text-xs font-semibold tracking-wide transition-colors ${
              tab === item ? 'border-primary text-primary' : 'border-transparent text-gray-500'
            }`}
          >
            {item}
          </button>
        ))}
      </div>

      {/* ── Tab content ─────────────────────────────────────────────────── */}
      {/*  Extra bottom padding when the sticky settle bar is visible       */}
      <div className={`px-4 py-4 ${showSettleBar && !settleBarHidden ? 'pb-64' : showSettleBar ? 'pb-24' : ''}`}>

        {/* ══ EXPENSES ═════════════════════════════════════════════════════ */}
        {tab === 'Expenses' && (
          <>
            {/* Filter bar */}
            <div className="mb-3 flex gap-2">
              <input
                value={expenseSearch}
                onChange={(e) => setExpenseSearch(e.target.value)}
                placeholder="Search expenses"
                className="h-8 min-w-0 flex-1 rounded-lg border border-[#E8E8E8] px-3 text-xs outline-none focus:border-primary"
              />
              <select
                value={expenseSortKey}
                onChange={(e) => setExpenseSortKey(e.target.value)}
                className="h-8 rounded-lg border border-[#E8E8E8] px-2 text-xs font-medium text-gray-700 outline-none focus:border-primary bg-white"
              >
                <option value="date_asc">Oldest → Latest</option>
                <option value="date_desc">Latest → Oldest</option>
                <option value="title_asc">A-Z</option>
                <option value="me_asc">Lowest → Highest (Me)</option>
                <option value="me_desc">Highest → Lowest (Me)</option>
                <option value="total_asc">Lowest → Highest (Total)</option>
                <option value="total_desc">Highest → Lowest (Total)</option>
              </select>
            </div>

            {/* + New Expense — hidden in settle mode */}
            {!settleMode && canEdit && col?.status === 'ACTIVE' && (
              <Button className="mb-3 w-full" onClick={() => setShowExpenseModal(true)}>
                + New Expense
              </Button>
            )}

            {/* Expense list */}
            {filteredExpenses.length === 0 ? (
              <EmptyState title={expenses.length === 0 ? 'No expenses yet' : 'No matching expenses'} />
            ) : (
              <div className="space-y-2">
                {filteredExpenses.map((expense) => {
                  const isEligible = settleMode && hasSettlementWallet && expense.myPosition > 0
                  const isSelected = selectedSettleIds.has(expense.expenseId)
                  const display = expense.myDisplay ?? { state: 'UNRELATED', amount: 0, prefix: '' }
                  const posClass = display.state === 'SETTLED'
                    ? 'font-bold text-gray-400'
                    : display.state === 'PAYABLE'
                    ? 'font-bold text-danger'
                    : display.state === 'RECEIVABLE'
                    ? 'font-bold text-success'
                    : 'text-gray-400'
                  return (
                    <Card
                      key={expense.expenseId}
                      onClick={
                        settleMode
                          ? (isEligible ? () => toggleSettle(expense.expenseId) : undefined)
                          : () => navigate(`/app/collections/${id}/expenses/${expense.expenseId}`)
                      }
                      className={isSelected ? 'ring-2 ring-primary' : ''}
                    >
                      <div className="flex items-center gap-2">
                        {/* Checkbox in settle mode */}
                        {settleMode && (
                          <div className="shrink-0 flex items-center justify-center w-5">
                            {isEligible && (
                              <input
                                type="checkbox"
                                checked={isSelected}
                                onChange={() => toggleSettle(expense.expenseId)}
                                onClick={(e) => e.stopPropagation()}
                                className="w-4 h-4 accent-primary"
                              />
                            )}
                          </div>
                        )}
                        {/* Content row — min-w-0 ensures title truncates before squeezing balance */}
                        <div className="flex flex-1 min-w-0 items-center justify-between gap-2">
                          <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-medium text-gray-800">
                              {expense.title ?? expense.description}
                            </p>
                            {expense.description && expense.title && (
                              <p className="truncate text-xs text-gray-400">{expense.description}</p>
                            )}
                            <p className="text-xs text-gray-400">
                              Paid by {memberByUserId[expense.paidBy]?.userNickname ?? memberByUserId[expense.paidBy]?.name ?? expense.paidBy}
                            </p>
                            <p className="text-xs text-gray-400">{formatDate(expense.createdAt)}</p>
                          </div>
                          {/* shrink-0 guarantees this never gets squeezed */}
                          <p className={`shrink-0 text-sm ${posClass}`}>
                            {display.state === 'UNRELATED'
                              ? '-'
                              : `${display.prefix}${formatAmount(display.amount, currency)}`}
                          </p>
                        </div>
                      </div>
                    </Card>
                  )
                })}
              </div>
            )}
            {settleMode && !hasSettlementWallet && (
              <p className="mt-3 rounded-lg bg-red-50 px-3 py-2 text-xs text-danger">
                You need an active {currency} wallet before settling expenses in this collection.
              </p>
            )}
          </>
        )}

        {/* ══ MEMBERS ══════════════════════════════════════════════════════ */}
        {tab === 'Members' && (
          <>
            <input
              value={memberSearch}
              onChange={(e) => setMemberSearch(e.target.value)}
              placeholder="Search members"
              className="mb-3 h-8 w-full rounded-lg border border-[#E8E8E8] px-3 text-xs outline-none focus:border-primary"
            />
            <div className="mb-4 flex gap-1 overflow-x-auto">
              {MEMBER_TABS.map(({ value, label }) => (
                <button
                  key={value}
                  onClick={() => setMemberRole(value)}
                  className={`whitespace-nowrap border-b-2 px-3 py-2 text-xs font-semibold ${memberRole === value ? 'border-primary text-primary' : 'border-transparent text-gray-500'}`}
                >
                  {label}
                </button>
              ))}
            </div>
            {canEdit && col?.status === 'ACTIVE' && (
              <Button className="mb-4 w-full" onClick={() => navigate(`/app/invitations?colId=${id}`)}>
                + Invite member
              </Button>
            )}
            <div className="space-y-2">
              {(memberRole === 'PENDING' || memberRole === 'DECLINED') && filteredInvitationMembers.length === 0 && (
                <EmptyState title={memberRole === 'PENDING' ? 'No pending invitations' : 'No rejected invitations'} />
              )}
              {(memberRole === 'PENDING' || memberRole === 'DECLINED') && filteredInvitationMembers.map((inv) => {
                const code = inv.inviteeInvitationCode ?? inv.invitationCode ?? inv.inviteeId
                return (
                  <div key={inv.invitationId} className="flex items-center gap-3 py-2">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gray-100 text-xs font-bold text-gray-500">
                      {memberRole === 'PENDING' ? '...' : 'X'}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-mono text-sm font-medium text-gray-800">{code}</p>
                      <p className="truncate text-xs text-gray-400">Assigned role: {inv.role}</p>
                    </div>
                    <Badge color={memberRole === 'PENDING' ? 'yellow' : 'red'}>
                      {memberRole === 'PENDING' ? 'PENDING' : 'REJECTED'}
                    </Badge>
                  </div>
                )
              })}
              {memberRole !== 'PENDING' && memberRole !== 'DECLINED' && filteredMembers.length === 0 && (
                <EmptyState title="No matching members" />
              )}
              {memberRole !== 'PENDING' && memberRole !== 'DECLINED' && filteredMembers.map((member) => {
                const nickname = member.userNickname ?? member.name ?? member.userId
                const initial = (nickname ?? '?')[0]?.toUpperCase() ?? '?'
                return (
                  <div key={member.userId} className="flex items-center gap-3 py-2">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">{initial}</div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium text-gray-800">{nickname}</p>
                      {isAdmin && member.invitationCode && (
                        <p className="truncate font-mono text-xs text-gray-400">{member.invitationCode}</p>
                      )}
                    </div>
                    <Badge color={member.role === 'ADMIN' ? 'purple' : member.role === 'EDITOR' ? 'blue' : 'gray'}>
                      {member.role}
                    </Badge>
                  </div>
                )
              })}
            </div>
          </>
        )}

        {/* ══ BALANCES ═════════════════════════════════════════════════════ */}
        {tab === 'Balances' && (
          <>
            <div className="mb-4 flex gap-2">
              <input
                value={balanceSearch}
                onChange={(e) => setBalanceSearch(e.target.value)}
                placeholder="Search members"
                className="h-8 min-w-0 flex-1 rounded-lg border border-[#E8E8E8] px-3 text-xs outline-none focus:border-primary"
              />
              <button
                onClick={() => setBalanceSortDirection((v) => v === 'asc' ? 'desc' : 'asc')}
                className="h-8 rounded-lg border border-[#E8E8E8] px-3 text-xs font-medium text-gray-700"
              >
                Amount {balanceSortDirection === 'asc' ? '↑' : '↓'}
              </button>
            </div>
            <div className="space-y-2">
              {filteredBalances.length === 0 ? (
                <EmptyState title="All settled up!" />
              ) : filteredBalances.map((balance) => {
                const nickname = balance.userNickname ?? balance.name ?? balance.userId
                const initial = (nickname ?? '?')[0]?.toUpperCase() ?? '?'
                return (
                  <div key={balance.userId} className="flex items-center gap-3 border-b border-gray-50 py-2 last:border-0">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">{initial}</div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium text-gray-800">{nickname}</p>
                      {isAdmin && balance.invitationCode && (
                        <p className="truncate font-mono text-xs text-gray-400">{balance.invitationCode}</p>
                      )}
                    </div>
                    <span className={`shrink-0 text-sm font-normal ${(balance.netBalance ?? 0) > 0 ? 'text-success' : (balance.netBalance ?? 0) < 0 ? 'text-danger' : 'text-gray-400'}`}>
                      {(balance.netBalance ?? 0) > 0 ? '+' : ''}{formatAmount(Math.abs(balance.netBalance ?? 0), balance.currency ?? currency)}
                    </span>
                  </div>
                )
              })}
            </div>
          </>
        )}

        {/* ══ DETAILS ══════════════════════════════════════════════════════ */}
        {tab === 'Details' && (
          <div className="space-y-3 text-sm">
            <div className="flex items-start justify-between gap-3 border-b border-gray-50 py-2">
              <span className="text-gray-500">Description</span>
              <span className="max-w-[65%] text-right font-medium text-gray-900">{col?.description || '-'}</span>
            </div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Type</span><span className="font-medium text-gray-900">{col?.typeName ?? col?.category}</span></div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Currency</span><span className="font-medium text-gray-900">{col?.currency}</span></div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Creator</span><span className="font-medium text-gray-900">{owner?.userNickname ?? owner?.name ?? col?.ownerId}</span></div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Created</span><span className="font-medium text-gray-900">{formatDate(col?.createdAt)}</span></div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Last modified</span><span className="font-medium text-gray-900">{col?.updatedAt ? formatDate(col.updatedAt) : '-'}</span></div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Last settle</span><span className="font-medium text-gray-900">{col?.lastSettledAt ? formatDate(col.lastSettledAt) : '-'}</span></div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Members</span><span className="font-medium text-gray-900">{members.length}</span></div>
            <div className="flex items-center justify-between border-b border-gray-50 py-2"><span className="text-gray-500">Admins</span><span className="font-medium text-gray-900">{adminCount}</span></div>
            <div className="flex items-center justify-between py-2"><span className="text-gray-500">Editors</span><span className="font-medium text-gray-900">{editorCount}</span></div>
          </div>
        )}

        {/* ══ ME ═══════════════════════════════════════════════════════════ */}
        {tab === 'Me' && (
          <div>
            {overallRows.length === 0 && meRows.length === 0 && whoIOweRows.length === 0 && selfShareRows.length === 0 ? (
              <EmptyState title="All clear" description="No pending balances in this collection." />
            ) : (
              <div className="space-y-5">
                {overallRows.length > 0 && (
                  <MeSection title="Overall" rows={overallRows} currency={currency} />
                )}
                {meRows.length > 0 && (
                  <MeSection title="Owed to you" rows={meRows} currency={currency} mode="receivable" />
                )}
                {whoIOweRows.length > 0 && (
                  <MeSection title="You owe" rows={whoIOweRows} currency={currency} mode="payable" />
                )}
                {selfShareRows.length > 0 && (
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-wide text-gray-400 pb-1">Cleared payer shares</p>
                    <div className="space-y-2">
                      {selfShareRows.map(({ expense, share }) => (
                        <div key={share.shareId ?? expense.expenseId} className="flex items-center gap-3 border-b border-gray-50 py-2 last:border-0">
                          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gray-100 text-sm font-bold text-gray-500">ME</div>
                          <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-medium text-gray-800">{expense.title ?? expense.description}</p>
                          </div>
                          <span className="shrink-0 text-sm font-normal text-gray-400">
                            {formatAmount(share.totalAmount, currency)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
            <div className="mt-6 border-t border-gray-100 pt-4">
              {leaveError && <p className="mb-2 text-xs text-danger">{leaveError}</p>}
              <Button variant="outline" className="w-full border-red-200 text-danger" onClick={handleLeaveCollection}>
                Delete collection from my list
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* ── Settle bar ── sits right above the BottomNav ───────────────── */}
      {showSettleBar && (
        settleBarHidden ? (
          /* Collapsed pill */
          <div className="fixed bottom-[52px] left-1/2 z-20 flex w-full max-w-[480px] -translate-x-1/2 justify-center border-t border-gray-100 bg-white px-4 py-2">
            <button
              onClick={() => setSettleBarHidden(false)}
              className="flex items-center gap-1 rounded-full border border-gray-200 bg-white px-3 py-1 text-xs font-medium text-gray-500 shadow-sm hover:border-primary hover:text-primary transition-colors"
            >
              Summary <span className="text-[10px]">▲</span>
            </button>
          </div>
        ) : (
          /* Full bar */
          <div className="fixed bottom-[52px] left-1/2 z-20 w-full max-w-[480px] -translate-x-1/2 border-t border-gray-100 bg-white px-4 pt-3 pb-3 shadow-[0_-4px_16px_rgba(0,0,0,0.07)] space-y-2.5">

            {/* Settle-mode live summary */}
            {settleMode && selectedSettleIds.size > 0 && (
              <div className="rounded-lg bg-blue-50 border border-blue-100 px-3 py-2 space-y-1">
                <p className="text-[10px] font-semibold uppercase tracking-widest text-danger mb-1">Settlement preview</p>
                {settleSummary.map(({ userId, amount, member }) => (
                  <div key={userId} className="flex justify-between text-xs">
                    <span className="text-gray-600 truncate mr-2">To {member?.userNickname ?? member?.name ?? userId}</span>
                    <span className="shrink-0 font-semibold text-danger">-{formatAmount(amount, currency)}</span>
                  </div>
                ))}
                <div className="border-t border-blue-200 pt-1 flex justify-between text-xs font-bold">
                  <span className="text-gray-700">Total</span>
                  <span className="text-danger">-{formatAmount(totalSettleAmount, currency)}</span>
                </div>
              </div>
            )}

            {/* Summary row: My payable | My receivable | Settled | collapse */}
            <div className="flex items-start gap-1">
              <div className="flex-1 min-w-0">
                <p className="text-[10px] text-gray-400 leading-tight">Payable</p>
                <p className="text-sm font-bold text-danger leading-tight">
                  {formatAmount(myExpenseSummary.payable, currency)}
                </p>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[10px] text-gray-400 leading-tight">Receivable</p>
                <p className="text-sm font-bold text-success leading-tight">
                  {formatAmount(myExpenseSummary.receivable, currency)}
                </p>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-[10px] text-gray-400 leading-tight">Settled</p>
                {mySettledSummary.paid > 0 && (
                  <p className="text-sm font-semibold text-gray-400 leading-tight">
                    -{formatAmount(mySettledSummary.paid, currency)}
                  </p>
                )}
                {mySettledSummary.received > 0 && (
                  <p className="text-sm font-semibold text-gray-400 leading-tight">
                    +{formatAmount(mySettledSummary.received, currency)}
                  </p>
                )}
                {mySettledSummary.paid === 0 && mySettledSummary.received === 0 && (
                  <p className="text-sm font-semibold text-gray-400 leading-tight">—</p>
                )}
              </div>
              {/* Hide button */}
              <button
                onClick={() => setSettleBarHidden(true)}
                className="shrink-0 mt-0.5 flex h-7 w-7 items-center justify-center rounded-md text-gray-300 hover:text-gray-500 hover:bg-gray-50 transition-colors text-xs"
                title="Collapse"
              >
                ▼
              </button>
            </div>

            {/* View settlement history */}
            {settledItems.length > 0 && (
              <button
                onClick={() => setShowSettleHistory(true)}
                className="text-[11px] font-medium text-primary hover:underline"
              >
                View history ({settledItems.length} settlement{settledItems.length !== 1 ? 's' : ''}) →
              </button>
            )}

            {/* Action buttons */}
            {settleMode ? (
              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" onClick={exitSettleMode}>
                  Cancel
                </Button>
                <Button
                  className="flex-1"
                  disabled={selectedSettleIds.size === 0 || !hasSettlementWallet}
                  onClick={() => setShowSettleModal(true)}
                >
                  Confirm ({selectedSettleIds.size})
                </Button>
              </div>
            ) : (
              <>
              {!hasSettlementWallet && (
                <p className="rounded-lg bg-red-50 px-3 py-2 text-xs text-danger">
                  You need an active {currency} wallet before settling expenses in this collection.
                </p>
              )}
              <Button variant="outline" className="w-full" disabled={!hasSettlementWallet} onClick={() => setSettleMode(true)}>
                Choose to settle
              </Button>
              </>
            )}
          </div>
        )
      )}

      {/* ── New Expense Modal ────────────────────────────────────────────── */}
      <Modal open={showExpenseModal} onClose={() => setShowExpenseModal(false)} title="New expense">
        <ExpenseForm
          collectionId={id}
          currency={currency}
          members={members}
          showCancel
          onCancel={() => setShowExpenseModal(false)}
          onSaved={() => {
            qc.invalidateQueries({ queryKey: ['collection', id, 'expenses'] })
            qc.invalidateQueries({ queryKey: ['collection', id, 'balances'] })
            setShowExpenseModal(false)
          }}
        />
      </Modal>

      {/* ── Settlement Confirmation Modal ────────────────────────────────── */}
      <Modal open={showSettleModal} onClose={() => setShowSettleModal(false)} title="Confirm settlement">
        <div className="space-y-4 text-sm">
          <p className="text-gray-600">
            You are about to settle{' '}
            <span className="font-semibold text-gray-900">{selectedSettleIds.size}</span>{' '}
            expense{selectedSettleIds.size !== 1 ? 's' : ''}.
          </p>

          {/* Paying section */}
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500 mb-2">You will pay</p>
            {settleSummary.length === 0 ? (
              <p className="text-gray-400 italic">Nothing selected</p>
            ) : (
              <div className="space-y-1 rounded-lg bg-blue-50 p-3">
                {settleSummary.map(({ userId, amount, member }) => {
                  const name = member?.userNickname ?? member?.name ?? userId
                  return (
                    <div key={userId} className="flex justify-between gap-2">
                      <span className="truncate text-gray-700">{name}</span>
                      <span className="shrink-0 font-semibold text-danger">-{formatAmount(amount, currency)}</span>
                    </div>
                  )
                })}
                <div className="border-t border-blue-200 mt-2 pt-2 flex justify-between font-semibold">
                  <span className="text-gray-700">Total</span>
                  <span className="text-danger">-{formatAmount(totalSettleAmount, currency)}</span>
                </div>
              </div>
            )}
          </div>

          {/* Receivables — informational */}
          {myReceivableRows.length > 0 && (
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-gray-500 mb-2">Others owe you (pending)</p>
              <div className="space-y-1 rounded-lg bg-blue-50 p-3">
                {myReceivableRows.map(({ userId, amount, member }) => {
                  const name = member?.userNickname ?? member?.name ?? userId
                  return (
                    <div key={userId} className="flex justify-between gap-2">
                      <span className="truncate text-gray-700">{name}</span>
                      <span className="shrink-0 font-semibold text-success">+{formatAmount(amount, currency)}</span>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {/* Notification notice */}
          <div className="rounded-lg bg-gray-50 px-3 py-2 text-xs text-gray-500">
            A notification will be sent to{' '}
            <span className="font-medium text-gray-700">
              {settleSummary.map(({ userId, member }) => member?.userNickname ?? member?.name ?? userId).join(', ') || 'all payees'}
            </span>{' '}
            once you confirm.
          </div>

          <div className="flex gap-2 pt-1">
            <Button variant="outline" className="flex-1" onClick={() => setShowSettleModal(false)} disabled={settleLoading}>
              Go back
            </Button>
            <Button className="flex-1" loading={settleLoading} disabled={selectedSettleIds.size === 0 || !hasSettlementWallet} onClick={handleConfirmSettle}>
              Confirm settlement
            </Button>
          </div>
        </div>
      </Modal>

      {/* ── Settlement History Modal ─────────────────────────────────────── */}
      <Modal open={showSettleHistory} onClose={() => setShowSettleHistory(false)} title="Settlement history">
        {settledByDate.length === 0 ? (
          <EmptyState title="No settlements yet" />
        ) : (
          <div className="space-y-5">
            {settledByDate.map(({ key, label, items: dateItems }) => (
              <div key={key}>
                {/* Date group header */}
                <p className="mb-2 pb-1.5 text-[10px] font-semibold uppercase tracking-widest text-gray-400 border-b border-gray-100">
                  {label}
                </p>
                {/* Items under this date */}
                <div>
                  {dateItems.map((item) => {
                    const counterpartyName = item.counterpartyMember?.userNickname ?? item.counterpartyMember?.name ?? item.counterpartyId
                    return (
                      <div key={item.shareId} className="flex items-center gap-2 py-2.5 border-b border-gray-50 last:border-0">
                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-medium text-gray-800">{item.expenseTitle}</p>
                          <p className="text-xs text-gray-400">
                            {item.iMadePayment ? `Paid to ${counterpartyName}` : `Received from ${counterpartyName}`}
                          </p>
                        </div>
                        <p className="shrink-0 text-sm font-bold text-gray-400">
                          {item.iMadePayment ? '-' : '+'}{formatAmount(item.amount, currency)}
                        </p>
                      </div>
                    )
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </Modal>
    </PageLayout>
  )
}
