import Badge from '../../../components/ui/Badge'
import MaskedValue from '../../../components/ui/MaskedValue'

const STATUS_BADGE = {
  ACTIVE:    { color: 'green',  label: 'Active' },
  INACTIVE:  { color: 'gray',   label: 'Inactive' },
  SUSPENDED: { color: 'red',    label: 'Suspended' },
}

function badge(map, key) {
  const k = (key ?? '').toUpperCase()
  return map[k] ?? { color: 'gray', label: key || 'Unknown' }
}

export default function ProfileOverviewCard({ user, wallet }) {
  const displayName = user?.userNickname || user?.firstName || 'User'
  const legalName   = [user?.firstName, user?.lastName].filter(Boolean).join(' ')
  const initial     = (displayName?.[0] ?? 'U').toUpperCase()

  const statusInfo = badge(STATUS_BADGE, user?.status)
  const accountId  = wallet?.accountId ?? wallet?.walletId

  return (
    <div className="bg-white rounded-xl shadow-card p-5">
      <div className="flex items-center gap-4">
        <div className="w-16 h-16 rounded-full bg-gray-100 border border-gray-200 flex items-center justify-center text-2xl font-semibold text-gray-700">
          {initial}
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-base font-semibold text-gray-900 truncate">{displayName}</p>
          {legalName && legalName !== displayName && (
            <p className="text-xs text-gray-400 truncate">{legalName}</p>
          )}
          <p className="text-sm text-gray-500 truncate">{user?.email ?? '-'}</p>
          <div className="flex flex-wrap gap-1.5 mt-2">
            <Badge color={statusInfo.color}>{statusInfo.label}</Badge>
          </div>
        </div>
      </div>

      <dl className="mt-5 space-y-3 text-xs">
        <InlineField label="Phone" value={user?.phone} />
        <InlineField label="Invitation code" value={user?.invitationCode} monospace masked />
        <InlineField label="Account ID" value={accountId} monospace masked />
        <InlineField label="User ID" value={user?.userId} monospace masked />
      </dl>
    </div>
  )
}

function InlineField({ label, value, monospace = false, masked = false }) {
  return (
    <div className="flex items-baseline justify-between gap-3">
      <dt className="text-gray-400 shrink-0">{label}</dt>
      <dd
        className={`min-w-0 text-gray-700 text-right ${monospace ? 'font-mono text-[9px] sm:text-[11px]' : ''}`}
        title={value || undefined}
      >
        {masked ? (
          <MaskedValue
            value={value}
            mask="********"
            className="text-right whitespace-nowrap"
          />
        ) : (
          <span className="block truncate">{value || '-'}</span>
        )}
      </dd>
    </div>
  )
}
