import { useState } from 'react'

/**
 * MaskedValue — renders a sensitive value (id, code, balance) as masked
 * asterisks by default with an inline eye-icon button that reveals the
 * real value on click. Each instance owns its own reveal state.
 *
 * Props:
 *   value         the raw value to display when revealed (string | number)
 *   mask          (optional) the masked placeholder. Defaults to "********".
 *                 Pass a custom mask for amounts, e.g. "RM ****".
 *   masked        (optional) initial state. Defaults to true (hidden).
 *   className     (optional) styling applied to the value span.
 *   buttonLabel   (optional) accessible label override.
 *   persistKey    (optional) when set, the reveal state is persisted in
 *                 localStorage under `mypay-mask-<persistKey>`. Each key
 *                 is independent. Defaults to true (unmasked) when absent.
 */
export default function MaskedValue({
  value,
  mask = '••••••••',
  masked: initiallyMasked = true,
  className = '',
  containerClassName = '',
  buttonLabel,
  persistKey,
}) {
  const [masked, setMasked] = useState(() => {
    if (!persistKey) return initiallyMasked
    const stored = localStorage.getItem(`mypay-mask-${persistKey}`)
    return stored !== null ? stored === 'true' : initiallyMasked
  })

  const hasValue = value !== undefined && value !== null && value !== ''
  const label =
    buttonLabel ?? (masked ? 'Show value' : 'Hide value')

  // When there's no value at all, render a clearly-distinguishable
  // placeholder. Helps debugging — "(not available)" tells the user the
  // backend didn't return this field, vs the masked dots which only mean
  // "intentionally hidden".
  if (!hasValue) {
    return (
      <span className={`${className} text-gray-300 italic`}>(not available)</span>
    )
  }

  const display = masked ? mask : String(value)

  return (
    <span className={`inline-flex items-center ${containerClassName || 'gap-1'} align-middle`}>
      <span className={className}>{display}</span>
      <button
        type="button"
        onClick={(event) => {
          event.stopPropagation()
          setMasked((m) => {
            const next = !m
            if (persistKey) {
              localStorage.setItem(`mypay-mask-${persistKey}`, String(next))
            }
            return next
          })
        }}
        aria-label={label}
        title={label}
        className="text-gray-400 hover:text-gray-700 transition-colors p-0 -m-0.5 rounded focus:outline-none focus:ring-2 focus:ring-primary/30 shrink-0"
      >
        {masked ? <EyeOffIcon /> : <EyeIcon />}
      </button>
    </span>
  )
}

function EyeIcon() {
  return (
    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2 12s4-4 10-4 10 4 10 4" />
      <path d="M4 14s3.5 2 8 2 8-2 8-2" />
      <path d="M9 15l-1 2" />
      <path d="M15 15l1 2" />
    </svg>
  )
}
