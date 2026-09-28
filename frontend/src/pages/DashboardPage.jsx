import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { matchService, sessionService, goalService, notificationService } from '../services/domainServices'
import { userService } from '../services/userService'
import { Spinner, EmptyState } from '../components/Feedback.jsx'

export default function DashboardPage() {
  const { user } = useAuth()
  const [matches, setMatches] = useState([])
  const [sessions, setSessions] = useState([])
  const [goals, setGoals] = useState([])
  const [activity, setActivity] = useState([])
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let mounted = true
    Promise.all([
      matchService.myMatches(6),
      sessionService.upcoming(),
      goalService.list(),
      notificationService.list({ size: 6 }),
      userService.getById(user.id),
    ])
      .then(([m, s, g, n, profile]) => {
        if (!mounted) return
        setMatches(m)
        setSessions(s.slice(0, 4))
        setGoals(g.slice(0, 3))
        setActivity(n.content)
        setStats(profile)
      })
      .finally(() => mounted && setLoading(false))
    return () => { mounted = false }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (loading) return <Spinner label="Loading your dashboard…" />

  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">
          Good to see you, {user.name.split(' ')[0]} 👋
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">Here's what's happening with your skill exchanges.</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <StatCard label="XP" value={stats?.points ?? 0} />
        <StatCard label="Level" value={stats?.level ?? 1} />
        <StatCard label="Teaching sessions" value={stats?.completedSessionsAsTeacher ?? 0} />
        <StatCard label="Learning sessions" value={stats?.completedSessionsAsLearner ?? 0} />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Matches */}
        <div className="lg:col-span-2">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-display text-lg font-semibold text-slate-900 dark:text-white">Recommended matches</h2>
            <Link to="/discover" className="text-sm font-medium text-brand-600">See all</Link>
          </div>
          {matches.length === 0 ? (
            <EmptyState
              title="No matches yet"
              description="Add skills you can teach and want to learn to start getting matched."
              action={<Link to="/profile/me" className="btn-primary">Update your skills</Link>}
            />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              {matches.map((m) => (
                <div key={m.userId} className="card">
                  <div className="flex items-center gap-3">
                    <span className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
                      {m.name[0]}
                    </span>
                    <div className="min-w-0">
                      <p className="truncate text-sm font-semibold text-slate-800 dark:text-slate-100">{m.name}</p>
                      <p className="truncate text-xs text-slate-400">{m.location || 'Location not set'}</p>
                    </div>
                    <span className="badge ml-auto shrink-0">{m.matchPercentage}% Match</span>
                  </div>
                  <p className="mt-3 text-sm text-slate-600 dark:text-slate-300">{m.explanation}</p>
                  <Link to={`/profile/${m.userId}`} className="btn-secondary mt-3 w-full justify-center text-xs">View Profile</Link>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Sidebar: sessions + goals */}
        <div className="space-y-6">
          <div>
            <h2 className="mb-3 font-display text-lg font-semibold text-slate-900 dark:text-white">Upcoming sessions</h2>
            {sessions.length === 0 ? (
              <EmptyState title="Nothing scheduled" description="Accept a swap request to schedule your first session." />
            ) : (
              <div className="space-y-3">
                {sessions.map((s) => (
                  <div key={s.id} className="card">
                    <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">{s.skillName}</p>
                    <p className="text-xs text-slate-500">{s.scheduledDate} · {s.startTime}–{s.endTime}</p>
                    <span className="badge mt-2">{s.status}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            <h2 className="mb-3 font-display text-lg font-semibold text-slate-900 dark:text-white">Learning goals</h2>
            {goals.length === 0 ? (
              <EmptyState title="No goals yet" description="Set a learning goal to track your progress." />
            ) : (
              <div className="space-y-3">
                {goals.map((g) => (
                  <div key={g.id} className="card">
                    <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">{g.title}</p>
                    <div className="mt-2 h-2 w-full rounded-full bg-slate-100">
                      <div className="h-2 rounded-full bg-brand-600" style={{ width: `${g.progress}%` }} />
                    </div>
                    <p className="mt-1 text-xs text-slate-400">{g.progress}% complete</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Recent activity */}
      <div>
        <h2 className="mb-3 font-display text-lg font-semibold text-slate-900 dark:text-white">Recent activity</h2>
        {activity.length === 0 ? (
          <EmptyState title="No activity yet" description="Your notifications will show up here." />
        ) : (
          <div className="card divide-y divide-slate-100">
            {activity.map((a) => (
              <div key={a.id} className="flex items-center justify-between py-2.5 text-sm">
                <span className="text-slate-700 dark:text-slate-200">{a.title}</span>
                <span className="text-xs text-slate-400">{new Date(a.createdAt).toLocaleDateString()}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function StatCard({ label, value }) {
  return (
    <div className="card text-center">
      <p className="font-display text-2xl font-bold text-brand-600">{value}</p>
      <p className="mt-1 text-xs text-slate-500">{label}</p>
    </div>
  )
}
