import client from './client'

export const reportingApi = {
  dashboard:           ()    => client.get('/api/reports/dashboard').then(r => r.data.data),
  spending:            ()    => client.get('/api/reports/spending').then(r => r.data.data),
  ledger:              cur   => client.get(`/api/reports/ledger/${cur}`).then(r => r.data.data),
  position:            ()    => client.get('/api/reports/position').then(r => r.data.data),
  collectionStatement: id    => client.get(`/api/reports/collections/${id}`).then(r => r.data.data),
}

export const getDashboard           = () => reportingApi.dashboard().then(data => ({ data }))
export const getSpending            = () => reportingApi.spending().then(data => ({ data }))
export const getLedger              = (cur) => reportingApi.ledger(cur).then(data => ({ data }))
export const getPosition            = () => reportingApi.position().then(data => ({ data }))
export const getCollectionStatement = (id) => reportingApi.collectionStatement(id).then(data => ({ data }))
