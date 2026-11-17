@echo off
setlocal
cd /d "%~dp0cpma-desktop-delegado"
echo ========================================================
echo   CPMA - Sistema do Delegado (Electron + React)
echo ========================================================
if not exist "dist\index.html" (
    call npm run build
)
call npm run electron
