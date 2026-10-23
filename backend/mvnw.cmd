@echo off
setlocal
set "MAVEN_VERSION=3.9.9"
set "MAVEN_DIR=%~dp0..\.tools\apache-maven-%MAVEN_VERSION%"
if exist "%MAVEN_DIR%\bin\mvn.cmd" goto run
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-maven.ps1"
if errorlevel 1 exit /b 1
:run
call "%MAVEN_DIR%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%