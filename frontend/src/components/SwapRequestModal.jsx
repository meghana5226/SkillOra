import { useState } from 'react'
import { swapService } from '../services/domainServices'
import { getErrorMessage } from '../services/api'
import { Alert } from './Feedback.jsx'

export default function SwapRequestModal({ receiver, myOfferedSkills, theirOfferedSkills, onClose, onSent }) {
  const [offeredSkillId, setOfferedSkillId] = useState('')
  const [requestedSkillId, setRequestedSkillId] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)

  async function handleSend(e) {
    e.preventDefault()
    setError('')
    if (!offeredSkillId || !requestedSkillId) {
      setError('Please select both a skill you offer and a skill you want.')
      return
    }
    setSending(true)
    try {
      await swapService.create({
        receiverId: receiver.id,
        offeredSkillId: Number(offeredSkillId),
        requestedSkillId: Number(requestedSkillId),
        message,
      })
      onSent?.()
      onClose()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-md rounded-xl2 bg-white p-6 shadow-card-hover dark:bg-slate-800">
        <h3 className="font-display text-lg font-semibold text-slate-900 dark:text-white">
          Send swap request to {receiver.name}
        </h3>
        <form onSubmit={handleSend} className="mt-4 space-y-4">
          <div>
            <label className="label">Skill I can teach</label>
            <select className="input-field" value={offeredSkillId} onChange={(e) => setOfferedSkillId(e.target.value)}>
              <option value="">Select…</option>
              {(myOfferedSkills || []).map((s) => (
                <option key={s.skillId} value={s.skillId}>{s.skillName}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Skill I want to learn from them</label>
            <select className="input-field" value={requestedSkillId} onChange={(e) => setRequestedSkillId(e.target.value)}>
              <option value="">Select…</option>
              {(theirOfferedSkills || []).map((s) => (
                <option key={s.skillId} value={s.skillId}>{s.skillName}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Message</label>
            <textarea className="input-field" rows={3} value={message} onChange={(e) => setMessage(e.target.value)} />
          </div>
          <Alert message={error} />
          <div className="flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={sending} className="btn-primary">{sending ? 'Sending…' : 'Send request'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}
