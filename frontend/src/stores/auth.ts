import { defineStore } from 'pinia'
import { post } from '../api'

export type Role = 'ADMIN' | 'DORM_MANAGER' | 'COUNSELOR' | 'STUDENT'
interface LoginResult { token: string; nickname: string; role: Role; mustChangePassword: boolean }

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    nickname: localStorage.getItem('nickname') || '',
    role: (localStorage.getItem('role') || '') as Role | '',
    mustChangePassword: localStorage.getItem('mustChangePassword') === 'true'
  }),
  actions: {
    async login(username: string, password: string) {
      const result = await post<LoginResult>('/auth/login', { username, password })
      this.token = result.token; this.nickname = result.nickname; this.role = result.role
      this.mustChangePassword = result.mustChangePassword
      localStorage.setItem('token', result.token); localStorage.setItem('nickname', result.nickname); localStorage.setItem('role', result.role)
      localStorage.setItem('mustChangePassword', String(result.mustChangePassword))
    },
    passwordChanged() {
      this.mustChangePassword = false
      localStorage.setItem('mustChangePassword', 'false')
    },
    logout() {
      this.token = ''; this.nickname = ''; this.role = ''; this.mustChangePassword = false
      localStorage.removeItem('token'); localStorage.removeItem('nickname'); localStorage.removeItem('role'); localStorage.removeItem('mustChangePassword')
    }
  }
})
