import { createContext, useContext, useState, useEffect } from 'react'
import { notificationApi } from '../api/notification'
import { useAuth } from './AuthContext'

const NotificationContext = createContext({ count: 0, refresh: () => {} })

export function NotificationProvider({ children }) {
  const { user } = useAuth()
  const [count, setCount] = useState(0)

  const refresh = async () => {
    try {
      const data = await notificationApi.unreadCount()
      setCount(typeof data === 'number' ? data : data?.count ?? 0)
    } catch { /* ignore */ }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (!user) { setCount(0); return }
    refresh()
    const id = setInterval(refresh, 30000)
    return () => clearInterval(id)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.userId])

  return (
    <NotificationContext.Provider value={{ count, refresh }}>
      {children}
    </NotificationContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useNotifications() {
  return useContext(NotificationContext)
}
