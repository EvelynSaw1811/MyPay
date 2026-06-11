import ProfileSection from './ProfileSection'
import EmptyState from '../../../components/ui/EmptyState'
import Badge from '../../../components/ui/Badge'

/**
 * Payment Methods section — display-only.
 *
 * No linked-cards/banks entity exists in the backend yet, so we render an
 * empty state with a masked-card preview that demonstrates how sensitive
 * data will be displayed (never the full PAN, never CVV) once the feature
 * is implemented.
 */
export default function PaymentMethodsSection() {
  return (
    <ProfileSection title="Payment Methods">
      <div className="space-y-4">
        <div className="rounded-lg border border-dashed border-gray-200 px-4 py-3">
          <p className="text-[10px] uppercase tracking-widest text-gray-400 mb-1">Example display</p>
          <div className="flex items-center justify-between">
            <p className="font-mono text-sm text-gray-700">**** **** **** 1234</p>
            <Badge color="gray">Coming soon</Badge>
          </div>
        </div>
        <EmptyState
          title="No payment methods linked"
          description="You'll be able to link cards and bank accounts here once the feature is available."
        />
      </div>
    </ProfileSection>
  )
}
