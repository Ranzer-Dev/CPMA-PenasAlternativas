@echo off
setlocal
cd /d "%~dp0cpma-desktop-totem"

echo ==========================================================
echo    CPMA - TERMINAL TOTEM KIOSK (Desktop Electron)         
echo ==========================================================
echo.
echo Verificando build estatico do Totem...
if not exist "dist\index.html" (
    echo Gerando build do React com Vite...
    call npm run build
)

echo Iniciando janela nativa Kiosk do Totem...
npx electron .
