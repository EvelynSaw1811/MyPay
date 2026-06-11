import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import CurrencyBalanceCard from '../../components/wallet/CurrencyBalanceCard'
import { getRegistrationStatus, openWallet } from '../../api/wallet'
import { getApiErrorMessage } from '../../utils/apiError'

const TERMS = [
  'By opening this wallet, you agree to hold and manage funds in the selected currency.',
  'You are responsible for all transactions made through this wallet.',
  'MyPay reserves the right to suspend or close a wallet in the event of suspected fraudulent activity.',
  'Cross-currency transfers are subject to applicable exchange rates and fees at the time of the transaction.',
  'Wallet balances do not accrue interest and are not covered by any deposit insurance scheme.',
  'You may close this wallet at any time provided the balance is zero.',
]

export default function RegisterWalletPage() {
  const { currency } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [agreed, setAgreed] = useState(false)
  const [registered, setRegistered] = useState(false)

  const { data, isLoading, error } = useQuery({
    queryKey: ['wallet-registration', currency],
    queryFn: () => getRegistrationStatus(currency),
  })
  const status = data?.data

  const registerMutation = useMutation({
    mutationFn: () => openWallet({ currency }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['wallet'] })
      queryClient.invalidateQueries({ queryKey: ['wallet-registration', currency] })
      setRegistered(true)
    },
  })

  useEffect(() => {
    if (status?.walletExists) {
      navigate(`/app/wallet/info/${currency}`, { replace: true })
    }
  }, [currency, navigate, status?.walletExists])

  if (isLoading) {
    return <PageLayout title="Register Wallet" back backTo="/app/wallet"><LoadingSpinner fullPage /></PageLayout>
  }

  if (status?.walletExists) return null

  if (registered) {
    return (
      <PageLayout title={`${currency} Wallet`} back backTo="/app/wallet">
        <div className="px-4 py-16 flex flex-col items-center gap-5 text-center">
          <div className="text-5xl">🎉</div>
          <div>
            <p className="text-xl font-bold text-gray-900">{currency} Wallet Registered!</p>
            <p className="text-sm text-gray-500 mt-1.5">
              Your {currency} wallet is now active and ready to use.
            </p>
          </div>
          <Button className="w-full mt-2" onClick={() => navigate(`/app/wallet/info/${currency}`, { replace: true })}>
            Go to my {currency} wallet
          </Button>
        </div>
      </PageLayout>
    )
  }

  return (
    <PageLayout title={`Register ${currency} Wallet`} back backTo="/app/wallet">
      <div className="px-4 py-5 space-y-5">
        <CurrencyBalanceCard currency={currency} promotional />

        <p className="text-sm text-gray-600">
          Open a <span className="font-semibold">{currency}</span> wallet to receive, spend,
          and settle in this currency.
        </p>

        {/* Terms & Conditions */}
        <div className="rounded-xl border border-gray-200 bg-gray-50 p-4 space-y-2">
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-500 mb-3">
            Terms &amp; Conditions
          </p>
          <ul className="space-y-2">
            {TERMS.map((t, i) => (
              <li key={i} className="flex gap-2 text-xs text-gray-600">
                <span className="shrink-0 mt-0.5 text-gray-400">{i + 1}.</span>
                <span>{t}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* I agree checkbox */}
        <label className="flex items-start gap-3 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
            className="mt-0.5 h-4 w-4 rounded border-gray-300 accent-primary"
          />
          <span className="text-sm text-gray-700">
            I have read and agree to the Terms &amp; Conditions for opening a{' '}
            <span className="font-semibold">{currency}</span> wallet.
          </span>
        </label>

        {(error || registerMutation.error) && (
          <p className="text-sm text-danger">
            {getApiErrorMessage(registerMutation.error ?? error, 'Unable to register wallet')}
          </p>
        )}

        <Button
          className="w-full"
          loading={registerMutation.isPending}
          disabled={!agreed || !status?.canRegister}
          onClick={() => registerMutation.mutate()}
        >
          Register {currency} wallet
        </Button>
      </div>
    </PageLayout>
  )
}
