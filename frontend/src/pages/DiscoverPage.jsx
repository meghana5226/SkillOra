import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { userService } from '../services/userService'
import { Spinner, EmptyState } from '../components/Feedback.jsx'

export default function DiscoverPage() {
  const [query, setQuery] = useState('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    const handle = setTimeout(() => {
      userService
        .discover({ query: query || undefined, page, size: 9 })
        .then(setData)
        .finally(() => setLoading(false))
    }, 300)
    return () => clearTimeout(handle)
  }, [query, page])

  return (
    <div>
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Discover</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">Search Skillora members by name.</p>
        </div>
        <input
          className="input-field max-w-xs"
          placeholder="Search by name…"
          value={query}
          onChange={(e) => { setQuery(e.target.value); setPage(0) }}
        />
      </div>

      {loading ? (
        <Spinner />
      ) : !data || data.content.length === 0 ? (
        <EmptyState title="No members found" description="Try a different search term." />
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((u) => (
              <div key={u.id} className="card">
                <div className="flex items-center gap-3">
                  <span className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
                    {u.name[0]}
                  </span>
                  <div className="min-w-0">
                    <p className="truncate text-sm font-semibold text-slate-800 dark:text-slate-100">{u.name}</p>
                    <p className="truncate text-xs text-slate-400">{u.location || 'Location not set'}</p>
                  </div>
                </div>
                {u.reputationScore != null && Number(u.reputationScore) > 0 && (
                  <p className="mt-2 text-xs text-amber-500">★ {Number(u.reputationScore).toFixed(1)}</p>
                )}
                <Link to={`/profile/${u.id}`} className="btn-secondary mt-3 w-full justify-center text-xs">View Profile</Link>
              </div>
            ))}
          </div>

          <div className="mt-6 flex items-center justify-center gap-3">
            <button
              disabled={data.first}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="btn-secondary disabled:opacity-40"
            >
              Previous
            </button>
            <span className="text-sm text-slate-500">Page {data.number + 1} of {Math.max(1, data.totalPages)}</span>
            <button
              disabled={data.last}
              onClick={() => setPage((p) => p + 1)}
              className="btn-secondary disabled:opacity-40"
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  )
}
