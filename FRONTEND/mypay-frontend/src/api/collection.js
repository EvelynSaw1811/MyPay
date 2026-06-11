import client from './client'

export const collectionApi = {
  list:         ()           => client.get('/api/collections').then(r => r.data.data),
  get:          id           => client.get(`/api/collections/${id}`).then(r => r.data.data),
  create:       body         => client.post('/api/collections', body).then(r => r.data.data),
  update:       (id, body)   => client.put(`/api/collections/${id}`, body).then(r => r.data.data),
  close:        id           => client.post(`/api/collections/${id}/close`),
  getMembers:   id           => client.get(`/api/collections/${id}/members`).then(r => r.data.data),
  removeMember: (id, userId) => client.delete(`/api/collections/${id}/members/${userId}`),
  leave:        id           => client.delete(`/api/collections/${id}/members/me`),
  getBalances:  id           => client.get(`/api/collections/${id}/balances`).then(r => r.data.data),
  listTypes:     ()           => client.get('/api/collections/types').then(r => r.data.data),
  createType:    body         => client.post('/api/collections/types', body).then(r => r.data.data),
  deleteType:    id           => client.delete(`/api/collections/types/${id}`),
}

export const listCollections   = () => collectionApi.list().then(data => ({ data }))
export const getCollection     = (id) => collectionApi.get(id).then(data => ({ data }))
export const createCollection  = (body) => collectionApi.create(body).then(data => ({ data }))
export const updateCollection  = (id, body) => collectionApi.update(id, body)
export const closeCollection   = (id) => collectionApi.close(id)
export const getMembers        = (id) => collectionApi.getMembers(id).then(data => ({ data }))
export const removeMember      = (id, userId) => collectionApi.removeMember(id, userId)
export const leaveCollection   = (id) => collectionApi.leave(id)
export const getBalances       = (id) => collectionApi.getBalances(id).then(data => ({ data }))
export const listCollectionTypes = () => collectionApi.listTypes().then(data => ({ data }))
export const createCollectionType = (body) => collectionApi.createType(body).then(data => ({ data }))
export const deleteCollectionType = (id) => collectionApi.deleteType(id)
