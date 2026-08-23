# Task Manager — учебный sandbox для курса Java AQA

Это живое приложение, с которым вы будете работать: сначала в Postman, потом на Rest Assured.

---

## Что это за приложение

**Task Manager** — упрощённый таск-трекер (аналог Jira/Trello):

- пользователи логинятся по JWT;
- работают в **проектах** (у каждого свой ключ, например `DEMO`);
- создают и меняют **задачи** (статусы, приоритеты, assignee, комментарии);
- действуют **роли** — не всё доступно всем (это важно для негативных сценариев).

На VPS развёрнуты **backend + frontend + PostgreSQL**. В браузере можно посмотреть UI; в курсе основной фокус — **тестирование API**.

Возможно работать UI будет работать только с VPN.

---

## Дорожная карта: что вас ждёт

| Этап | Модуль | Что делаете |
|------|--------|-------------|
| 1 | **Postman** | Коллекции, переменные, окружения, первые автотесты в Tests. Ручная и полуавтоматическая проверка API на **VPS**. |
| 2 | **Практика (Postman)** | Свои запросы, негативы, цепочки login → CRUD, Collection Runner. Работа с тем же API и теми же пользователями. |
| 3 | **Rest Assured** | Те же эндпоинты — уже из Java: specs, auth, JSON assertions, параметризация. |
| 4 | **Практика (Rest Assured)** | Домашние задания и мини-набор автотестов против sandbox (и при желании — локально). |
| 5 | **Дальше по курсу** | Allure, CI/CD, расширение покрытия — **всё на этом же проекте**, без смены «тренировочного» API на другой. |

Один продукт — несколько уровней зрелости тестирования. Вы не учитесь «на абстрактном jsonplaceholder», а на системе, которую можно реально сломать, починить и покрыть тестами.

---

## Sandbox — подключение для студентов

### Публичный стенд (VPS)

| | |
|---|---|
| **Base URL** | `http://51.195.82.237:8090` |
| **Swagger UI** | http://51.195.82.237:8090/swagger-ui.html |
| **Health** | http://51.195.82.237:8090/actuator/health |
| **UI (Kanban)** | http://51.195.82.237:8090/ |

> Указывайте порт **`:8090`**. Без порта или с другим портом на этом сервере откроется другое приложение.

**Demo-пользователь для большинства заданий:**

| Email | Password | Роль в sandbox |
|-------|----------|----------------|
| `qa@demo.com` | `Demo123!` | Обычный участник, создаёт задачи в проекте **DEMO** |

Дополнительные пользователи (негативы, RBAC): `admin@demo.com`, `viewer@demo.com` — пароль тот же `Demo123!`.

### Локально (опционально)

Если поднимаете проект у себя: `http://localhost:8080` — те же пути `/api/v1/...`, те же demo-данные в профиле `dev`.  
Подробности — в конце README для тех, кто хочет копать глубже.

---

## С чего начать в Postman

