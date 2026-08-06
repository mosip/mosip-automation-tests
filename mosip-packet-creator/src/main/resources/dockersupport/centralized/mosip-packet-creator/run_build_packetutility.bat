
timeout 10

call chdir /d D:\GITAuto\dsl121\mosip-automation-tests\mosipTestDataProvider

call mvn clean install
if errorlevel 1 exit /b 1



call chdir /d  D:\GITAuto\dsl121\mosip-automation-tests\mosip-packet-creator

call mvn clean install
if errorlevel 1 exit /b 1

copy D:\GITAuto\dsl121\mosip-automation-tests\mosip-packet-creator\target\dslrig-packetcreator-1.5.0-SNAPSHOT.jar  D:\centralized\mosip-packet-creator\dslrig-packetcreator-1.5.0-SNAPSHOT.jar
if errorlevel 1 exit /b 1
pause

