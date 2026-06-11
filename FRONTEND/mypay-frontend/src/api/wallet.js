import client from './client'

export const walletApi = {
  getWallet:  ()       => client.get('/api/wallets/me').then(r => r.data.data),
  getAccount: ()       => client.get('/api/accounts/me').then(r => r.data.data),
  getBalance: currency => client.get(`/api/wallets/balance/${currency}`).then(r => r.data.data),
  getRegistrationStatus: currency => client.get(`/api/wallets/registration/${currency}`).then(r => r.data.data),
  openWallet:  body     => client.post('/api/accounts/wallets', body).then(r => r.data.data),
  closeWallet: currency => client.delete(`/api/accounts/wallets/${currency}`).then(r => r.data.data),
  topUp:       body     => client.post('/api/wallets/topup', body).then(r => r.data.data),
}

export const getWallet  = () => walletApi.getWallet().then(data => ({ data }))
export const getAccount = () => walletApi.getAccount().then(data => ({ data }))
export const getBalance = (currency) => walletApi.getBalance(currency).then(data => ({ data }))
export const getRegistrationStatus = (currency) => walletApi.getRegistrationStatus(currency).then(data => ({ data }))
export const openWallet  = (body)     => walletApi.openWallet(body)
export const closeWallet = (currency) => walletApi.closeWallet(currency)
export const topUp       = (body)     => walletApi.topUp(body)
