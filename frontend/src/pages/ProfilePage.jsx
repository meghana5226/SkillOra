import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { userService } from '../services/userService'
import { reviewService, messageService } from '../services/domainServices'
import { Spinner, EmptyState } from '../components/Feedback.jsx'
import SwapRequestModal from '../components/SwapRequestModal.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProfilePage() {
  const { id } = useParams()
  const { user: me } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [myProfile, setMyProfile] = useState(null)
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      userService.getById(id),
      userService.getById(me.id),
      reviewService.forUser(id, { size: 10 }),
    ])
      .then(([p, mp, r]) => {
        setProfile(p)
        setMyProfile(mp)
        setReviews(r.content)
      })
      .finally(() => setLoading(false))
  }, [id, me.id])

  async function handleMessage() {
    const conversationId = await messageService.startWith(id)
    navigate(`/messages/${conversationId}`)
  }

  if (loading) return <Spinner />
  if (!profile) return <EmptyState title="Profile not found" />

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="card">
        <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-center">
          <span className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-100 text-xl font-semibold text-brand-700">
            {profile.name[0]}
          </span>
          <div className="flex-1">
            <h1 className="font-display text-xl font-bold text-slate-900 dark:text-white">{profile.name}</h1>
            <p className="text-sm text-slate-500">{profile.location || 'Location not set'}</p>
            {profile.averageRating && (
              <p className="mt-1 text-sm text-amber-500">★ {profile.averageRating.toFixed(1)} ({profile.reviewCount} reviews)</p>
            )}
          </div>
          <div className="flex gap-2">
            <button onClick={handleMessage} className="btn-secondary">Message</button>
            <button onClick={() => setShowModal(true)} className="btn-primary">Send Swap Request</button>
          </div>
        </div>
        {profile.bio && <p className="mt-4 text-sm text-slate-600 dark:text-slate-300">{profile.bio}</p>}
        <div className="mt-4 grid grid-cols-3 gap-4 border-t border-slate-100 pt-4 text-center dark:border-slate-700">
          <div>
            <p className="font-display text-lg font-bold text-slate-800 dark:text-white">{profile.completedSessionsAsTeacher ?? 0}</p>
            <p className="text-xs text-slate-400">Sessions taught</p>
          </div>
          <div>
            <p className="font-display text-lg font-bold text-slate-800 dark:text-white">{profile.completedSessionsAsLearner ?? 0}</p>
            <p className="text-xs text-slate-400">Sessions learned</p>
          </div>
          <div>
            <p className="font-display text-lg font-bold text-slate-800 dark:text-white">Lvl {profile.level}</p>
            <p className="text-xs text-slate-400">{profile.points} XP</p>
          </div>
        </div>
      </div>

      <div className="grid gap-6 sm:grid-cols-2">
        <div>
          <h2 className="mb-2 font-display text-sm font-semibold text-slate-800 dark:text-white">Can teach</h2>
          <div className="flex flex-wrap gap-2">
            {(profile.offeredSkills || []).length === 0 && <p className="text-sm text-slate-400">Nothing listed yet.</p>}
            {(profile.offeredSkills || []).map((s) => (
              <span key={s.id} className="badge">{s.skillName} · {s.proficiencyLevel}</span>
            ))}
          </div>
        </div>
        <div>
          <h2 className="mb-2 font-display text-sm font-semibold text-slate-800 dark:text-white">Wants to learn</h2>
          <div className="flex flex-wrap gap-2">
            {(profile.wantedSkills || []).length === 0 && <p className="text-sm text-slate-400">Nothing listed yet.</p>}
            {(profile.wantedSkills || []).map((s) => (
              <span key={s.id} className="badge">{s.skillName} · {s.targetLevel}</span>
            ))}
          </div>
        </div>
      </div>

      <div>
        <h2 className="mb-3 font-display text-sm font-semibold text-slate-800 dark:text-white">Reviews</h2>
        {reviews.length === 0 ? (
          <EmptyState title="No reviews yet" />
        ) : (
          <div className="space-y-3">
            {reviews.map((r) => (
              <div key={r.id} className="card">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-semibold text-slate-800 dark:text-white">{r.reviewerName}</p>
                  <span className="text-amber-500">{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
                </div>
                {r.comment && <p className="mt-1 text-sm text-slate-600 dark:text-slate-300">{r.comment}</p>}
              </div>
            ))}
          </div>
        )}
      </div>

      {showModal && (
        <SwapRequestModal
          receiver={profile}
          myOfferedSkills={myProfile.offeredSkills}
          theirOfferedSkills={profile.offeredSkills}
          onClose={() => setShowModal(false)}
        />
      )}
    </div>
  )
}
