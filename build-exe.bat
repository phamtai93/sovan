@echo off
REM Build Sovan Windows EXE Installer
REM This script builds the fat JAR and creates a Windows EXE using jpackage

setlocal enabledelayedexpansion

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM Path to jpackage - update this path if your Java installation is different
set JPACKAGE=C:\Program Files\Java\jdk-18.0.2.1\bin\jpackage.exe

REM Alternative paths to try if the above doesn't work
if not exist "%JPACKAGE%" (
    if exist "C:\Program Files\Java\jdk-19*\bin\jpackage.exe" (
        for /f "tokens=*" %%A in ('dir /b /s "C:\Program Files\Java\jdk-*\bin\jpackage.exe" 2^>nul ^| sort /r ^| findstr /m "jpackage"') do (
            set "JPACKAGE=%%A"
            goto found_jpackage
        )
    )
)

:found_jpackage
REM Check if jpackage exists
if not exist "%JPACKAGE%" (
    echo Error: jpackage not found at "%JPACKAGE%"
    echo.
    echo Please verify Java 18+ is installed.
    echo To find jpackage location, run:
    echo   where java
    echo   cd to that folder\..\..\bin and verify jpackage.exe exists there
    echo.
    echo Then update JPACKAGE variable in this script.
    exit /b 1
)

echo.
echo ====================================
echo Building Sovan Windows EXE Installer
echo ====================================
echo.

REM Step 0: Verify template files exist in resources
echo [0/4] Verifying template files in resources...
if not exist "src\main\resources" mkdir "src\main\resources"

set "templates=printCoverTemplate.docx printLabelSaoHanTemplate.docx printNotebookTemplate.docx printSoSaoHanTemplate.docx"
for %%t in (%templates%) do (
    if exist "src\main\resources\%%t" (
        echo [0/4] ✓ %%t found in resources
    ) else (
        echo [0/4] Warning: %%t not found in src\main\resources
    )
)
echo.

REM Step 1: Clean and package with Maven
echo [1/4] Building JAR with Maven...
call mvn clean package -q
if errorlevel 1 (
    echo Error: Maven build failed
    exit /b 1
)
echo [1/4] ✓ JAR built successfully

REM Step 2: Create output directory
echo [2/4] Preparing output directory...
if not exist "target\dist" mkdir "target\dist"
echo [2/4] ✓ Output directory ready
echo.

REM Step 3: Run jpackage
echo [3/4] Running jpackage to create EXE installer...
cd target

REM Try with exe first (requires WiX)
REM resource-dir points to classes folder where Maven compiled resources (App.config, templates, icons)
"%JPACKAGE%" --input . --name Sovan --main-jar Sovan-0.0.2-SNAPSHOT.jar --main-class vn.tafi.process.MainUIProcessor --type exe --vendor TAFI --app-version 0.0.2 --win-menu --win-menu-group TAFI --win-shortcut --resource-dir classes --dest dist --icon classes/images/app.ico
set EXE_RESULT=%errorlevel%

if %EXE_RESULT% neq 0 (
    echo.
    echo [Note] EXE creation requires WiX Toolset. Creating app-image instead...
    echo Download WiX from: https://wixtoolset.org
    echo.

    REM Fallback to app-image (also includes App.config and templates via resource-dir)
    "%JPACKAGE%" --input . --name Sovan --main-jar Sovan-0.0.2-SNAPSHOT.jar --main-class vn.tafi.process.MainUIProcessor --type app-image --vendor TAFI --app-version 0.0.2 --resource-dir classes --dest dist --icon classes/images/app.ico

    if errorlevel 1 (
        echo Error: jpackage app-image creation failed
        cd ..
        exit /b 1
    )
)

cd ..
echo [3/4] ✓ Packaging complete
echo.

REM Step 4: Verify the output
echo [4/4] Verifying output...
if exist "target\dist\Sovan-0.0.2.exe" (
    echo [4/4] ✓ Verification successful
    echo.
    echo ====================================
    echo Build Complete - EXE Installer!
    echo ====================================
    echo.
    echo Installer location:
    echo   %SCRIPT_DIR%target\dist\Sovan-0.0.2.exe
    echo.
    echo To install and test:
    echo   1. Run: target\dist\Sovan-0.0.2.exe
    echo   2. Follow the installation wizard
    echo   3. Launch Sovan from Start Menu or Desktop shortcut
    echo.
    dir /l "target\dist\Sovan-*.exe"
) else if exist "target\dist\Sovan" (
    echo [4/4] ✓ Verification successful
    echo.
    echo ====================================
    echo Build Complete - Portable App!
    echo ====================================
    echo.
    echo Application location:
    echo   %SCRIPT_DIR%target\dist\Sovan
    echo.
    echo To run Sovan:
    echo   1. Navigate to: target\dist\Sovan
    echo   2. Run: Sovan.exe or Sovan.bat
    echo   3. The app includes a bundled Java runtime
    echo.
    dir /l "target\dist\Sovan"
    echo.
    echo Note: This is a portable version. To create a Windows installer (EXE),
    echo install WiX Toolset from: https://wixtoolset.org
    echo Then run this script again.
) else (
    echo Error: Output not found in target\dist\
    echo.
    ls -la target\dist\
    exit /b 1
)

endlocal
exit /b 0
