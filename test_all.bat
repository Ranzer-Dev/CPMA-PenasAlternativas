@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo   CPMA - EXECUTANDO BATERIA COMPLETA DE TESTES
echo ========================================================

set MVN_CMD=mvn
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "D:\apache-maven-3.9.9\bin\mvn.cmd" set MVN_CMD=D:\apache-maven-3.9.9\bin\mvn.cmd
)

echo.
echo [1/4] Testando cpma-backend (Java Spring Boot 3 + MockMvc)...
call %MVN_CMD% test -f cpma-backend/pom.xml
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha nos testes do cpma-backend!
    exit /b %ERRORLEVEL%
)

echo.
echo [2/4] Testando cpma-facial-service (Python FastAPI + Biometria)...
set PYTHONPATH=cpma-facial-service
python -m pytest -c cpma-facial-service/pytest.ini cpma-facial-service/tests
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha nos testes do servico biometrico!
    exit /b %ERRORLEVEL%
)

echo.
echo [3/4] Validando build de cpma-desktop-delegado (React + TypeScript + Vite)...
call npm --prefix cpma-desktop-delegado run build
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha no build do cpma-desktop-delegado!
    exit /b %ERRORLEVEL%
)

echo.
echo [4/4] Validando build de cpma-desktop-totem (React + TypeScript + Vite)...
call npm --prefix cpma-desktop-totem run build
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha no build do cpma-desktop-totem!
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================================
echo   TODOS OS TESTES E BUILDS FORAM APROVADOS COM SUCESSO!
echo ========================================================
