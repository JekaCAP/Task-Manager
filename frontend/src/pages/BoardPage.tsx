import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { projectsApi, statsApi, tasksApi } from '../api'
import { useAuth } from '../auth'
import { KanbanBoard } from '../components/KanbanBoard'
import { TaskModal } from '../components/TaskModal'
import type { DashboardStats, Project, Task } from '../types'

export function BoardPage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [projects, setProjects] = useState<Project[]>([])
  const [projectId, setProjectId] = useState('')
  const [tasks, setTasks] = useState<Task[]>([])
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [loading, setLoading] = useState(true)

  const loadTasks = useCallback(async (pid: string) => {
    const [tasksRes, statsRes] = await Promise.all([
      tasksApi.list(pid),
      statsApi.dashboard(pid),
    ])
    setTasks(tasksRes.content)
    setStats(statsRes)
  }, [])

  useEffect(() => {
    projectsApi
      .list()
      .then((res) => {
        setProjects(res.content)
        const demo = res.content.find((p) => p.key === 'DEMO') ?? res.content[0]
        if (demo) setProjectId(demo.id)
      })
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (projectId) loadTasks(projectId)
  }, [projectId, loadTasks])

  function handleLogout() {
    logout()
    navigate('/login')
  }

  function openCreate() {
    setSelectedTask(null)
    setModalOpen(true)
  }

  function openEdit(task: Task) {
    setSelectedTask(task)
    setModalOpen(true)
  }

  if (loading) {
    return (
      <div className="loading-screen" data-testid="loading-screen">
        Loading…
      </div>
    )
  }

  return (
    <div className="app-shell" data-testid="board-page">
      <header className="topbar">
        <div className="topbar-left">
          <span className="logo-dot" />
          <h1>Task Manager</h1>
        </div>
        <div className="topbar-center">
          <label htmlFor="project-select" className="sr-only">
            Project
          </label>
          <select
            id="project-select"
            value={projectId}
            onChange={(e) => setProjectId(e.target.value)}
            data-testid="project-select"
          >
            {projects.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name} ({p.key})
              </option>
            ))}
          </select>
        </div>
        <div className="topbar-right">
          <span className="user-badge" data-testid="user-name">
            {user?.firstName} {user?.lastName}
          </span>
          <button type="button" className="btn-secondary" onClick={handleLogout} data-testid="logout-btn">
            Logout
          </button>
        </div>
      </header>

      {stats && (
        <section className="stats-bar" data-testid="stats-bar">
          <div className="stat-card" data-testid="stat-total-tasks">
            <span>Tasks</span>
            <strong>{stats.totalTasks}</strong>
          </div>
          <div className="stat-card" data-testid="stat-overdue">
            <span>Overdue</span>
            <strong>{stats.overdueTasks}</strong>
          </div>
          <div className="stat-card" data-testid="stat-in-progress">
            <span>In Progress</span>
            <strong>{stats.tasksByStatus.IN_PROGRESS ?? 0}</strong>
          </div>
          <div className="stat-card" data-testid="stat-done">
            <span>Done</span>
            <strong>{stats.tasksByStatus.DONE ?? 0}</strong>
          </div>
        </section>
      )}

      <div className="board-toolbar">
        <h2 data-testid="board-title">Kanban Board</h2>
        <button type="button" onClick={openCreate} data-testid="task-create-btn">
          + New task
        </button>
      </div>

      <KanbanBoard tasks={tasks} onTaskClick={openEdit} />

      <TaskModal
        open={modalOpen}
        projectId={projectId}
        task={selectedTask}
        onClose={() => setModalOpen(false)}
        onSaved={() => loadTasks(projectId)}
      />
    </div>
  )
}
