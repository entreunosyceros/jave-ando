@echo off
setlocal EnableExtensions
REM JAVe-Ando — Windows (CMD). Requiere JDK 17+ en PATH o JAVA_HOME.
cd /d "%~dp0"
set "VIEWER=%~dp0"
set "ROOT=%VIEWER%.."

if not exist out mkdir out

set "JAVAC=javac"
set "JAVA_BIN=java"
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC=%JAVA_HOME%\bin\javac.exe"
  if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
)

REM Genera sources.txt con rutas relativas (forward slashes; valido para javac en Windows)
set "SOURCES_OK=0"
where powershell >nul 2>&1
if %ERRORLEVEL%==0 (
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Get-ChildItem -Path 'src' -Filter '*.java' -Recurse | ForEach-Object { ($_.FullName.Substring((Get-Location).Path.Length + 1)) -replace '\\','/' } | Sort-Object | Set-Content -Encoding utf8 'out/sources.txt'"
  if not errorlevel 1 set "SOURCES_OK=1"
)

if "%SOURCES_OK%"=="0" (
  echo [run.bat] PowerShell no disponible: compilando paquete viewer directamente.
  "%JAVAC%" -encoding UTF-8 -d out src\com\ifcd0112\viewer\*.java
  if errorlevel 1 exit /b 1
  goto :launch
)

"%JAVAC%" -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 exit /b 1

:launch
cd /d "%ROOT%"
"%JAVA_BIN%" -cp "%VIEWER%out" com.ifcd0112.viewer.Launcher
endlocal
