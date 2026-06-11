import { formatAmount } from '../../utils/currency'
import Badge from '../ui/Badge'

const strategyLabel = {
  EQUAL: 'Equal split',
  PERCENTAGE: 'Percentage split',
  EXACT: 'Exact amounts',
  HYBRID: 'Hybrid split',
  HIERARCHICAL: 'Hierarchical split',
}

export default function ExpenseShareBreakdown({ expense, shares = [], memberByUserId = {} }) {
  if (!expense) return null

  const { currency = 'MYR', amount } = expense
  const type = expense.splitType ?? expense.splitRule?.type
  const taxRate = expense.taxRate ?? expense.splitRule?.taxRate
  const taxType = expense.taxType ?? expense.splitRule?.taxType
  const payerName = expense.paidBy
    ? (memberByUserId[expense.paidBy]?.userNickname ?? memberByUserId[expense.paidBy]?.name ?? expense.paidBy)
    : '-'

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <span className="text-xs text-gray-500">Split strategy</span>
        <Badge color="purple">{strategyLabel[type] ?? type}</Badge>
      </div>

      {taxRate > 0 && (
        <div className="flex items-center justify-between">
          <span className="text-xs text-gray-500">Tax ({taxType})</span>
          <span className="text-xs font-medium text-gray-700">{taxRate}%</span>
        </div>
      )}

      <div className="flex items-center justify-between">
        <span className="text-xs text-gray-500">Total</span>
        <span className="text-sm font-semibold text-gray-900">{formatAmount(amount, currency)}</span>
      </div>
      <div className="flex items-center justify-between">
        <span className="text-xs text-gray-500">Paid by</span>
        <span className="text-xs font-medium text-gray-700">{payerName}</span>
      </div>

      {shares.length > 0 && (
        <div className="mt-2 space-y-1">
          <p className="text-xs font-medium text-gray-500 mb-2">Member breakdown</p>
          {shares.map((s) => {
            const nickname = s.userNickname ?? memberByUserId[s.userId]?.userNickname ?? s.userName ?? s.userId
            return (
              <div key={s.shareId ?? s.userId} className="flex items-center justify-between py-1.5 border-b border-gray-50 last:border-0">
                <div className="flex items-center gap-2">
                  <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center text-xs font-semibold text-primary">
                    {(nickname ?? '?')[0].toUpperCase()}
                  </div>
                  <span className="text-sm text-gray-700">{nickname}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-semibold text-gray-900">{formatAmount(s.totalAmount ?? s.shareAmount ?? s.amount, currency)}</span>
                  <Badge color={s.settled ? 'green' : 'yellow'}>{s.settled ? 'Paid' : 'Pending'}</Badge>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
