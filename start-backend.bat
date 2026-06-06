@echo off
set "JAVA_HOME=%~dp0tools\jdk17\jdk-17.0.14+7"
set "MAVEN_HOME=%~dp0tools\maven\apache-maven-3.9.8"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"
echo JAVA_HOME=%JAVA_HOME%
echo.
cd /d "%~dp0asset-management-backend"
call mvn spring-boot:run
pause