@echo off
REM ─────────────────────────────────────────────────────────────────────
REM  start-all-dev.bat
REM
REM  Boots every MyPay microservice in its own terminal window with the
REM  "dev" Spring profile (so the @Profile("dev") seeders run).
REM
REM  Boot order matches each service's discovery dependency:
REM    config-server  → discovery-server →
REM      auth-service → wallet-service → notification-service →
REM      collection-service → transaction-service →
REM      currency-service → reporting-service →
REM        api-gateway
REM
REM  Each service runs in a separate window so you can read its logs and
REM  Ctrl+C it independently. Closing the spawned window stops that one
REM  service; this launcher window can be closed safely.
REM
REM  REQUIRES:
REM    * Each service folder has a working mvnw.cmd (Maven Wrapper).
REM    * MySQL is running and reachable.
REM    * RabbitMQ is running (for services that publish/consume events).
REM ─────────────────────────────────────────────────────────────────────

setlocal
set "ROOT=%~dp0"
set "PROFILE_ARG=-Dspring-boot.run.profiles=dev"

echo Launching MyPay services with --spring.profiles.active=dev
echo Root: %ROOT%
echo.

call :launch "config-server"        15
call :launch "discovery-server"     20
call :launch "auth-service"          8
call :launch "wallet-service"        5
call :launch "notification-service"  5
call :launch "collection-service"    5
call :launch "transaction-service"   5
call :launch "currency-service"      5
call :launch "reporting-service"     5
call :launch "api-gateway"           0

echo.
echo ====================================================================
echo   All services launched.
echo ====================================================================
echo Watch each spawned window for "Started ... in N seconds" and the
echo "[Seed] ..." lines. The api-gateway is the last one up.
echo.
echo Front-end: cd FRONTEND\mypay-frontend ^&^& npm run dev
echo.
endlocal
pause
exit /b 0

REM ─────────────────────────────────────────────────────────────────────
REM  :launch  <service-folder>  <wait-seconds-before-next>
REM ─────────────────────────────────────────────────────────────────────
:launch
set "SVC=%~1"
set "WAIT=%~2"
echo Starting %SVC% ...
start "MyPay - %SVC%" cmd /k "cd /d %ROOT%%SVC% && mvnw.cmd spring-boot:run %PROFILE_ARG%"
if not "%WAIT%"=="0" (
    echo   (waiting %WAIT%s for %SVC% to register before next service^)
    timeout /t %WAIT% /nobreak >nul
)
goto :eof
