# PulseDesk

Backend + React UI that accepts user comments, analyzes them with Hugging Face, and creates support tickets when needed.

## Features

- `POST /comments`, `GET /comments`
- Automatic triage with Hugging Face Inference Providers
- Keyword-based fallback triage if HF is unavailable
- Ticket fields: title, category, priority, summary
- `GET /tickets`, `GET /tickets/{id}`
- H2 in-memory database
- Optional React UI in `frontend/`

## Stack

- Java 17, Spring Boot 4
- Spring Web, Validation, Spring Data JPA, H2
- Hugging Face chat completions (`router.huggingface.co`)
- React + Vite frontend
- JUnit 5 + MockMvc tests, JaCoCo coverage

## Prerequisites

- JDK 17+
- Node.js 18+ (for UI)
- Hugging Face account + token with Inference Providers access
- HF Inference Providers enabled (e.g. Featherless) and billing/credits if required

## Backend setup

```bash
cd pulsedesk
export HF_API_TOKEN=hf_your_token_here
./mvnw spring-boot:run
```

API: http://localhost:8080  
Health: http://localhost:8080/api/health  
H2 console: http://localhost:8080/h2-console  
- JDBC URL: `jdbc:h2:mem:pulsedesk`
- User: `sa`
- Password: empty

### Triage mode

Default uses Hugging Face:

```yaml
pulsedesk:
  triage:
    provider: huggingface
```

Force keyword-only triage:

```yaml
pulsedesk:
  triage:
    provider: dummy
```

If Hugging Face fails (missing token, network, provider error), the app logs a warning and falls back to keyword triage automatically.

## Frontend setup

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

UI: http://localhost:5173

Optional `.env`:

```bash
VITE_API_URL=http://localhost:8080
```

## Example API calls

```bash
# Creates a ticket (issue)
curl -s -X POST http://localhost:8080/comments \
  -H "Content-Type: application/json" \
  -d '{"text":"App crashes when I open settings","channel":"web"}'

# Usually no ticket (compliment)
curl -s -X POST http://localhost:8080/comments \
  -H "Content-Type: application/json" \
  -d '{"text":"I love this app, thanks!","channel":"app-review"}'

curl -s http://localhost:8080/comments
curl -s http://localhost:8080/tickets
```

## Tests

```bash
./mvnw test
```

Coverage report:

```bash
./mvnw test
open target/site/jacoco/index.html
```

## Project layout

```
src/main/java/com/pulsedesk/
  controller/   REST endpoints
  service/      comment/ticket/triage logic
  repository/   JPA repositories
  model/        entities + DTOs
  client/       Hugging Face client
  config/       CORS, RestClient, properties
frontend/       React UI
```

## Deploy

Live demo:
- Frontend: https://pulsedesk-zeta.vercel.app
- Backend: https://pulsedesk-gifc.onrender.com

Backend is on Render (Docker), frontend on Vercel (`frontend/` + `VITE_API_URL`).

> **Note:** Render’s free tier sleeps when idle, so the **first request may take about 30–60 seconds**. Later requests are faster.

## Notes

- Do not commit `.env` or API tokens
- H2 data is in-memory and resets when the backend restarts
- Ticket categories: `BUG`, `FEATURE`, `BILLING`, `ACCOUNT`, `OTHER`
- Priorities: `LOW`, `MEDIUM`, `HIGH`
