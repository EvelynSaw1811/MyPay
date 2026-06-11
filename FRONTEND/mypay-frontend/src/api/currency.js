import client from './client'

const FALLBACK_USD_RATES = { MYR: 4.47, SGD: 1.35, USD: 1 }

function fallbackRate(from, to) {
  if (from === to) return 1
  return FALLBACK_USD_RATES[to] / FALLBACK_USD_RATES[from]
}

export const currencyApi = {
  list:    ()                  => client.get('/api/currency/currencies').then(r => r.data.data),
  rates:   (base = 'MYR')      => client.get('/api/currency/rates', { params: { base } }).then(r => r.data.data),
  rate:    (from, to)           => client.get(`/api/currency/rates/${from}/${to}`).then(r => r.data.data),
  convert: (from, to, amount)   => client.get('/api/currency/convert', { params: { from, to, amount } }).then(r => r.data.data),
}

export const getRates = (base = 'MYR') => currencyApi.rates(base)
  .then(data => ({ data }))
  .catch(() => ({
    data: {
      baseCurrency: base,
      rates: Object.fromEntries(
        Object.keys(FALLBACK_USD_RATES)
          .filter((currency) => currency !== base)
          .map((currency) => [currency, fallbackRate(base, currency)]),
      ),
      fallback: true,
    },
  }))
export const convertCurrency = ({ from, to, amount }) =>
  currencyApi.convert(from, to, amount)
    .then(data => ({ data }))
    .catch(() => {
      const rate = fallbackRate(from, to)
      return {
        data: {
          fromCurrency: from,
          toCurrency: to,
          originalAmount: amount,
          convertedAmount: Number(amount) * rate,
          rate,
          fallback: true,
        },
      }
    })
