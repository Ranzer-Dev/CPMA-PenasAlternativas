@echo off
set SCRIPT_DIR=%~dp0
set SERVER_IP=localhost

if exist "%SCRIPT_DIR%cpma-server-ip.txt" set /p SERVER_IP=<"%SCRIPT_DIR%cpma-server-ip.txt"

set TOTEM_URL=http://%SERVER_IP%:8080/api/v1/totem/index.html
set KIOSK_PROFILE=%TEMP%\cpma_totem_kiosk_profile

echo ==========================================================
echo    INICIANDO CPMA - TOTEM WEB KIOSK (NAVEGADOR)          
echo ==========================================================
echo Acessando terminal em: %TOTEM_URL%
echo.

set KIOSK_FLAGS=--kiosk "%TOTEM_URL%" --edge-kiosk-type=fullscreen --user-data-dir="%KIOSK_PROFILE%" --no-first-run --no-default-browser-check --disable-pinch --overscroll-history-navigation=0 --disable-features=Translate,FullscreenExitUI,EdgeSwipe --disable-session-crashed-bubble --disable-infobars --disable-notifications --disable-component-update --noerrdialogs --hide-scrollbars --incognito

if exist "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" goto run_edge_x86
if exist "C:\Program Files\Microsoft\Edge\Application\msedge.exe" goto run_edge_64
if exist "C:\Program Files\Google\Chrome\Application\chrome.exe" goto run_chrome_64
if exist "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" goto run_chrome_x86
if exist "%LOCALAPPDATA%\Google\Chrome\Application\chrome.exe" goto run_chrome_local

where msedge >nul 2>nul
if %ERRORLEVEL% equ 0 goto run_edge_where

where chrome >nul 2>nul
if %ERRORLEVEL% equ 0 goto run_chrome_where

start "" "%TOTEM_URL%"
exit /b 0

:run_edge_x86
start "" "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" %KIOSK_FLAGS%
exit /b 0

:run_edge_64
start "" "C:\Program Files\Microsoft\Edge\Application\msedge.exe" %KIOSK_FLAGS%
exit /b 0

:run_chrome_64
start "" "C:\Program Files\Google\Chrome\Application\chrome.exe" %KIOSK_FLAGS%
exit /b 0

:run_chrome_x86
start "" "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" %KIOSK_FLAGS%
exit /b 0

:run_chrome_local
start "" "%LOCALAPPDATA%\Google\Chrome\Application\chrome.exe" %KIOSK_FLAGS%
exit /b 0

:run_edge_where
start "" msedge %KIOSK_FLAGS%
exit /b 0

:run_chrome_where
start "" chrome %KIOSK_FLAGS%
exit /b 0
