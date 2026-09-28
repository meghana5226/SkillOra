// Captures REAL screenshots and a demo video from your locally running Skillora stack.
//
// Prerequisites:
//   1. `docker compose up --build` is running (frontend on :5173, backend on :8080)
//   2. cd scripts && npm install && npx playwright install chromium
//   3. node capture-media.mjs
//
// Output: ../screenshots/*.png and ../docs/demo-video.webm
import { chromium } from 'playwright'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const SHOTS = path.resolve(__dirname, '../screenshots')
const DOCS = path.resolve(__dirname, '../docs')
const BASE = process.env.FRONTEND_URL || 'http://localhost:5173'
const PASSWORD = process.env.DEMO_PASSWORD || 'Demo@1234'

fs.mkdirSync(SHOTS, { recursive: true })
fs.mkdirSync(DOCS, { recursive: true })

const browser = await chromium.launch()
const context = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  recordVideo: { dir: DOCS, size: { width: 1440, height: 900 } },
})
const page = await context.newPage()

async function shot(name) {
  await page.waitForTimeout(800)
  await page.screenshot({ path: path.join(SHOTS, name), fullPage: true })
  console.log('saved', name)
}

async function login(email) {
  await page.goto(`${BASE}/login`)
  await page.fill('input[type=email]', email)
  await page.fill('input[type=password]', PASSWORD)
  await page.click('button[type=submit]')
  await page.waitForURL('**/dashboard')
}

async function logout() {
  await page.evaluate(() => localStorage.clear())
}

// 1-2. Public pages
await page.goto(BASE)
await shot('01-landing.png')
await page.goto(`${BASE}/login`)
await shot('02-login.png')
await page.goto(`${BASE}/register`)
await shot('03-register.png')

// 3. Alex: dashboard, discover, profile, swaps, sessions, goals, notifications
await login('alex@skillora.demo')
await shot('04-dashboard.png')
await page.goto(`${BASE}/discover`)
await shot('05-discover.png')
await page.goto(`${BASE}/profile/me`)
await shot('06-my-profile.png')
await page.goto(`${BASE}/swaps`)
await shot('07-swaps.png')
await page.goto(`${BASE}/sessions`)
await shot('08-sessions.png')
await page.goto(`${BASE}/messages`)
await shot('09-messages.png')
await page.goto(`${BASE}/goals`)
await shot('10-goals.png')
await page.goto(`${BASE}/notifications`)
await shot('11-notifications.png')

// Dark mode
await page.goto(`${BASE}/dashboard`)
await page.click('button[title="Toggle dark mode"]')
await shot('12-dark-mode.png')
await page.click('button[title="Toggle dark mode"]')

// Admin
await logout()
await login('admin@skillora.demo')
await page.goto(`${BASE}/admin`)
await shot('13-admin.png')

await context.close() // flushes the video
await browser.close()

const videos = fs.readdirSync(DOCS).filter((f) => f.endsWith('.webm'))
if (videos.length) {
  const latest = videos.map((f) => ({ f, t: fs.statSync(path.join(DOCS, f)).mtimeMs })).sort((a, b) => b.t - a.t)[0].f
  fs.renameSync(path.join(DOCS, latest), path.join(DOCS, 'demo-video.webm'))
  console.log('saved docs/demo-video.webm')
}
