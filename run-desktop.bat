@echo off
REM ===========================================================================
REM  Racion - Desktop (Windows)
REM  Doble clic para compilar y abrir la app.
REM  No requiere configurar JAVA_HOME ni el PATH: el script los detecta.
REM ===========================================================================
setlocal
cd /d "%~dp0"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\racion.ps1" desktop
set "EXITCODE=%ERRORLEVEL%"

echo.
echo ------------------------------------------------------------
if not "%EXITCODE%"=="0" (
    echo  Termino con errores ^(codigo %EXITCODE%^).
    echo  Lee los mensajes de arriba.
) else (
    echo  Todo bien.
)
echo ------------------------------------------------------------
echo.
pause
exit /b %EXITCODE%
