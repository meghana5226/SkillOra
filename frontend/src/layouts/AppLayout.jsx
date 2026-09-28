import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useTheme } from '../context/ThemeContext.jsx'
import { notificationService } from '../services/domainServices'

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: '🏠' },
  { to: '/discover', label: 'Discover', icon: '🔍' },
  { to: '/swaps', label: 'Swap Requests', icon: '🔄' },
  { to: '/sessions', label: 'Sessions', icon: '📅' },
  { to: '/messages', label: 'Messages', icon: '💬' },
  { to: '/goals', label: 'Learning Goals', icon: '🎯' },
  { to: '/profile/me', label: 'My Profile', icon: '👤' },
]

export default function AppLayout() {
  const { user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    let mounted = true
    notificationService
      .unreadCount()
      .then((d) => mounted && setUnread(d.unread))
      .catch(() => {})
    const interval = setInterval(() => {
      notificationService.unreadCount().then((d) => mounted && setUnread(d.unread)).catch(() => {})
    }, 20000)
    return () => {
      mounted = false
      clearInterval(interval)
    }
  }, [])

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <div className="flex min-h-screen bg-slate-50 dark:bg-slate-900">
      <aside className="hidden w-64 flex-col border-r border-slate-100 bg-white p-5 dark:border-slate-800 dark:bg-slate-900 md:flex">
        <Link to="/dashboard" className="mb-8 flex items-center gap-2 font-display text-lg font-bold text-slate-900 dark:text-white">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-brand-500 to-accent-500 text-white">S</span>
          Skillora
        </Link>
        <nav className="flex flex-1 flex-col gap-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
                  isActive
                    ? 'bg-brand-50 text-brand-700 dark:bg-brand-900/40 dark:text-brand-200'
                    : 'text-slate-600 hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-800'
                }`
              }
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
          {user?.role === 'ADMIN' && (
            <NavLink
              to="/admin"
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
                  isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-800'
                }`
              }
            >
              <span>🛠️</span>
              Admin
            </NavLink>
          )}
        </nav>
        <button onClick={handleLogout} className="btn-secondary mt-4 justify-center">
          Log out
        </button>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-3 dark:border-slate-800 dark:bg-slate-900">
          <div className="text-sm text-slate-500 dark:text-slate-400">
            {user && <>Good to see you, <span className="font-semibold text-slate-800 dark:text-slate-100">{user.name?.split(' ')[0]}</span> 👋</>}
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={toggleTheme}
              className="rounded-lg border border-slate-200 p-2 text-sm dark:border-slate-700"
              title="Toggle dark mode"
            >
              {theme === 'dark' ? '☀️' : '🌙'}
            </button>
            <Link to="/notifications" className="relative rounded-lg border border-slate-200 p-2 text-sm dark:border-slate-700">
              🔔
              {unread > 0 && (
                <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
                  {unread > 9 ? '9+' : unread}
                </span>
              )}
            </Link>
            <Link to="/profile/me" className="flex items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-slate-50 dark:hover:bg-slate-800">
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
                {user?.name?.[0]?.toUpperCase() || '?'}
              </span>
              <span className="hidden text-sm font-medium text-slate-700 dark:text-slate-200 sm:block">
                Lvl {user?.level ?? 1}
              </span>
            </Link>
          </div>
        </header>
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
