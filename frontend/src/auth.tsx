import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { authApi, clearAuth, getAccessToken, getStoredUser, saveAuth } from './api'
import type { User } from './types'

interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => getStoredUser())

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(getAccessToken() && user),
      async login(email: string, password: string) {
        const res = await authApi.login(email, password)
        saveAuth(res.accessToken, res.refreshToken, res.user)
        setUser(res.user)
      },
      logout() {
        clearAuth()
        setUser(null)
      },
    }),
    [user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
