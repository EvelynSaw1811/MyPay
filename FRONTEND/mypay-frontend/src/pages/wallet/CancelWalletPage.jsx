import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import PageLayout from '../../components/layout/PageLayout'
import Button from '../../components/ui/Button'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'
import Modal from '../../components/ui/Modal'
import { getWallet, closeWallet } from '../../api/wallet'
import { formatAmount } from '../../utils/currency'
import { getApiErrorMessage } from '../../utils/apiError'

const CURRENCY_LABELS = { MYR: 'Malaysian Ringgit', SGD: 'Singapore Dollar', USD: 'US Dollar' }
const CURRENCY_BG     = { MYR: 'bg-primary', SGD: 'bg-gray-800', USD: 'bg-gray-950' }

export default function CancelWalletPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [selected, setSelected]     = useState(null)   // wallet chosen for cancellation
  const [confirmed, setConfirmed]   = useState(false)
  const [showModal, setShowModal]   = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['wallet'],
    queryFn: () => getWallet().then((r) => r.data),
  })

  const wallets = (data?.wallets ?? data?.accounts ?? []).filter((w) => w.status !== 'CLOSED')

  const cancelMutation = useMutation({
    mutationFn: () => closeWallet(selected.currency),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['wallet'] })
      setShowModal(false)
      setSelected(null)
      setConfirmed(false)
      navigate('/app/wallet', { replace: true })
    },
  })

  function openConfirm(wallet) {
    setSelected(wallet)
    setConfirmed(false)
    setShowModal(true)
  }

  if (isLoading) {
    return <PageLayout title="Cancel Wallet" back backTo="/app/profile"><LoadingSpinner fullPage /></PageLayout>
  }

  return (
    <PageLayout title="Cancel Wallet" back backTo="/app/profile">
      <div className="px-4 py-5 space-y-5">

        <div className="rounded-xl border border-yellow-200 bg-yellow-50 p-4">
          <p className="text-xs font-semibold text-yellow-700 uppercase tracking-wide mb-1">⚠ Important</p>
          <p className="text-sm text-yellow-800">
            Cancelling a wallet is permanent. You must have a <span className="font-semibold">zero balance</span> before
            a wallet can be closed. Your MYR wallet cannot be cancelled as it is your primary wallet.
          </p>
        </div>

        {wallets.length === 0 ? (
          <EmptyState title="No active wallets" description="You have no wallets that can be cancelled." />
        ) : (
          <div className="space-y-3">
            <p className="text-xs text-gray-400 uppercase tracking-wide">Select a wallet to cancel</p>
            {wallets.map((w) => {
              const isMYR    = w.currency === 'MYR'
              const hasBalance = (w.walletBalance ?? w.balance ?? 0) !== 0
              const canCancel  = !isMYR && !hasBalance
              const bg = CURRENCY_BG[w.currency] ?? 'bg-gray-700'

              return (
                <div
                  key={w.currency}
                  className={`${bg} rounded-xl p-4 text-white ${canCancel ? 'opacity-100' : 'opacity-60'}`}
                >
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <p className="text-[10px] font-semibold uppercase tracking-widest text-white/60">
                        {CURRENCY_LABELS[w.currency] ?? w.currency}
                      </p>
                      <p className="text-lg font-bold mt-0.5">
                        {formatAmount(w.walletBalance ?? w.balance ?? 0, w.currency)}
                      </p>
                      {isMYR && (
                        <p className="text-[10px] text-white/60 mt-1">Primary wallet — cannot be cancelled</p>
                      )}
                      {!isMYR && hasBalance && (
                        <p className="text-[10px] text-white/60 mt-1">Balance must be zero to cancel</p>
                      )}
                    </div>
                    {canCancel && (
                      <Button
                        size="sm"
                        variant="secondary"
                        className="shrink-0 bg-white/10 text-white border-white/20 hover:bg-white/20"
                        onClick={() => openConfirm(w)}
                      >
                        Cancel wallet
                      </Button>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* Confirmation modal */}
      <Modal
        open={showModal}
        onClose={() => { setShowModal(false); setSelected(null) }}
        title={`Cancel ${selected?.currency} Wallet`}
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-700">
            Are you sure you want to permanently cancel your{' '}
            <span className="font-semibold">{selected?.currency}</span> ({CURRENCY_LABELS[selected?.currency]}) wallet?
            This action <span className="font-semibold text-danger">cannot be undone</span>.
          </p>

          <label className="flex items-start gap-3 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={confirmed}
              onChange={(e) => setConfirmed(e.target.checked)}
              className="mt-0.5 h-4 w-4 rounded border-gray-300 accent-primary"
            />
            <span className="text-sm text-gray-700">
              I understand this will permanently close my {selected?.currency} wallet.
            </span>
          </label>

          {cancelMutation.error && (
            <p className="text-sm text-danger">
              {getApiErrorMessage(cancelMutation.error, 'Failed to cancel wallet')}
            </p>
          )}

          <div className="grid grid-cols-2 gap-2">
            <Button variant="secondary" onClick={() => { setShowModal(false); setSelected(null) }}>
              Go back
            </Button>
            <Button
              danger
              disabled={!confirmed}
              loading={cancelMutation.isPending}
              onClick={() => cancelMutation.mutate()}
            >
              Confirm cancel
            </Button>
          </div>
        </div>
      </Modal>
    </PageLayout>
  )
}
