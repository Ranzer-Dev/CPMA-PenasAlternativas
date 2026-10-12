@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo   CPMA - Servidor Backend REST API (Spring Boot: 8080)
echo ========================================================
echo Verificando e liberando porta 8080...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }"

set MVN_CMD=mvn
if exist "D:\apache-maven-3.9.9\bin\mvn.cmd" set MVN_CMD=D:\apache-maven-3.9.9\bin\mvn.cmd

echo Iniciando Spring Boot na porta 8080...
%MVN_CMD% spring-boot:run -f cpma-backend/pom.xml
