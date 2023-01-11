chdir /d D:\centralized\orchestrator-dev2
java -Denv.user=api-internal.dev2 -Denv.endpoint=https://api-internal.dev2.mosip.net -Denv.testLevel=smoke -DscenarioSheet=scenariosheet_full.csv -Denv.langcode=eng -jar ivv-orchestrator-1.2.1-SNAPSHOT-jar-with-dependencies.jar


@echo off
For /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c-%%a-%%b)

cls

copy D:\centralized\orchestrator-dev2\testng-report\emailable-report.html D:\centralized\orchestrator-dev2\testng-report\dev2-DSL-report-%mydate%.html