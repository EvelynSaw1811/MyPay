import { useEffect } from 'react'

export default function Modal({ open, onClose, title, children }) {
  useEffect(() => {
    if (open) document.body.style.overflow = 'hidden'
    else document.body.style.overflow = ''
    return () => { document.body.style.overflow = '' }
  }, [open])

  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="relative w-full max-w-md max-h-[92dvh] overflow-y-auto bg-white rounded-t-3xl p-6 pb-8 animate-slide-up">
        {title && (
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-base font-semibold text-gray-900">{title}</h2>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">✕</button>
          </div>
        )}
        {children}
      </div>
    </div>
  )
}
