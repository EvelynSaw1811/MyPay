import client from './client'

export const invitationApi = {
  send:          (colId, body) => client.post(`/api/collections/${colId}/invitations`, body).then(r => r.data.data),
  listForCol:    colId         => client.get(`/api/collections/${colId}/invitations`).then(r => r.data.data),
  myInvitations: ()            => client.get('/api/invitations').then(r => r.data.data),
  respond:       (id, body)    => client.post(`/api/invitations/${id}/respond`, body).then(r => r.data.data),
}

export const sendInvitation   = (colId, body) => invitationApi.send(colId, body)
export const listForCollection = (colId) => invitationApi.listForCol(colId).then(data => ({ data }))
export const myInvitations    = () => invitationApi.myInvitations().then(data => ({ data }))
export const respond          = (id, action) => invitationApi.respond(id, { action })
