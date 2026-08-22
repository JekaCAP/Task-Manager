export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE' | 'CANCELLED'
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type GlobalRole = 'ADMIN' | 'USER'

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  globalRole: GlobalRole
  avatarUrl?: string | null
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface Project {
  id: string
  name: string
  description?: string
  key: string
  archived: boolean
  ownerId: string
  createdAt: string
  updatedAt: string
}

export interface Task {
  id: string
  projectId: string
  title: string
  description?: string
  status: TaskStatus
  priority: TaskPriority
  assigneeId?: string | null
  dueDate?: string | null
  version: number
  createdAt: string
  updatedAt: string
}

export interface PageMeta {
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PagedResponse<T> {
  content: T[]
  meta: PageMeta
}

export interface DashboardStats {
  totalProjects: number
  totalTasks: number
  tasksByStatus: Record<string, number>
  tasksByPriority: Record<string, number>
  overdueTasks: number
}

export interface Comment {
  id: string
  taskId: string
  authorId: string
  authorName: string
  body: string
  version: number
  createdAt: string
  updatedAt: string
}

export interface TaskDetail {
  task: Task
  tags: { id: string; name: string; color?: string }[]
  commentsCount: number
  attachmentsCount: number
}

export interface ApiError {
  code: string
  message: string
  details: { field: string; message: string }[]
}
