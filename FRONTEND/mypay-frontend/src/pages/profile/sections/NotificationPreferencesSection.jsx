import ProfileSection from './ProfileSection'

function Toggle({ checked, onChange, disabled }) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={() => onChange(!checked)}
      className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors
        ${checked ? 'bg-primary' : 'bg-gray-300'}
        ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
    >
      <span
        className={`inline-block h-5 w-5 transform rounded-full bg-white shadow transition
          ${checked ? 'translate-x-5' : 'translate-x-0.5'}`}
      />
    </button>
  )
}

function Row({ label, description, checked, onChange, disabled }) {
  return (
    <div className="flex items-start justify-between py-3 gap-4">
      <div className="min-w-0">
        <p className="text-sm text-gray-800">{label}</p>
        {description && <p className="text-xs text-gray-400 mt-0.5">{description}</p>}
      </div>
      <Toggle checked={checked} onChange={onChange} disabled={disabled} />
    </div>
  )
}

/**
 * Notification preferences — backed by the new
 * GET/PUT /api/notifications/preferences endpoints (notification-service).
 *
 * Each toggle issues an immediate PUT with just the changed field; the parent
 * owns the optimistic mutation and renders any error.
 */
export default function NotificationPreferencesSection({
  preferences,
  loading,
  saving,
  error,
  onToggle,
}) {
  if (loading) {
    return (
      <ProfileSection title="Notification Preferences">
        <p className="py-4 text-xs text-gray-400 text-center">Loading preferences…</p>
      </ProfileSection>
    )
  }

  const disabled = saving

  return (
    <ProfileSection title="Notification Preferences">
      <div className="divide-y divide-gray-100">
        <Row
          label="Email notifications"
          description="Transaction receipts, security alerts, monthly summaries"
          checked={!!preferences?.emailEnabled}
          onChange={(v) => onToggle('emailEnabled', v)}
          disabled={disabled}
        />
        <Row
          label="SMS notifications"
          description="Critical alerts and one-time codes"
          checked={!!preferences?.smsEnabled}
          onChange={(v) => onToggle('smsEnabled', v)}
          disabled={disabled}
        />
        <Row
          label="Push notifications"
          description="In-app updates and reminders"
          checked={!!preferences?.pushEnabled}
          onChange={(v) => onToggle('pushEnabled', v)}
          disabled={disabled}
        />
        <Row
          label="Promotional emails"
          description="Product news and seasonal offers"
          checked={!!preferences?.promoEnabled}
          onChange={(v) => onToggle('promoEnabled', v)}
          disabled={disabled}
        />
      </div>
      {error && <p className="mt-3 text-xs text-danger">{error}</p>}
    </ProfileSection>
  )
}
