color f9

TITLE MOSIP - Run Auth Demo Service-upgrade2


timeout 10

chdir /d C:\Users\pankaj.godiyal\.m2\repository\io\mosip\authentication\authentication-demo-service\1.2.1-SNAPSHOT

cls
java -jar -Dmosip.base.url=https://api-internal.upgrade2.mosip.net -Dserver.port=8083 -Dauth-token-generator.rest.clientId=mosip-resident-client -Dauth-token-generator.rest.secretKey=jGSnSgsErPFNwlmA  -Dauth-token-generator.rest.appId=resident authentication-demo-service-1.2.1-develop-SNAPSHOT.jar





