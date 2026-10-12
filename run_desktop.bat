@echo off
setlocal
cd /d "%~dp0"
echo ========================================================
echo   CPMA - Aplicativo Desktop (JavaFX + AtlantaFX)
echo ========================================================
set MVN_CMD=mvn
if exist "D:\apache-maven-3.9.9\bin\mvn.cmd" set MVN_CMD=D:\apache-maven-3.9.9\bin\mvn.cmd

%MVN_CMD% javafx:run -f cpma-desktop/pom.xml
