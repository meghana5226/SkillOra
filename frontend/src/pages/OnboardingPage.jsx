import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { userService } from '../services/userService'
import { skillService, goalService } from '../services/domainServices'
import { getErrorMessage } from '../services/api'
import { Alert } from '../components/Feedback.jsx'

const STEPS = ['Profile', 'Teach', 'Learn', 'Experience', 'Availability', 'Goals']

export default function OnboardingPage() {
  const { refreshUser } = useAuth()
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [skills, setSkills] = useState([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  const [profile, setProfile] = useState({ bio: '', location: '', timezone: '' })
  const [teachSkillId, setTeachSkillId] = useState('')
  const [learnSkillId, setLearnSkillId] = useState('')
  const [experienceLevel, setExperienceLevel] = useState('BEGINNER')
  const [availability, setAvailability] = useState('')
  const [goalTitle, setGoalTitle] = useState('')

  useEffect(() => {
    skillService.list().then(setSkills).catch(() => {})
  }, [])

  async function next() {
    setError('')
    setSaving(true)
    try {
      if (step === 0) {
        await userService.updateMe(profile)
      } else if (step === 1 && teachSkillId) {
        await userService.addOfferedSkill({ skillId: Number(teachSkillId), proficiencyLevel: 'INTERMEDIATE', yearsExperience: 1 })
      } else if (step === 2 && learnSkillId) {
        await userService.addWantedSkill({ skillId: Number(learnSkillId), targetLevel: 'INTERMEDIATE', priority: 1 })
      } else if (step === 3) {
        await userService.updateMe({ experienceLevel })
      } else if (step === 4 && availability) {
        await userService.updateMe({ availability })
      } else if (step === 5 && goalTitle) {
        await goalService.create({ title: goalTitle })
      }

      if (step === STEPS.length - 1) {
        await refreshUser()
        navigate('/dashboard')
      } else {
        setStep(step + 1)
      }
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  function skip() {
    if (step === STEPS.length - 1) {
      refreshUser().then(() => navigate('/dashboard'))
    } else {
      setStep(step + 1)
    }
  }

  return (
    <div className="mx-auto max-w-xl px-6 py-16">
      <div className="mb-8 flex items-center gap-2">
        {STEPS.map((s, i) => (
          <div key={s} className={`h-1.5 flex-1 rounded-full ${i <= step ? 'bg-brand-600' : 'bg-slate-200'}`} />
        ))}
      </div>
      <h1 className="font-display text-2xl font-bold text-slate-900">{STEPS[step]}</h1>

      <div className="mt-6 space-y-4">
        {step === 0 && (
          <>
            <div>
              <label className="label">Short bio</label>
              <textarea className="input-field" rows={3} value={profile.bio} onChange={(e) => setProfile({ ...profile, bio: e.target.value })} />
            </div>
            <div>
              <label className="label">Location</label>
              <input className="input-field" value={profile.location} onChange={(e) => setProfile({ ...profile, location: e.target.value })} />
            </div>
            <div>
              <label className="label">Timezone</label>
              <input className="input-field" placeholder="e.g. UTC, IST, EST" value={profile.timezone} onChange={(e) => setProfile({ ...profile, timezone: e.target.value })} />
            </div>
          </>
        )}

        {step === 1 && (
          <div>
            <label className="label">What can you teach?</label>
            <select className="input-field" value={teachSkillId} onChange={(e) => setTeachSkillId(e.target.value)}>
              <option value="">Select a skill…</option>
              {skills.map((s) => (
                <option key={s.id} value={s.id}>{s.name} · {s.category}</option>
              ))}
            </select>
            <p className="mt-2 text-xs text-slate-400">You can add more skills later from your profile.</p>
          </div>
        )}

        {step === 2 && (
          <div>
            <label className="label">What do you want to learn?</label>
            <select className="input-field" value={learnSkillId} onChange={(e) => setLearnSkillId(e.target.value)}>
              <option value="">Select a skill…</option>
              {skills.map((s) => (
                <option key={s.id} value={s.id}>{s.name} · {s.category}</option>
              ))}
            </select>
          </div>
        )}

        {step === 3 && (
          <div>
            <label className="label">Overall experience level</label>
            <select className="input-field" value={experienceLevel} onChange={(e) => setExperienceLevel(e.target.value)}>
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
              <option value="MENTOR">Mentor</option>
            </select>
          </div>
        )}

        {step === 4 && (
          <div>
            <label className="label">When are you usually available?</label>
            <input className="input-field" placeholder="e.g. Saturday morning, weekday evenings" value={availability} onChange={(e) => setAvailability(e.target.value)} />
          </div>
        )}

        {step === 5 && (
          <div>
            <label className="label">Set a learning goal (optional)</label>
            <input className="input-field" placeholder="e.g. Become interview-ready in Java" value={goalTitle} onChange={(e) => setGoalTitle(e.target.value)} />
          </div>
        )}

        <Alert message={error} />
      </div>

      <div className="mt-8 flex items-center justify-between">
        <button onClick={skip} className="text-sm font-medium text-slate-400 hover:text-slate-600">Skip</button>
        <button onClick={next} disabled={saving} className="btn-primary">
          {saving ? 'Saving…' : step === STEPS.length - 1 ? 'Finish' : 'Continue'}
        </button>
      </div>
    </div>
  )
}
