import type { TaskStatus } from './types'

const ALLOWED: Record<TaskStatus, TaskStatus[]> = {
  TODO: ['TODO', 'IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['IN_PROGRESS', 'REVIEW', 'TODO', 'CANCELLED'],
  REVIEW: ['REVIEW', 'DONE', 'IN_PROGRESS', 'CANCELLED'],
  DONE: ['DONE', 'CANCELLED'],
  CANCELLED: ['CANCELLED'],
}

export function allowedStatuses(current: TaskStatus): TaskStatus[] {
  return ALLOWED[current] ?? [current]
}
