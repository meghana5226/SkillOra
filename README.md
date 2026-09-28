# Skillora

Skillora is a peer-to-peer skill exchange platform. Members list what they can teach and what they want to
learn, get matched with people whose skills complement theirs, then message, schedule sessions, review each
other and earn XP.

**Stack:** React + Vite + Tailwind CSS · Java 21 + Spring Boot 3 · Spring Security + JWT · PostgreSQL + Flyway · Docker

<!-- Live demo: Frontend (Vercel) and Backend (Render) links can be added here after deployment. -->

## Demo

Full walkthrough of the running application: [docs/Demo_video.mp4](docs/Demo_video.mp4)

## Screenshots

| | |
|---|---|
| **Landing**<br>![Landing](screenshots/Landing.png) | **Login**<br>![Login](screenshots/Login.png) |
| **Register**<br>![Register](screenshots/Register.png) | **Dashboard**<br>![Dashboard](screenshots/Dashboard.png) |
| **Discover**<br>![Discover](screenshots/Discover.png) | **Swap requests**<br>![Swap requests](screenshots/Swap_Request.png) |
| **Swap request**<br>![Swap request](screenshots/Request.png) | **Sessions**<br>![Sessions](screenshots/Sessions.png) |
| **Messages**<br>![Messages](screenshots/Messages.png) | **Learning goals**<br>![Learning goals](screenshots/Learning_Goals.png) |
| **Notifications**<br>![Notifications](screenshots/Notifications.png) | **Dark mode**<br>![Dark mode](screenshots/Dark_mode.png) |
| **Admin dashboard**<br>![Admin dashboard](screenshots/Skillora_Admin.png) | |

## Features

