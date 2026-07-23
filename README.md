# PulseDesk (starter)

Spring Boot starter for a comment-to-ticket triage backend.  
This repository is a **scaffold only** — no triage / Hugging Face business logic yet.

## Stack

- Java 17+
- Spring Boot 4
- Spring Web, Validation, Spring Data JPA
- H2 (in-memory)
- Maven Wrapper (`mvnw`)

## Prerequisites

- JDK 17 or newer
- A [Hugging Face](https://huggingface.co/) account and API token (when you add AI later)

## Setup

```bash
cd pulsedesk
cp .env.example .env
# edit .env and set HF_API_TOKEN when needed
```

Export the token in your shell (or load `.env` with your preferred tool):

```bash
export HF_API_TOKEN=hf_your_token_here
```

## Run

```bash
./mvnw spring-boot:run
```

App: http://localhost:8080  
Health check: `GET http://localhost:8080/api/health`  
H2 console: http://localhost:8080/h2-console  
- JDBC URL: `jdbc:h2:mem:pulsedesk`  
- User: `sa`  
- Password: *(empty)*

## Project layout

```
src/main/java/com/pulsedesk/
  PulsedeskApplication.java
  config/          # app configuration (e.g. Hugging Face properties)
  controller/      # REST controllers
  model/           # entities / DTOs (to be added)
  repository/      # Spring Data repositories (to be added)
  service/         # business logic (to be added)
  client/          # external API clients (Hugging Face, to be added)
```

## Suggested next steps (assignment)

1. Add `Comment` and `Ticket` entities + repositories  
2. Implement `POST/GET /comments`, `GET /tickets`, `GET /tickets/{id}`  
3. Call Hugging Face Inference API and map the response to ticket fields  
4. Persist results in H2  
5. (Optional) Simple UI + deploy  

## Useful links

- [Hugging Face Inference API](https://huggingface.co/inference-api)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [H2 Database](https://www.h2database.com/)
