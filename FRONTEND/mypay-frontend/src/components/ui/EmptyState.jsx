export default function EmptyState({ title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-6 text-center">
      <div className="w-10 h-px bg-gray-200 mb-6" />
      <p className="font-medium text-gray-700 mb-1">{title}</p>
      {description && <p className="text-sm text-gray-400 mb-4">{description}</p>}
      {action}
    </div>
  )
}
