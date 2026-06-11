import Badge from '../ui/Badge'
import MaskedValue from '../ui/MaskedValue'
import { currencySymbol, formatAmount } from '../../utils/currency'

const config = {
  MYR: { bg: 'bg-primary',    label: 'Malaysian Ringgit' },
  SGD: { bg: 'bg-gray-800',   label: 'Singapore Dollar' },
  USD: { bg: 'bg-gray-950',   label: 'US Dollar' },
}

export default function CurrencyBalanceCard({
  currency,
  balance,
  status = 'ACTIVE',
  walletId,
  promotional = false,
  onClick,
}) {
  const { bg, label } = config[currency] ?? { bg: 'bg-gray-700', label: currency }
  const symbol = currencySymbol(currency)
  const normalizedStatus = (status || 'ACTIVE').toUpperCase()
  const statusLabel = normalizedStatus.charAt(0) + normalizedStatus.slice(1).toLowerCase()
  const labelClass = 'text-[10px] font-semibold uppercase tracking-widest text-white/50'

  return (
    <div
      onClick={onClick}
      className={`${bg} rounded-xl p-5 text-white ${onClick ? 'cursor-pointer active:opacity-90 transition-opacity' : ''}`}
    >
      {promotional ? (
        <>
          <div className="flex items-start gap-2">
            <p className={`min-w-0 ${labelClass}`}>{label}</p>
            <p className={`shrink-0 ${labelClass}`}>{currency}</p>
          </div>
          {onClick && (
            <div className="mt-3 text-right text-lg font-semibold tracking-tight">Tap to get started</div>
          )}
          {onClick && (
            <p className="text-[10px] text-white/40 mt-2 text-right">
              Add this wallet for overseas spending and settlements
            </p>
          )}
        </>
      ) : (
        <>
          <div className="min-w-0">
            <div className="flex items-start gap-2">
              <p className={`min-w-0 ${labelClass}`}>{label}</p>
              <p className={`shrink-0 ${labelClass}`}>{currency}</p>
            </div>
            <p className="mt-1 text-[10px] text-white/50">
              Wallet ID <MaskedValue value={walletId} mask="********" className="text-white/70" />
            </p>
          </div>
          <div className="mt-3 text-right text-xl font-bold tracking-tight">
            <MaskedValue
              value={formatAmount(balance, currency)}
              mask={`${symbol} ********`}
              className="text-white"
              containerClassName="gap-2.5"
              buttonLabel={`Toggle ${symbol} balance`}
              persistKey={`balance-${currency}`}
            />
          </div>
          <div className="flex items-center justify-between mt-2">
            <Badge
              color={normalizedStatus === 'ACTIVE' ? 'green' : 'gray'}
              className="bg-white/15 text-white text-[9px] px-1.5 py-0"
            >
              {statusLabel}
            </Badge>
            {onClick && (
              <p className="text-[10px] text-white/40">Tap to view history</p>
            )}
          </div>
        </>
      )}
    </div>
  )
}