1. Установите [Postman](https://www.postman.com/downloads/).
2. Импортируйте из папки [`postman/`](postman/):
   - `Task-Manager-Smoke.postman_collection.json` — запросы и примеры Tests;
   - `Task-Manager-Sandbox-VPS.postman_environment.json` — URL и учётные данные VPS.
3. Выберите environment **Task Manager — Sandbox (VPS)** в правом верхнем углу.
4. Отправьте **Login** → **GET /auth/me** → **List Projects** — убедитесь, что видите проект `DEMO`.

**Справочник всех эндпоинтов** (URL, body, ответы, доменная модель):  
[`postman/API-ENDPOINTS.md`](postman/API-ENDPOINTS.md)

**Подробнее про коллекцию и Runner:** [`postman/README.md`](postman/README.md)

---

## API в двух словах

- Префикс: **`/api/v1/`**
- Авторизация: `Authorization: Bearer <accessToken>` после `POST /api/v1/auth/login`
- OpenAPI: `/openapi.yaml` · live docs: `/swagger-ui.html`

| Группа | Путь | Зачем в курсе |
|--------|------|----------------|
| Auth | `/api/v1/auth` | Login, refresh, me — основа цепочек |
| Projects | `/api/v1/projects` | Проект DEMO, members, RBAC |
| Tasks | `/api/v1/tasks` | CRUD, статусы, optimistic lock (`version`) |
| Comments, Tags, Stats | `/api/v1/...` | Расширенные сценарии на практике |

Формат ошибок единый — удобно проверять в Postman и Rest Assured:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [{ "field": "title", "message": "must not be blank" }],
  "timestamp": "2026-08-22T10:30:00Z",
  "path": "/api/v1/tasks"
}
```

---

## Что лежит в репозитории

| Путь | Для кого | Назначение |
|------|----------|------------|
| [`postman/`](postman/) | **Студенты** | Коллекция, environments, справочник API |
| [`postman/API-ENDPOINTS.md`](postman/API-ENDPOINTS.md) | **Студенты** | Полный список эндпоинтов для ручного тестирования |
| [`src/main/resources/static/openapi.yaml`](src/main/resources/static/openapi.yaml) | Студенты / RA | Контракт API для импорта и ассертов |
| [`frontend/`](frontend/) | UI-модуль курса | React Kanban, `data-testid` для Selenium |
| [`DEPLOY.md`](DEPLOY.md) | Преподаватель | Деплой и обновление VPS |
| `src/test/java/.../aqa/` | **Студенты / prod** | Каркас RA; на `prod` — эталон в `reference/` |

Автотестов приложения **мало** — smoke Spring. API-тесты RA пишут студенты в `aqa/hw/` (ветка `main`).

---

## Rest Assured (курс Java AQA)

| | |
|---|---|
| Каркас | `src/test/java/itk/student/task/manager/aqa/` |
| Настройки | `src/test/resources/aqa.properties` |
| Студенты | `aqa/hw/` — домашка в своей ветке |
| **Эталон (ветка `prod`)** | `aqa/reference/` — полное решение практики |

```bash
./mvnw test -Dtest=Reference*ApiTest
```

Нужен **JDK 21** (`JAVA_HOME`). На Windows: `set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.x.x`.

Локальный override URL/логинов: скопируйте `src/test/resources/aqa-local.properties.example` → `aqa-local.properties` (файл в `.gitignore`).

ТЗ — урок **09. Практика. Task Manager** и **02. Практика. Rest assured** в vault курса.

---

## Чего ждать от вас на практике

**После модуля Postman:**

- уметь собрать коллекцию с переменными (`baseUrl`, `accessToken`, `projectId`, `taskId`);
- прогнать smoke-сценарий и негативы (401, 400, validation);
- читать Swagger и сверять фактический ответ с контрактом.

**После модуля Rest Assured:**

- поднять Java-проект с RA против того же `baseUrl`;
- вынести login в `@BeforeAll` / filter;
- покрыть happy path + несколько негативов;
- (далее) отчёты Allure, прогон в CI.

Все задания — про **этот** Task Manager, чтобы к концу курса у вас был связный набор артеfactов, а не разрозненные упражнения.

---

## Для самостоятельного запуска (не обязательно на старте)

```bash
./mvnw spring-boot:run
```

- Swagger: http://localhost:8080/swagger-ui.html  
- Health: http://localhost:8080/actuator/health  
- Demo users: `qa@demo.com` / `Demo123!` (и другие — см. seed в `dev` profile)

Frontend локально:

```bash
cd frontend && npm install && npm run dev
```

→ http://127.0.0.1:5173

Stack: Java 21, Spring Boot 4.1, JWT, H2 (dev) / PostgreSQL (prod), React.

Деплой sandbox на VPS: [`DEPLOY.md`](DEPLOY.md).

---

## Вопросы и проблемы

| Симптом | Что проверить |
|---------|----------------|
| 404 / timeout | `baseUrl` и порт **8090** на VPS |
| 401 после login | Сначала login, в Token — `{{accessToken}}`, environment активен |
| Пустой `projectId` | Выполните List Projects, возьмите `id` проекта **DEMO** |
| Login 200, но Tests падают | Вкладка Tests на login — скрипт сохранения `accessToken` |

Если sandbox недоступен — сообщите преподавателю; локальный запуск — запасной вариант.

---

**Итог:** этот репозиторий — ваш общий полигон на весь курс Java AQA. Сначала Postman и руки, потом Rest Assured и код, потом инфраструктура вокруг тех же тестов. Добро пожаловать в sandbox.
