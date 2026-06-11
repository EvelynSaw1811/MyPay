import { useNavigate } from 'react-router-dom'
import ProfileSection from './ProfileSection'
import Badge from '../../../components/ui/Badge'
import EmptyState from '../../../components/ui/EmptyState'
import Button from '../../../components/ui/Button'
import MaskedValue from '../../../components/ui/MaskedValue'
import { CURRENCIES, formatAmount } from '../../../utils/currency'

const CURRENCY_LABELS = {
  MYR: 'Malaysian Ringgit',
  SGD: 'Singapore Dollar',
  USD: 'US Dollar',
}

const CURRENCY_MASK = {
  MYR: 'RM ••••',
  SGD: 'S$ ••••',
  USD: 'US$ ••••',
}

export default function WalletSummarySection({
  wallet,
  loading,
  error,
  onRetry,
  onOpenWallet,
  openingCurrency,
  openError,
}) {
  const navigate = useNavigate()
  const wallets = wallet?.wallets ?? wallet?.accounts ?? []
  const walletByCurrency = Object.fromEntries(wallets.map((item) => [item.currency, item]))

  return (
    <ProfileSection
      title="Wallet Summary"
      action={
        <button
          onClick={() => navigate('/app/wallet')}
          className="min-h-9 px-2 -mr-2 text-primary font-medium hover:underline"
        >
          View wallet
        </button>
      }
      padding="p-3"
    >
      {loading ? (
        <div className="py-8 text-center text-xs text-gray-400">Loading balances...</div>
      ) : error ? (
        <div className="py-6 text-center">
          <p className="text-xs text-danger mb-2">Could not load wallet balances.</p>
          {onRetry && (
            <button onClick={onRetry} className="text-xs text-primary font-medium hover:underline">
              Retry
            </button>
          )}
        </div>
      ) : wallets.length === 0 ? (
        <EmptyState
          title="No account found"
          description="Your account will appear here once it's created."
        />
      ) : (
        <>
          {openError && <p className="px-1 pb-2 text-xs text-danger">{openError}</p>}
          <ul className="divide-y divide-gray-100">
            {CURRENCIES.map((cur) => {
              const openedWallet = walletByCurrency[cur]
              const isOpen = Boolean(openedWallet)
              return (
                <li key={cur} className="flex items-center justify-between gap-3 py-3 px-1">
                  {/* Left: currency code + full name + status badge inline */}
                  <div className="min-w-0 flex flex-wrap items-center gap-x-2 gap-y-1">
                    <p className="text-sm font-medium text-gray-900">
                      {cur}{' '}
                      <span className="text-xs text-gray-400 font-normal">
                        - {CURRENCY_LABELS[cur] ?? cur}
                      </span>
                    </p>
                    <Badge color={isOpen ? 'green' : 'gray'}>
                      {isOpen ? 'Active' : 'Not opened'}
                    </Badge>
                  </div>

                  {/* Right: balance (masked) or open/required affordance */}
                  <div className="shrink-0">
                    {isOpen ? (
                      <MaskedValue
                        value={formatAmount(openedWallet.balance ?? 0, cur)}
                        mask={CURRENCY_MASK[cur] ?? '••••'}
                        className="text-sm font-semibold text-gray-900 tabular-nums"
                        persistKey={`balance-${cur}`}
                      />
                    ) : cur === 'MYR' ? (
                      <span className="text-xs text-gray-400">Required</span>
                    ) : (
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        loading={openingCurrency === cur}
                        onClick={() => onOpenWallet?.(cur)}
                      >
                        Open
                      </Button>
                    )}
                  </div>
                </li>
              )
            })}
          </ul>
        </>
      )}
    </ProfileSection>
  )
}
