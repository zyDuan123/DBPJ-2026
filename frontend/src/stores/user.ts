import { defineStore } from 'pinia'
import request from '../utils/request'

type Role = 'STUDENT' | 'ORGANIZER' | 'ADMIN'

interface UserInfo {
  id: number
  name: string
  role: Role
  studentNo?: string
  phone?: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null') as UserInfo | null,
  }),
  actions: {
    async login(username: string, password: string) {
      const data = await request.post('/auth/login', { username, password }) as { token: string; user: UserInfo }
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(data.user))
      return data.user
    },
    async fetchMe() {
      const data = await request.get('/auth/me') as UserInfo
      this.user = data
      localStorage.setItem('user', JSON.stringify(data))
      return data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
  },
})
