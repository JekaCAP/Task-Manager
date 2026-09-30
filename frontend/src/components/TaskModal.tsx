import { type FormEvent, useEffect, useState } from 'react'
import { tasksApi } from '../api'
import { allowedStatuses } from '../taskStatus'
import type { Comment, Task, TaskDetail, TaskPriority, TaskStatus } from '../types'
const PRIORITIES: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

interface TaskModalProps {
  open: boolean
  projectId: string
  task: Task | null
  onClose: () => void
  onSaved: () => void
}

export function TaskModal({ open, projectId, task, onClose, onSaved }: TaskModalProps) {
  const isNew = !task
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [status, setStatus] = useState<TaskStatus>('TODO')
  const [dueDate, setDueDate] = useState('')
  const [detail, setDetail] = useState<TaskDetail | null>(null)
  const [comments, setComments] = useState<Comment[]>([])
  const [commentText, setCommentText] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!open) return
    setError('')
    if (task) {
      setTitle(task.title)
      setDescription(task.description ?? '')
      setPriority(task.priority)
      setStatus(task.status)
      setDueDate(task.dueDate ? task.dueDate.slice(0, 10) : '')
      tasksApi.get(task.id).then(setDetail).catch(() => setDetail(null))
      tasksApi.comments(task.id).then((r) => setComments(r.content)).catch(() => setComments([]))
    } else {
      setTitle('')
      setDescription('')
      setPriority('MEDIUM')
      setStatus('TODO')
      setDueDate('')
      setDetail(null)
      setComments([])
    }
    setCommentText('')
  }, [open, task])

  if (!open) return null

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (isNew) {
        await tasksApi.create({
          projectId,
          title,
          description: description || undefined,
          priority,
          dueDate: dueDate ? `${dueDate}T00:00:00Z` : undefined,
        })
      } else if (task) {
        let version = task.version

        if (status !== task.status) {
          const updated = await tasksApi.updateStatus(task.id, status, version)
          version = updated.version
        }

        const dueIso = dueDate ? `${dueDate}T00:00:00Z` : undefined
        const changed =
          title !== task.title ||
          (description || '') !== (task.description || '') ||
          priority !== task.priority ||
          dueIso !== (task.dueDate ?? undefined)

        if (changed) {
          await tasksApi.update(task.id, {
            title,
            description: description || undefined,
            priority,
            dueDate: dueIso,
            version,
          })
        }
      }
      onSaved()
      onClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  async function handleAddComment(e: FormEvent) {
    e.preventDefault()
    if (!task || !commentText.trim()) return
    await tasksApi.addComment(task.id, commentText.trim())
    const res = await tasksApi.comments(task.id)
    setComments(res.content)
    setCommentText('')
  }

  return (
    <div className="modal-overlay" onClick={onClose} data-testid="task-modal-overlay">
      <div
        className="modal"
        onClick={(e) => e.stopPropagation()}
        data-testid="task-modal"
        role="dialog"
        aria-modal="true"
      >
        <header className="modal-header">
          <h2 data-testid="task-modal-title">{isNew ? 'Новая задача' : 'Редактирование'}</h2>
          <button type="button" className="icon-btn" onClick={onClose} data-testid="task-modal-close">
            ✕
          </button>
        </header>

        <form onSubmit={handleSubmit} data-testid="task-form">
          {error && (
            <div className="alert alert-error" data-testid="task-form-error">
              {error}
            </div>
          )}

          <label htmlFor="task-title-input">Title</label>
          <input
            id="task-title-input"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            data-testid="task-title-input"
            required
            maxLength={255}
          />

          <label htmlFor="task-desc-input">Description</label>
          <textarea
            id="task-desc-input"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            data-testid="task-description-input"
          />

          <div className="form-row">
            <div>
              <label htmlFor="task-priority-select">Priority</label>
              <select
                id="task-priority-select"
                value={priority}
                onChange={(e) => setPriority(e.target.value as TaskPriority)}
                data-testid="task-priority-select"
              >
                {PRIORITIES.map((p) => (
                  <option key={p} value={p}>
                    {p}
                  </option>
                ))}
              </select>
            </div>
            {!isNew && (
              <div>
                <label htmlFor="task-status-select">Status</label>
                <select
                  id="task-status-select"
                  value={status}
                  onChange={(e) => setStatus(e.target.value as TaskStatus)}
                  data-testid="task-status-select"
                >
                  {allowedStatuses(task.status).map((s) => (
                    <option key={s} value={s}>
                      {s.replace('_', ' ')}
                    </option>
                  ))}
                </select>
              </div>
            )}
            <div>
              <label htmlFor="task-due-input">Due date</label>
              <input
                id="task-due-input"
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
                data-testid="task-due-input"
              />
            </div>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} data-testid="task-cancel-btn">
              Отмена
            </button>
            <button type="submit" disabled={saving} data-testid="task-save-btn">
              {saving ? 'Сохранение…' : 'Сохранить'}
            </button>
          </div>
        </form>

        {!isNew && detail && (
          <section className="comments-section" data-testid="task-comments-section">
            <h3>Comments ({detail.commentsCount})</h3>
            <ul className="comments-list">
              {comments.map((c) => (
                <li key={c.id} data-testid={`comment-${c.id}`}>
                  <strong>{c.authorName}</strong>
                  <span>{c.body}</span>
                </li>
              ))}
            </ul>
            <form onSubmit={handleAddComment} className="comment-form">
              <input
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                placeholder="Add a comment…"
                data-testid="comment-input"
              />
              <button type="submit" data-testid="comment-submit-btn">
                Send
              </button>
            </form>
          </section>
        )}
      </div>
    </div>
  )
}
