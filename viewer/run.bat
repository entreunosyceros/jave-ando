@echo off
setlocal EnableExtensions
REM JAVe-Ando — Windows (CMD). Requiere JDK 17+ en PATH o JAVA_HOME.
cd /d "%~dp0"
set "VIEWER=%~dp0"
if "%VIEWER:~-1%"=="\" set "VIEWER=%VIEWER:~0,-1%"
set "ROOT=%VIEWER%\.."

if not exist out mkdir out

set "JAVAC=javac"
set "JAVA_BIN=java"
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC=%JAVA_HOME%\bin\javac.exe"
  if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
)

REM sources.txt sin BOM (PowerShell UTF-8 suele romper javac @file)
if exist out\sources.txt del /q out\sources.txt
for /r src %%f in (*.java) do (
  echo %%f>> out\sources.txt
)

if not exist out\sources.txt (
  echo ERROR: no hay archivos .java en src\
  pause
  exit /b 1
)

"%JAVAC%" -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 (
  echo.
  echo ERROR: compilacion fallida. Instala JDK 17+ y comprueba PATH o JAVA_HOME.
  pause
  exit /b 1
)

cd /d "%ROOT%"
"%JAVA_BIN%" -cp "%VIEWER%\out" com.ifcd0112.viewer.Launcher
if errorlevel 1 (
  echo.
  echo ERROR al iniciar JAVe-Ando.
  pause
  exit /b 1
)
endlocal
