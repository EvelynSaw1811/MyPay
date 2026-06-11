export default function Select({ label, error, options, children, className = '', ...props }) {
  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label className="text-xs font-medium text-gray-500 uppercase tracking-wide">
          {label}
        </label>
      )}
      <select
        className={`w-full bg-white border rounded-lg px-3 py-2.5 text-sm text-gray-900
          outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition
          ${error ? 'border-danger' : 'border-[#E8E8E8]'}
          ${className}`}
        {...props}
      >
        {options
          ? options.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))
          : children}
      </select>
      {error && <p className="text-xs text-danger">{error}</p>}
    </div>
  )
}
