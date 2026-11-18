@echo off
setlocal
cd /d "%~dp0cpma-desktop-delegado"

echo ==========================================================
echo    CPMA - MESA DO DELEGADO (React 18 + Vite + Tailwind)   
echo ==========================================================
echo.
echo Verificando servicos necessarios (Backend :8080)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$b = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue; if (-not $b) { Write-Host '  [AVISO] Backend Spring Boot (porta 8080) nao detectado. Os dados serao carregados do MockStore em memoria.' -ForegroundColor Yellow } else { Write-Host '  [OK] Backend Spring Boot conectado via Proxy reverso!' -ForegroundColor Green }"
echo.
echo Iniciando servidor de desenvolvimento Vite na porta 3000...
start "" http://localhost:3000
npm run dev
