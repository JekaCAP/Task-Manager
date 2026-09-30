# Деплой task-manager на VPS (Docker-схема).
# Всё собирается на VPS внутри Docker: нужен только git-доступ к репозиторию.
#
# Пример: .\deploy-docker.ps1 -VpsIp 203.0.113.10
#         .\deploy-docker.ps1 -VpsIp 203.0.113.10 -ProjectDir /opt/task-manager
param(
    [Parameter(Mandatory = $true)]
    [string]$VpsIp,
    [string]$ProjectDir = "/opt/task-manager"
)

$ErrorActionPreference = "Stop"

# 1. Локально: убедиться, что свежий main запушен
git checkout main
git pull

# 2. На VPS: забрать код и пересобрать ТОЛЬКО app (nginx и certbot не трогаем)
ssh "root@$VpsIp" "cd $ProjectDir && git pull && docker compose build app && docker compose up -d app"

# 3. Проверка (Spring поднимается ~15-20 сек)
Write-Host "Ждём поднятия Spring..." -ForegroundColor Yellow
Start-Sleep -Seconds 20
curl.exe -s -o NUL -w "%{http_code}`n" https://demo.itklabs.online/
