import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Card from '../../components/ui/Card'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { formatAmount } from '../../utils/currency'
import { getDashboard, getLedger } from '../../api/reporting'
import { getWallet } from '../../api/wallet'
import { useState, useEffect } from 'react'

const reports = [
  { label: 'Spending summary',  sub: 'Spent vs received by category', to: '/app/reports/spending' },
  { label: 'My position',       sub: 'Total owed and owing',          to: '/app/reports/position' },
]

export default function ReportsHubPage() {
  const navigate = useNavigate()
  const [selectedCurrency, setSelectedCurrency] = useState('MYR')

  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getDashboard,
    staleTime: 60_000,
  })

  const { data: walletData } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })

  const allWallets = walletData?.wallets ?? walletData?.accounts ?? []
  const ownedCurrencies = allWallets
    .filter((w) => w.status !== 'CLOSED')
    .map((w) => w.currency)

  // Reset selectedCurrency if it's no longer an owned currency
  useEffect(() => {
    if (ownedCurrencies.length > 0 && !ownedCurrencies.includes(selectedCurrency)) {
      setSelectedCurrency(ownedCurrencies[0])
    }
  }, [ownedCurrencies, selectedCurrency])

  const dash = data?.data
  const { data: ledgerData, isLoading: ledgerLoading } = useQuery({
    queryKey: ['report', 'ledger', selectedCurrency],
    queryFn: () => getLedger(selectedCurrency),
    enabled: !!selectedCurrency,
  })
  const ledger = ledgerData?.data

  return (
    <PageLayout title="Reports">
      <div className="px-4 py-5 space-y-5">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : (
          <>
            {dash && (
              <Card>
                <p className="text-xs text-gray-400 uppercase tracking-wide mb-3">Financial overview</p>
                <div className="grid grid-cols-2 gap-4 divide-x divide-gray-100">
                  <div>
                    <p className="text-xs text-gray-400 mb-1">Receivable</p>
                    <p className="text-lg font-semibold text-success">{formatAmount(dash.totalReceivable ?? 0, 'MYR')}</p>
                  </div>
                  <div className="pl-4">
                    <p className="text-xs text-gray-400 mb-1">Payable</p>
                    <p className="text-lg font-semibold text-danger">{formatAmount(dash.totalPayable ?? 0, 'MYR')}</p>
                  </div>
                </div>
              </Card>
            )}

            <div className="border border-gray-200 rounded-xl overflow-hidden divide-y divide-gray-100">
              {reports.map((r) => (
                <button
                  key={r.to}
                  onClick={() => navigate(r.to)}
                  className="w-full flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors text-left"
                >
                  <div>
                    <p className="text-sm font-medium text-gray-800">{r.label}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{r.sub}</p>
                  </div>
                  <span className="text-gray-300 text-base ml-3">›</span>
                </button>
              ))}

              <div className="px-4 py-4">
                <div className="mb-3">
                  <p className="text-sm font-medium text-gray-800">Currency ledger</p>
                  <p className="text-xs text-gray-400 mt-0.5">Inflow / outflow per currency</p>
                </div>
                {ownedCurrencies.length === 0 ? (
                  <p className="text-xs text-gray-400">No active wallets.</p>
                ) : (
                  <div className="flex gap-2 flex-wrap">
                    {ownedCurrencies.map((c) => (
                      <button
                        key={c}
                        onClick={() => setSelectedCurrency(c)}
                        className={`px-4 py-2 text-xs font-medium border rounded-lg transition-colors ${
                          selectedCurrency === c
                            ? 'border-primary bg-primary text-white'
                            : 'border-gray-200 text-gray-600 hover:border-primary hover:text-primary hover:bg-blue-50'
                        }`}
                      >
                        {c}
                      </button>
                    ))}
                  </div>
                )}
                <div className="mt-4">
                  {ledgerLoading ? (
                    <LoadingSpinner />
                  ) : (
                    <div className="grid grid-cols-3 gap-2">
                      <div className="rounded-lg bg-gray-50 p-3">
                        <p className="text-[10px] uppercase tracking-wide text-gray-400">Inflow</p>
                        <p className="mt-1 text-sm font-bold text-success">{formatAmount(ledger?.totalInflow ?? 0, selectedCurrency)}</p>
                      </div>
                      <div className="rounded-lg bg-gray-50 p-3">
                        <p className="text-[10px] uppercase tracking-wide text-gray-400">Outflow</p>
                        <p className="mt-1 text-sm font-bold text-danger">{formatAmount(ledger?.totalOutflow ?? 0, selectedCurrency)}</p>
                      </div>
                      <div className="rounded-lg bg-gray-50 p-3">
                        <p className="text-[10px] uppercase tracking-wide text-gray-400">Net</p>
                        <p className={`mt-1 text-sm font-bold ${(ledger?.net ?? 0) > 0 ? 'text-success' : (ledger?.net ?? 0) < 0 ? 'text-danger' : 'text-gray-400'}`}>
                          {formatAmount(Math.abs(ledger?.net ?? 0), selectedCurrency)}
                        </p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </PageLayout>
  )
}
