import { createContext, useContext, useState, useCallback } from 'react'
import { authApi } from '../api/auth'
import { profileApi } from '../api/profile'

const AuthContext = createContext(null)

/** Build the in-memory user profile object from any source (login, register, refresh). */
function buildProfile(data, fallback = {}) {
  return {
    userId:         data.userId         ?? fallback.userId,
    firstName:      data.firstName      ?? fallback.firstName,
    lastName:       data.lastName       ?? fallback.lastName,
    userNickname:   data.userNickname   ?? data.name ?? fallback.userNickname,
    invitationCode: data.invitationCode ?? fallback.invitationCode,
    email:          data.email          ?? fallback.email,
    phone:          data.phone          ?? fallback.phone,
    status:         data.status         ?? fallback.status,
    verificationStatus: data.verificationStatus ?? fallback.verificationStatus,
    createdAt:      data.createdAt      ?? fallback.createdAt,
    updatedAt:      data.updatedAt      ?? fallback.updatedAt,
  }
}

function persistUser(profile) {
  localStorage.setItem('user', JSON.stringify(profile))
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('user')) } catch { return null }
  })

  const login = useCallback(async (email, password) => {
    const data = await authApi.login({ email, password })
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    const profile = buildProfile(data)
    persistUser(profile)
    setUser(profile)
    return profile
  }, [])

  const register = useCallback(async (body) => {
    const data = await authApi.register(body)
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    const profile = buildProfile(data, body)
    persistUser(profile)
    setUser(profile)
    return profile
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem('refreshToken')
    try { if (refreshToken) await authApi.logout({ refreshToken }) } catch { /* ignore */ }
    localStorage.clear()
    setUser(null)
  }, [])

  /**
   * Pull the latest profile from the server (used by the Profile page after
   * an edit, and to replace stale data cached at login time).
   */
  const refreshUser = useCallback(async () => {
    setUser((current) => {
      if (!current?.userId) return current
      // Fetch in the background; merge when it returns.
      profileApi.getProfile(current.userId)
        .then((fresh) => {
          const merged = buildProfile(fresh, current)
          persistUser(merged)
          setUser(merged)
        })
        .catch(() => { /* keep cached profile on error */ })
      return current
    })
  }, [])

  /** Apply a freshly-returned profile (e.g. after PUT /users/{id}) to context + storage. */
  const applyProfile = useCallback((fresh) => {
    setUser((current) => {
      const merged = buildProfile(fresh, current ?? {})
      persistUser(merged)
      return merged
    })
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, register, logout, refreshUser, applyProfile }}>
      {children}
    </AuthContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext)
}
