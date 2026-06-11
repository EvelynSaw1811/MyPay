import { useEffect, useState } from 'react'
import ProfileSection from './ProfileSection'
import Modal from '../../../components/ui/Modal'
import Input from '../../../components/ui/Input'
import Button from '../../../components/ui/Button'

function Row({ label, value }) {
  return (
    <div className="flex items-start justify-between py-2.5">
      <p className="text-xs text-gray-400 uppercase tracking-wide">{label}</p>
      <p className="text-sm text-gray-800 text-right max-w-[60%] truncate">{value || '—'}</p>
    </div>
  )
}

/** Build a fresh form snapshot from the latest user prop. */
function snapshot(user) {
  const displayName = user?.userNickname ?? user?.name ?? user?.firstName ?? ''
  return {
    firstName:    user?.firstName    ?? '',
    lastName:     user?.lastName     ?? '',
    userNickname: displayName,
    email:        user?.email        ?? '',
    phone:        user?.phone        ?? '',
  }
}

function validate(form) {
  const errors = {}
  if (form.email && !/^\S+@\S+\.\S+$/.test(form.email)) {
    errors.email = 'Enter a valid email'
  }
  if (form.userNickname !== undefined && form.userNickname !== '' && form.userNickname.length > 100) {
    errors.userNickname = 'Nickname is too long'
  }
  if (form.firstName !== undefined && form.firstName.length > 100) {
    errors.firstName = 'First name is too long'
  }
  if (form.lastName !== undefined && form.lastName.length > 100) {
    errors.lastName = 'Last name is too long'
  }
  if (form.phone && !/^[+0-9 ()-]{6,20}$/.test(form.phone)) {
    errors.phone = 'Phone format is invalid'
  }
  return errors
}

/**
 * Personal Information section — read-only display + an "Edit" modal that
 * patches mutable fields via PUT /api/auth/users/{userId}.
 *
 * Note: invitationCode / userId / status / verification are NOT edited here.
 */
export default function PersonalInfoSection({ user, onSave, saving, saveError, saveSuccess }) {
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState(() => snapshot(user))
  const [errors, setErrors] = useState({})

  // Re-sync the form whenever the user prop changes (e.g. when the profile
  // query resolves after the first render, or after a successful save).
  // Skip while the modal is open so we never clobber in-progress edits.
  useEffect(() => {
    if (!open) setForm(snapshot(user))
  }, [user, open])

  function openEditor() {
    setForm(snapshot(user))
    setErrors({})
    setOpen(true)
  }

  function handleChange(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const v = validate(form)
    setErrors(v)
    if (Object.keys(v).length) return

    // Only send fields that actually changed.
    const diff = {}
    for (const key of ['firstName', 'lastName', 'userNickname', 'email', 'phone']) {
      const next = (form[key] ?? '').trim()
      const prev = (user?.[key] ?? '').trim()
      if (next !== prev) diff[key] = next
    }
    if (Object.keys(diff).length === 0) {
      setOpen(false)
      return
    }

    try {
      await onSave(diff)
      setOpen(false)
    } catch {
      // saveError state is owned by the parent and surfaces below.
    }
  }

  return (
    <>
      <ProfileSection
        title="Personal Information"
        action={
          <button onClick={openEditor} className="min-h-9 px-2 -mr-2 text-primary font-medium hover:underline">
            Edit
          </button>
        }
      >
        <div className="divide-y divide-gray-100">
          {/* Display name falls back to first name if no nickname is set —
              avoids showing a bare em-dash for users like Ivy who legitimately
              have a null nickname, or while the profile query is still loading. */}
          <Row label="Display name" value={user?.userNickname || user?.firstName} />
          <Row label="First name"   value={user?.firstName} />
          <Row label="Last name"    value={user?.lastName} />
          <Row label="Email"        value={user?.email} />
          <Row label="Phone"        value={user?.phone} />
        </div>
        {saveSuccess && (
          <p className="mt-3 text-xs text-green-600">{saveSuccess}</p>
        )}
      </ProfileSection>

      <Modal open={open} onClose={() => setOpen(false)} title="Edit profile">
        <form onSubmit={handleSubmit} className="space-y-3">
          <Input
            label="Display name"
            value={form.userNickname}
            onChange={handleChange('userNickname')}
            error={errors.userNickname}
            placeholder="How others see you"
          />
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="First name"
              value={form.firstName}
              onChange={handleChange('firstName')}
              error={errors.firstName}
            />
            <Input
              label="Last name"
              value={form.lastName}
              onChange={handleChange('lastName')}
              error={errors.lastName}
            />
          </div>
          <Input
            label="Email"
            type="email"
            value={form.email}
            onChange={handleChange('email')}
            error={errors.email}
            placeholder="you@example.com"
          />
          <Input
            label="Phone"
            value={form.phone}
            onChange={handleChange('phone')}
            error={errors.phone}
            placeholder="+60 12 345 6789"
          />

          {saveError && (
            <p className="text-xs text-danger">{saveError}</p>
          )}

          <div className="flex gap-2 pt-1">
            <Button variant="secondary" type="button" className="flex-1" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="flex-1" loading={saving} disabled={saving}>
              Save changes
            </Button>
          </div>
        </form>
      </Modal>
    </>
  )
}
