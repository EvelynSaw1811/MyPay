export default function Button({ children, variant = 'primary', size = 'md', className = '', disabled, loading, ...props }) {
  const base = 'inline-flex items-center justify-center font-medium rounded-lg transition-all focus:outline-none select-none'
  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2 text-sm',
    lg: 'px-5 py-2.5 text-sm w-full',
  }
  const variants = {
    primary:   'bg-primary text-white hover:bg-primary-dark active:opacity-90 disabled:opacity-40',
    secondary: 'bg-gray-100 text-gray-700 hover:bg-gray-200 disabled:opacity-40',
    danger:    'bg-danger text-white hover:opacity-90 disabled:opacity-40',
    ghost:     'text-primary hover:bg-blue-50 disabled:opacity-40',
    outline:   'border border-primary text-primary hover:bg-blue-50 disabled:opacity-40',
  }
  return (
    <button
      className={`${base} ${sizes[size]} ${variants[variant]} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <span className="mr-2 h-3.5 w-3.5 border-2 border-current border-t-transparent rounded-full animate-spin opacity-70" />
      )}
      {children}
    </button>
  )
}
