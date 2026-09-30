# Деплой task-manager в Docker на новой VPS

Runbook переезда со старой схемы (systemd + jar + nginx на хосте) на Docker.

## Архитектура

```
браузер → nginx-контейнер (:80/:443, Let's Encrypt)
            ├── demo.itklabs.online        → app-контейнер (:8090, Spring Boot + UI в jar)
            └── bot/resume/youtrack.itklabs.ru → https://51.195.82.237 (прокси, как на старой VPS)
          certbot-контейнер (webroot-продление сертификатов раз в 12 часов)
```

Состав репозитория для деплоя:

| Файл | Назначение |
|------|-----------|
| `Dockerfile` | multi-stage: npm `build:embed` → maven package → JRE 21. Сборка самодостаточная, локально ничего собирать не нужно |
| `docker-compose.yml` | app + nginx + certbot |
| `nginx/conf.d/*.conf` | конфиги nginx (заменяют `/etc/nginx/conf.d` в контейнере) |
| `deploy-docker.ps1` | ежедневный деплой с рабочей машины |
| `.env.example` | шаблон секретов (опционально) |

Приложение stateless: H2 in-memory, demo-данные сеются при старте, вложения в БД —
**ничего переносить (БД/файлы) не нужно**, volume для app отсутствует намеренно.

## 1. Подготовка новой VPS

```bash
# Docker + compose plugin
curl -fsSL https://get.docker.com | sh

# Код (деплоим из ветки prod; репозиторий публичный)
mkdir -p /opt && cd /opt
git clone -b prod https://github.com/JekaCAP/Task-Manager.git task-manager
cd task-manager

# Опционально: свой JWT-секрет
cp .env.example .env   # и раскомментируй JWT_SECRET в .env и в docker-compose.yml
```

## 2. Сертификаты — перенос со старой VPS

Контейнер nginx не стартует без сертификатов, поэтому переносим их **до** первого
`docker compose up`. Переносим и renewal переключаем на webroot (authenticator=nginx
внутри контейнера работать не будет).

```bash
# --- на СТАРОЙ VPS ---
tar czf /root/letsencrypt.tar.gz -C /etc letsencrypt
scp /root/letsencrypt.tar.gz root@NEW_IP:/tmp/

# --- на НОВОЙ VPS ---
cd /opt/task-manager
mkdir -p certbot/conf certbot/www
tar xzf /tmp/letsencrypt.tar.gz -C certbot/conf --strip-components=1
ls certbot/conf/live/   # должно быть: demo.itklabs.online  bot.itklabs.ru  README
```

Переключить renewal на webroot в обоих файлах
`certbot/conf/renewal/demo.itklabs.online.conf` и `certbot/conf/renewal/bot.itklabs.ru.conf` —
секцию с аутентификатором привести к виду:

```ini
authenticator = webroot
installer = none
webroot_path = /var/www/certbot,
[[webroot_map]]
demo.itklabs.online = /var/www/certbot
```

(для bot.itklabs.ru — свои домены в `webroot_map`).

> Проверь, что сертификат bot.itklabs.ru покрывает SAN'ами resume/youtrack:
> `openssl x509 -in certbot/conf/live/bot.itklabs.ru/cert.pem -noout -text | grep DNS:`
> Если нет — после переключения DNS выпусти отдельные сертификаты (см. п. 6).

## 3. Сборка и проверка ДО переключения DNS

```bash
cd /opt/task-manager
docker compose build
docker compose up -d
docker compose ps          # app должен стать healthy
docker compose logs app | tail -30

# Проверка по HTTPS через локальный nginx (DNS ещё смотрит на старую VPS):
curl -k --resolve demo.itklabs.online:443:127.0.0.1 https://demo.itklabs.online/actuator/health
curl -kI --resolve bot.itklabs.ru:443:127.0.0.1 https://bot.itklabs.ru/
# Ожидаем: {"status":"UP"} и ответ от прокси на 51.195.82.237
```

Тест продления (должен сказать «not due yet» или « Congratulations», без ошибок):

```bash
docker compose run --rm certbot renew --webroot -w /var/www/certbot --dry-run
```

## 4. Переключение (даунтайм ~2–5 минут)

За сутки: у A-записей **всех четырёх** доменов (demo.itklabs.online,
bot/resume/youtrack.itklabs.ru) поставить TTL 300 у регистратора.

```bash
# 1. На старой VPS остановить приложение (nginx пусть живёт, пока не пропагейтится DNS)
systemctl stop task-manager

# 2. Переключить A-записи всех 4 доменов на новый IP

# 3. Подождать пропагации (1–5 мин) и проверить уже по DNS:
dig +short demo.itklabs.online
curl -I https://demo.itklabs.online/actuator/health
curl -I https://bot.itklabs.ru/

# 4. В браузере: https://demo.itklabs.online → залогиниться qa@demo.com / Demo123!
```

## 5. После переезда

- [ ] Все 4 домена отвечают с нового IP, HTTPS валиден
- [ ] Логин, API, загрузка вложений, Swagger (`/swagger-ui.html`) работают
- [ ] `certbot renew --dry-run` (п. 3) прошёл; контейнер certbot жив: `docker compose logs certbot`
- [ ] Через 1–2 дня: на старой VPS `systemctl stop nginx`
- [ ] Через 3–7 дней: снапшот старой VPS, затем удаление

## 6. Выпуск сертификата заново (если переносить не хочется)

После переключения DNS: остановить nginx, выпустить certbot'ом, запустить обратно.

```bash
docker compose stop nginx
docker compose run --rm -p 80:80 certbot certonly --webroot -w /var/www/certbot \
  -d demo.itklabs.online --email you@example.com --agree-tos --no-eff-email
docker compose start nginx
```

(сначала подними только `docker compose up -d app`, иначе проверка домена не пройдёт)

## 7. Ежедневный деплой (новый вариант deploy.ps1)

Всё собирается на VPS, на рабочей машине ни JDK, ни Node не нужно:

```powershell
.\deploy-docker.ps1 -VpsIp NEW_IP
```

Что делает: `git pull` локально → по ssh `git pull && docker compose build app && docker compose up -d app`.
Пересобирается только app — nginx и сертификаты не дёргаются. Даунтайм ~15–20 сек
(пока стартует Spring). Кэш слоёв Docker делает пересборку быстрой (1–2 мин).

## 8. Полезные команды на VPS

```bash
docker compose ps                  # статус (app должен быть healthy)
docker compose logs -f app         # логи приложения (замена journalctl)
docker compose restart app         # рестарт приложения
docker compose up -d --build app   # пересборка + рестарт (после изменения кода)
docker compose pull && docker compose up -d   # обновить nginx/certbot
docker system df                   # место; при нехватке — docker system prune
```

## Отличия от старой схемы

- `server.address=127.0.0.1` (systemd) → в контейнере `SERVER_ADDRESS=0.0.0.0`
  (иначе nginx-контейнер не достучится; наружу порт всё равно не торчит — только `expose`).
- forwarded-заголовки: `SERVER_FORWARD_HEADERS_STRATEGY=framework` в образе
  (раньше задавалось аргументами в systemd drop-in).
- Логи: `docker compose logs app` вместо `journalctl -u task-manager`
  (ротация 3×10MB настроена в compose).
- Память: `-Xmx256m` жёстко → `mem_limit: 768m` + `-XX:MaxRAMPercentage=75`
  (JVM видит лимит контейнера).
- Продление сертификатов: certbot-контейнер (webroot) + reload nginx раз в 6 часов,
  вместо certbot.timer с authenticator=nginx.
