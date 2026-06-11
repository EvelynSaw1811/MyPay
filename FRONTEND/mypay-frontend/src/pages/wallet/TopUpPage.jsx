import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import PageLayout from '../../components/layout/PageLayout'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Button from '../../components/ui/Button'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import { getWallet, topUp } from '../../api/wallet'
import { getApiErrorMessage } from '../../utils/apiError'

const CURRENCY_NAMES = { MYR: 'Malaysian Ringgit', SGD: 'Singapore Dollar', USD: 'US Dollar' }

export default function TopUpPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [currency, setCurrency] = useState('')
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const walletQuery = useQuery({
    queryKey: ['wallet'],
    queryFn: getWallet,
  })

  const account = walletQuery.data?.data ?? walletQuery.data
  const wallets = (account?.wallets ?? account?.accounts ?? [])
    .filter((w) => (w.status ?? w.walletStatus ?? 'ACTIVE').toUpperCase() !== 'CLOSED')
  const activeCurrencies = [...new Set(wallets.map((w) => w.currency).filter(Boolean))]

  useEffect(() => {
    if (!currency && activeCurrencies.length > 0) {
      setCurrency(activeCurrencies[0])
    }
  }, [activeCurrencies, currency])

  async function handleSubmit(e) {
    e.preventDefault()
    if (!amount || parseFloat(amount) <= 0) return
    setError('')
    setLoading(true)
    try {
      await topUp({ currency, amount: parseFloat(amount) })
      qc.invalidateQueries({ queryKey: ['wallet'] })
      setSuccess(true)
      setTimeout(() => navigate('/app/wallet'), 1500)
    } catch (err) {
      setError(getApiErrorMessage(err, 'Top up failed'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <PageLayout title="Top up" back>
      <div className="px-4 py-5">
        {success ? (
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <div className="text-5xl">✅</div>
            <p className="font-semibold text-gray-900">Top up successful!</p>
          </div>
        ) : walletQuery.isLoading ? (
          <LoadingSpinner fullPage />
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <Select
              label="Currency"
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              options={activeCurrencies.map((c) => ({ value: c, label: `${c} - ${CURRENCY_NAMES[c] ?? c}` }))}
            />
            <Input
              label="Amount"
              type="number"
              min="1"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
            />
            {error && <p className="text-sm text-danger">{error}</p>}
            <Button type="submit" className="w-full" loading={loading} disabled={!currency || !activeCurrencies.includes(currency)}>
              Top up {currency}
            </Button>
          </form>
        )}
      </div>
    </PageLayout>
  )
}
