import { useMemo } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../../contexts/AuthContext'
import PageLayout from '../../components/layout/PageLayout'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import { listCollections, getMembers } from '../../api/collection'
import { listExpenses } from '../../api/expense'
import { formatAmount } from '../../utils/currency'

async function loadMoneyDetails(userId, kind) {
  const collections = await listCollections().then((res) => res.data)
  const rows = []

  await Promise.all(collections.map(async (collection) => {
    const [members, expenses] = await Promise.all([
      getMembers(collection.collectionId).then((res) => res.data),
      listExpenses(collection.collectionId).then((res) => res.data),
    ])
    const memberById = Object.fromEntries(members.map((member) => [member.userId, member]))
    const creator = memberById[collection.ownerId]

    for (const expense of expenses) {
      const payer = memberById[expense.paidBy]
      for (const share of expense.shares ?? []) {
        if (share.settled || Number(share.totalAmount ?? 0) === 0) continue
        const isOwedToMe = kind === 'owed' && expense.paidBy === userId && share.userId !== userId
        const iOwe = kind === 'owe' && share.userId === userId && expense.paidBy !== userId
        if (!isOwedToMe && !iOwe) continue

        const counterparty = isOwedToMe ? memberById[share.userId] : payer
        rows.push({
          id: `${expense.expenseId}-${share.shareId}`,
          collectionId: collection.collectionId,
          collectionName: collection.name,
          expenseId: expense.expenseId,
          expenseName: expense.description,
          amount: Number(share.totalAmount ?? 0),
          currency: expense.currency,
          creatorName: creator?.userNickname ?? creator?.name ?? collection.ownerId,
          payerName: payer?.userNickname ?? payer?.name ?? expense.paidBy,
          counterpartyName: counterparty?.userNickname ?? counterparty?.name ?? share.userId,
          myRole: collection.myRole,
          createdAt: expense.createdAt,
        })
      }
    }
  }))

  return rows
}

export default function MoneyListPage() {
  const { kind } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const userId = user?.userId || user?.sub
  const mode = kind === 'owed' ? 'owed' : 'owe'

  const { data, isLoading } = useQuery({
    queryKey: ['money-details', mode, userId],
    queryFn: () => loadMoneyDetails(userId, mode),
    enabled: !!userId,
  })

  const rows = data ?? []
  const total = useMemo(() => rows.reduce((sum, row) => sum + row.amount, 0), [rows])

  return (
    <PageLayout title={mode === 'owed' ? 'Money owed to me' : 'Money I owe'} back backTo="/app/home">
      <div className="px-4 py-5 space-y-4">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : rows.length === 0 ? (
          <EmptyState title={mode === 'owed' ? 'No one owes you right now' : 'You are all settled up'} />
        ) : (
          <>
            <div className="rounded-xl bg-gray-50 p-4">
              <p className="text-xs uppercase tracking-wide text-gray-400">Total</p>
              <p className={`mt-1 text-2xl font-bold ${mode === 'owed' ? 'text-success' : 'text-danger'}`}>
                {formatAmount(total, rows[0]?.currency ?? 'MYR')}
              </p>
            </div>
            <div className="space-y-3">
              {rows.map((row) => (
                <button
                  key={row.id}
                  onClick={() => navigate(`/app/collections/${row.collectionId}/expenses/${row.expenseId}`)}
                  className="w-full rounded-xl border border-[#E8E8E8] p-4 text-left transition-colors hover:border-primary hover:bg-blue-50"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-gray-900">{row.expenseName}</p>
                      <p className="mt-0.5 text-xs text-gray-400">{row.collectionName}</p>
                    </div>
                    <p className={`shrink-0 text-sm font-bold ${mode === 'owed' ? 'text-success' : 'text-danger'}`}>
                      {formatAmount(row.amount, row.currency)}
                    </p>
                  </div>
                  <div className="mt-3 grid grid-cols-2 gap-x-3 gap-y-1 text-xs text-gray-500">
                    <span>Creator: {row.creatorName}</span>
                    <span>Role: {row.myRole}</span>
                    <span>Paid by: {row.payerName}</span>
                    <span>{mode === 'owed' ? 'Owes me' : 'I owe'}: {row.counterpartyName}</span>
                  </div>
                </button>
              ))}
            </div>
          </>
        )}
      </div>
    </PageLayout>
  )
}
