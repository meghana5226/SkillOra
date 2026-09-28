import { useEffect, useState } from 'react'
import { notificationService } from '../services/domainServices'
import { EmptyState, Spinner } from '../components/Feedback.jsx'

export default function NotificationsPage() {
  const [data, setData] = useState(null)

  function load() {
    notificationService.list({ size: 50 }).then(setData)
  }

  useEffect(load, [])

  async function markAll() {
    await notificationService.markAllRead()
    load()
  }

  async function markOne(id) {
    await notificationService.markRead(id)
    load()
  }

  if (!data) return <Spinner />

  return (
    <div className="mx-auto max-w-2xl">
      <div className="flex items-center justify-between">
        <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Notifications</h1>
        <button onClick={markAll} className="btn-secondary text-xs">Mark all as read</button>
      </div>

      <div className="mt-4 space-y-3">
        {data.content.length === 0 ? (
          <EmptyState title="You're all caught up" description="New activity will appear here." />
        ) : (
          data.content.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.read && markOne(n.id)}
              className={`card cursor-pointer ${n.read ? 'opacity-70' : 'border-brand-200'}`}
            >
              <div className="flex items-center justify-between">
                <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">
                  {!n.read && <span className="mr-2 inline-block h-2 w-2 rounded-full bg-brand-600" />}
                  {n.title}
                </p>
                <span className="text-xs text-slate-400">{new Date(n.createdAt).toLocaleString()}</span>
              </div>
              <p className="mt-1 text-sm text-slate-500">{n.message}</p>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
