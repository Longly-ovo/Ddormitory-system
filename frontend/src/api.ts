import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from './router'
import type { ApiResponse } from './types'

const http = axios.create({ baseURL: '/api', timeout: 10000 })

function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('nickname')
  localStorage.removeItem('role')
  localStorage.removeItem('mustChangePassword')
}

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  response => response,
  error => {
    const message = error.response?.data?.message || '网络请求失败'
    if (error.response?.status === 401) {
      clearAuth()
      if (router.currentRoute.value.path !== '/login') router.push('/login')
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export async function get<T>(url: string, params?: object): Promise<T> {
  return (await http.get<ApiResponse<T>>(url, { params })).data.data
}
export async function post<T>(url: string, data?: object): Promise<T> {
  return (await http.post<ApiResponse<T>>(url, data)).data.data
}
export async function put<T>(url: string, data?: object): Promise<T> {
  return (await http.put<ApiResponse<T>>(url, data)).data.data
}
export async function remove<T = void>(url: string): Promise<T> {
  return (await http.delete<ApiResponse<T>>(url)).data.data
}
