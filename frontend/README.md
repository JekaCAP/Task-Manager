# Task Manager Frontend

React + Vite + TypeScript. Минимальный UI для курса Java AQA и будущих Selenium-тестов.

## Запуск

```bash
# Терминал 1 — backend
./mvnw spring-boot:run

# Терминал 2 — frontend
cd frontend
npm install
npm run dev
```

Открыть: http://localhost:5173

Vite проксирует `/api` → `http://localhost:8080`.

## URL

| Адрес | Экран |
|-------|--------|
| http://127.0.0.1:5173/login | Вход |
| http://127.0.0.1:5173/ | Kanban (после login) |

**Не используй `localhost` в Yandex Browser** — будет «Страница не found / connectionfailure».  
Открывай **`127.0.0.1`** или используй Chrome / Edge.

Терминал с `npm run dev` **должен оставаться открытым** — пока он работает, Vite отдаёт фронт.

## Экраны

| Экран | URL | Описание |
|-------|-----|----------|
| Login | `/login` | Форма email/password |
| Board | `/` | Kanban + stats + create/edit task |

## data-testid (Selenium)

| testid | Элемент |
|--------|---------|
| `login-page` | Страница входа |
| `login-email-input` | Email |
| `login-password-input` | Password |
| `login-submit-btn` | Кнопка входа |
| `login-error` | Ошибка авторизации |
| `logout-btn` | Logout |
| `project-select` | Выбор проекта |
| `user-name` | Имя пользователя |
| `stats-bar` | Блок статистики |
| `task-create-btn` | Создать задачу |
| `kanban-board` | Kanban |
| `kanban-column-TODO` | Колонка To Do |
| `kanban-column-IN_PROGRESS` | In Progress |
| `kanban-column-REVIEW` | Review |
| `kanban-column-DONE` | Done |
| `task-card-{uuid}` | Карточка задачи |
| `task-modal` | Модалка задачи |
| `task-title-input` | Title |
| `task-priority-select` | Priority |
| `task-status-select` | Status |
| `task-save-btn` | Сохранить |
| `comment-input` | Поле комментария |
| `comment-submit-btn` | Отправить комментарий |

Demo: `qa@demo.com` / `Demo123!`
