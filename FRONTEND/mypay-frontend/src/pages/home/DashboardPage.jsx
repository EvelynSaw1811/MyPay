import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import PageLayout from '../../components/layout/PageLayout'
import Card from '../../components/ui/Card'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import CurrencyBalanceCard from '../../components/wallet/CurrencyBalanceCard'
import MaskedValue from '../../components/ui/MaskedValue'
import { CURRENCIES, currencySymbol, formatAmount } from '../../utils/currency'
import { getWallet } from '../../api/wallet'
import { listCollections, getMembers } from '../../api/collection'
import { listExpenses } from '../../api/expense'

const quickActions = [
  { label: 'Collections', to: '/app/collections' },
  { label: 'Top up',      to: '/app/wallet/topup' },
  { label: 'Settle net',  to: '/app/settle-net' },
  { label: 'Reports',     to: '/app/reports' },
]

const walletBackgrounds = {
  MYR: 'bg-primary',
  SGD: 'bg-gray-800',
  USD: 'bg-gray-950',
}

const sectionLabelClass = 'text-xs font-semibold uppercase tracking-wide text-gray-400'

function useMaskedSection(key, initiallyMasked = true) {
  const [masked, setMasked] = useState(() => {
    const stored = localStorage.getItem(`mypay-mask-${key}`)
    return stored !== null ? stored === 'true' : initiallyMasked
  })

  const toggle = (event) => {
    event.stopPropagation()
    setMasked((current) => {
      const next = !current
      localStorage.setItem(`mypay-mask-${key}`, String(next))
      return next
    })
  }

  return [masked, toggle]
}

function SummarySectionLabel({ title, masked, onToggle }) {
  return (
    <div className="flex min-w-0 items-center gap-1.5">
      <p className={`min-w-0 whitespace-nowrap ${sectionLabelClass}`}>
        {title}
      </p>
      <button
        type="button"
        onClick={onToggle}
        aria-label={masked ? `Show ${title.toLowerCase()} amounts` : `Hide ${title.toLowerCase()} amounts`}
        title={masked ? 'Show value' : 'Hide value'}
        className="shrink-0 text-gray-400 hover:text-gray-700 transition-colors p-0 -m-0.5 rounded focus:outline-none focus:ring-2 focus:ring-primary/30"
      >
        <span className="inline-flex h-4 items-center">
          {masked ? <EyeOffIcon /> : <EyeIcon />}
        </span>
      </button>
    </div>
  )
}

function MoneySummaryPanel({ title, rows, kind, masked, onToggle, onOpen }) {
  const valueClass = kind === 'receivable' ? 'text-success' : 'text-danger'

  const handleKeyDown = (event) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      onOpen()
    }
  }

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={onOpen}
      onKeyDown={handleKeyDown}
      className="min-w-0 px-2 py-3 text-left hover:bg-blue-50 transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-primary/20"
    >
      <div className="mb-2">
        <SummarySectionLabel title={title} masked={masked} onToggle={onToggle} />
      </div>
      <div className="space-y-1.5">
        {rows.map((row) => (
          <div key={`${kind}-${row.currency}`} className="flex items-center justify-between gap-2">
            <span className="text-xs font-normal text-gray-400">{currencySymbol(row.currency)}</span>
            <span className={`text-sm font-normal ${valueClass}`}>
              {masked ? '****' : Number(row[kind] ?? 0).toFixed(2)}
            </span>
          </div>
        ))}
      </div>
      <p className="text-right text-[9px] text-gray-400 mt-1">Click to view list</p>
    </div>
  )
}

