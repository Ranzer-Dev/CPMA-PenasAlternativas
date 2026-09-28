@echo off
set SCRIPT_DIR=%~dp0
set SERVER_IP=localhost

if exist "%SCRIPT_DIR%cpma-server-ip.txt" (
    set /p SERVER_IP=<"%SCRIPT_DIR%cpma-server-ip.txt"
)

set TOTEM_URL=http://%SERVER_IP%:8080/api/v1/totem/index.html
set KIOSK_PROFILE=%TEMP%\cpma_totem_kiosk_profile

echo ==========================================================
echo    INICIANDO CPMA - TOTEM WEB KIOSK (NAVEGADOR)          
echo ==========================================================
echo Acessando terminal em: %TOTEM_URL%
echo.

set KIOSK_FLAGS=--kiosk "%TOTEM_URL%" --edge-kiosk-type=fullscreen --user-data-dir="%KIOSK_PROFILE%" --no-first-run --no-default-browser-check --disable-pinch --overscroll-history-navigation=0 --disable-features=Translate --disable-session-crashed-bubble --disable-infobars

where msedge >nul 2>nul
if %ERRORLEVEL% equ 0 (
    start msedge %KIOSK_FLAGS%
    exit /b 0
)

where chrome >nul 2>nul
if %ERRORLEVEL% equ 0 (
    start chrome %KIOSK_FLAGS%
    exit /b 0
)

start "" "%TOTEM_URL%"
