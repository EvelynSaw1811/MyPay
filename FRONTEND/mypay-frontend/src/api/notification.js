import client from './client'

export const notificationApi = {
  list:        ()  => client.get('/api/notifications').then(r => r.data.data),
  unread:      ()  => client.get('/api/notifications/unread').then(r => r.data.data),
  unreadCount: ()  => client.get('/api/notifications/unread/count').then(r => r.data.data),
  markRead:    id  => client.put(`/api/notifications/${id}/read`),
  markAllRead: ()  => client.put('/api/notifications/read-all'),
}

export const listNotifications = () => notificationApi.list().then(data => ({ data }))
export const markRead          = (id) => notificationApi.markRead(id)
export const markAllRead       = () => notificationApi.markAllRead()
