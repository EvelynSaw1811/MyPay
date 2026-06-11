import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import Select from '../../components/ui/Select'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import { formatAmount } from '../../utils/currency'
import { getPosition } from '../../api/reporting'
import { settleNet } from '../../api/transaction'
import { getWallet } from '../../api/wallet'
import { getApiErrorMessage } from '../../utils/apiError'

export default function NettingPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [currency, setCurrency] = useState('MYR')
  const [idempotencyKey] = useState(() => crypto.randomUUID())
  const [done, setDone] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['position'],
    queryFn: getPosition,
  })
  const walletQuery = useQuery({
    queryKey: ['wallet'],
    queryFn: getWallet,
  })

  const position = data?.data
  const netDebts = position?.netDebts ?? []
  const selectedDebt = netDebts[0]
  const account = walletQuery.data?.data
  const openedCurrencies = (account?.wallets ?? account?.accounts ?? []).map((wallet) => wallet.currency)

  const settleMut = useMutation({
    mutationFn: () =>
      settleNet({
        counterpartyId: selectedDebt?.counterpartyId ?? selectedDebt?.creditorId ?? selectedDebt?.payeeId,
        collectionIds: selectedDebt?.collectionId ? [selectedDebt.collectionId] : (selectedDebt?.collectionIds ?? []),
        currency,
        idempotencyKey,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['wallet'] })
      qc.invalidateQueries({ queryKey: ['position'] })
      setDone(true)
    },
  })

  if (isLoading || walletQuery.isLoading) return <PageLayout title="Settle Net" back><LoadingSpinner fullPage /></PageLayout>

  if (done) {
    return (
      <PageLayout title="Done!" back>
        <div className="flex flex-col items-center justify-center py-24 gap-4 text-center px-6">
          <div className="text-6xl">✅</div>
          <p className="text-xl font-bold text-gray-900">Net settlement complete</p>
          <Button onClick={() => navigate('/app/home')} className="w-full">Go home</Button>
        </div>
      </PageLayout>
    )
  }

  const totalOwed = netDebts.reduce((s, d) => s + (d.amount ?? 0), 0)

  return (
    <PageLayout title="Settle Net" back>
      <div className="px-4 py-5 space-y-4">
        <div className="bg-gray-50 rounded-2xl p-4">
          <p className="text-xs text-gray-500 mb-1">Total outstanding (bilateral)</p>
          <p className="text-2xl font-bold text-gray-900">{formatAmount(totalOwed, 'MYR')}</p>
          <p className="text-xs text-gray-400 mt-1">Consolidated across all collections</p>
        </div>

        {netDebts.length === 0 ? (
          <EmptyState icon="🎉" title="You're all settled up!" />
        ) : (
          <div className="space-y-2">
            {netDebts.map((d, i) => (
              <div key={i} className="flex items-center justify-between py-2 border-b border-gray-50">
                <div>
                  <p className="text-sm font-medium text-gray-800">{d.creditorName ?? d.creditorId}</p>
                  <p className="text-xs text-gray-400">{d.collectionName ?? 'Multiple collections'}</p>
                </div>
                <span className="text-sm font-semibold text-danger">
                  {formatAmount(d.amount, d.currency ?? 'MYR')}
                </span>
              </div>
            ))}
          </div>
        )}

        {netDebts.length > 0 && (
          <>
            <Select
              label="Pay with currency"
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              options={openedCurrencies.map((c) => ({ value: c, label: c }))}
            />
            <Button
              className="w-full"
              loading={settleMut.isPending}
              onClick={() => settleMut.mutate()}
              disabled={!openedCurrencies.includes(currency)}
            >
              Settle all now
            </Button>
            {settleMut.isError && (
              <p className="text-sm text-danger text-center">
                {getApiErrorMessage(settleMut.error, 'Settlement failed')}
              </p>
            )}
          </>
        )}
      </div>
    </PageLayout>
  )
}
