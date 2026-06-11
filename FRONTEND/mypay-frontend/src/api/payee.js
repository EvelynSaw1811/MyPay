import client from './client'

export const payeeApi = {
  getPayees:   ()     => client.get('/api/wallets/payees').then(r => r.data.data),
  addPayee:    body   => client.post('/api/wallets/payees', body).then(r => r.data.data),
  removePayee: id     => client.delete(`/api/wallets/payees/${id}`),
}

export const getPayees   = () => payeeApi.getPayees().then(data => ({ data }))
export const addPayee    = ({ identifier, nickname }) => payeeApi.addPayee({ identifier, nickname })
export const removePayee = (id) => payeeApi.removePayee(id)
