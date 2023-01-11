color f9

TITLE MOSIP - Run Auth Demo Service

timeout 10

chdir /d C:\Users\Neeharika.Garg\.m2\repository\io\mosip\authentication\authentication-demo-service\1.2.1-SNAPSHOT

cls
java -jar -Dmosip.base.url=https://api-internal.collab.mosip.net -Dserver.port=8384 -Dauth-token-generator.rest.clientId=mosip-resident-client -Dauth-token-generator.rest.secretKey=cI51BMfdTKoDrmly -Dauth-token-generator.rest.appId=resident authentication-demo-service-1.2.1-SNAPSHOT.jar