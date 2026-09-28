import { useEffect, useState } from 'react'
import { goalService, skillService } from '../services/domainServices'
import { getErrorMessage } from '../services/api'
import { Alert, EmptyState, Spinner } from '../components/Feedback.jsx'

export default function GoalsPage() {
  const [goals, setGoals] = useState(null)
  const [skills, setSkills] = useState([])
  const [form, setForm] = useState({ title: '', skillId: '', targetDate: '' })
  const [error, setError] = useState('')

  function load() {
    goalService.list().then(setGoals)
  }

  useEffect(() => {
    load()
    skillService.list().then(setSkills)
  }, [])

  async function handleCreate(e) {
    e.preventDefault()
    setError('')
    try {
      await goalService.create({
        title: form.title,
        skillId: form.skillId ? Number(form.skillId) : null,
        targetDate: form.targetDate || null,
      })
      setForm({ title: '', skillId: '', targetDate: '' })
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  async function updateProgress(goal, progress) {
    await goalService.update(goal.id, { progress })
    load()
  }

  if (goals === null) return <Spinner />

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">Learning Goals</h1>

      <form onSubmit={handleCreate} className="card grid gap-3 sm:grid-cols-4">
        <input required className="input-field sm:col-span-2" placeholder="e.g. Become interview-ready in Java" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        <select className="input-field" value={form.skillId} onChange={(e) => setForm({ ...form, skillId: e.target.value })}>
          <option value="">Skill (optional)</option>
          {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
        <input type="date" className="input-field" value={form.targetDate} onChange={(e) => setForm({ ...form, targetDate: e.target.value })} />
        <div className="sm:col-span-4"><Alert message={error} /></div>
        <button className="btn-primary sm:col-span-4">Add goal</button>
      </form>

      {goals.length === 0 ? (
        <EmptyState title="No goals yet" description="Create your first learning goal above." />
      ) : (
        <div className="space-y-3">
          {goals.map((g) => (
            <div key={g.id} className="card">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">{g.title}</p>
                  <p className="text-xs text-slate-400">
                    {g.skillName ? `${g.skillName} · ` : ''}{g.targetDate ? `Target ${g.targetDate}` : 'No target date'}
                  </p>
                </div>
                <span className="badge">{g.status.replace('_', ' ')}</span>
              </div>
              <div className="mt-3 h-2 w-full rounded-full bg-slate-100">
                <div className="h-2 rounded-full bg-brand-600 transition-all" style={{ width: `${g.progress}%` }} />
              </div>
              <div className="mt-2 flex items-center justify-between">
                <span className="text-xs text-slate-400">{g.progress}% complete</span>
                <input
                  type="range" min="0" max="100" step="5"
                  value={g.progress}
                  onChange={(e) => updateProgress(g, Number(e.target.value))}
                  className="w-40"
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
