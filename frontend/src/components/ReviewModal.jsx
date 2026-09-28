import { useState } from 'react'
import { reviewService } from '../services/domainServices'
import { getErrorMessage } from '../services/api'
import { Alert } from './Feedback.jsx'

export default function ReviewModal({ session, onClose, onSubmitted }) {
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      await reviewService.create({ sessionId: session.id, rating, comment })
      onSubmitted?.()
      onClose()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-sm rounded-xl2 bg-white p-6 shadow-card-hover dark:bg-slate-800">
        <h3 className="font-display text-lg font-semibold text-slate-900 dark:text-white">
          Rate your {session.skillName} session
        </h3>
        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div className="flex justify-center gap-1 text-3xl">
            {[1, 2, 3, 4, 5].map((n) => (
              <button type="button" key={n} onClick={() => setRating(n)} className={n <= rating ? 'text-amber-400' : 'text-slate-200'}>
                ★
              </button>
            ))}
          </div>
          <textarea className="input-field" rows={3} placeholder="Optional comment…" value={comment} onChange={(e) => setComment(e.target.value)} />
          <Alert message={error} />
          <div className="flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Submitting…' : 'Submit review'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}
