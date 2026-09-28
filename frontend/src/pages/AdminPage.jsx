import { useEffect, useState } from 'react'
import { adminService } from '../services/domainServices'
import { getErrorMessage } from '../services/api'
import { Alert, Spinner } from '../components/Feedback.jsx'

export default function AdminPage() {
  const [stats, setStats] = useState(null)
  const [users, setUsers] = useState(null)
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')

  useEffect(() => {
    adminService.dashboard().then(setStats)
  }, [])

  useEffect(() => {
    adminService.users({ page, size: 10 }).then(setUsers)
  }, [page])

  async function toggle(u) {
    setError('')
    try {
      await adminService.setUserStatus(u.id, !u.active)
      const [s, list] = await Promise.all([adminService.dashboard(), adminService.users({ page, size: 10 })])
      setStats(s)
      setUsers(list)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  if (!stats || !users) return <Spinner />

  const cards = [
    ['Total users', stats.totalUsers],
    ['Active users', stats.activeUsers],
    ['Skills', stats.totalSkills],
    ['Active swaps', stats.activeSwaps],
    ['Pending swaps', stats.pendingSwaps],
    ['Completed sessions', stats.completedSessions],
    ['Avg. rating', stats.averageRating],
  ]

  return (
    <div className="space-y-8">
      <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Admin Dashboard</h1>

      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        {cards.map(([label, value]) => (
          <div key={label} className="card text-center">
            <p className="font-display text-2xl font-bold text-brand-600">{value}</p>
            <p className="mt-1 text-xs text-slate-500">{label}</p>
          </div>
        ))}
      </div>

      <div>
        <h2 className="mb-3 font-display text-lg font-semibold text-slate-900 dark:text-white">Users</h2>
        <Alert message={error} />
        <div className="card overflow-x-auto p-0">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-100 text-xs uppercase text-slate-400">
              <tr>
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Role</th>
                <th className="px-4 py-3">XP</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {users.content.map((u) => (
                <tr key={u.id} className="border-b border-slate-50 last:border-0">
                  <td className="px-4 py-3 font-medium text-slate-800 dark:text-slate-100">{u.name}</td>
                  <td className="px-4 py-3 text-slate-500">{u.email}</td>
                  <td className="px-4 py-3 text-slate-500">{u.role}</td>
                  <td className="px-4 py-3 text-slate-500">{u.points}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${u.active ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'}`}>
                      {u.active ? 'Active' : 'Deactivated'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    {u.role !== 'ADMIN' && (
                      <button onClick={() => toggle(u)} className="btn-secondary text-xs">
                        {u.active ? 'Deactivate' : 'Activate'}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="mt-4 flex items-center justify-center gap-3">
          <button disabled={users.first} onClick={() => setPage((p) => p - 1)} className="btn-secondary disabled:opacity-40">Previous</button>
          <span className="text-sm text-slate-500">Page {users.number + 1} of {Math.max(1, users.totalPages)}</span>
          <button disabled={users.last} onClick={() => setPage((p) => p + 1)} className="btn-secondary disabled:opacity-40">Next</button>
        </div>
      </div>
    </div>
  )
}
