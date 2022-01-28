rem FOR /F "delims=" %i IN ('cd') DO set curwd=%i
rem echo %cwd%
rem start Simulator.exe
java -Dfile.encoding=UTF-8  -jar mosip-packet-creator-0.0.1-SNAPSHOT.jar --spring.config.location=file:///D:\GITDSLFresh\mosip-automation-tests\deploy\config\application.properties>>runlog.txt
