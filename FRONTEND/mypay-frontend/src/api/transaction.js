import client from './client'

export const transactionApi = {
  settle:            body => client.post('/api/transactions/settle', body).then(r => r.data.data),
  settleNet:         body => client.post('/api/transactions/settle-net', body).then(r => r.data.data),
  history:           ()   => client.get('/api/transactions/history').then(r => r.data.data),
  historyByCurrency: cur  => client.get(`/api/transactions/history/${cur}`).then(r => r.data.data),
}

export const settle             = (body) => transactionApi.settle(body)
export const settleNet          = (body) => transactionApi.settleNet(body)
export const getHistory         = () => transactionApi.history().then(data => ({ data }))
export const getHistoryByCurrency = (cur) => transactionApi.historyByCurrency(cur).then(data => ({ data }))
