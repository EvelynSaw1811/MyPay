@echo off
REM ─────────────────────────────────────────────────────────────────────
REM  reset-databases.bat
REM
REM  Drops every MyPay database and re-runs init-schemas.sql so the next
REM  boot of each Spring Boot service (with the "dev" profile) recreates
REM  tables via JPA ddl-auto and refills seed data.
REM
REM  USAGE:  Double-click this file, or run it from a cmd / PowerShell.
REM
REM  REQUIRES:
REM    * MySQL Server installed somewhere standard, OR provide the full
REM      path to mysql.exe when prompted.  (Run find-mysql.bat first if
REM      you're not sure where mysql.exe lives.)
REM    * All Spring Boot services STOPPED before running this script.
REM ─────────────────────────────────────────────────────────────────────

setlocal EnableDelayedExpansion

REM ── Configurable ────────────────────────────────────────────────────
set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
set "INIT_SQL=%~dp0init-schemas.sql"
REM ────────────────────────────────────────────────────────────────────

REM ── Locate mysql.exe ────────────────────────────────────────────────
set "MYSQL_BIN="

REM 1. Already on PATH?
for /f "delims=" %%I in ('where mysql 2^>nul') do (
    if not defined MYSQL_BIN set "MYSQL_BIN=%%I"
)

REM 2. Dynamically scan for any "MySQL Server <version>" folder under the
REM    standard install roots — picks up 5.x, 8.x, 9.x and beyond without
REM    needing this script to be edited every time MySQL ships a release.
if not defined MYSQL_BIN call :scan_root "C:\Program Files\MySQL"
if not defined MYSQL_BIN call :scan_root "C:\Program Files (x86)\MySQL"
if not defined MYSQL_BIN call :scan_root "D:\Program Files\MySQL"

REM 3. XAMPP / WAMP / MariaDB fallbacks.
if not defined MYSQL_BIN call :try "C:\xampp\mysql\bin\mysql.exe"
if not defined MYSQL_BIN call :try "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
if not defined MYSQL_BIN call :try "C:\Program Files\MariaDB 11.5\bin\mysql.exe"
if not defined MYSQL_BIN call :try "C:\Program Files\MariaDB 11.4\bin\mysql.exe"
if not defined MYSQL_BIN call :try "C:\Program Files\MariaDB 10.11\bin\mysql.exe"
if not defined MYSQL_BIN call :try "C:\Program Files\MariaDB 10.6\bin\mysql.exe"

REM 4. Last resort — ask the user. Loop until they give a real path or cancel.
if not defined MYSQL_BIN goto :prompt_path
goto :have_path

:prompt_path
echo.
echo Could not auto-detect mysql.exe in any standard location.
echo TIP: run find-mysql.bat first to see what paths exist on this machine.
set "MYSQL_BIN="
set /p "MYSQL_BIN=Paste the full path to mysql.exe (blank to abort): "
if not defined MYSQL_BIN goto :user_aborted
if "!MYSQL_BIN!"=="" goto :user_aborted
if not exist "!MYSQL_BIN!" (
    echo.
    echo ERROR: "!MYSQL_BIN!" does not exist on disk. Try again.
    goto :prompt_path
)

:have_path
echo Using mysql client: !MYSQL_BIN!
echo.

REM ── Confirm + collect password ───────────────────────────────────────
echo ====================================================================
echo   MyPay - Database reset
echo ====================================================================
echo.
echo This will DROP the following databases and recreate them empty:
echo   - ewallet_auth_db
echo   - ewallet_wallet_db
echo   - ewallet_collection_db
echo   - ewallet_transaction_db
echo   - ewallet_currency_db
echo   - ewallet_notification_db
echo.
echo Make sure ALL Spring Boot services are stopped first.
echo.
set "CONFIRM="
set /p "CONFIRM=Type RESET to continue, anything else to abort: "
if /I not "!CONFIRM!"=="RESET" goto :user_aborted

echo.
set "MYSQL_PWD="
set /p "MYSQL_PWD=Enter MySQL password for user "%MYSQL_USER%" (input is echoed): "

REM ── Drop ─────────────────────────────────────────────────────────────
echo.
echo [1/2] Dropping databases...
"!MYSQL_BIN!" -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p!MYSQL_PWD! -e "DROP DATABASE IF EXISTS ewallet_auth_db; DROP DATABASE IF EXISTS ewallet_wallet_db; DROP DATABASE IF EXISTS ewallet_collection_db; DROP DATABASE IF EXISTS ewallet_transaction_db; DROP DATABASE IF EXISTS ewallet_currency_db; DROP DATABASE IF EXISTS ewallet_notification_db;"
if errorlevel 1 goto :drop_failed

REM ── Recreate from init-schemas.sql ───────────────────────────────────
echo [2/2] Recreating empty databases from init-schemas.sql...
"!MYSQL_BIN!" -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p!MYSQL_PWD! < "%INIT_SQL%"
if errorlevel 1 (
    echo.
    echo WARN: init-schemas.sql did not run cleanly. Databases will still be
    echo recreated on first service boot because the JDBC URL has
    echo createDatabaseIfNotExist=true, so this is recoverable.
)

echo.
echo ====================================================================
echo   Done. Databases are empty.
echo ====================================================================
echo.
echo Next step: run start-all-dev.bat to boot every service with the
echo "dev" profile. Seeders will repopulate the tables on startup.
echo.
endlocal
pause
exit /b 0

:drop_failed
echo.
echo ERROR: DROP failed. Check credentials, that MySQL is running,
echo and that no Spring Boot service is still holding connections.
endlocal
pause
exit /b 1

:user_aborted
echo Aborted.
endlocal
pause
exit /b 1

REM ─────────────────────────────────────────────────────────────────────
REM  :try  <full-path-to-mysql.exe>
REM    Sets MYSQL_BIN if the file exists.
REM ─────────────────────────────────────────────────────────────────────
:try
if exist %~1 set "MYSQL_BIN=%~1"
goto :eof

REM ─────────────────────────────────────────────────────────────────────
REM  :scan_root  <vendor-folder>
REM    Walks every "MySQL Server *" subfolder of <vendor-folder> and sets
REM    MYSQL_BIN to the first bin\mysql.exe it finds. Workbench folders
REM    are intentionally ignored — their bundled mysql.exe is a stripped
REM    client that may not behave like the full Server CLI.
REM
REM    Note on quoting: <vendor-folder> may contain spaces ("Program
REM    Files"). Every reference must be wrapped in double quotes, and the
REM    `for /d` glob pattern also needs quoting.
REM ─────────────────────────────────────────────────────────────────────
:scan_root
set "ROOT=%~1"
if not exist "%ROOT%" goto :eof
for /d %%D in ("%ROOT%\MySQL Server *") do (
    if not defined MYSQL_BIN (
        if exist "%%~fD\bin\mysql.exe" set "MYSQL_BIN=%%~fD\bin\mysql.exe"
    )
)
goto :eof
