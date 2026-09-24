@echo off
echo ========================================================
echo   INICIANDO ECOSSISTEMA COMPLETO CPMA PENAS ALTERNATIVAS
echo ========================================================

echo [1/3] Iniciando Servico Biometrico Facial (Python: 8001)...
start "CPMA - Servico Biometrico" cmd /k "run_facial_service.bat"

timeout /t 2 /nobreak >nul

echo [2/3] Iniciando Backend REST API (Spring Boot: 8080)...
start "CPMA - Servidor Backend" cmd /k "run_backend.bat"

timeout /t 5 /nobreak >nul

echo [3/3] Iniciando Aplicativo Desktop (JavaFX + AtlantaFX)...
start "CPMA - Desktop App" cmd /k "run_desktop.bat"

echo.
echo Todos os modulos foram iniciados em suas respectivas janelas.
