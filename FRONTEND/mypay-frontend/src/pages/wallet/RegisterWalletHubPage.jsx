import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { getWallet } from '../../api/wallet'
import { CURRENCIES } from '../../utils/currency'

const CURRENCY_LABELS = { MYR: 'Malaysian Ringgit', SGD: 'Singapore Dollar', USD: 'US Dollar' }
const CURRENCY_DESC   = {
  MYR: 'Your primary wallet for local transactions and settlements.',
  SGD: 'Spend, receive, and settle in Singapore Dollars.',
  USD: 'Hold US Dollars for overseas spending and settlements.',
}

export default function RegisterWalletHubPage() {
  const navigate = useNavigate()

  const { data, isLoading } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })

  const wallets = data?.wallets ?? data?.accounts ?? []
  const openedSet = new Set(wallets.map((w) => w.currency))
  const missing = CURRENCIES.filter((c) => !openedSet.has(c))

  if (isLoading) {
    return (
      <PageLayout title="Register Wallet" back backTo="/app/wallet">
        <LoadingSpinner fullPage />
      </PageLayout>
    )
  }

  return (
    <PageLayout title="Register Wallet" back backTo="/app/wallet">
      <div className="px-4 py-5 space-y-5">
        {missing.length === 0 ? (
          /* ── All wallets owned ── */
          <div className="flex flex-col items-center justify-center py-16 gap-4 text-center">
            <span className="text-5xl">🎉</span>
            <p className="text-lg font-semibold text-gray-800">You own all the currency wallets, enjoy!</p>
            <p className="text-sm text-gray-500">
              You have active wallets for all supported currencies: {CURRENCIES.join(', ')}.
            </p>
          </div>
        ) : (
          /* ── Show missing currencies to register ── */
          <>
            <p className="text-sm text-gray-600">
              Select a currency to open a new wallet. You currently own{' '}
              <span className="font-semibold">{wallets.length}</span> of{' '}
              <span className="font-semibold">{CURRENCIES.length}</span> available wallets.
            </p>

            <div className="space-y-3">
              {missing.map((cur) => (
                <button
                  key={cur}
                  onClick={() => navigate(`/app/wallet/register/${cur}`)}
                  className="w-full flex items-center justify-between rounded-xl border border-gray-200 bg-white p-4 text-left hover:border-primary hover:bg-blue-50 transition-colors"
                >
                  <div>
                    <p className="text-sm font-semibold text-gray-800">{cur}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{CURRENCY_LABELS[cur]}</p>
                    <p className="text-xs text-gray-500 mt-1">{CURRENCY_DESC[cur]}</p>
                  </div>
                  <span className="text-primary text-xl ml-4">›</span>
                </button>
              ))}
            </div>

            {/* Already owned wallets (reference) */}
            {wallets.length > 0 && (
              <div className="pt-2 border-t border-gray-100">
                <p className="text-xs text-gray-400 uppercase tracking-wide mb-2">Already owned</p>
                <div className="flex flex-wrap gap-2">
                  {wallets.map((w) => (
                    <span
                      key={w.currency}
                      className="px-3 py-1 rounded-full bg-green-100 text-green-700 text-xs font-semibold"
                    >
                      ✓ {w.currency}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </PageLayout>
  )
}
