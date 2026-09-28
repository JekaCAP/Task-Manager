# Task Manager Sandbox

Учебный REST API + UI для курса **Java AQA**. Практика: Postman, Rest Assured, Selenium, CI/CD.

## Stack

- Java 21, Spring Boot 4.1, Spring MVC
- Spring Data JPA, Liquibase, Spring Security (JWT)
- H2 in-memory (demo-данные при каждом старте)
- React UI (опционально в JAR)
- Swagger UI: [springdoc](https://springdoc.org/)

## API Specification (единый файл)

**Canonical OpenAPI:** [`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml)

| URL | Назначение |
|-----|------------|
| http://localhost:8080/openapi.yaml | Скачать / импорт в Postman |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/v3/api-docs | Live JSON из springdoc |

## Quick start (локально)

```bash
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Frontend (отдельно, для разработки UI)

```bash
cd frontend && npm install && npm run dev
```

→ http://127.0.0.1:5173 (login + Kanban board)

**Важно:** используй `127.0.0.1`, не `localhost` — Yandex Browser часто не открывает localhost.

Подробнее: [`frontend/README.md`](frontend/README.md) — `data-testid` для Selenium.

### Frontend в JAR (VPS, один процесс)

```bash
cd frontend && npm install && npm run build:embed
cd .. && ./mvnw package -DskipTests
java -Xms128m -Xmx256m -jar target/task-manager-0.0.1-SNAPSHOT.jar --server.port=8090
```

→ UI + API на одном порту (например http://VPS:8090/login).

## Demo users (profile `dev`)

| Email | Password | Role |
|-------|----------|------|
| admin@demo.com | Demo123! | ADMIN |
| lead@demo.com | Demo123! | PROJECT ADMIN |
| qa@demo.com | Demo123! | MEMBER |
| viewer@demo.com | Demo123! | VIEWER |

## API overview

| Group | Base path |
|-------|-----------|
| Auth | `/api/v1/auth` |
| Users | `/api/v1/users` |
| Projects | `/api/v1/projects` |
| Tasks | `/api/v1/tasks` |
| Comments | `/api/v1/comments` |
| Tags | `/api/v1/tags` |
| Stats | `/api/v1/stats` |

Авторизация: `Authorization: Bearer <accessToken>` после `POST /api/v1/auth/login`.

## Error format

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [{ "field": "title", "message": "must not be blank" }],
  "timestamp": "2026-08-22T10:30:00Z",
  "path": "/api/v1/tasks"
}
```

## Tests

Smoke test `TaskManagerApplicationTests` uses **H2 in-memory** (profile `test`) — Docker не нужен.

## Project structure

```
controller/   REST endpoints + Swagger
service/      business logic
repository/   Spring Data JPA
entity/       JPA entities
dto/          request/response records
mapper/       manual mappers (no MapStruct)
config/       Security, OpenAPI, seed data
security/     JWT filter, UserPrincipal
exception/    GlobalExceptionHandler
```

Тесты в проекте намеренно минимальны — домашние задания пишут студенты на Rest Assured.

## Rest Assured (курс Java AQA)

Каркас для домашки: `src/test/java/itk/student/task/manager/aqa/` (`config`, `support`).

| | |
|---|---|
| Настройки стенда | `src/test/resources/aqa.properties` |
| Локальный override | `aqa-local.properties` (см. `.example`) |
| Ваш код | `aqa.hw` — пакет создаёте сами |

**Sandbox:** `https://demo.itklabs.online` · **Demo:** `qa@demo.com` / `Demo123!`

```bash
./mvnw test -Dtest=IvanovTaskApiTest
```
