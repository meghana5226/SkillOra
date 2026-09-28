import { Link } from 'react-router-dom'

const steps = [
  { title: 'Build your profile', desc: 'Tell us about yourself, your experience level, and when you\'re free to connect.' },
  { title: 'Add skills to teach and learn', desc: 'List what you can teach others and what you\'re hoping to pick up next.' },
  { title: 'Find compatible people', desc: 'Our reciprocal match score surfaces people whose skills complement yours.' },
  { title: 'Exchange skills and grow', desc: 'Message, schedule a session, and track your progress as you go.' },
]

const categories = ['Programming', 'Data Science', 'AI & ML', 'Design', 'Marketing', 'Communication', 'Languages', 'Music', 'Finance', 'Career']

const benefits = [
  { title: 'Learn from real people', desc: 'Skip generic courses — learn directly from someone who\'s done it.' },
  { title: 'Teach what you know', desc: 'Reinforce your own skills by mentoring someone else.' },
  { title: 'Build reputation', desc: 'Every completed session and review builds your Trust Score.' },
  { title: 'Track learning', desc: 'Goals, XP, and a visible Skill Journey keep you motivated.' },
  { title: 'Discover opportunities', desc: 'Meet people across categories you might never have searched for.' },
]

const testimonials = [
  { name: 'Priya M. (demo)', quote: 'Traded my design chops for Python lessons — three sessions in and I already shipped my first script.' },
  { name: 'Diego C. (demo)', quote: 'The match explanations actually make sense — I know exactly why someone was suggested.' },
  { name: 'Emma R. (demo)', quote: 'Scheduling around two calendars used to be a headache. The overlap suggestion fixed that.' },
]

export default function LandingPage() {
  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-b from-brand-50 to-white">
        <div className="mx-auto grid max-w-6xl items-center gap-10 px-6 py-20 md:grid-cols-2 md:py-28">
          <div>
            <span className="badge">Peer-to-peer learning, reimagined</span>
            <h1 className="mt-4 font-display text-4xl font-extrabold leading-tight text-slate-900 md:text-5xl">
              Trade Skills. Build Skills. <span className="text-brand-600">Grow Together.</span>
            </h1>
            <p className="mt-4 max-w-md text-lg text-slate-600">
              Skillora connects people who want to learn with people who want to teach — matched by a
              transparent, reciprocal algorithm, not a black box.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/register" className="btn-primary px-6 py-3 text-base">Start Learning</Link>
              <a href="#categories" className="btn-secondary px-6 py-3 text-base">Explore Skills</a>
            </div>
          </div>
          <div className="relative">
            <div className="card rotate-1">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-full bg-brand-100" />
                <div>
                  <p className="text-sm font-semibold text-slate-800">Maya P.</p>
                  <p className="text-xs text-slate-500">Wants: Java · Offers: UI Design</p>
                </div>
                <span className="ml-auto badge">92% Match</span>
              </div>
              <p className="mt-3 text-sm text-slate-600">"You can teach UI Design while learning Java."</p>
            </div>
            <div className="card -mt-4 ml-10 -rotate-1">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-full bg-accent-400/30" />
                <div>
                  <p className="text-sm font-semibold text-slate-800">Kenji T.</p>
                  <p className="text-xs text-slate-500">Wants: Spanish · Offers: Guitar</p>
                </div>
                <span className="ml-auto badge">86% Match</span>
              </div>
              <p className="mt-3 text-sm text-slate-600">"They can teach you Guitar."</p>
            </div>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section id="how-it-works" className="mx-auto max-w-6xl px-6 py-20">
        <h2 className="font-display text-3xl font-bold text-slate-900">How it works</h2>
        <div className="mt-10 grid gap-6 md:grid-cols-4">
          {steps.map((s, i) => (
            <div key={s.title} className="card">
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-600 text-sm font-bold text-white">{i + 1}</span>
              <h3 className="mt-4 font-display text-base font-semibold text-slate-900">{s.title}</h3>
              <p className="mt-1.5 text-sm text-slate-500">{s.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Smart matching */}
      <section className="bg-slate-900 py-20 text-white">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="font-display text-3xl font-bold">Smart, explainable matching</h2>
          <p className="mt-3 max-w-xl text-slate-300">
            No mystery scores. Every match shows exactly which skills line up and why the percentage is what it is.
          </p>
          <div className="mt-10 grid gap-6 md:grid-cols-2">
            <div className="rounded-xl2 bg-white/5 p-6 backdrop-blur">
              <div className="flex items-center justify-between">
                <p className="font-semibold">You can teach Java</p>
                <span className="badge">92% Match</span>
              </div>
              <p className="mt-2 text-sm text-slate-300">They can teach UI Design</p>
              <button className="btn-primary mt-4">View Match</button>
            </div>
            <div className="rounded-xl2 bg-white/5 p-6 backdrop-blur">
              <div className="flex items-center justify-between">
                <p className="font-semibold">You can teach SQL</p>
                <span className="badge">94% Match</span>
              </div>
              <p className="mt-2 text-sm text-slate-300">They can teach Spring Boot</p>
              <button className="btn-primary mt-4">View Match</button>
            </div>
          </div>
        </div>
      </section>

      {/* Categories */}
      <section id="categories" className="mx-auto max-w-6xl px-6 py-20">
        <h2 className="font-display text-3xl font-bold text-slate-900">Skill categories</h2>
        <div className="mt-8 flex flex-wrap gap-3">
          {categories.map((c) => (
            <span key={c} className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700">
              {c}
            </span>
          ))}
        </div>
      </section>

      {/* Benefits */}
      <section className="bg-brand-50/60 py-20">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="font-display text-3xl font-bold text-slate-900">Why Skillora</h2>
          <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-5">
            {benefits.map((b) => (
              <div key={b.title} className="card">
                <h3 className="font-display text-sm font-semibold text-slate-900">{b.title}</h3>
                <p className="mt-1.5 text-sm text-slate-500">{b.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Gamification */}
      <section className="mx-auto max-w-6xl px-6 py-20">
        <h2 className="font-display text-3xl font-bold text-slate-900">Level up as you learn</h2>
        <div className="mt-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
          {[
            ['XP', 'Earn points for every session and review'],
            ['Levels', '5 levels from Beginner to Mentor'],
            ['Badges', 'Unlock achievements as you go'],
            ['Streaks', 'Keep your learning momentum visible'],
          ].map(([title, desc]) => (
            <div key={title} className="card text-center">
              <p className="font-display text-2xl font-bold text-brand-600">{title}</p>
              <p className="mt-1 text-xs text-slate-500">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Testimonials */}
      <section id="testimonials" className="bg-slate-50 py-20">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="font-display text-3xl font-bold text-slate-900">What members say</h2>
          <p className="mt-2 text-sm text-slate-400">Illustrative quotes from demo accounts, for preview purposes.</p>
          <div className="mt-8 grid gap-6 md:grid-cols-3">
            {testimonials.map((t) => (
              <div key={t.name} className="card">
                <p className="text-sm italic text-slate-600">"{t.quote}"</p>
                <p className="mt-4 text-xs font-semibold text-slate-800">{t.name}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="bg-gradient-to-r from-brand-600 to-accent-500 py-20 text-center text-white">
        <h2 className="font-display text-3xl font-bold">Your next skill exchange is one connection away.</h2>
        <Link to="/register" className="mt-6 inline-flex rounded-xl bg-white px-7 py-3 text-sm font-semibold text-brand-700 shadow-card">
          Create your free account
        </Link>
      </section>
    </div>
  )
}
