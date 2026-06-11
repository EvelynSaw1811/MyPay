/**
 * Shared wrapper used by every Profile section.
 * Renders a consistent title row + optional action slot + a Card body.
 */
import Card from '../../../components/ui/Card'

export default function ProfileSection({ title, action, children, padding = 'p-4' }) {
  return (
    <section className="space-y-2">
      <div className="flex items-center justify-between gap-3 px-1">
        <h2 className="text-xs font-semibold uppercase tracking-wide text-gray-500">
          {title}
        </h2>
        {action && <div className="shrink-0 text-xs">{action}</div>}
      </div>
      <Card padding={padding}>
        {children}
      </Card>
    </section>
  )
}
