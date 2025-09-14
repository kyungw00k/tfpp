@echo off
rem TFPP (Thymeleaf File Preprocessor) Wrapper Script for Windows
rem Usage: tfpp [options] <command> [args...]

setlocal enabledelayedexpansion

rem Determine the script directory
set "SCRIPT_DIR=%~dp0"
set "TFPP_HOME=%SCRIPT_DIR%.."

rem Set JAVA_HOME if not already set
if "%JAVA_HOME%"=="" (
    for /f "delims=" %%i in ('where java 2^>nul') do (
        set "JAVA_PATH=%%i"
        for %%j in ("!JAVA_PATH!") do set "JAVA_HOME=%%~dpj.."
        goto :found_java
    )
    echo Error: Java not found. Please install Java or set JAVA_HOME.
    exit /b 1
    :found_java
)

rem Check if Java is available
if not exist "%JAVA_HOME%\bin\java.exe" (
    if not exist "%JAVA_HOME%\bin\java" (
        where java >nul 2>&1
        if errorlevel 1 (
            echo Error: Java not found. Please install Java or set JAVA_HOME.
            exit /b 1
        )
        set "JAVA_CMD=java"
    ) else (
        set "JAVA_CMD=%JAVA_HOME%\bin\java"
    )
) else (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
)

rem TFPP JAR location
set "TFPP_JAR=%TFPP_HOME%\app\build\libs\tfpp.jar"

rem Check if JAR exists
if not exist "%TFPP_JAR%" (
    echo Error: TFPP JAR not found at %TFPP_JAR%
    echo Please build TFPP first by running: gradlew.bat jar
    exit /b 1
)

rem JVM options (can be overridden by environment variables)
if "%TFPP_JVM_OPTS%"=="" set "TFPP_JVM_OPTS=-Xmx512m"

rem Execute TFPP
"%JAVA_CMD%" %TFPP_JVM_OPTS% %TFPP_OPTS% -jar "%TFPP_JAR%" %*