import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../../contexts/AuthContext'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import ExpenseShareBreakdown from '../../components/collection/ExpenseShareBreakdown'
import SettlementModal from '../../components/transaction/SettlementModal'
import { formatAmount } from '../../utils/currency'
import { formatDate } from '../../utils/date'
import { getExpense, removeExpense, sendSettlementReminder } from '../../api/expense'
import { getMembers } from '../../api/collection'
import { getWallet } from '../../api/wallet'
import { getApiErrorMessage } from '../../utils/apiError'

export default function ExpenseDetailPage() {
  const { id, eid } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { user } = useAuth()
  const [settleOpen, setSettleOpen] = useState(false)
  const [deleteError, setDeleteError] = useState('')
  const [reminderMessage, setReminderMessage] = useState('')

  const { data: expData, isLoading } = useQuery({
    queryKey: ['expense', id, eid],
    queryFn: () => getExpense(id, eid),
  })
  const { data: membersData } = useQuery({
    queryKey: ['collection', id, 'members'],
    queryFn: () => getMembers(id),
  })
  const { data: walletData } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })

  const expense = expData?.data
  const members = membersData?.data ?? []
  const memberByUserId = Object.fromEntries(members.map((member) => [member.userId, member]))
  const payerName = expense?.paidBy
    ? (memberByUserId[expense.paidBy]?.userNickname ?? memberByUserId[expense.paidBy]?.name ?? expense.paidBy)
    : '-'
  const activeWallets = (walletData?.wallets ?? walletData?.accounts ?? [])
    .filter((wallet) => (wallet.status ?? wallet.walletStatus ?? 'ACTIVE').toUpperCase() !== 'CLOSED')
  const hasSettlementWallet = !!expense?.currency && activeWallets.some((wallet) => wallet.currency === expense.currency)
  const myMembership = members.find((m) => m.userId === user?.userId || m.userId === user?.sub)
  const myRole = myMembership?.role
  const canEdit = myRole === 'ADMIN' || myRole === 'EDITOR'

  // Find my share in the breakdown
  const myShare = expense?.shares?.find(
    (s) => s.userId === user?.userId || s.userId === user?.sub
  )
  const currentUserId = user?.userId || user?.sub
  const myExpensePosition = expense?.paidBy === currentUserId
    ? -(expense?.shares ?? [])
        .filter((share) => share.userId !== currentUserId && !share.settled)
        .reduce((sum, share) => sum + Number(share.totalAmount ?? 0), 0)
    : myShare && !myShare.settled
      ? Number(myShare.totalAmount ?? 0)
      : 0
  const reminderTargets = (expense?.shares ?? []).filter((share) =>
    share.userId !== expense?.paidBy &&
    !share.settled &&
    Number(share.totalAmount ?? 0) > 0
  )
  const canSendSettlementReminder =
    reminderTargets.length > 0 &&
    (expense?.paidBy === currentUserId || expense?.createdBy === currentUserId)

  const deleteMut = useMutation({
    mutationFn: (options) => removeExpense(id, eid, options),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['collection', id, 'expenses'] })
      navigate(`/app/collections/${id}`)
    },
    onError: (error) => setDeleteError(getApiErrorMessage(error, 'Failed to delete expense')),
  })

  const reminderMut = useMutation({
    mutationFn: () => sendSettlementReminder(id, eid),
    onSuccess: (data) => {
      const sent = data?.sent ?? reminderTargets.length
      setReminderMessage(`Reminder sent to ${sent} member${sent === 1 ? '' : 's'}.`)
      qc.invalidateQueries({ queryKey: ['notifications'] })
      qc.invalidateQueries({ queryKey: ['notificationCount'] })
    },
    onError: (error) => setReminderMessage(getApiErrorMessage(error, 'Failed to send reminder')),
  })

  function handleDeleteExpense() {
    setDeleteError('')
    const shares = expense?.shares ?? []
    const hasUnsettledOthers = shares.some((share) => share.userId !== expense?.paidBy && !share.settled)
    const allSettled = shares.every((share) => share.settled)
    if (allSettled) {
      if (window.confirm('Delete this fully settled expense?')) {
        deleteMut.mutate({ waiveSettlements: false })
      }
      return
    }
    if (hasUnsettledOthers) {
      if (window.confirm('This expense still has unsettled shares. Delete it now and waive the remaining settlement from others?')) {
        deleteMut.mutate({ waiveSettlements: true })
      }
      return
    }
    window.alert('This expense can only be deleted after every share is settled, unless you choose to waive the remaining settlement.')
  }

  if (isLoading) return <PageLayout title="Expense" back><LoadingSpinner fullPage /></PageLayout>

  return (
    <PageLayout
      title={expense?.description ?? 'Expense'}
      back
      actions={
        canEdit && (
          <button
            onClick={() => navigate(`/app/collections/${id}/expenses/${eid}/edit`)}
            className="text-sm text-primary font-medium"
          >
            Edit
          </button>
        )
      }
    >
      <div className="px-4 py-5 space-y-5">
        <div className="bg-primary rounded-xl p-5 text-white">
          <p className="text-xs text-white/60 uppercase tracking-wide mb-1">Total amount</p>
          <p className="text-3xl font-semibold mt-1">{formatAmount(expense?.amount, expense?.currency)}</p>
          <p className="text-xs text-white/70 mt-1">Paid by {payerName}</p>
          <p className="text-xs text-white/50 mt-2">{formatDate(expense?.createdAt)}</p>
        </div>

        {/* Share breakdown */}
        <div className="bg-white rounded-2xl border border-gray-100 p-4">
          <p className="text-sm font-semibold text-gray-700 mb-4">Split breakdown</p>
          <ExpenseShareBreakdown expense={expense} shares={expense?.shares ?? []} memberByUserId={memberByUserId} />
        </div>

        {/* My share + settle button */}
        {myExpensePosition !== 0 && (
          <div className="bg-amber-50 border border-amber-200 rounded-2xl p-4">
            <p className="text-xs text-amber-700 mb-1">My outstanding share</p>
            <p className="text-xl font-bold text-amber-900">{formatAmount(Math.abs(myExpensePosition), expense?.currency)}</p>
            <Button
              className="w-full mt-3"
              loading={myExpensePosition < 0 && reminderMut.isPending}
              onClick={() => {
                setReminderMessage('')
                if (myExpensePosition > 0) {
                  setSettleOpen(true)
                  return
                }
                if (canSendSettlementReminder) {
                  reminderMut.mutate()
                }
              }}
              disabled={myExpensePosition > 0 ? !hasSettlementWallet : !canSendSettlementReminder}
            >
              {myExpensePosition < 0 ? 'Settle my share - Receive Money' : 'Settle my share - Pay Money'}
            </Button>
            {myExpensePosition < 0 && (
              <p className="mt-2 text-xs text-amber-700">
                Sends a settlement reminder to unsettled expense members except the payer.
              </p>
            )}
            {myExpensePosition > 0 && !hasSettlementWallet && (
              <p className="mt-2 text-xs text-amber-700">
                Open an active {expense?.currency} wallet before settling this expense.
              </p>
            )}
            {reminderMessage && (
              <p className="mt-2 text-xs text-amber-700">{reminderMessage}</p>
            )}
          </div>
        )}

        {myShare?.settled && (
          <Badge color="green" className="w-full justify-center py-2">Your share is settled ✓</Badge>
        )}

        {canEdit && (
          <button
            onClick={handleDeleteExpense}
            className="w-full text-sm text-danger py-2"
          >
            Delete expense
          </button>
        )}
        {deleteError && <p className="text-center text-xs text-danger">{deleteError}</p>}
      </div>

      <SettlementModal
        open={settleOpen}
        onClose={() => setSettleOpen(false)}
        share={myShare}
        expense={{ ...expense, collectionId: id, expenseId: eid }}
        onSettled={() => {
          qc.invalidateQueries({ queryKey: ['expense', id, eid] })
          qc.invalidateQueries({ queryKey: ['collection', id, 'balances'] })
        }}
      />
    </PageLayout>
  )
}
