@echo off
set SCRIPT_DIR=%~dp0
set SERVER_IP=localhost

if exist "%SCRIPT_DIR%cpma-server-ip.txt" (
    set /p SERVER_IP=<"%SCRIPT_DIR%cpma-server-ip.txt"
)

set TOTEM_URL=http://%SERVER_IP%:8080/api/v1/totem/index.html

echo ==========================================================
echo    INICIANDO CPMA - TOTEM WEB KIOSK (NAVEGADOR)          
echo ==========================================================
echo Acessando terminal em: %TOTEM_URL%
echo.

where msedge >nul 2>nul
if %ERRORLEVEL% equ 0 (
    start msedge --kiosk "%TOTEM_URL%" --edge-kiosk-type=fullscreen
    exit /b 0
)

where chrome >nul 2>nul
if %ERRORLEVEL% equ 0 (
    start chrome --kiosk "%TOTEM_URL%"
    exit /b 0
)

start "" "%TOTEM_URL%"
