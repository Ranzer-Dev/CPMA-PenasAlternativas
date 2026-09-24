@echo off
echo ========================================================
echo   CPMA - EXECUTANDO BATERIA COMPLETA DE TESTES
echo ========================================================
echo.
echo [1/3] Testando cpma-backend (Java Spring Boot 3 + MockMvc)...
call D:\apache-maven-3.9.9\bin\mvn.cmd test -f cpma-backend/pom.xml
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha nos testes do cpma-backend!
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Testando cpma-facial-service (Python FastAPI + Biometria)...
set PYTHONPATH=cpma-facial-service
python -m pytest -c cpma-facial-service/pytest.ini cpma-facial-service/tests
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha nos testes do servico biometrico!
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Testando cpma-desktop (JavaFX + AtlantaFX + ApiClient)...
call D:\apache-maven-3.9.9\bin\mvn.cmd test -f cpma-desktop/pom.xml
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha nos testes do cpma-desktop!
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================================
echo   TODOS OS TESTES FORAM APROVADOS COM SUCESSO! (100%%)
echo ========================================================
