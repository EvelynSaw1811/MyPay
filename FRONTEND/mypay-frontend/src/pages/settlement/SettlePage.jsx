import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import Select from '../../components/ui/Select'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { formatAmount } from '../../utils/currency'
import { getExpense } from '../../api/expense'
import { convertCurrency } from '../../api/currency'
import { settle } from '../../api/transaction'
import { getWallet } from '../../api/wallet'
import { getApiErrorMessage } from '../../utils/apiError'

export default function SettlePage() {
  const { colId, eid, shareId } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [idempotencyKey] = useState(() => crypto.randomUUID())
  const [done, setDone] = useState(false)

  const { data: expData, isLoading: expLoading } = useQuery({
    queryKey: ['expense', colId, eid],
    queryFn: () => getExpense(colId, eid),
  })
  const walletQuery = useQuery({
    queryKey: ['wallet'],
    queryFn: getWallet,
  })
  const expense = expData?.data
  const share = expense?.shares?.find((s) => s.shareId === shareId)
  const [currency, setCurrency] = useState(() => expense?.currency ?? 'MYR')
  const account = walletQuery.data?.data
  const openedCurrencies = (account?.wallets ?? account?.accounts ?? []).map((wallet) => wallet.currency)

  const isCross = currency && expense?.currency && currency !== expense.currency

  const { data: convertData, isFetching: converting } = useQuery({
    queryKey: ['convert', expense?.currency, currency, share?.totalAmount],
    queryFn: () => convertCurrency({ from: expense.currency, to: currency, amount: share.totalAmount }),
    enabled: isCross && !!share?.totalAmount,
    staleTime: 10_000,
  })

  const settleMut = useMutation({
    mutationFn: () =>
      settle({
        shareId,
        collectionId: colId,
        expenseId: eid,
        payeeCurrency: currency,
        idempotencyKey,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['expense', colId, eid] })
      qc.invalidateQueries({ queryKey: ['collection', colId, 'balances'] })
      qc.invalidateQueries({ queryKey: ['wallet'] })
      setDone(true)
    },
  })

  if (expLoading || walletQuery.isLoading || !currency) return <PageLayout title="Settle" back><LoadingSpinner fullPage /></PageLayout>

  if (done) {
    return (
      <PageLayout title="Settled!" back hideNav>
        <div className="flex flex-col items-center justify-center py-24 gap-4 px-6 text-center">
          <div className="text-6xl">✅</div>
          <p className="text-xl font-bold text-gray-900">Payment successful</p>
          <Button className="w-full" onClick={() => navigate(`/app/collections/${colId}/expenses/${eid}`)}>
            Back to expense
          </Button>
        </div>
      </PageLayout>
    )
  }

  const displayAmount = isCross && convertData?.data?.convertedAmount
    ? formatAmount(convertData.data.convertedAmount, currency)
    : share ? formatAmount(share.totalAmount, expense.currency) : '—'

  return (
    <PageLayout title="Settle payment" back hideNav>
      <div className="px-4 py-5 space-y-4">
        <div className="bg-primary rounded-xl p-5 text-white">
          <p className="text-xs text-white/70">Settling for</p>
          <p className="text-xl font-bold mt-1">{expense?.description}</p>
          <p className="text-2xl font-bold mt-2">{share ? formatAmount(share.totalAmount, expense.currency) : '—'}</p>
        </div>

        <Select
          label="Pay with currency"
          value={currency}
          onChange={(e) => setCurrency(e.target.value)}
          options={openedCurrencies.map((c) => ({ value: c, label: c }))}
        />

        {isCross && (
          <div className="bg-primary/5 rounded-xl p-4">
            {converting ? (
              <p className="text-sm text-gray-400">Converting…</p>
            ) : convertData?.data ? (
              <>
                <p className="text-xs text-gray-500">You will pay approximately</p>
                <p className="text-xl font-bold text-primary mt-1">{displayAmount}</p>
                <p className="text-xs text-gray-400 mt-1">
                  Rate: 1 {expense.currency} = {Number(convertData.data.rate).toFixed(4)} {currency}
                </p>
              </>
            ) : null}
          </div>
        )}

        <Button
          className="w-full"
          loading={settleMut.isPending}
          onClick={() => settleMut.mutate()}
          disabled={(isCross && converting) || !openedCurrencies.includes(currency)}
        >
          Confirm · {displayAmount}
        </Button>

        {settleMut.isError && (
          <p className="text-sm text-danger text-center">
            {getApiErrorMessage(settleMut.error, 'Payment failed')}
          </p>
        )}
      </div>
    </PageLayout>
  )
}
