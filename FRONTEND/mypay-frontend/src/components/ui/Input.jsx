export default function Input({ label, error, suffix, className = '', ...props }) {
  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label className="text-xs font-medium text-gray-500 uppercase tracking-wide">
          {label}
        </label>
      )}
      <div className="relative">
        <input
          className={`w-full bg-white border rounded-lg px-3 py-2.5 text-sm text-gray-900 placeholder-gray-400
            outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition
            ${suffix ? 'pr-10' : ''}
            ${error ? 'border-danger focus:border-danger focus:ring-danger/20' : 'border-[#E8E8E8]'}
            ${className}`}
          {...props}
        />
        {suffix && (
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 pointer-events-none">
            {suffix}
          </span>
        )}
      </div>
      {error && <p className="text-xs text-danger">{error}</p>}
    </div>
  )
}
