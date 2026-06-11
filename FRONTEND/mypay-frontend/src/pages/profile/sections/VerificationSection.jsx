import ProfileSection from './ProfileSection'
import Badge from '../../../components/ui/Badge'

const STATUS_META = {
  VERIFIED: {
    color:       'green',
    label:       'Verified',
    description: 'Your identity has been verified.',
  },
  PENDING: {
    color:       'yellow',
    label:       'Pending review',
    description: 'We\'re reviewing the documents you submitted.',
  },
  REJECTED: {
    color:       'red',
    label:       'Rejected',
    description: 'Your previous submission was rejected. Please contact support.',
  },
  UNVERIFIED: {
    color:       'gray',
    label:       'Unverified',
    description: 'Verify your identity to unlock higher transaction limits.',
  },
}

/**
 * Verification / KYC section — display-only.
 *
 * No upload flow is implemented because the backend does not yet support KYC.
 * The status badge reflects the new userVerificationStatus column on USER_T.
 */
export default function VerificationSection({ status }) {
  const key  = (status ?? 'UNVERIFIED').toUpperCase()
  const meta = STATUS_META[key] ?? STATUS_META.UNVERIFIED

  return (
    <ProfileSection title="Verification">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <p className="text-sm font-medium text-gray-900">Identity verification</p>
            <Badge color={meta.color}>{meta.label}</Badge>
          </div>
          <p className="text-xs text-gray-500 mt-1.5">{meta.description}</p>
        </div>
        <span className="text-[10px] uppercase tracking-wider text-gray-400 shrink-0 mt-1">
          Coming soon
        </span>
      </div>
    </ProfileSection>
  )
}
