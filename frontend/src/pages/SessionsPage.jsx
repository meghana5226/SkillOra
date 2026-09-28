import { useEffect, useState } from 'react'
import { sessionService } from '../services/domainServices'
import { useAuth } from '../context/AuthContext.jsx'
import { getErrorMessage } from '../services/api'
import { Alert, EmptyState, Spinner } from '../components/Feedback.jsx'
import ReviewModal from '../components/ReviewModal.jsx'

const TABS = ['upcoming', 'history']

export default function SessionsPage() {
  const { user } = useAuth()
  const [tab, setTab] = useState('upcoming')
  const [sessions, setSessions] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reviewTarget, setReviewTarget] = useState(null)

  function load() {
    setLoading(true)
    const call = tab === 'upcoming' ? sessionService.upcoming() : sessionService.history()
    call.then(setSessions).finally(() => setLoading(false))
  }

  useEffect(load, [tab])

  async function act(action, id) {
    setError('')
    try {
      await action(id)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  return (
    <div>
      <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Sessions</h1>
      <div className="mt-4 flex gap-2">
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`rounded-lg px-4 py-2 text-sm font-medium capitalize ${
              tab === t ? 'bg-brand-600 text-white' : 'bg-white text-slate-600 border border-slate-200'
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      <div className="mt-4"><Alert message={error} /></div>

      <div className="mt-4">
        {loading ? (
          <Spinner />
        ) : !sessions || sessions.length === 0 ? (
          <EmptyState title="No sessions" description="Accept a swap request, then schedule a session from your conversation." />
        ) : (
          <div className="space-y-3">
            {sessions.map((s) => {
              const isReceiver = s.receiverId === user.id
              const otherName = isReceiver ? s.requesterName : s.receiverName
              return (
                <div key={s.id} className="card flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">
                      {s.skillName} with {otherName}
                    </p>
                    <p className="text-sm text-slate-500">{s.scheduledDate} · {s.startTime}–{s.endTime}</p>
                    {s.meetingLink && (
                      <a href={s.meetingLink} target="_blank" rel="noreferrer" className="text-xs text-brand-600 underline">
                        Meeting link
                      </a>
                    )}
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    <StatusBadge status={s.status} />
                    {s.status === 'REQUESTED' && isReceiver && (
                      <button onClick={() => act(sessionService.confirm, s.id)} className="btn-primary text-xs">Confirm</button>
                    )}
                    {s.status === 'CONFIRMED' && (
                      <button onClick={() => act(sessionService.complete, s.id)} className="btn-primary text-xs">Mark complete</button>
                    )}
                    {(s.status === 'REQUESTED' || s.status === 'CONFIRMED') && (
                      <button onClick={() => act(sessionService.cancel, s.id)} className="btn-secondary text-xs">Cancel</button>
                    )}
                    {s.status === 'COMPLETED' && !s.hasReview && (
                      <button onClick={() => setReviewTarget(s)} className="btn-secondary text-xs">Leave review</button>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {reviewTarget && (
        <ReviewModal session={reviewTarget} onClose={() => setReviewTarget(null)} onSubmitted={load} />
      )}
    </div>
  )
}

function StatusBadge({ status }) {
  const colors = {
    REQUESTED: 'bg-amber-50 text-amber-700',
    CONFIRMED: 'bg-brand-50 text-brand-700',
    COMPLETED: 'bg-emerald-50 text-emerald-700',
    CANCELLED: 'bg-slate-100 text-slate-500',
  }
  return <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${colors[status] || 'bg-slate-100 text-slate-600'}`}>{status}</span>
}
