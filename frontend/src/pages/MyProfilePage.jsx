import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import { userService } from '../services/userService'
import { skillService } from '../services/domainServices'
import { getErrorMessage } from '../services/api'
import { Alert, Spinner } from '../components/Feedback.jsx'

export default function MyProfilePage() {
  const { user, refreshUser } = useAuth()
  const [profile, setProfile] = useState(null)
  const [skills, setSkills] = useState([])
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [newOffered, setNewOffered] = useState({ skillId: '', proficiencyLevel: 'INTERMEDIATE', yearsExperience: 1 })
  const [newWanted, setNewWanted] = useState({ skillId: '', targetLevel: 'INTERMEDIATE', priority: 1 })

  function load() {
    Promise.all([userService.getById(user.id), skillService.list()]).then(([p, s]) => {
      setProfile(p)
      setSkills(s)
      setForm({
        name: p.name, bio: p.bio || '', location: p.location || '',
        timezone: p.timezone || '', availability: p.availability || '', experienceLevel: p.experienceLevel,
      })
    })
  }

  useEffect(load, [user.id])

  async function handleSaveProfile(e) {
    e.preventDefault()
    setError(''); setSuccess(''); setSaving(true)
    try {
      await userService.updateMe(form)
      await refreshUser()
      setSuccess('Profile updated.')
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleAddOffered(e) {
    e.preventDefault()
    if (!newOffered.skillId) return
    setError('')
    try {
      await userService.addOfferedSkill({ ...newOffered, skillId: Number(newOffered.skillId) })
      setNewOffered({ skillId: '', proficiencyLevel: 'INTERMEDIATE', yearsExperience: 1 })
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  async function handleAddWanted(e) {
    e.preventDefault()
    if (!newWanted.skillId) return
    setError('')
    try {
      await userService.addWantedSkill({ ...newWanted, skillId: Number(newWanted.skillId) })
      setNewWanted({ skillId: '', targetLevel: 'INTERMEDIATE', priority: 1 })
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  async function removeOffered(id) {
    await userService.removeOfferedSkill(id)
    load()
  }

  async function removeWanted(id) {
    await userService.removeWantedSkill(id)
    load()
  }

  if (!profile || !form) return <Spinner />

  return (
    <div className="mx-auto max-w-3xl space-y-8">
      <div>
        <h1 className="font-display text-2xl font-bold text-slate-900 dark:text-white">My Profile</h1>
        <p className="text-sm text-slate-500">
          Your public Skill Passport is shareable at <code className="rounded bg-slate-100 px-1.5 py-0.5 text-xs">/profile/{profile.id}</code>
        </p>
      </div>

      <form onSubmit={handleSaveProfile} className="card space-y-4">
        <h2 className="font-display text-sm font-semibold text-slate-800 dark:text-white">Basic info</h2>
        <div>
          <label className="label">Name</label>
          <input className="input-field" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </div>
        <div>
          <label className="label">Bio</label>
          <textarea className="input-field" rows={3} value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Location</label>
            <input className="input-field" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
          </div>
          <div>
            <label className="label">Timezone</label>
            <input className="input-field" value={form.timezone} onChange={(e) => setForm({ ...form, timezone: e.target.value })} />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Experience level</label>
            <select className="input-field" value={form.experienceLevel} onChange={(e) => setForm({ ...form, experienceLevel: e.target.value })}>
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
              <option value="MENTOR">Mentor</option>
            </select>
          </div>
          <div>
            <label className="label">Availability</label>
            <input className="input-field" value={form.availability} onChange={(e) => setForm({ ...form, availability: e.target.value })} />
          </div>
        </div>
        <Alert message={error} />
        <Alert type="success" message={success} />
        <button disabled={saving} className="btn-primary">{saving ? 'Saving…' : 'Save changes'}</button>
      </form>

      <div className="card">
        <h2 className="font-display text-sm font-semibold text-slate-800 dark:text-white">Skills I can teach</h2>
        <div className="mt-3 flex flex-wrap gap-2">
          {(profile.offeredSkills || []).map((s) => (
            <span key={s.id} className="badge flex items-center gap-1.5">
              {s.skillName} · {s.proficiencyLevel}
              <button onClick={() => removeOffered(s.id)} className="text-brand-400 hover:text-brand-700">×</button>
            </span>
          ))}
        </div>
        <form onSubmit={handleAddOffered} className="mt-4 flex flex-wrap gap-2">
          <select className="input-field max-w-[200px]" value={newOffered.skillId} onChange={(e) => setNewOffered({ ...newOffered, skillId: e.target.value })}>
            <option value="">Add a skill…</option>
            {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
          <select className="input-field max-w-[160px]" value={newOffered.proficiencyLevel} onChange={(e) => setNewOffered({ ...newOffered, proficiencyLevel: e.target.value })}>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
            <option value="MENTOR">Mentor</option>
          </select>
          <button className="btn-secondary">Add</button>
        </form>
      </div>

      <div className="card">
        <h2 className="font-display text-sm font-semibold text-slate-800 dark:text-white">Skills I want to learn</h2>
        <div className="mt-3 flex flex-wrap gap-2">
          {(profile.wantedSkills || []).map((s) => (
            <span key={s.id} className="badge flex items-center gap-1.5">
              {s.skillName} · target {s.targetLevel}
              <button onClick={() => removeWanted(s.id)} className="text-brand-400 hover:text-brand-700">×</button>
            </span>
          ))}
        </div>
        <form onSubmit={handleAddWanted} className="mt-4 flex flex-wrap gap-2">
          <select className="input-field max-w-[200px]" value={newWanted.skillId} onChange={(e) => setNewWanted({ ...newWanted, skillId: e.target.value })}>
            <option value="">Add a skill…</option>
            {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
          <select className="input-field max-w-[160px]" value={newWanted.targetLevel} onChange={(e) => setNewWanted({ ...newWanted, targetLevel: e.target.value })}>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
            <option value="MENTOR">Mentor</option>
          </select>
          <button className="btn-secondary">Add</button>
        </form>
      </div>
    </div>
  )
}
