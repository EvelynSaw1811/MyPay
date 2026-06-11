import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { getRates, convertCurrency } from '../../api/currency'
import { CURRENCIES } from '../../utils/currency'

export default function RatesPage() {
  const [base, setBase] = useState('MYR')
  const [from, setFrom] = useState('MYR')
  const [to, setTo] = useState('SGD')
  const [amount, setAmount] = useState('100')

  const { data: ratesData, isLoading } = useQuery({
    queryKey: ['rates', base],
    queryFn: () => getRates(base),
    staleTime: 60_000,
  })

  const { data: convertData } = useQuery({
    queryKey: ['convert', from, to, amount],
    queryFn: () => convertCurrency({ from, to, amount: parseFloat(amount) }),
    enabled: !!amount && parseFloat(amount) > 0,
    staleTime: 15_000,
  })

  const rates = ratesData?.data?.rates ?? {}
  const usingFallbackRates = ratesData?.data?.fallback || convertData?.data?.fallback

  return (
    <PageLayout title="Rates & Converter" back>
      <div className="px-4 py-5 space-y-6">
        {/* Exchange rates table */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <p className="text-sm font-semibold text-gray-800">Exchange rates</p>
            <div className="w-24">
              <Select
                value={base}
                onChange={(e) => setBase(e.target.value)}
                options={CURRENCIES.map((c) => ({ value: c, label: c }))}
              />
            </div>
          </div>
          {isLoading ? (
            <LoadingSpinner fullPage />
          ) : (
            <div className="space-y-2">
              {usingFallbackRates && (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-700">
                  Live rates are temporarily unavailable, showing fallback rates.
                </p>
              )}
              {Object.entries(rates)
                .filter(([cur]) => cur !== base)
                .map(([cur, rate]) => (
                  <div key={cur} className="flex justify-between py-2 border-b border-gray-50">
                    <span className="text-sm text-gray-700">1 {base} →</span>
                    <span className="text-sm font-semibold text-gray-900">{Number(rate).toFixed(4)} {cur}</span>
                  </div>
                ))}
            </div>
          )}
        </div>

        {/* Converter */}
        <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
          <p className="text-sm font-semibold text-gray-800">Currency converter</p>
          <div className="grid grid-cols-2 gap-2">
            <Select
              label="From"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              options={CURRENCIES.map((c) => ({ value: c, label: c }))}
            />
            <Select
              label="To"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              options={CURRENCIES.map((c) => ({ value: c, label: c }))}
            />
          </div>
          <Input
            label="Amount"
            type="number"
            min="0"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
          />
          {convertData?.data && (
            <div className="bg-white rounded-xl p-3 text-center">
              <p className="text-xs text-gray-400 mb-1">{amount} {from} =</p>
              <p className="text-xl font-bold text-primary">
                {Number(convertData.data.convertedAmount).toFixed(4)} {to}
              </p>
              <p className="text-xs text-gray-400 mt-1">Rate: {Number(convertData.data.rate).toFixed(6)}</p>
            </div>
          )}
        </div>
      </div>
    </PageLayout>
  )
}
