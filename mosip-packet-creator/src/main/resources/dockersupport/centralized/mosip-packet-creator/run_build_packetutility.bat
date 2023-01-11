
timeout 10

call chdir /d D:\GITDSLFresh121Snap1\mosip-automation-tests\mosipTestDataProvider

call mvn clean install



call chdir /d  D:\GITDSLFresh121Snap1\mosip-automation-tests\mosip-packet-creator

call mvn clean install

copy D:\GITDSLFresh121Snap1\mosip-automation-tests\mosip-packet-creator\target\mosip-packet-creator-1.2.1-SNAPSHOT.jar  D:\centralized\mosip-packet-creator\mosip-packet-creator-1.2.1-SNAPSHOT.jar  
pause

