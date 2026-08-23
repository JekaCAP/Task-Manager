# Postman — Task Manager Sandbox

**Справочник всех эндпоинтов:** [API-ENDPOINTS.md](API-ENDPOINTS.md) — URL, query, body, ответы для ручного тестирования.

Два файла для импорта в Postman (это **не две коллекции**, а **коллекция + environment**).

| Файл | Назначение |
|------|------------|
| `Task-Manager-Smoke.postman_collection.json` | Smoke + базовые негативы (модули 1–2) |
| `Task-Manager-Course-Module3.postman_collection.json` | **Модуль 3 курса:** Pre-request, CRUD E2E, OpenAPI schema, домашка |
| `openapi.yaml` | OpenAPI spec — Import → File в Postman (урок 3.3) |
| `Task-Manager-Sandbox.postman_environment.json` | localhost |
| `Task-Manager-Sandbox-VPS.postman_environment.json` | VPS: `http://51.195.82.237:8090` |

**Почему два файла:** коллекция описывает *что* вызывать и *как* проверять, environment — *куда* стучаться (localhost / VPS) и *чем* авторизоваться. Токены и id подставляются автоматически между шагами.

---

## Быстрый старт

1. Запусти backend: `TaskManagerApplication` (профиль `dev`, порт **8080**).
2. Postman → **Import** → оба JSON из этой папки.
3. Справа сверху выбери environment **Task Manager — Sandbox (local)**.
4. **ПКМ** на папке **01 Smoke** → **Run folder** → убедись, что все запросы отмечены галочками → **Start run**.

Проверка API: http://localhost:8080/actuator/health

---

## Папки внутри коллекции (их две)

| Папка | Запросов | Когда запускать |
|-------|----------|-----------------|
| **01 Smoke** | 11 | Основной сценарий: login → JWT → проект DEMO → CRUD задачи → logout |
| **02 Negative** | 5 | Негативы: 401 + validation. Запускай **все 5** (есть Setup перед create task) |

Smoke нужно гонять **сверху вниз** — каждый шаг сохраняет переменные для следующего (`accessToken`, `demoProjectId`, `taskId` …).

---

## Коллекция модуля 3 (`Task-Manager-Course-Module3`)

Эталон под уроки **3.1–3.3** и домашнее задание. На коллекции включён **Pre-request auto-login** (`await pm.sendRequest`).

| Папка | Запросов | Содержание |
|-------|----------|------------|
| **03 Pre-request** | 3 | List DEMO → Get Me → Create Task (UUID, TaskResponse + jsonSchema) |
| **04 CRUD E2E** | 6 | create → get → PATCH status → delete → 404 (+ TaskDetailResponse schema) |
| **05 Homework Negatives** | 8 | 401, empty title, search q&lt;2, invalid status TODO→DONE |

**Запуск на VPS:**

1. Import: `Task-Manager-Course-Module3.postman_collection.json` + `Task-Manager-Sandbox-VPS.postman_environment.json`
2. Environment **Task Manager — Sandbox (VPS)** активен
3. ПКМ на коллекции → **Run collection** (или по папкам)
4. **Keep variable values** — ON

Ожидание: все assertions зелёные (~50+ проверок на полный прогон).

---

## Environment — переменные

| Переменная | Задаётся вручную | Заполняется автоматически |
|------------|------------------|---------------------------|
| `baseUrl` | `http://localhost:8080` | — |
| `userEmail` | `qa@demo.com` | — |
| `userPassword` | `Demo123!` | — |
| `accessToken` | — | Login / Refresh |
| `refreshToken` | — | Login / Refresh |
| `demoProjectId` | — | List Projects |
| `taskId`, `taskVersion`, `taskTitle` | — | Create / Get Task |

Для **VPS sandbox** измени только `baseUrl`.

---

## Настройки Runner

- **Functional** (не Performance)
- **Keep variable values** — включено
- **Iterations:** 1

Если **Start run** серая — слева не выбраны запросы. Отметь все в папке или запускай через **ПКМ → Run folder**.

---

## Demo-пользователи (profile dev)

| Email | Password | Комментарий |
|-------|----------|-------------|
| `qa@demo.com` | `Demo123!` | Используется в environment, может создавать задачи |
| `admin@demo.com` | `Demo123!` | ADMIN |
| `viewer@demo.com` | `Demo123!` | Только чтение, для негативов по RBAC |

---

## Swagger

Спецификация и ручные вызовы: http://localhost:8080/swagger-ui.html
