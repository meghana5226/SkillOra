import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { authService } from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('skillora_user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('skillora_token')
    if (!token) {
      setLoading(false)
      return
    }
    authService
      .me()
      .then((profile) => {
        setUser(profile)
        localStorage.setItem('skillora_user', JSON.stringify(profile))
      })
      .catch(() => {
        localStorage.removeItem('skillora_token')
        localStorage.removeItem('skillora_user')
        setUser(null)
      })
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const login = useCallback(async (credentials) => {
    const data = await authService.login(credentials)
    localStorage.setItem('skillora_token', data.token)
    localStorage.setItem('skillora_user', JSON.stringify(data.user))
    setUser(data.user)
    return data.user
  }, [])

  const register = useCallback(async (payload) => {
    const data = await authService.register(payload)
    localStorage.setItem('skillora_token', data.token)
    localStorage.setItem('skillora_user', JSON.stringify(data.user))
    setUser(data.user)
    return data.user
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('skillora_token')
    localStorage.removeItem('skillora_user')
    setUser(null)
  }, [])

  const refreshUser = useCallback(async () => {
    const profile = await authService.me()
    setUser(profile)
    localStorage.setItem('skillora_user', JSON.stringify(profile))
    return profile
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshUser, setUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
