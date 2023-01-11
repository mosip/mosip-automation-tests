color f9

TITLE MOSIP - Orachestrastor collab

timeout 10

chdir /d D:\centralized\orchestrator-collab

cls
java -Denv.user=api-internal.collab -Denv.endpoint=https://api-internal.collab.mosip.net -Denv.testLevel=smoke -DscenarioSheet=scenariosheet_sanity.csv -Denv.langcode=eng -jar ivv-orchestrator-1.2.1-SNAPSHOT-jar-with-dependencies.jar