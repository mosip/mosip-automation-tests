
timeout 10

call chdir /d D:\GITAuto\dsl121\mosip-automation-tests\mosipTestDataProvider

call mvn clean install
if errorlevel 1 (
  echo Maven build failed for mosipTestDataProvider.
  exit /b 1
)



call chdir /d  D:\GITAuto\dsl121\mosip-automation-tests\mosip-packet-creator

call mvn clean install
if errorlevel 1 (
  echo Maven build failed for mosip-packet-creator.
  exit /b 1
)

copy /Y D:\GITAuto\dsl121\mosip-automation-tests\mosip-packet-creator\target\dslrig-packetcreator-1.6.0-SNAPSHOT.jar  D:\centralized\mosip-packet-creator\dslrig-packetcreator-1.6.0-SNAPSHOT.jar
if errorlevel 1 (
  echo JAR copy failed.
  exit /b 1
)
pause
