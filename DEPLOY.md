# Task Manager — деплой на VPS (HTTP)

Sandbox для курса Java AQA: **backend + frontend + PostgreSQL** в Docker.  
Публичный доступ: `http://51.195.82.237` (порт 90).

Стек в `docker-compose.prod.yml`:

| Сервис | Контейнер | Назначение |
|--------|-----------|------------|
| `postgres` | taskmanager-postgres | БД |
| `app` | taskmanager-app | Spring Boot, только `127.0.0.1:8084` (или `APP_HOST_PORT`) |
| `web` | taskmanager-web | nginx: React static + proxy `/api` |

---

## A. Первый деплой на VPS (один раз)

### 1. Подготовка VPS

```bash
ssh ubuntu@51.195.82.237
```

Установи Docker (если ещё нет):

```bash
sudo apt update
sudo apt install -y git docker.io docker-compose-v2
sudo usermod -aG docker ubuntu
# перелогинься или: newgrp docker
```

Firewall:

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw enable
sudo ufw status
```

### 2. Клонирование репозитория

```bash
cd ~
git clone <URL_ТВОЕГО_REPO> task-manager
cd task-manager
git checkout prod
```

### 3. Секреты

```bash
cp .env.prod.example .env.prod
nano .env.prod
```

Обязательно задай:

| Переменная | Пример |
|------------|--------|
| `POSTGRES_PASSWORD` | длинный случайный пароль |
| `JWT_SECRET` | минимум 32 символа |
| `CORS_ALLOWED_ORIGINS` | `http://51.195.82.237` |

`.env.prod` **не коммитить**.

### 4. Сборка и запуск всего стека

```bash
cd ~/task-manager
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

Первый запуск ~5–10 мин (Maven + npm внутри Docker).

### 5. Проверка

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
```

Все сервисы `running`, `app` — `healthy`.

**Backend напрямую (только с VPS):**

```bash
curl -s http://127.0.0.1:8084/actuator/health
# {"status":"UP"}
```

**Через nginx (как у студентов):**

```bash
curl -s http://localhost/actuator/health
curl -s -X POST http://localhost/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"qa@demo.com","password":"Demo123!"}'
```

**С локальной машины:**

```bash
curl -s http://51.195.82.237/actuator/health
```

Браузер:

| URL | Что |
|-----|-----|
| http://51.195.82.237/ | Kanban UI (login) |
| http://51.195.82.237/swagger-ui.html | Swagger |
| http://51.195.82.237/openapi.yaml | OpenAPI для Postman |

Demo: `qa@demo.com` / `Demo123!`

**Логи:**

```bash
docker logs taskmanager-app --tail 100
docker logs taskmanager-web --tail 50
docker logs taskmanager-postgres --tail 30
```

---

## B. Обычное обновление (как study-hub)

### 1. Локально

```bash
git add .
git commit -m "your message"
git push origin prod
```

### 2. SSH на VPS

```bash
ssh ubuntu@51.195.82.237
cd ~/task-manager
git pull origin prod
```

### 3. Пересборка

**Только backend** (Java/API):

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build app
```

**Только frontend** (React/nginx):

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build web
```

**Backend + frontend:**

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build app web
```

**Весь стек с нуля:**

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

### 4. Проверка после деплоя

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
curl -s http://127.0.0.1:8084/actuator/health
curl -s http://localhost/actuator/health
docker logs taskmanager-app --tail 100
```

Postman: environment **Task Manager — Sandbox (VPS)**, Runner → **01 Smoke**.

---

## C. Postman для студентов

Импорт из `postman/`:

- `Task-Manager-Smoke.postman_collection.json`
- `Task-Manager-Sandbox-VPS.postman_environment.json` → `baseUrl = http://51.195.82.237`

---

## D. Альтернатива: nginx на хосте (без контейнера `web`)

Если порт 80 уже занят или хочешь static без Docker:

1. Запускай только `postgres` + `app` (закомментируй `web` в compose или `up postgres app`).
2. Собери фронт: `cd frontend && npm ci && npm run build`
3. Скопируй: `sudo rsync -av --delete frontend/dist/ /var/www/taskmanager/`
4. Nginx: `deploy/nginx-taskmanager.conf.example` → `/etc/nginx/sites-available/taskmanager`

```bash
sudo ln -s /etc/nginx/sites-available/taskmanager /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

---

## E. Troubleshooting

| Симптом | Решение |
|---------|---------|
| `app` unhealthy | `docker logs taskmanager-app --tail 200` — часто Liquibase/Postgres |
| 502 на `/api` | `app` не healthy; проверь `curl http://127.0.0.1:8084/actuator/health` |
| `Bind for 8083 failed` | Порт занят (часто study-hub); в `.env.prod` поставь `APP_HOST_PORT=8084` |
| CORS в браузере | `CORS_ALLOWED_ORIGINS` в `.env.prod` = origin фронта (`http://51.195.82.237`) |
| Maven timeout на VPS | используется `.mvn/settings.xml` (Aliyun mirror) |
| Порт 80 занят | смени `WEB_HOST_PORT=8080` в `.env.prod` → `http://IP:8080` |
| Сброс demo-данных | `docker compose ... down -v` (удалит volume Postgres!) + `up --build` |

---

## F. Чеклист перед prod

- [ ] `.env.prod` на VPS с сильными `POSTGRES_PASSWORD` и `JWT_SECRET`
- [ ] `.env.prod` не в git
- [ ] `CORS_ALLOWED_ORIGINS` = публичный URL
- [ ] UFW: 22, 80
- [ ] Smoke Postman зелёный против VPS
- [ ] Login UI + Kanban открываются в браузере
