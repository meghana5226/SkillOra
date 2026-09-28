# Demo Video

**Status:** the video is not bundled in this repo. It has to be recorded from your own running instance.

## Option A: automatic capture (recommended)

```bash
docker compose up --build          # in the repo root, wait until the backend is healthy
cd scripts
npm install
npx playwright install chromium
node capture-media.mjs
```

This writes real screenshots to `screenshots/` and a browser recording to `docs/demo-video.webm`.
The script walks the seeded demo accounts through the main pages. It does not execute the full
register → swap → complete → review flow, so use Option B for a complete walkthrough.

## Option B: manual recording (full flow)

Use any screen recorder (OBS, QuickTime, Loom). Suggested sequence (about 4 minutes):

1. **Landing page**: scroll through the hero, matching, categories.
2. **Register** a new user (any email) and complete onboarding: add one teach skill and one learn skill.
3. **Log out, log in as `alex@skillora.demo`** (password in the README).
4. **Dashboard**: point out XP, level, recommended matches with the explanation text.
5. **Discover**: search a name, open a profile.
6. **Send Swap Request** to Sarah (offer Java, request React).
7. **Log in as `sarah@skillora.demo`** in a private window: **Swap Requests → Accept**.
8. **Messages**: a conversation now exists. Send a message.
9. **Schedule session**: the modal shows availability overlap. Book a session.
10. As the teacher, **Confirm** the session on the Sessions page.
11. **Mark complete**: both users get +50 XP.
12. **Leave review** (5 stars): the reputation score updates and +25 XP is awarded.
13. **Notifications**: show the XP and review notifications, then "Mark all as read".
14. **Goals**: create a goal and drag the progress slider.
15. **Toggle dark mode**.
16. **Log in as `admin@skillora.demo`** → **Admin**: stats and user activate/deactivate.

## Embedding

- GitHub README: upload the video to YouTube/Loom and paste the link in the README "Demo Video" line,
  or drag the `.mp4` into a GitHub issue/PR comment and copy the generated URL.
- Convert webm to mp4 if needed: `ffmpeg -i docs/demo-video.webm docs/demo-video.mp4`
