
timeout 10

call chdir /d D:\GITAuto\dsl121\mosip-automation-tests\mosipTestDataProvider

call mvn clean install



call chdir /d  D:\GITAuto\dsl121\mosip-automation-tests\mosip-packet-creator

call mvn clean install

copy D:\GITAuto\dsl121\mosip-automation-tests\mosip-packet-creator\target\dslrig-packetcreator-1.2.1-develop-SNAPSHOT.jar  D:\centralized\mosip-packet-creator\dslrig-packetcreator-1.2.1-develop-SNAPSHOT.jar  
pause

