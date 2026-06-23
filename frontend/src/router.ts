import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import MainLayout from './views/MainLayout.vue'
import HomeView from './views/HomeView.vue'
import StudentView from './views/StudentView.vue'
import StructureView from './views/StructureView.vue'
import MyDormitoryView from './views/MyDormitoryView.vue'
import ChangePasswordView from './views/ChangePasswordView.vue'
import type { Role } from './stores/auth'

const homeFor = (role: string | null) => role === 'STUDENT' ? '/my-dormitory' : role === 'COUNSELOR' ? '/students' : '/map'
const validRoles: Role[] = ['ADMIN', 'DORM_MANAGER', 'COUNSELOR', 'STUDENT']
let sessionCheck: Promise<Role | null> | null = null

function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('nickname')
  localStorage.removeItem('role')
  localStorage.removeItem('mustChangePassword')
}

function isValidToken(token: string) {
  const parts = token.split('.')
  if (parts.length !== 3) return false
  try {
    const encodedPayload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const paddedPayload = encodedPayload.padEnd(encodedPayload.length + (4 - encodedPayload.length % 4) % 4, '=')
    const payload = JSON.parse(atob(paddedPayload))
    return typeof payload.exp === 'number' && payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}

async function verifyAuth(token: string) {
  if (!sessionCheck) {
    sessionCheck = fetch('/api/auth/me', { headers: { Authorization: `Bearer ${token}` } })
      .then(async response => {
        if (!response.ok) return null
        const result = await response.json()
        const user = result.data
        if (!user || !validRoles.includes(user.role)) return null
        localStorage.setItem('nickname', user.nickname || '')
        localStorage.setItem('role', user.role)
        localStorage.setItem('mustChangePassword', String(!!user.mustChangePassword))
        return user.role as Role
      })
      .catch(() => null)
      .finally(() => {
        sessionCheck = null
      })
  }
  return sessionCheck
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/', component: MainLayout,
      children: [
        { path: '', redirect: () => homeFor(localStorage.getItem('role')) },
        { path: 'map', component: HomeView, meta: { roles: ['ADMIN', 'DORM_MANAGER', 'COUNSELOR'] satisfies Role[] } },
        { path: 'students', component: StudentView, meta: { roles: ['ADMIN', 'COUNSELOR'] satisfies Role[] } },
        { path: 'structure', component: StructureView, meta: { roles: ['ADMIN', 'DORM_MANAGER'] satisfies Role[] } },
        { path: 'my-dormitory', component: MyDormitoryView, meta: { roles: ['STUDENT'] satisfies Role[] } },
        { path: 'change-password', component: ChangePasswordView, meta: { roles: ['STUDENT'] satisfies Role[] } }
      ]
    }
  ]
})

router.beforeEach(async to => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const hasLocalAuth = !!token && !!role && validRoles.includes(role as Role) && isValidToken(token)

  if (!hasLocalAuth) {
    clearAuth()
    if (to.path !== '/login') return '/login'
    return
  }

  const verifiedRole = await verifyAuth(token)
  if (!verifiedRole) {
    clearAuth()
    if (to.path !== '/login') return '/login'
    return
  }

  if (to.path === '/login') return homeFor(verifiedRole)
  const roles = to.meta.roles as Role[] | undefined
  if (roles && !roles.includes(verifiedRole)) return homeFor(verifiedRole)
})

export default router
