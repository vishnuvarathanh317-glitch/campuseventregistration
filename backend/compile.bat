@echo off
echo ================================================
echo  Campus Event Registration - Compile
echo ================================================

set SRC=src\main\java
set LIB=lib\*
set OUT=out

if not exist %OUT% mkdir %OUT%

echo Compiling Java sources...
javac -cp "%LIB%" -d %OUT% ^
  %SRC%\util\*.java ^
  %SRC%\model\*.java ^
  %SRC%\dao\*.java ^
  %SRC%\service\*.java ^
  %SRC%\controller\*.java ^
  %SRC%\Main.java

if %ERRORLEVEL% == 0 (
    echo.
    echo [SUCCESS] Compilation complete. Run run.bat to start the server.
) else (
    echo.
    echo [ERROR] Compilation failed. Check errors above.
)
