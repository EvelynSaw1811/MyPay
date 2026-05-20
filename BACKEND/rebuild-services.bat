@echo off
REM ─────────────────────────────────────────────────────────────────────
REM  rebuild-services.bat
REM
REM  Clean-builds common-lib first (every other service depends on it),
REM  then rebuilds each microservice in a sensible order. Skips tests
REM  so the loop is fast — flip SKIP_TESTS to "false" to run them.
REM
REM  USAGE:  Double-click, or run from cmd / PowerShell.
REM
REM  Always run this after editing:
REM    * an entity (DDL changes pick up on next service boot)
REM    * a DTO, mapper, controller, or service
REM    * common-lib (every dependent service must be rebuilt)
REM ─────────────────────────────────────────────────────────────────────

setlocal
set "ROOT=%~dp0"
set "SKIP_TESTS=true"

set "MVN_FLAGS=clean install"
if /I "%SKIP_TESTS%"=="true" set "MVN_FLAGS=%MVN_FLAGS% -DskipTests"

echo ====================================================================
echo   MyPay - Rebuilding all services
echo   Tests: skipped=%SKIP_TESTS%
echo ====================================================================
echo.

call :build "common-lib"
if errorlevel 1 goto :failed

call :build "auth-service"
if errorlevel 1 goto :failed

call :build "wallet-service"
if errorlevel 1 goto :failed

call :build "notification-service"
if errorlevel 1 goto :failed

call :build "collection-service"
if errorlevel 1 goto :failed

call :build "transaction-service"
if errorlevel 1 goto :failed

call :build "currency-service"
if errorlevel 1 goto :failed

call :build "reporting-service"
if errorlevel 1 goto :failed

call :build "api-gateway"
if errorlevel 1 goto :failed

call :build "discovery-server"
if errorlevel 1 goto :failed

call :build "config-server"
if errorlevel 1 goto :failed

echo.
echo ====================================================================
echo   All services rebuilt successfully.
echo ====================================================================
echo.
echo Next: run start-all-dev.bat to launch every service with --spring.profiles.active=dev
echo.
endlocal
pause
exit /b 0

:failed
echo.
echo ====================================================================
echo   BUILD FAILED in %SVC%. Stopping.
echo ====================================================================
echo Look above for the compiler / test error and fix it before retrying.
echo.
endlocal
pause
exit /b 1

REM ─────────────────────────────────────────────────────────────────────
REM  :build  <service-folder-name>
REM ─────────────────────────────────────────────────────────────────────
:build
set "SVC=%~1"
if not exist "%ROOT%%SVC%\mvnw.cmd" (
    echo [skip] %SVC% has no mvnw.cmd, skipping.
    exit /b 0
)
echo.
echo --- Building %SVC% ---
pushd "%ROOT%%SVC%"
call mvnw.cmd %MVN_FLAGS%
set "RC=%ERRORLEVEL%"
popd
if not "%RC%"=="0" (
    echo *** %SVC% FAILED with exit code %RC% ***
    exit /b %RC%
)
echo --- %SVC% done ---
exit /b 0
