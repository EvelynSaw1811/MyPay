import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const client = axios.create({ baseURL: BASE_URL })

// attach token
client.interceptors.request.use(cfg => {
  const token = localStorage.getItem('accessToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  cfg.headers['X-Request-Id'] = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
  return cfg
})

let refreshing = false
let queue = []

function flushQueue(error, token) {
  queue.forEach(p => error ? p.reject(error) : p.resolve(token))
  queue = []
}

// auto-refresh on 401
client.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config
    if (err.response?.status !== 401 || original._retry) return Promise.reject(err)

    if (refreshing) {
      return new Promise((resolve, reject) => {
        queue.push({ resolve, reject })
      }).then(token => {
        original.headers.Authorization = `Bearer ${token}`
        return client(original)
      })
    }

    original._retry = true
    refreshing = true

    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) throw new Error('No refresh token')

      const { data } = await axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken })
      const newAccess = data.data.accessToken
      const newRefresh = data.data.refreshToken
      localStorage.setItem('accessToken', newAccess)
      localStorage.setItem('refreshToken', newRefresh)

      flushQueue(null, newAccess)
      original.headers.Authorization = `Bearer ${newAccess}`
      return client(original)
    } catch (e) {
      flushQueue(e, null)
      localStorage.clear()
      window.location.replace('/login')
      return Promise.reject(e)
    } finally {
      refreshing = false
    }
  }
)

export default client