- **Authentication:** registration and login with BCrypt-hashed passwords and JWT; USER and ADMIN roles.
- **Onboarding:** six skippable steps (profile, skills to teach, skills to learn, experience, availability, goal).
- **Skills:** 35 seeded skills across 10 categories.
- **Reciprocal match score:** a transparent score computed in Java, with a breakdown and a plain-English
  explanation for every match (see [How matching works](#how-matching-works)).
- **Swap requests:** send, accept, reject and cancel. Accepting a request opens a conversation.
- **Messaging:** conversations, message history, unread counts and read status (REST polling).
- **Sessions:** request, confirm, complete or cancel, with status transitions enforced by the backend.
  The scheduler shows an availability overlap hint.
- **Reviews:** 1-5 stars after a completed session, one review per person per session, updates reputation.
- **XP and levels:** +50 XP per completed session (both people), +25 XP for a 5-star review,
  +20 XP for completing the profile. Levels 1-5 at 0 / 100 / 250 / 500 / 1000 XP.
- **Notifications:** swap, message, session, review and XP events, with mark-as-read.
- **Learning goals:** create goals and track progress.
- **Discover:** search members by name, with pagination.
- **Admin:** platform statistics, user list, and activate/deactivate with an audit record.
- **Dark mode** (remembered), responsive layout, and Swagger API documentation.

### Not implemented yet

- Badges and streak XP
- Trust score breakdown and a dedicated skill passport page (the public profile at `/profile/{id}` is the
  closest equivalent)
- Discover filters beyond name search
- Admin UI for skill management, reports and announcements (skill create/edit/delete exists as an API)
- Email notifications, session reminders, WebSockets and frontend tests

## How matching works

For each candidate, `MatchService` adds up six weighted signals (total 100):

| Signal | Weight | Meaning |
|---|---|---|
| Skill compatibility | 40 | How much of what each person wants the other can teach |
| Reciprocal potential | 20 | Full credit if both can teach each other, partial if only one can |
| Experience fit | 15 | Whether the teacher's level meets the learner's target level |
| Availability overlap | 10 | Shared words in both free-text availabilities |
| Reputation | 10 | The candidate's average review rating |
| Activity | 5 | Accumulated XP |

Example: you teach SQL and want Spring Boot; they teach Spring Boot and want SQL. That is a full reciprocal
match, shown as *"You can teach SQL while learning Spring Boot."* No external AI service is used.

## Architecture

```
React (Vite, Tailwind)  ──REST + JWT──▶  Spring Boot  ──JPA──▶  PostgreSQL (Flyway migrations)
                                        controller → service → repository
```

A modular monolith with packages `controller`, `service`, `repository`, `entity`, `dto`, `mapper`,
`security`, `exception` and `config`.

```
skillora/
├── backend/            Spring Boot API, Flyway migration, tests, Dockerfile
├── frontend/           React app, nginx config, Dockerfile, vercel.json
├── docs/               API reference and demo video
├── screenshots/        README images
├── scripts/            Playwright script that records screenshots and video
├── docker-compose.yml
└── .env.example
```

## Getting started

### Run with Docker (recommended)

Requires Docker Desktop.

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

Demo data is created automatically on first start.

To stop: `docker compose down`. To also wipe the database: `docker compose down -v`.

**Port already in use?** Copy `.env.example` to `.env` and change `BACKEND_PORT` and/or `FRONTEND_PORT`.
If you change `BACKEND_PORT`, set `VITE_API_BASE_URL` to the same port. If you change `FRONTEND_PORT`, add
`http://localhost:<port>` to `CORS_ALLOWED_ORIGINS`. Then run `docker compose up --build` again.

### Run without Docker

Requires Java 21, Maven 3.9+, Node 20 and PostgreSQL 16.

```bash
# 1. Create a PostgreSQL database and user, both named "skillora" (password "skillora")

# 2. Backend (http://localhost:8080)
cd backend
mvn spring-boot:run

# 3. Frontend (http://localhost:5173), in a second terminal
cd frontend
cp .env.example .env
npm install
npm run dev
```

The backend reads `DATABASE_URL`, `DATABASE_USERNAME` and `DATABASE_PASSWORD` if your database differs.

### Run the tests

```bash
cd backend
mvn test
```

The tests use an in-memory H2 database and cover registration and login, JWT-protected access, the matching
algorithm, and the swap → session → review lifecycle.

## Demo accounts

Seeded on first start (21 members with sample swaps, sessions, a review and goals).

| Role | Email | Password |
|---|---|---|
| Admin | `admin@skillora.demo` | `Demo@1234` |
| Member | `alex@skillora.demo` | `Demo@1234` |
| Member | `sarah@skillora.demo` | `Demo@1234` |

The other seeded members use `firstname.lastname@skillora.demo` with the same password.
Set `SEED_ENABLED=false` to disable demo data.

## API documentation

Interactive docs are at `/swagger-ui/index.html`. Choose **Authorize** and paste the token returned by
`POST /api/auth/login`. A summary of all endpoints is in [docs/API.md](docs/API.md).

Errors always use one format:

```json
{ "success": false, "message": "Human readable message", "timestamp": "...", "path": "/api/..." }
```

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `DATABASE_URL` | JDBC URL, e.g. `jdbc:postgresql://host:5432/skillora` | `jdbc:postgresql://localhost:5432/skillora` |
| `DATABASE_USERNAME` | Database user | `skillora` |
| `DATABASE_PASSWORD` | Database password | `skillora` |
| `JWT_SECRET` | Token signing key, at least 32 characters | development placeholder |
| `JWT_EXPIRATION_MS` | Token lifetime | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | `http://localhost:5173` |
| `SEED_ENABLED` | Load demo data when the database is empty | `true` |
| `DEMO_PASSWORD` | Password for seeded accounts | `Demo@1234` |
| `VITE_API_BASE_URL` | Backend URL used by the frontend (set at build time) | `http://localhost:8080` |

Always set your own `JWT_SECRET` outside local development. Never commit a real `.env` file.

## Deployment

### Backend and database on Render

1. Create a **PostgreSQL** database on Render.
2. Create a **Web Service** from this repository: runtime **Docker**, root directory `backend`,
   health check path `/actuator/health`.
3. Add environment variables:
   - `DATABASE_URL`: `jdbc:postgresql://<host>:5432/<database>` (JDBC format, not `postgres://`)
   - `DATABASE_USERNAME` and `DATABASE_PASSWORD` from the Render database
   - `JWT_SECRET`: a long random string
   - `CORS_ALLOWED_ORIGINS`: your Vercel URL, with no trailing slash
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `SEED_ENABLED`: `true` for a demo, `false` otherwise

Flyway creates the schema on first start. Demo data loads only when the users table is empty.

### Frontend on Vercel

1. Import the repository and set the root directory to `frontend` (framework preset: Vite).
2. Add the environment variable `VITE_API_BASE_URL` set to your Render service URL.
3. Deploy. `vercel.json` already sets the build command and single-page-app routing.
4. Add the final Vercel URL to `CORS_ALLOWED_ORIGINS` on Render and redeploy the backend.

## Design decisions

- **Modular monolith:** one deployable and one database keeps the project simple; microservices would add
  operational cost with no benefit at this size.
- **PostgreSQL:** the data is relational (users, skills, swaps, sessions, reviews) and benefits from
  constraints, indexes and transactions.
- **Security:** BCrypt password hashing, stateless JWT, role-based access, request validation, and CORS
  limited to configured origins. The token is stored in `localStorage`, which is simple but exposed to XSS;
  httpOnly cookies would be stronger. There is no rate limiting yet.
- **Known limits:** matches are scored in memory per request (fine for a demo, first thing to optimise at
  scale), and availability overlap is a simple word comparison rather than real calendar logic.

## Future improvements

Video calls, semantic matching, calendar integration, email notifications and a mobile app.