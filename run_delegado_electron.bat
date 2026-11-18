@echo off
setlocal
cd /d "%~dp0cpma-desktop-delegado"

echo ==========================================================
echo    CPMA - MESA DO DELEGADO (Desktop Electron)             
echo ==========================================================
echo.
echo Verificando build estatico...
if not exist "dist\index.html" (
    echo Gerando build do React com Vite...
    call npm run build
)

echo Iniciando janela nativa do Electron...
npx electron .
