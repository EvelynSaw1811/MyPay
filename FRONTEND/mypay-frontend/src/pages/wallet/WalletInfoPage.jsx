import { useQuery } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import MaskedValue from '../../components/ui/MaskedValue'
import { currencySymbol, formatAmount } from '../../utils/currency'
import { formatDate } from '../../utils/date'
import { getWallet } from '../../api/wallet'
import { getHistoryByCurrency } from '../../api/transaction'

const CURRENCY_LABELS = { MYR: 'Malaysian Ringgit', SGD: 'Singapore Dollar', USD: 'US Dollar' }
const CURRENCY_BG     = { MYR: 'bg-primary', SGD: 'bg-gray-800', USD: 'bg-gray-950' }

const typeColor = {
  TOPUP: 'green', SETTLEMENT_IN: 'green', SETTLEMENT_OUT: 'red',
  TRANSFER_IN: 'blue', TRANSFER_OUT: 'red',
}

export default function WalletInfoPage() {
  const { currency } = useParams()
  const navigate = useNavigate()

  const { data: walletData, isLoading: walletLoading } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })
  const { data: histData, isLoading: histLoading } = useQuery({
    queryKey: ['txHistory', currency],
    queryFn: () => getHistoryByCurrency(currency),
  })

  const wallets = walletData?.wallets ?? walletData?.accounts ?? []
  const wallet  = wallets.find((w) => w.currency === currency)
  const recentTxs = (histData?.data ?? []).slice(0, 10)

  if (walletLoading) {
    return <PageLayout title={`${currency} Wallet`} back><LoadingSpinner fullPage /></PageLayout>
  }

  if (!wallet) {
    return (
      <PageLayout title={`${currency} Wallet`} back>
        <div className="px-4 py-5">
          <EmptyState
            title="Wallet not found"
            description={`You don't have a ${currency} wallet yet.`}
          />
          <Button className="w-full mt-4" onClick={() => navigate(`/app/wallet/register/${currency}`)}>
            Click to register for {currency} wallet
          </Button>
        </div>
      </PageLayout>
    )
  }

  if (wallet.status === 'CLOSED') {
    return (
      <PageLayout title={`${currency} Wallet`} back>
        <div className="px-4 py-5">
          <EmptyState
            icon="🔒"
            title="Wallet closed"
            description={`Your ${currency} wallet has been cancelled and is no longer active.`}
          />
          <Button className="w-full mt-4" onClick={() => navigate('/app/wallet')}>
            Back to wallet
          </Button>
        </div>
      </PageLayout>
    )
  }

  const bg = CURRENCY_BG[currency] ?? 'bg-gray-700'

  return (
    <PageLayout title={`${currency} Wallet`} back>
      <div className="px-4 py-5 space-y-5">

        {/* Balance card */}
        <div className={`${bg} rounded-2xl px-5 py-6 text-white`}>
          <p className="text-[10px] font-semibold uppercase tracking-widest text-white/60 mb-1">
            {CURRENCY_LABELS[currency] ?? currency}
          </p>
          <p className="text-3xl font-bold tracking-tight">
            <MaskedValue
              value={formatAmount(wallet.balance ?? 0, currency)}
              mask={`${currencySymbol(currency)} ********`}
              className="text-white"
              containerClassName="gap-2.5"
              buttonLabel={`Toggle ${currency} balance`}
              persistKey={`balance-${currency}`}
            />
          </p>
          <div className="mt-3 flex items-center gap-2">
            <Badge color="green" className="bg-white/15 text-white text-[9px] px-1.5 py-0">
              {wallet.status ?? 'Active'}
            </Badge>
            <span className="text-[10px] text-white/50">
              Wallet ID: <MaskedValue value={wallet.walletId} mask="••••••••" className="text-white/70" />
            </span>
          </div>
        </div>

        {/* Action buttons */}
        <div className="grid grid-cols-2 gap-2">
          <Button variant="secondary" onClick={() => navigate('/app/wallet/topup')}>
            Top up
          </Button>
          <Button variant="secondary" onClick={() => navigate(`/app/wallet/history/${currency}`)}>
            View history
          </Button>
        </div>

        {/* Recent transactions */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <p className="text-xs text-gray-400 uppercase tracking-wide">Recent transactions</p>
            <button
              onClick={() => navigate(`/app/wallet/history/${currency}`)}
              className="text-xs font-semibold text-primary hover:underline"
            >
              View all
            </button>
          </div>

          {histLoading ? (
            <LoadingSpinner />
          ) : recentTxs.length === 0 ? (
            <EmptyState icon="📭" title="No transactions yet" description="Transactions will appear here once you start using this wallet." />
          ) : (
            <div className="space-y-0 divide-y divide-gray-50">
              {recentTxs.map((tx) => {
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
                      <Badge color={typeColor[tx.type] ?? 'gray'} className="text-[9px] px-1.5 py-0">
                        {tx.type}
                      </Badge>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </PageLayout>
  )
}
