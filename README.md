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

### Live demo

- Frontend (Vercel): https://pulsedesk-zeta.vercel.app
- Backend (Render): https://pulsedesk-gifc.onrender.com

> **Note:** The backend runs on Render’s free tier. After idle time the service sleeps, so the **first request may take about 30–60 seconds** while the app wakes up. Later requests are much faster.

### Backend on Render

1. Push this repo to GitHub (includes `Dockerfile`)
2. Create a **Web Service** on [Render](https://render.com)
3. Settings:
   - Runtime: **Docker**
   - Root Directory: empty (repo root)
   - Branch: `main`
4. Environment variables:
   - `HF_API_TOKEN` = your Hugging Face token
   - `CORS_ALLOWED_ORIGINS` = `https://pulsedesk-zeta.vercel.app,http://localhost:5173`
5. Deploy, then check:

```bash
curl https://pulsedesk-gifc.onrender.com/api/health
```

### Frontend on Vercel

1. Import the same GitHub repo in [Vercel](https://vercel.com)
2. Settings:
   - Root Directory: `frontend`
   - Framework Preset: **Vite**
   - Build Command: `npm run build`
   - Output Directory: `dist`
3. Environment variable:
   - `VITE_API_URL` = `https://pulsedesk-gifc.onrender.com` (no trailing slash)
4. Deploy

After the Vercel domain is known, update `CORS_ALLOWED_ORIGINS` on Render and redeploy the backend.

## Notes

- Do not commit `.env` or API tokens
- H2 data is in-memory and resets when the backend restarts
- Free Render instances sleep when idle (see cold-start note above)
- Ticket categories: `BUG`, `FEATURE`, `BILLING`, `ACCOUNT`, `OTHER`
- Priorities: `LOW`, `MEDIUM`, `HIGH`
