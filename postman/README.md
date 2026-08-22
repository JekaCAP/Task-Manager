# Postman — Task Manager Sandbox

Два файла для импорта в Postman (это **не две коллекции**, а **коллекция + environment**).

| Файл | Назначение |
|------|------------|
| `Task-Manager-Smoke.postman_collection.json` | Запросы и автотесты (Tests) |
| `Task-Manager-Sandbox.postman_environment.json` | Переменные: URL, логин, токены, id задач |

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
| **02 Negative** | 5 | Негативы: 401, 400 validation. Есть свои Setup-запросы перед последним тестом |

Smoke нужно гонять **сверху вниз** — каждый шаг сохраняет переменные для следующего (`accessToken`, `demoProjectId`, `taskId` …).

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
