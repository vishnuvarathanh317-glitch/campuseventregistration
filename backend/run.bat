@echo off
echo ================================================
echo  Campus Event Registration - Start Server
echo ================================================

set LIB=lib\*
set OUT=out

echo Starting server on http://localhost:8080
java -cp "%OUT%;%LIB%" Main
