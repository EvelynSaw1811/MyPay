import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import ProfileSection from './ProfileSection'
import Badge from '../../../components/ui/Badge'
import Modal from '../../../components/ui/Modal'
import Input from '../../../components/ui/Input'
import Button from '../../../components/ui/Button'
import LoadingSpinner from '../../../components/ui/LoadingSpinner'
import { deleteAccount } from '../../../api/profile'
import { getWallet } from '../../../api/wallet'
import { listCollections } from '../../../api/collection'
import { getApiErrorMessage } from '../../../utils/apiError'
import { useAuth } from '../../../contexts/AuthContext'

function Row({ label, description, onClick, disabled, badge, danger }) {
  const isDisabled = disabled || !onClick
  return (
    <button
      type="button"
      onClick={isDisabled ? undefined : onClick}
      disabled={isDisabled}
      className={`w-full flex items-center justify-between py-3 text-left
        ${isDisabled ? 'opacity-60 cursor-not-allowed' : 'hover:bg-gray-50 active:bg-gray-100 transition-colors'}`}
    >
      <div className="min-w-0">
        <p className={`text-sm ${danger ? 'text-danger' : 'text-gray-800'}`}>{label}</p>
        {description && <p className="text-xs text-gray-400 mt-0.5">{description}</p>}
      </div>
      <div className="flex items-center gap-2 shrink-0">
        {badge && <Badge color="gray">{badge}</Badge>}
        {!isDisabled && <span className="text-gray-300 text-base">›</span>}
      </div>
    </button>
  )
}

// ── Step constants ────────────────────────────────────────────────────────────
const STEP_IDLE     = 'idle'
const STEP_CHECKING = 'checking'
const STEP_BLOCKED  = 'blocked'
const STEP_CONFIRM  = 'confirm'

/**
 * Support & account actions section — Help / Report issue / Delete account
 * Sign-out lives in the Security section.
 */
export default function SupportSection() {
  const navigate  = useNavigate()
  const { user, logout } = useAuth()

  const [step, setStep]             = useState(STEP_IDLE)
  const [blockReasons, setBlockReasons] = useState([])
  const [password, setPassword]     = useState('')
  const [deleteError, setDeleteError] = useState('')

  const deleteMut = useMutation({
    mutationFn: () => deleteAccount(user.userId, { password }),
    onSuccess: async () => {
      await logout()
      navigate('/login', { replace: true })
    },
    onError: (err) => setDeleteError(getApiErrorMessage(err, 'Failed to delete account')),
  })

  async function handleDeleteClick() {
    setStep(STEP_CHECKING)
    const reasons = []

    try {
      // 1. Check wallet balances
      const walletRes = await getWallet()
      const wallets = walletRes?.data?.wallets ?? walletRes?.data?.accounts ?? []
      const nonZeroWallets = wallets.filter(w =>
        w.walletStatus !== 'CLOSED' && (w.walletBalance ?? w.balance ?? 0) !== 0
      )
      if (nonZeroWallets.length > 0) {
        reasons.push(
          `You have non-zero wallet balance${nonZeroWallets.length > 1 ? 's' : ''}: ` +
          nonZeroWallets.map(w => `${w.currency} ${(w.walletBalance ?? w.balance ?? 0).toFixed(2)}`).join(', ') +
          '. Please withdraw or transfer before deleting.'
        )
      }

      // 2. Check collections — net balance must be zero for each
      const colRes = await listCollections()
      const collections = colRes?.data ?? []
      const unsettled = collections.filter(c => (c.myNetBalance ?? 0) !== 0)
      if (unsettled.length > 0) {
        reasons.push(
          `You have unsettled balances in ${unsettled.length} collection${unsettled.length > 1 ? 's' : ''}. ` +
          'Please settle all debts before deleting.'
        )
      }
    } catch {
      reasons.push('Could not verify your wallet and collection status. Please try again.')
    }

    if (reasons.length > 0) {
      setBlockReasons(reasons)
      setStep(STEP_BLOCKED)
    } else {
      setPassword('')
      setDeleteError('')
      setStep(STEP_CONFIRM)
    }
  }

  function closeModal() {
    setStep(STEP_IDLE)
    setBlockReasons([])
    setPassword('')
    setDeleteError('')
  }

  const modalOpen = step !== STEP_IDLE

  return (
    <>
      <ProfileSection title="Support & Account" padding="px-4">
        <div className="divide-y divide-gray-100">
          <Row
            label="Help center"
            description="FAQ, guides, and troubleshooting"
            badge="Coming soon"
          />
          <Row
            label="Report an issue"
            description="Tell us what went wrong"
            badge="Coming soon"
          />
          <Row
            label="Cancel a currency wallet"
            description="Close an unused wallet with zero balance"
            onClick={() => navigate('/app/wallet/cancel')}
            danger
          />
          <Row
            label="Delete account"
            description="Permanently remove your MyPay account"
            onClick={handleDeleteClick}
            danger
          />
        </div>
      </ProfileSection>

      {/* ── Checking modal ── */}
      <Modal open={step === STEP_CHECKING} onClose={closeModal} title="Checking your account">
        <div className="flex flex-col items-center py-4 gap-3">
          <LoadingSpinner />
          <p className="text-sm text-gray-500">Verifying wallet balances and collections…</p>
        </div>
      </Modal>

      {/* ── Blocked modal ── */}
      <Modal open={step === STEP_BLOCKED} onClose={closeModal} title="Cannot delete account">
        <div className="space-y-4">
          <p className="text-sm text-gray-700">
            Please resolve the following before deleting your account:
          </p>
          <ul className="space-y-2">
            {blockReasons.map((r, i) => (
              <li key={i} className="flex gap-2 text-sm text-danger">
                <span className="shrink-0">•</span>
                <span>{r}</span>
              </li>
            ))}
          </ul>
          <Button className="w-full" variant="secondary" onClick={closeModal}>Got it</Button>
        </div>
      </Modal>

      {/* ── Confirm modal ── */}
      <Modal open={step === STEP_CONFIRM} onClose={closeModal} title="Delete account">
        <div className="space-y-4">
          <div className="rounded-xl border border-red-200 bg-red-50 p-3">
            <p className="text-xs font-semibold text-red-700 uppercase tracking-wide mb-1">⚠ Warning</p>
            <p className="text-sm text-red-800">
              This action is <span className="font-semibold">permanent and cannot be undone</span>. All your data will be
              archived and your account will be closed immediately.
            </p>
          </div>

          <Input
            label="Enter your password to confirm"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoFocus
          />

          {deleteError && <p className="text-sm text-danger">{deleteError}</p>}

          <div className="grid grid-cols-2 gap-2">
            <Button variant="secondary" onClick={closeModal}>Cancel</Button>
            <Button
              danger
              disabled={!password}
              loading={deleteMut.isPending}
              onClick={() => deleteMut.mutate()}
            >
              Delete account
            </Button>
          </div>
        </div>
      </Modal>
    </>
  )
}
