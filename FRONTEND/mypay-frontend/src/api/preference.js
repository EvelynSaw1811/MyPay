import client from './client'

/**
 * Notification preferences API — calls notification-service
 * (GET/PUT /api/notifications/preferences) via the gateway.
 * The backend lazily creates a row with safe defaults the first
 * time the user fetches their preferences.
 */
export const preferenceApi = {
  get:    ()    => client.get('/api/notifications/preferences').then(r => r.data.data),
  update: (body) => client.put('/api/notifications/preferences', body).then(r => r.data.data),
}

export const getPreferences    = ()     => preferenceApi.get()
export const updatePreferences = (body) => preferenceApi.update(body)
