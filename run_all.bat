@echo off
setlocal
cd /d "%~dp0"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run_ecosystem.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [AVISO] Execucao do orquestrador PowerShell falhou. Tentando modo de contingencia...
    timeout /t 2 /nobreak >nul
    start "CPMA - Servico Biometrico" cmd /k "run_facial_service.bat"
    timeout /t 3 /nobreak >nul
    start "CPMA - Servidor Backend" cmd /k "run_backend.bat"
    timeout /t 6 /nobreak >nul
    start "CPMA - Desktop App" cmd /k "run_desktop.bat"
)
