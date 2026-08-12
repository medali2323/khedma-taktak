@echo off
cd /d "%~dp0"
echo === Compilation (nouveau JwtService) ===
call mvnw.cmd clean compile -DskipTests
if errorlevel 1 goto err
if not exist "target\classes\com\khedmataktak\security\JwtService.class" (
  echo ERREUR: JwtService.class introuvable
  goto err
)
echo === Demarrage ===
if "%JWT_SECRET%"=="" echo JWT_SECRET non defini - valeur par defaut application.yml utilisee
if "%DB_PASSWORD%"=="" echo Astuce: set DB_PASSWORD=... pour MySQL
call mvnw.cmd spring-boot:run
goto end
:err
pause
exit /b 1
:end
