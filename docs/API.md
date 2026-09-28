# API Reference

Interactive docs: `http://localhost:8080/swagger-ui/index.html` (use **Authorize** and paste the JWT from `/api/auth/login`).

All errors share one shape:

```json
{ "success": false, "message": "Human readable message", "timestamp": "...", "path": "/api/..." }
```

| Area | Method & path | Auth | Notes |
|---|---|---|---|
| Auth | `POST /api/auth/register` | public | name, email, password, confirmPassword → `{token,user}` |
| Auth | `POST /api/auth/login` | public | email, password → `{token,user}` |
| Auth | `GET /api/auth/me` | user | full profile incl. skills |
| Users | `GET /api/users?query=&page=&size=` | user | paginated discovery by name |
| Users | `GET /api/users/{id}` | user | public profile + stats |
| Users | `PUT /api/users/me` | user | update own profile |
| Users | `POST /api/users/me/offered-skills` | user | skillId, proficiencyLevel, yearsExperience |
| Users | `POST /api/users/me/wanted-skills` | user | skillId, targetLevel, priority |
| Users | `DELETE /api/users/me/offered-skills/{id}` | user | |
| Users | `DELETE /api/users/me/wanted-skills/{id}` | user | |
| Skills | `GET /api/skills?category=` | public | |
| Skills | `POST/PUT/DELETE /api/skills[/{id}]` | admin | |
| Matches | `GET /api/matches?limit=` | user | scored, with breakdown and explanation |
| Swaps | `POST /api/swaps` | user | receiverId, offeredSkillId, requestedSkillId, message |
| Swaps | `GET /api/swaps/sent`, `/received` | user | paginated |
| Swaps | `PUT /api/swaps/{id}/accept` / `reject` | receiver | accept creates a conversation |
| Swaps | `PUT /api/swaps/{id}/cancel` | sender | |
| Sessions | `POST /api/sessions` | user | requester = learner, receiver = teacher |
| Sessions | `GET /api/sessions/upcoming`, `/history` | user | |
| Sessions | `PUT /api/sessions/{id}/confirm` | teacher | REQUESTED → CONFIRMED |
| Sessions | `PUT /api/sessions/{id}/complete` | participant | CONFIRMED → COMPLETED, awards XP |
| Sessions | `PUT /api/sessions/{id}/cancel` | participant | not allowed once COMPLETED |
| Messages | `GET /api/conversations` | user | with unread counts |
| Messages | `POST /api/conversations/with/{userId}` | user | get or create |
| Messages | `GET/POST /api/conversations/{id}/messages` | participant | GET marks incoming as read |
| Reviews | `POST /api/reviews` | participant | completed sessions only, one per reviewer per session |
| Reviews | `GET /api/users/{id}/reviews` | user | paginated |
| Notifications | `GET /api/notifications`, `/unread-count` | user | |
| Notifications | `PUT /api/notifications/{id}/read`, `/read-all` | user | |
| Goals | `GET/POST /api/goals`, `PUT /api/goals/{id}` | user | |
| Admin | `GET /api/admin/dashboard`, `/users` | admin | |
| Admin | `PUT /api/admin/users/{id}/status` | admin | writes an audit row |

## Database

Schema lives in `backend/src/main/resources/db/migration/V1__initial_schema.sql` (Flyway).
Tables: `users`, `skills`, `user_offered_skills`, `user_wanted_skills`, `swap_requests`, `conversations`,
`messages`, `sessions`, `reviews`, `notifications`, `learning_goals`, `admin_audit`.
Foreign keys cascade on user deletion; unique constraints prevent duplicate skills per user,
duplicate reviews per session/reviewer, and duplicate conversations per pair.
