import type { ApiError } from './types'

const TOKEN_KEY = 'tm_access_token'
const REFRESH_KEY = 'tm_refresh_token'
const USER_KEY = 'tm_user'

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function getStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function saveAuth(accessToken: string, refreshToken: string, user: unknown) {
  localStorage.setItem(TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_KEY, refreshToken)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USER_KEY)
}

export class ApiClientError extends Error {
  status: number
  body: ApiError

  constructor(status: number, body: ApiError) {
    super(body.message)
    this.status = status
    this.body = body
  }
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  const token = getAccessToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(path, { ...options, headers })

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  const data = text ? JSON.parse(text) : null

  if (!response.ok) {
    if (response.status === 401 && !path.includes('/auth/login')) {
      clearAuth()
      window.location.href = '/login'
    }
    throw new ApiClientError(response.status, data ?? { code: 'ERROR', message: response.statusText, details: [] })
  }

  return data as T
}

export const authApi = {
  login: (email: string, password: string) =>
    api<import('./types').AuthResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  me: () => api<import('./types').User>('/api/v1/auth/me'),
}

export const projectsApi = {
  list: () => api<import('./types').PagedResponse<import('./types').Project>>('/api/v1/projects?size=50'),
}

export const tasksApi = {
  list: (projectId: string) =>
    api<import('./types').PagedResponse<import('./types').Task>>(
      `/api/v1/tasks?projectId=${projectId}&size=100&sort=createdAt,desc`,
    ),
  get: (id: string) => api<import('./types').TaskDetail>(`/api/v1/tasks/${id}`),
  create: (body: object) =>
    api<import('./types').Task>('/api/v1/tasks', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: string, body: object) =>
    api<import('./types').Task>(`/api/v1/tasks/${id}`, { method: 'PATCH', body: JSON.stringify(body) }),
  updateStatus: (id: string, status: string, version: number) =>
    api<import('./types').Task>(`/api/v1/tasks/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status, version }),
    }),
  comments: (taskId: string) =>
    api<import('./types').PagedResponse<import('./types').Comment>>(
      `/api/v1/tasks/${taskId}/comments?size=50`,
    ),
  addComment: (taskId: string, body: string) =>
    api<import('./types').Comment>(`/api/v1/tasks/${taskId}/comments`, {
      method: 'POST',
      body: JSON.stringify({ body }),
    }),
}

export const statsApi = {
  dashboard: (projectId?: string) =>
    api<import('./types').DashboardStats>(
      projectId ? `/api/v1/stats/dashboard?projectId=${projectId}` : '/api/v1/stats/dashboard',
    ),
}
