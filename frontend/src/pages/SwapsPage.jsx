import { useEffect, useState } from 'react'
import { swapService } from '../services/domainServices'
import { getErrorMessage } from '../services/api'
import { Alert, EmptyState, Spinner } from '../components/Feedback.jsx'

const TABS = ['received', 'sent']

export default function SwapsPage() {
  const [tab, setTab] = useState('received')
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  function load() {
    setLoading(true)
    const call = tab === 'received' ? swapService.received({ size: 20 }) : swapService.sent({ size: 20 })
    call.then(setData).finally(() => setLoading(false))
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
      <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Swap Requests</h1>
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
        ) : !data || data.content.length === 0 ? (
          <EmptyState title="Nothing here yet" description={tab === 'received' ? 'Swap requests sent to you will show up here.' : 'Requests you send will show up here.'} />
        ) : (
          <div className="space-y-3">
            {data.content.map((s) => (
              <div key={s.id} className="card flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">
                    {tab === 'received' ? s.senderName : s.receiverName}
                  </p>
                  <p className="text-sm text-slate-500">
                    Offers <strong>{s.offeredSkillName}</strong> for <strong>{s.requestedSkillName}</strong>
                  </p>
                  {s.message && <p className="mt-1 text-xs italic text-slate-400">"{s.message}"</p>}
                </div>
                <div className="flex items-center gap-2">
                  <StatusBadge status={s.status} />
                  {tab === 'received' && s.status === 'PENDING' && (
                    <>
                      <button onClick={() => act(swapService.accept, s.id)} className="btn-primary text-xs">Accept</button>
                      <button onClick={() => act(swapService.reject, s.id)} className="btn-secondary text-xs">Reject</button>
                    </>
                  )}
                  {tab === 'sent' && (s.status === 'PENDING' || s.status === 'ACCEPTED') && (
                    <button onClick={() => act(swapService.cancel, s.id)} className="btn-secondary text-xs">Cancel</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function StatusBadge({ status }) {
  const colors = {
    PENDING: 'bg-amber-50 text-amber-700',
    ACCEPTED: 'bg-emerald-50 text-emerald-700',
    REJECTED: 'bg-red-50 text-red-700',
    CANCELLED: 'bg-slate-100 text-slate-500',
    COMPLETED: 'bg-brand-50 text-brand-700',
  }
  return <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${colors[status] || 'bg-slate-100 text-slate-600'}`}>{status}</span>
}
