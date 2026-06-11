import client from './client'

/**
 * Profile API — thin wrapper around the auth-service user endpoints.
 * Returns unwrapped `data` payloads (the API gateway envelopes responses
 * with { success, message, data, timestamp }).
 */
export const profileApi = {
  getProfile:     (userId)       => client.get(`/api/auth/users/${userId}`).then(r => r.data.data),
  updateProfile:  (userId, body) => client.put(`/api/auth/users/${userId}`, body).then(r => r.data.data),
  changePassword: (userId, body) => client.put(`/api/auth/users/${userId}/password`, body).then(r => r.data),
  deleteAccount:  (userId, body) => client.delete(`/api/auth/users/${userId}`, { data: body }).then(r => r.data),
}

export const getProfile     = (userId)       => profileApi.getProfile(userId)
export const updateProfile  = (userId, body) => profileApi.updateProfile(userId, body)
export const changePassword = (userId, body) => profileApi.changePassword(userId, body)
export const deleteAccount  = (userId, body) => profileApi.deleteAccount(userId, body)
