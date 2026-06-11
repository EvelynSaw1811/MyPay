const CURRENCY_SYMBOLS = { MYR: 'RM', SGD: 'SG$', USD: 'US$' }
const CURRENCY_FLAGS   = { MYR: '🇲🇾', SGD: '🇸🇬', USD: '🇺🇸' }

export function currencySymbol(currency = 'MYR') {
  return CURRENCY_SYMBOLS[currency] ?? currency
}

export function formatAmount(value, currency = 'MYR') {
  const num = Number(value ?? 0)
  const symbol = currencySymbol(currency)
  return `${symbol} ${num.toFixed(2)}`
}

export function currencyFlag(currency) {
  return CURRENCY_FLAGS[currency] ?? '💱'
}

export const CURRENCIES = ['MYR', 'SGD', 'USD']
