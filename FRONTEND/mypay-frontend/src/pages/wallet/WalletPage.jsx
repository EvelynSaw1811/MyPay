import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import CurrencyBalanceCard from '../../components/wallet/CurrencyBalanceCard'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import MaskedValue from '../../components/ui/MaskedValue'
import { getWallet } from '../../api/wallet'
import { getRates } from '../../api/currency'
import { CURRENCIES, currencySymbol, formatAmount } from '../../utils/currency'

const ACTIONS = [
  { label: 'Top up', to: '/app/wallet/topup' },
  { label: 'Rates',  to: '/app/wallet/rates' },
]

export default function WalletPage() {
  const navigate = useNavigate()
  const [totalCurrency, setTotalCurrency] = useState(null)

  const { data, isLoading } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })

  const { data: ratesResp, dataUpdatedAt: ratesUpdatedAt } = useQuery({
    queryKey: ['rates', 'MYR'],
    queryFn: () => getRates('MYR'),
    staleTime: 5 * 60 * 1000,
  })
  const ratesData = ratesResp?.data // { baseCurrency:'MYR', rates:{SGD:0.302,USD:0.224}, fallback:bool }

  const allWallets      = data?.wallets ?? data?.accounts ?? []
  const activeWallets   = allWallets.filter((w) => w.status !== 'CLOSED')
  const openedCurrencies  = new Set(activeWallets.map((w) => w.currency))
  const missingCurrencies = CURRENCIES.filter((c) => !openedCurrencies.has(c))
  const walletByCurrency  = Object.fromEntries(activeWallets.map((w) => [w.currency, w]))

  // Default selection: first active wallet's currency
  useEffect(() => {
    if (!totalCurrency && activeWallets.length > 0) {
      setTotalCurrency(activeWallets[0].currency)
    }
  }, [activeWallets, totalCurrency])

  const effectiveCurrency = totalCurrency ?? activeWallets[0]?.currency ?? 'MYR'

  // ── Cross-currency total ─────────────────────────────────────────────────
  // All balances → MYR first (1 MYR = rates[currency] of that currency)
  const totalInMYR = activeWallets.reduce((sum, w) => {
    const balance = Number(w.balance ?? 0)
    if (w.currency === 'MYR') return sum + balance
    const rateMYRtoForeign = ratesData?.rates?.[w.currency]
    return sum + (rateMYRtoForeign ? balance / rateMYRtoForeign : balance)
  }, 0)

  // Then convert MYR total → effectiveCurrency
  const computedTotal = (() => {
    if (effectiveCurrency === 'MYR' || !ratesData) return totalInMYR
    const rate = ratesData.rates?.[effectiveCurrency]
    return rate ? totalInMYR * rate : totalInMYR
  })()

  const showDisclaimer  = activeWallets.length > 0 && ratesData != null
  const ratesSummary    = ratesData
    ? `1 ${currencySymbol('MYR')} = ${(ratesData.rates?.USD ?? 0).toFixed(3)} ${currencySymbol('USD')} = ${(ratesData.rates?.SGD ?? 0).toFixed(3)} ${currencySymbol('SGD')}`
    : ''
  const ratesTime = ratesUpdatedAt
    ? new Date(ratesUpdatedAt).toLocaleString('en-MY', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit',
      })
    : null

  return (
    <PageLayout title="Wallet">
      <div className="px-4 py-5 space-y-5">
        {isLoading ? (
          <LoadingSpinner fullPage />
        ) : (
          <>
            {/* Total balance header */}
            <div className="bg-primary rounded-2xl px-5 py-6 text-white">
              <div className="mb-2 flex items-center gap-2 flex-wrap">
                <p className="text-[11px] font-semibold uppercase tracking-widest text-white/60">
                  Total Balance
                </p>
                <div className="flex gap-1">
                  {CURRENCIES.map((currencyOption) => (
                    <button
                      key={currencyOption}
                      onClick={() => setTotalCurrency(currencyOption)}
                      className={`px-2 py-0.5 rounded text-xs font-medium transition-colors ${
                        effectiveCurrency === currencyOption
                          ? 'bg-white text-primary'
                          : 'bg-white/15 text-white hover:bg-white/25'
                      }`}
                    >
                      {currencyOption}
                    </button>
                  ))}
                </div>
              </div>

              {activeWallets.length === 0 ? (
                <p className="text-xl font-bold tracking-tight text-white/70">No active wallets</p>
              ) : (
                <>
                  <p className="text-3xl font-bold tracking-tight">
                    <MaskedValue
                      value={formatAmount(computedTotal, effectiveCurrency)}
                      mask={`${currencySymbol(effectiveCurrency)} ****`}
                      className="text-white"
                      containerClassName="gap-2.5"
                      buttonLabel="Toggle total balance"
                      persistKey="wallet-total-balance"
                    />
                  </p>
                  {showDisclaimer && (
                    <div className="mt-1.5 text-[10px] text-white/50 leading-relaxed">
                      <p className="text-left">
                        Approximate sum across all wallets (estimated rates) · {ratesSummary}
                      </p>
                      {ratesTime && (
                        <p className="text-right">as of {ratesTime}</p>
                      )}
                    </div>
                  )}
                </>
              )}

              <div className="mt-4 grid grid-cols-2 gap-1">
                {ACTIONS.map(({ label, to }) => (
                  <button
                    key={to}
                    onClick={() => navigate(to)}
                    className="bg-white/10 hover:bg-white/20 transition-colors rounded-lg py-2 text-[12px] font-medium text-white text-center"
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>

            {/* Per-currency cards */}
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-3">Account wallets</p>
              <div className="space-y-3">
                {activeWallets.map((item) => (
                  <CurrencyBalanceCard
                    key={item.walletId ?? item.currency}
                    currency={item.currency}
                    balance={item.balance ?? 0}
                    status={item.status}
                    walletId={item.walletId}
                    onClick={() => navigate(`/app/wallet/info/${item.currency}`)}
                  />
                ))}
                {missingCurrencies.map((currency) => (
                  <CurrencyBalanceCard
                    key={`missing-${currency}`}
                    currency={currency}
                    promotional
                    onClick={() => navigate(`/app/wallet/register/${currency}`)}
                  />
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </PageLayout>
  )
}
