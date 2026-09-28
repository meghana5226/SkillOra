export function Spinner({ label = 'Loading…' }) {
  return (
    <div className="flex items-center justify-center gap-2 py-10 text-slate-400">
      <svg className="h-5 w-5 animate-spin" viewBox="0 0 24 24" fill="none">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
      </svg>
      <span className="text-sm">{label}</span>
    </div>
  )
}

export function EmptyState({ title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl2 border border-dashed border-slate-200 bg-white/60 px-6 py-14 text-center">
      <h3 className="font-display text-base font-semibold text-slate-800">{title}</h3>
      {description && <p className="mt-1.5 max-w-sm text-sm text-slate-500">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}

export function Alert({ type = 'error', message }) {
  if (!message) return null
  const styles =
    type === 'error'
      ? 'bg-red-50 text-red-700 border-red-100'
      : type === 'success'
      ? 'bg-emerald-50 text-emerald-700 border-emerald-100'
      : 'bg-brand-50 text-brand-700 border-brand-100'
  return <div className={`rounded-lg border px-4 py-2.5 text-sm ${styles}`}>{message}</div>
}
