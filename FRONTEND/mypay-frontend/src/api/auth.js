import client from './client'
import axios from 'axios'

const BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export const authApi = {
  register: body => axios.post(`${BASE}/api/auth/register`, body).then(r => r.data.data),
  login:    body => axios.post(`${BASE}/api/auth/login`, body).then(r => r.data.data),
  logout:   body => axios.post(`${BASE}/api/auth/logout`, body),
  getUser:  id   => client.get(`/api/auth/users/${id}`).then(r => r.data.data),
}
