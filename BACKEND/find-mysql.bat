@echo off
REM ─────────────────────────────────────────────────────────────────────
REM  find-mysql.bat
REM
REM  Hunts for mysql.exe on this machine and prints every copy it finds,
REM  so you know what to paste into reset-databases.bat.
REM ─────────────────────────────────────────────────────────────────────

setlocal EnableDelayedExpansion
set "FOUND=0"

echo.
echo Searching for mysql.exe on this machine...
echo (this can take 10-60 seconds)
echo.

REM Quick check — is it already on PATH?
for /f "delims=" %%I in ('where mysql 2^>nul') do (
    echo [PATH] %%I
    set "FOUND=1"
)

REM Deep search in the typical install roots.
for %%R in ("C:\Program Files" "C:\Program Files (x86)" "C:\xampp" "C:\wamp64" "C:\MariaDB" "D:\Program Files") do (
    if exist %%~R (
        for /f "delims=" %%F in ('dir /s /b "%%~R\mysql.exe" 2^>nul') do (
            echo [FOUND] %%F
            set "FOUND=1"
        )
    )
)

echo.
if "!FOUND!"=="0" (
    echo No mysql.exe found in standard install roots.
    echo MySQL may not be installed, or it lives somewhere unusual.
) else (
    echo Copy one of the [FOUND] paths above and paste it into
    echo reset-databases.bat when it asks for the mysql.exe path.
)

echo.
endlocal
pause
