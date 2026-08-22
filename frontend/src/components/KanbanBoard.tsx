import type { Task, TaskStatus } from '../types'
import { TaskCard } from './TaskCard'

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: 'TODO', label: 'To Do' },
  { status: 'IN_PROGRESS', label: 'In Progress' },
  { status: 'REVIEW', label: 'Review' },
  { status: 'DONE', label: 'Done' },
]

interface KanbanBoardProps {
  tasks: Task[]
  onTaskClick: (task: Task) => void
}

export function KanbanBoard({ tasks, onTaskClick }: KanbanBoardProps) {
  return (
    <div className="kanban" data-testid="kanban-board">
      {COLUMNS.map(({ status, label }) => {
        const columnTasks = tasks.filter((t) => t.status === status)
        return (
          <section
            key={status}
            className="kanban-column"
            data-testid={`kanban-column-${status}`}
          >
            <header>
              <h3>{label}</h3>
              <span className="count" data-testid={`kanban-count-${status}`}>
                {columnTasks.length}
              </span>
            </header>
            <div className="kanban-cards">
              {columnTasks.map((task) => (
                <TaskCard key={task.id} task={task} onClick={() => onTaskClick(task)} />
              ))}
            </div>
          </section>
        )
      })}
    </div>
  )
}
