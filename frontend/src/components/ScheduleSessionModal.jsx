import { useEffect, useState } from 'react'
import { sessionService } from '../services/domainServices'
import { userService } from '../services/userService'
import { useAuth } from '../context/AuthContext.jsx'
import { getErrorMessage } from '../services/api'
import { Alert } from './Feedback.jsx'

export default function ScheduleSessionModal({ otherUserId, onClose, onScheduled }) {
  const { user: me } = useAuth()
  const [otherUser, setOtherUser] = useState(null)
  const [myAvailability, setMyAvailability] = useState('')
  const [skillId, setSkillId] = useState('')
  const [date, setDate] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [meetingLink, setMeetingLink] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    Promise.all([userService.getById(otherUserId), userService.getById(me.id)]).then(([other, mine]) => {
      setOtherUser(other)
      setMyAvailability(mine.availability || '')
    })
  }, [otherUserId, me.id])

  const overlapTokens = computeOverlap(myAvailability, otherUser?.availability)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (!skillId || !date || !startTime || !endTime) {
      setError('Please fill in skill, date, and time.')
      return
    }
    setSaving(true)
    try {
      await sessionService.create({
        receiverId: Number(otherUserId),
        skillId: Number(skillId),
        scheduledDate: date,
        startTime,
        endTime,
        meetingLink,
      })
      onScheduled?.()
      onClose()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (!otherUser) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-md rounded-xl2 bg-white p-6 shadow-card-hover dark:bg-slate-800">
        <h3 className="font-display text-lg font-semibold text-slate-900 dark:text-white">
          Schedule a session with {otherUser.name}
        </h3>

        <div className="mt-3 rounded-lg bg-brand-50 p-3 text-xs text-brand-700">
          <p><strong>Your availability:</strong> {myAvailability || 'not set'}</p>
          <p><strong>{otherUser.name}'s availability:</strong> {otherUser.availability || 'not set'}</p>
          {overlapTokens.length > 0 ? (
            <p className="mt-1 font-semibold">Best overlap: {overlapTokens.join(', ')}</p>
          ) : (
            <p className="mt-1 text-brand-500">No obvious overlap detected — coordinate a time manually below.</p>
          )}
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="label">Skill</label>
            <select className="input-field" value={skillId} onChange={(e) => setSkillId(e.target.value)}>
              <option value="">Select…</option>
              {(otherUser.offeredSkills || []).map((s) => (
                <option key={s.skillId} value={s.skillId}>{s.skillName}</option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-3 gap-2">
            <div>
              <label className="label">Date</label>
              <input type="date" className="input-field" value={date} onChange={(e) => setDate(e.target.value)} />
            </div>
            <div>
              <label className="label">Start</label>
              <input type="time" className="input-field" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
            </div>
            <div>
              <label className="label">End</label>
              <input type="time" className="input-field" value={endTime} onChange={(e) => setEndTime(e.target.value)} />
            </div>
          </div>
          <div>
            <label className="label">Meeting link (optional)</label>
            <input className="input-field" placeholder="https://…" value={meetingLink} onChange={(e) => setMeetingLink(e.target.value)} />
          </div>
          <Alert message={error} />
          <div className="flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Scheduling…' : 'Schedule session'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}

function computeOverlap(a, b) {
  if (!a || !b) return []
  const tokenize = (s) => s.toLowerCase().split(/[^a-z0-9]+/).filter(Boolean)
  const setA = new Set(tokenize(a))
  const tokensB = tokenize(b)
  return tokensB.filter((t) => setA.has(t))
}
