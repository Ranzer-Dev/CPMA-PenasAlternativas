@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo   CPMA - Servico Biometrico Facial (Python: 8001)
echo ========================================================
echo Verificando e liberando porta 8001...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-NetTCPConnection -LocalPort 8001 -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }"

set PYTHONPATH=cpma-facial-service
python cpma-facial-service/app/main.py
