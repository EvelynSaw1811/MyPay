import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import Modal from '../ui/Modal'
import Button from '../ui/Button'
import Select from '../ui/Select'
import { formatAmount, CURRENCIES } from '../../utils/currency'
import { convertCurrency } from '../../api/currency'
import { settleShare } from '../../api/expense'

export default function SettlementModal({ open, onClose, share, expense, onSettled }) {
  const [currency, setCurrency] = useState(expense?.currency ?? 'MYR')
  const [idempotencyKey] = useState(() => crypto.randomUUID())

  const isCross = currency !== (expense?.currency ?? 'MYR')

  const { data: preview, isFetching: converting } = useQuery({
    queryKey: ['convert', expense?.currency, currency, share?.totalAmount],
    queryFn: () => convertCurrency({ from: expense.currency, to: currency, amount: share.totalAmount }),
    enabled: open && isCross && !!share?.totalAmount,
    staleTime: 10_000,
  })

  const settle = useMutation({
    mutationFn: () =>
      settleShare(expense.collectionId, expense.expenseId, share.shareId, { currency, idempotencyKey }),
    onSuccess: () => {
      onSettled?.()
      onClose()
    },
  })

  if (!share || !expense) return null

  const displayAmount = isCross && preview?.data?.convertedAmount
    ? formatAmount(preview.data.convertedAmount, currency)
    : formatAmount(share.totalAmount, expense.currency)

  return (
    <Modal open={open} onClose={onClose} title="Settle payment">
      <div className="space-y-4">
        <div className="bg-gray-50 rounded-xl p-4">
          <p className="text-xs text-gray-500 mb-1">Amount owed</p>
          <p className="text-2xl font-bold text-gray-900">{formatAmount(share.totalAmount, expense.currency)}</p>
          <p className="text-xs text-gray-400 mt-1">{expense.description}</p>
        </div>

        <Select
          label="Pay with currency"
          value={currency}
          onChange={(e) => setCurrency(e.target.value)}
          options={CURRENCIES.map((c) => ({ value: c, label: c }))}
        />

        {isCross && (
          <div className="bg-primary/5 rounded-xl p-4 text-sm">
            {converting ? (
              <p className="text-gray-400">Converting…</p>
            ) : preview?.data ? (
              <>
                <p className="text-gray-500">You will pay approximately</p>
                <p className="text-xl font-bold text-primary mt-1">{displayAmount}</p>
                <p className="text-xs text-gray-400 mt-1">Rate: 1 {expense.currency} = {preview.data.rate?.toFixed(4)} {currency}</p>
              </>
            ) : null}
          </div>
        )}

        <Button
          className="w-full"
          loading={settle.isPending}
          onClick={() => settle.mutate()}
          disabled={isCross && converting}
        >
          Confirm payment · {displayAmount}
        </Button>

        {settle.isError && (
          <p className="text-xs text-danger text-center">{settle.error?.message ?? 'Payment failed'}</p>
        )}
      </div>
    </Modal>
  )
}
