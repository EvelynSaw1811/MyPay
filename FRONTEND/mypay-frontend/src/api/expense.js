import client from './client'

export const expenseApi = {
  list:        colId                       => client.get(`/api/collections/${colId}/expenses`).then(r => r.data.data),
  get:         (colId, expId)              => client.get(`/api/collections/${colId}/expenses/${expId}`).then(r => r.data.data),
  create:      (colId, body)               => client.post(`/api/collections/${colId}/expenses`, body).then(r => r.data.data),
  update:      (colId, expId, body)        => client.put(`/api/collections/${colId}/expenses/${expId}`, body).then(r => r.data.data),
  remove:      (colId, expId, options = {}) => client.delete(`/api/collections/${colId}/expenses/${expId}`, { params: options }),
  getShare:    (colId, expId, shareId)     => client.get(`/api/collections/${colId}/expenses/${expId}/shares/${shareId}`).then(r => r.data.data),
  settleShare: (colId, expId, shareId, body) => client.put(`/api/collections/${colId}/expenses/${expId}/shares/${shareId}/settle`, body),
}

export const listExpenses  = (colId) => expenseApi.list(colId).then(data => ({ data }))
export const getExpense    = (colId, expId) => expenseApi.get(colId, expId).then(data => ({ data }))
export const createExpense = (colId, body) => expenseApi.create(colId, body)
export const updateExpense = (colId, expId, body) => expenseApi.update(colId, expId, body)
export const removeExpense = (colId, expId, options) => expenseApi.remove(colId, expId, options)
export const getShare      = (colId, expId, shareId) => expenseApi.getShare(colId, expId, shareId).then(data => ({ data }))
export const settleShare   = (colId, expId, shareId, body) => expenseApi.settleShare(colId, expId, shareId, body)
