import { useNavigate } from 'react-router-dom'
import { useNotifications } from '../../contexts/NotificationContext'

export default function TopBar({ title, back, backTo, titleMeta, actions }) {
  const navigate = useNavigate()
  const { count } = useNotifications()

  return (
    <div className="sticky top-0 z-30 bg-white border-b border-[#E8E8E8] px-4 min-h-12 flex items-center gap-3">
      {back && (
        <button
          onClick={() => backTo ? navigate(backTo) : navigate(-1)}
          className="flex items-center justify-center w-10 h-10 -ml-2 text-gray-500 hover:text-gray-800"
          aria-label="Back"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
      )}
      <h1 className="flex-1 min-w-0 truncate text-[15px] font-semibold text-gray-900">{title}</h1>
      {(titleMeta || actions) && (
        <div className="ml-auto flex shrink-0 items-center justify-end gap-2">
          {titleMeta}
          {actions}
        </div>
      )}
      {!back && (
        <button
          onClick={() => navigate('/app/notifications')}
          className="relative flex items-center justify-center w-10 h-10 -mr-2 text-gray-500 hover:text-gray-800"
          aria-label="Notifications"
        >
          <svg className="w-[18px] h-[18px]" fill="none" stroke="currentColor" strokeWidth="1.75" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          {count > 0 && (
            <span className="absolute top-0.5 right-0.5 bg-danger text-white text-[9px] font-semibold rounded-full min-w-[14px] h-3.5 flex items-center justify-center px-0.5 leading-none">
              {count > 99 ? '99+' : count}
            </span>
          )}
        </button>
      )}
    </div>
  )
}
