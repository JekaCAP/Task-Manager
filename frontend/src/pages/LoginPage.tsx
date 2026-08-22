import { type FormEvent, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { ApiClientError } from '../api'
import { useAuth } from '../auth'

export function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const [email, setEmail] = useState('qa@demo.com')
  const [password, setPassword] = useState('Demo123!')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
    } catch (err) {
      const msg = err instanceof ApiClientError ? err.message : 'Login failed'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page" data-testid="login-page">
      <div className="login-card">
        <div className="login-brand">
          <span className="logo-dot" />
          <h1>Task Manager</h1>
          <p>Sandbox для Java AQA — войдите, чтобы работать с задачами</p>
        </div>

        <form onSubmit={handleSubmit} data-testid="login-form">
          {error && (
            <div className="alert alert-error" data-testid="login-error">
              {error}
            </div>
          )}

          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            autoComplete="username"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            data-testid="login-email-input"
            required
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            data-testid="login-password-input"
            required
          />

          <button type="submit" disabled={loading} data-testid="login-submit-btn">
            {loading ? 'Вход…' : 'Войти'}
          </button>
        </form>

        <p className="login-hint">
          Demo: <code>qa@demo.com</code> / <code>Demo123!</code>
        </p>
      </div>
    </div>
  )
}
