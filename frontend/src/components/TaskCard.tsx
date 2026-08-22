import type { Task, TaskPriority } from '../types'

const PRIORITY_CLASS: Record<TaskPriority, string> = {
  LOW: 'priority-low',
  MEDIUM: 'priority-medium',
  HIGH: 'priority-high',
  CRITICAL: 'priority-critical',
}

interface TaskCardProps {
  task: Task
  onClick: () => void
}

export function TaskCard({ task, onClick }: TaskCardProps) {
  return (
    <button
      type="button"
      className="task-card"
      onClick={onClick}
      data-testid={`task-card-${task.id}`}
    >
      <div className="task-card-top">
        <span className={`priority-badge ${PRIORITY_CLASS[task.priority]}`} data-testid="task-priority">
          {task.priority}
        </span>
        {task.dueDate && (
          <span className="task-due" data-testid="task-due-date">
            {new Date(task.dueDate).toLocaleDateString()}
          </span>
        )}
      </div>
      <h4 data-testid="task-title">{task.title}</h4>
      {task.description && <p className="task-desc">{task.description}</p>}
    </button>
  )
}