function NetPositionPanel({ rows, masked, onToggle }) {
  return (
    <div className="min-w-0 px-2 py-3 text-left">
      <div className="mb-2">
        <SummarySectionLabel title="Net" masked={masked} onToggle={onToggle} />
      </div>
      <div className="space-y-1.5">
        {rows.map((row) => (
          <div key={`net-${row.currency}`} className="flex items-center justify-between gap-2">
            <span className="text-xs font-normal text-gray-400">{currencySymbol(row.currency)}</span>
            <span className={`text-sm font-normal ${row.net > 0 ? 'text-success' : row.net < 0 ? 'text-danger' : 'text-gray-400'}`}>
              {masked ? '****' : `${row.net > 0 ? '+ ' : row.net < 0 ? '- ' : ''}${Number(Math.abs(row.net)).toFixed(2)}`}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

function EyeIcon() {
  return (
    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 12s4-4 10-4 10 4 10 4" />
      <path d="M4 14s3.5 2 8 2 8-2 8-2" />
      <path d="M9 15l-1 2" />
      <path d="M15 15l1 2" />
    </svg>
  )
}

async function loadMoneySummary(userId) {
  const collections = await listCollections().then((res) => res.data)
  const summary = {}

  await Promise.all(collections.map(async (collection) => {
    const [members, expenses] = await Promise.all([
      getMembers(collection.collectionId).then((res) => res.data),
      listExpenses(collection.collectionId).then((res) => res.data),
    ])
    const memberIds = new Set(members.map((member) => member.userId))
    if (!memberIds.has(userId)) return

    for (const expense of expenses) {
      const currency = expense.currency ?? collection.currency ?? 'MYR'
      if (!summary[currency]) {
        summary[currency] = { receivable: 0, payable: 0 }
      }
      for (const share of expense.shares ?? []) {
        if (share.settled || Number(share.totalAmount ?? 0) === 0) continue
        const amount = Number(share.totalAmount ?? 0)
        if (expense.paidBy === userId && share.userId !== userId) {
          summary[currency].receivable += amount
        }
        if (share.userId === userId && expense.paidBy !== userId) {
          summary[currency].payable += amount
        }
      }
    }
  }))

  return summary
}

export default function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const userId = user?.userId || user?.sub
  const { data: walletData, isLoading: walletLoading } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })
  const { data: moneySummary = {}, isLoading: moneyLoading } = useQuery({
    queryKey: ['dashboard-money-summary', userId],
    queryFn: () => loadMoneySummary(userId),
    enabled: !!userId,
  })
  const [receivableMasked, toggleReceivableMasked] = useMaskedSection('home-receivable')
  const [payableMasked, togglePayableMasked] = useMaskedSection('home-payable')
  const [netMasked, toggleNetMasked] = useMaskedSection('home-net-position')

  const displayName = user?.userNickname ?? user?.firstName ?? 'User'
  const allWallets = walletData?.wallets ?? walletData?.accounts ?? []
  const wallets = allWallets.filter((w) => w.status !== 'CLOSED')
  const walletByCurrency = Object.fromEntries(wallets.map((wallet) => [wallet.currency, wallet]))
  const missingCurrencies = CURRENCIES.filter((currency) => !walletByCurrency[currency])
  const ownedCurrencies = wallets.map((wallet) => wallet.currency)
  const moneyRows = ownedCurrencies.map((currency) => {
    const row = moneySummary[currency] ?? { receivable: 0, payable: 0 }
    return {
      currency,
      receivable: row.receivable,
      payable: row.payable,
      net: row.receivable - row.payable,
    }
  })

  return (
    <PageLayout title="MyPay">
      <div className="px-4 py-5 space-y-5">
        <div>
          <p className={sectionLabelClass}>Welcome back</p>
          <p className="text-xl font-semibold text-gray-900 mt-0.5">
            {displayName}
          </p>
        </div>

        {walletLoading || moneyLoading ? (
          <LoadingSpinner fullPage />
        ) : (
          <>
            <div>
              <p className={`${sectionLabelClass} mb-2`}>Wallets</p>
              <div className="grid grid-cols-3 gap-2">
                {CURRENCIES.map((currency) => {
                  const wallet = walletByCurrency[currency]
                  return (
                  <button
                    key={currency}
                    onClick={() => navigate(wallet ? `/app/wallet/info/${currency}` : `/app/wallet/register/${currency}`)}
                    className={`flex min-w-0 flex-col gap-2 rounded-xl px-2.5 py-3 text-left text-white active:opacity-90 transition-opacity ${walletBackgrounds[currency] ?? 'bg-gray-700'}`}
                  >
                    <p className="w-full shrink-0 text-right text-xs font-semibold text-white/70">{currency}</p>
                    <p className="min-h-[20px] min-w-0 w-full text-left text-[13px] font-semibold text-white">
                      {wallet && (
                        <MaskedValue
                          value={formatAmount(wallet.balance ?? 0, currency)}
                          mask={`${currencySymbol(currency)} ****`}
                          className="text-white"
                          containerClassName="w-full justify-between gap-2"
                          persistKey={`balance-${currency}`}
                        />
                      )}
                    </p>
                  </button>
                  )
                })}
              </div>
            </div>

            <Card padding="p-0" className="overflow-hidden">
              <div className="grid grid-cols-3 divide-x divide-gray-100">
                <MoneySummaryPanel
                  title="You are owed"
                  rows={moneyRows}
                  kind="receivable"
                  masked={receivableMasked}
                  onToggle={toggleReceivableMasked}
                  onOpen={() => navigate('/app/home/money/owed')}
                />
                <MoneySummaryPanel
                  title="You owe"
                  rows={moneyRows}
                  kind="payable"
                  masked={payableMasked}
                  onToggle={togglePayableMasked}
                  onOpen={() => navigate('/app/home/money/owe')}
                />
                <NetPositionPanel rows={moneyRows} masked={netMasked} onToggle={toggleNetMasked} />
              </div>
            </Card>

            {missingCurrencies.length > 0 && (
              <div>
                <p className={`${sectionLabelClass} mb-2`}>Open more wallets</p>
                <div className="space-y-3">
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
            )}

            <div>
              <p className={`${sectionLabelClass} mb-2`}>Quick actions</p>
              <div className="grid grid-cols-2 gap-2">
                {quickActions.map(({ label, to }) => (
                  <button
                    key={to}
                    onClick={() => navigate(to)}
                    className="border border-[#E8E8E8] rounded-xl px-4 py-3 text-sm font-medium text-gray-700 text-left hover:border-primary hover:text-primary hover:bg-blue-50 transition-colors"
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>
          </>
        )}
      </div>
    </PageLayout>
  )
}
