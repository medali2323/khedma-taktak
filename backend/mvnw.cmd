@REM Maven Wrapper - Windows
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

if not defined JAVA_HOME (
  if exist "C:\Users\PC_DALI\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.14.7-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Users\PC_DALI\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.14.7-hotspot"
  ) else if exist "C:\Users\PC_DALI\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.12.8-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Users\PC_DALI\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.12.8-hotspot"
  )
)

if not defined JAVA_HOME (
  echo ERREUR: definissez JAVA_HOME vers JDK 17 ou 21
  exit /b 1
)

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
if not exist "%WRAPPER_JAR%" (
  echo Telechargement maven-wrapper.jar...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "New-Item -ItemType Directory -Force -Path '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper' | Out-Null; ^
     Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%WRAPPER_JAR%'"
)

"%JAVA_HOME%\bin\java.exe" ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

exit /b %ERRORLEVEL%
