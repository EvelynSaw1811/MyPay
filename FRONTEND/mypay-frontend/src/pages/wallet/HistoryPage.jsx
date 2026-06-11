import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useParams, useNavigate } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import Badge from '../../components/ui/Badge'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import CurrencyBalanceCard from '../../components/wallet/CurrencyBalanceCard'
import { formatAmount, CURRENCIES } from '../../utils/currency'
import { formatDate } from '../../utils/date'
import { getHistory, getHistoryByCurrency } from '../../api/transaction'
import { getWallet } from '../../api/wallet'

const typeColor = {
  TOPUP: 'green',
  SETTLEMENT_IN: 'green',
  SETTLEMENT_OUT: 'red',
  TRANSFER_IN: 'blue',
  TRANSFER_OUT: 'red',
}

// ── Date filter presets ────────────────────────────────────────────────────
const DATE_PRESETS = [
  { label: 'Last 7 days', days: 7 },
  { label: 'Last 30 days',days: 30 },
  { label: 'Last 90 days',days: 90 },
  { label: 'Custom',      days: 'custom' },
]

const DIRECTION_FILTERS = ['ALL', 'IN', 'OUT']

function startOfDay(d) {
  const x = new Date(d); x.setHours(0, 0, 0, 0); return x
}
function endOfDay(d) {
  const x = new Date(d); x.setHours(23, 59, 59, 999); return x
}

export default function HistoryPage() {
  const { currency } = useParams()
  const navigate = useNavigate()

  const [preset, setPreset]         = useState(DATE_PRESETS[0])
  const [customFrom, setCustomFrom] = useState('')
  const [customTo,   setCustomTo]   = useState('')
  const [direction, setDirection]   = useState('ALL')

  const { data, isLoading } = useQuery({
    queryKey: ['txHistory', currency ?? 'all'],
    queryFn: () => (currency ? getHistoryByCurrency(currency) : getHistory()),
  })
  const { data: walletData, isLoading: walletLoading } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })

  const allTxs = data?.data ?? []
  const wallets = walletData?.wallets ?? walletData?.accounts ?? []
  const hasCurrencyWallet = !currency || wallets.some((w) => w.currency === currency)

  // Apply date + direction filter
  const txs = useMemo(() => {
    const now = new Date()
    let result = allTxs

    // Date filter
    if (preset.days === 'custom') {
      const from = customFrom ? startOfDay(customFrom) : null
      const to   = customTo   ? endOfDay(customTo)     : null
      result = result.filter((tx) => {
        const d = new Date(tx.createdAt ?? tx.timestamp)
        if (from && d < from) return false
        if (to   && d > to)   return false
        return true
      })
    } else {
      const cutoff = new Date(now.getTime() - preset.days * 24 * 60 * 60 * 1000)
      result = result.filter((tx) => new Date(tx.createdAt ?? tx.timestamp) >= cutoff)
    }

    // Direction filter
    if (direction !== 'ALL') {
      result = result.filter((tx) => {
        const isIn = tx.direction === 'IN' || tx.type?.includes('IN') || tx.type === 'TOPUP'
        return direction === 'IN' ? isIn : !isIn
      })
    }

    return result
  }, [allTxs, preset, customFrom, customTo, direction])

  return (
    <PageLayout title={currency ? `${currency} History` : 'Transaction History'} back backTo={currency ? `/app/wallet/info/${currency}` : '/app/wallet'}>

      {/* Currency filter pills */}
      <div className="px-4 pt-3 flex gap-1.5 overflow-x-auto pb-1 no-scrollbar">
        {[null, ...CURRENCIES].map((c) => (
          <button
            key={c ?? 'ALL_CURRENCY'}
            onClick={() => navigate(c ? `/app/wallet/history/${c}` : '/app/wallet/history')}
            className={`h-8 px-3 rounded-full text-xs font-medium whitespace-nowrap ${(!currency && c === null) || currency === c ? 'bg-primary text-white' : 'bg-gray-100 text-gray-600'}`}
          >
            {c ?? 'All'}
          </button>
        ))}
        {DIRECTION_FILTERS.map((d) => (
          <button
            key={`direction-${d}`}
            onClick={() => setDirection(d)}
            className={`h-8 px-3 rounded-full text-[11px] font-semibold border whitespace-nowrap transition-colors ${
              direction === d
                ? d === 'IN'  ? 'bg-success text-white border-success'
                : d === 'OUT' ? 'bg-danger text-white border-danger'
                :               'bg-gray-700 text-white border-gray-700'
                : 'bg-white text-gray-500 border-gray-200 hover:border-gray-400'
            }`}
          >
            {d === 'ALL' ? 'All' : d === 'IN' ? '↓ In' : '↑ Out'}
          </button>
        ))}
      </div>

      {/* Date filter presets */}
      <div className="px-4 pt-2">
        <select
          value={preset.label}
          onChange={(event) => {
            const next = DATE_PRESETS.find((item) => item.label === event.target.value)
            if (next) setPreset(next)
          }}
          className="h-8 w-full rounded-lg border border-gray-200 bg-white px-3 text-xs font-medium text-gray-700 outline-none focus:border-primary"
        >
          {DATE_PRESETS.map((p) => (
            <option key={p.label} value={p.label}>{p.label}</option>
          ))}
        </select>
      </div>

      {/* Custom date range inputs */}
      {preset.days === 'custom' && (
        <div className="px-4 pt-2 flex gap-2 items-center">
          <input
            type="date"
            value={customFrom}
            max={customTo || undefined}
            onChange={(e) => setCustomFrom(e.target.value)}
            className="flex-1 rounded-lg border border-gray-200 px-3 py-1.5 text-xs text-gray-700 outline-none focus:border-primary"
          />
          <input
            type="date"
            value={customTo}
            min={customFrom || undefined}
            onChange={(e) => setCustomTo(e.target.value)}
            className="flex-1 rounded-lg border border-gray-200 px-3 py-1.5 text-xs text-gray-700 outline-none focus:border-primary"
          />
        </div>
      )}

      <div className="px-4 py-3">
        {isLoading || walletLoading ? (
          <LoadingSpinner fullPage />
        ) : currency && !hasCurrencyWallet ? (
          <CurrencyBalanceCard
            currency={currency}
            promotional
            onClick={() => navigate(`/app/wallet/register/${currency}`)}
          />
        ) : txs.length === 0 ? (
          <EmptyState icon="📭" title="No transactions" description="No transactions match the selected filters." />
        ) : (
          <div className="space-y-0 divide-y divide-gray-50">
            {txs.map((tx) => {
              const isIn = tx.direction === 'IN' || tx.type?.includes('IN') || tx.type === 'TOPUP'
              return (
                <div key={tx.transactionId ?? tx.id} className="flex items-center gap-3 py-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-800 truncate">{tx.description ?? tx.type}</p>
                    <p className="text-xs text-gray-400">{formatDate(tx.createdAt ?? tx.timestamp)}</p>
                  </div>
                  <div className="text-right shrink-0">
                    <p className={`text-sm font-semibold ${isIn ? 'text-success' : 'text-danger'}`}>
                      {isIn ? '+' : '-'}{formatAmount(tx.amount, tx.currency)}
                    </p>
                    <Badge color={typeColor[tx.type] ?? 'gray'}>{tx.type}</Badge>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </PageLayout>
  )
}
