import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import ProfileSection from './ProfileSection'
import Badge from '../../../components/ui/Badge'
import Modal from '../../../components/ui/Modal'
import Input from '../../../components/ui/Input'
import Button from '../../../components/ui/Button'
import { changePassword } from '../../../api/profile'
import { getApiErrorMessage } from '../../../utils/apiError'

function ActionRow({ label, description, onClick, disabled, badge }) {
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
        <p className="text-sm text-gray-800">{label}</p>
        {description && <p className="text-xs text-gray-400 mt-0.5">{description}</p>}
      </div>
      <div className="flex items-center gap-2 shrink-0">
        {badge && <Badge color="gray">{badge}</Badge>}
        {!isDisabled && <span className="text-gray-300 text-base">›</span>}
      </div>
    </button>
  )
}

/**
 * Security section — change password / change PIN / last login / sign out.
 *
 * Accepts:
 *   onLogout         — called when Sign out is tapped
 *   userId           — current user's ID (needed for the change-password API call)
 */
export default function SecuritySection({ onLogout, userId, lastLogin }) {
  const [showModal, setShowModal]     = useState(false)
  const [currentPwd, setCurrentPwd]   = useState('')
  const [newPwd, setNewPwd]           = useState('')
  const [confirmPwd, setConfirmPwd]   = useState('')
  const [localError, setLocalError]   = useState('')
  const [success, setSuccess]         = useState(false)

  const changePwdMut = useMutation({
    mutationFn: () => changePassword(userId, { currentPassword: currentPwd, newPassword: newPwd }),
    onSuccess: () => {
      setSuccess(true)
      setCurrentPwd(''); setNewPwd(''); setConfirmPwd(''); setLocalError('')
      setTimeout(() => { setShowModal(false); setSuccess(false) }, 1500)
    },
    onError: (err) => setLocalError(getApiErrorMessage(err, 'Failed to change password')),
  })

  function openModal() {
    setCurrentPwd(''); setNewPwd(''); setConfirmPwd(''); setLocalError(''); setSuccess(false)
    setShowModal(true)
  }

  function handleSubmit() {
    if (!currentPwd || !newPwd || !confirmPwd) { setLocalError('All fields are required.'); return }
    if (newPwd.length < 8)                      { setLocalError('New password must be at least 8 characters.'); return }
    if (newPwd !== confirmPwd)                  { setLocalError('New passwords do not match.'); return }
    setLocalError('')
    changePwdMut.mutate()
  }

  return (
    <>
      <ProfileSection title="Security" padding="px-4">
        <div className="divide-y divide-gray-100">
          <ActionRow
            label="Change password"
            description="Set a new password for your account"
            onClick={openModal}
          />
          <ActionRow
            label="Change PIN"
            description="Update your transaction PIN"
            badge="Coming soon"
          />
          <ActionRow
            label="Last login"
            description={
              lastLogin
                ? new Date(lastLogin).toLocaleString(undefined, {
                    dateStyle: 'medium',
                    timeStyle: 'short',
                  })
                : 'Not available'
            }
            badge="Coming soon"
          />
          <ActionRow
            label="Sign out"
            description="End your session on this device"
            onClick={onLogout}
          />
        </div>
      </ProfileSection>

      <Modal open={showModal} onClose={() => setShowModal(false)} title="Change Password">
        <div className="space-y-3">
          {success ? (
            <p className="text-sm text-success font-medium text-center py-2">Password changed successfully ✓</p>
          ) : (
            <>
              <Input
                label="Current password"
                type="password"
                value={currentPwd}
                onChange={(e) => setCurrentPwd(e.target.value)}
                autoFocus
              />
              <Input
                label="New password"
                type="password"
                value={newPwd}
                onChange={(e) => setNewPwd(e.target.value)}
                placeholder="Min. 8 characters"
              />
              <Input
                label="Confirm new password"
                type="password"
                value={confirmPwd}
                onChange={(e) => setConfirmPwd(e.target.value)}
              />
              {localError && <p className="text-sm text-danger">{localError}</p>}
              {changePwdMut.error && !localError && (
                <p className="text-sm text-danger">{getApiErrorMessage(changePwdMut.error, 'Failed to change password')}</p>
              )}
              <div className="grid grid-cols-2 gap-2 pt-1">
                <Button variant="secondary" onClick={() => setShowModal(false)}>Cancel</Button>
                <Button
                  loading={changePwdMut.isPending}
                  disabled={!currentPwd || !newPwd || !confirmPwd}
                  onClick={handleSubmit}
                >
                  Update
                </Button>
              </div>
            </>
          )}
        </div>
      </Modal>
    </>
  )
}
