# Automation Tests

## Overview
This repo contains test framework for end2end testing of MOSIP functionality.  The following functionality is covered:
1. Registration 
1. Pre-registration + registration 
1. Authentication

## Components

1. API Automation (mosip-functional-tests)
     `https://github.com/mosip/mosip-functional-tests/blob/1.2.0-rc2/README.md`
1. E2E Automation (mosip-automation-tests)
1. Packet Generation tool (mosip-automation-tests)

## Execution flow	
![](docs/test-orchestrator.png)

## Prerequisites

1. For Windows
    * Java (11) and Maven  software should be installed on the machine from where the automation tests will be executed
    * Git bash
    * Run vcredist_x86.exe ( Note : one time activity it will be installed as a service, no need to do it again for any further update)
1. For Linux
     * Assume Java (11 or above), Maven and Git software are available

## Repository Details
Below are repository details of various modules used for the automation.

### DSL Orchestrator
* Authentication Demo Service: `https://github.com/mosip/mosip-functional-tests/tree/develop/authentication-demo-service `
* Automation Tests: `https://github.com/mosip/mosip-functional-tests/tree/develop/automationtests`
* IVV Orchestrator: `https://github.com/mosip/mosip-automation-tests/tree/1.2.0-rc2/mosip-acceptance-tests/ivv-orchestrator`

### Packet Utility(Packet Generation tool)
* Mosip Test Data Provider: `https://github.com/mosip/mosip-automation-tests/tree/main/mosipTestDataProvider`
* Mosip-Packet-Creator: `https://github.com/mosip/mosip-automation-tests/tree/main/mosip-packet-creator`


## Build and run
### To build End to End Automation Project Sequence:
* Authentication Demo Service `mvn clean install`
* Automation Tests `mvn clean install`
* Acceptance Tests(Location: mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator) `mvn clean install`
    - After Successful build will get the JAR (ivv-orchestrator-0.1.1-SNAPSHOT-jar-with-dependencies.jar)

### To build Packet Utility Project Sequence:
* Mosip Test Data Provider `mvn clean install`
* Mosip-Packet-Creator `mvn clean install`
    - After successful build will get the JAR (mosip-packet-creator-0.0.1-SNAPSHOT.jar)
    - Packet Utility is used to create and uploads the packet which is used by the e2e automation.


## Configuration - Packet Utility
1. Download deploy.zip version 0.6 `https://mosip.atlassian.net/wiki/spaces/R1/pages/330825775/Automation+release+notes+and+deliverables` and extract its contents into a folder.
1. Download vcredist_x86.exe version 0.6 `https://mosip.atlassian.net/wiki/spaces/R1/pages/330825775/Automation+release+notes+and+deliverables` and execute it.
  ( Note : one time activity it will be installed as a service , no need to do it again for any further update)
  ![](docs/deploy-folder-structure.png)
1. Start Mock mds by executing batch file (Location: deploy\mockmds\run.bat) and verify its running on port 4501(default port)
1. Update ..\config\application.properties with the following details
      * mosip.test.baseurl=https://qa.mosip.net
      * server.port=8080
      * mosip.test.temp=/temp/ (create empty 'temp` folder inside current directory e.g. D:\temp)
1. Update ..\resource\config\default.properties with the following details 
      * urlBase=https://qa.mosip.net -is the base URL of the target MOSIP server.
1.	Update ..\run.bat as mentioned below
	spring.config.location should have the absolute path of application.properties, e.g.
    `-Dspring.config.location=C:\Users\Downloads\deploy\config\application.properties`
1.	Execute run.bat
1.	Verify if the Packet utility is running by hitting :  `http://localhost:8080/swagger-ui.html#/ `
1.	For any failure in the packet utility verify the logs Location: deploy\runlog.txt


## Configuration - DSL Orchestrator
1. Build the E2E_Automation Acceptance Test Project and get the jar (Location: mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\target) 
2. Take the config folder from the mosip-acceptance test project (mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\src\main\resources\config) 
3. Take the local folder from the mosip-acceptance test project (mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\src\main\resources\local) 
4. End to End folder structure looks like below.
![](docs/e2efolder-structure)
1. Command to execute the e2e Automation (ivv-orchestrator-0.1.1-SNAPSHOT-jar-with-dependencies.jar) utility with below vm arguments
     * java `-Denv.user`=environment name `-Denv.endpoint`=baseURL `-Denv.testLevel`=smoke `-DscenarioSheet`=<Scenario Sheet Name> `-Denv.langcode`=eng -jar ivv-orchestrator-0.1.1-SNAPSHOT-jar-with-dependencies.jar
     * `env.user`  =  environment name example qa, qa2, dev
     * `env.endpoint` = env where the application under test is deployed. Change the env hostname from <base_env> to any env that you will work on.
     * `scenarioSheet` = Scenariosheet.csv ( which we want to execute)
     * `env.testLevel` = smoke
     * `env.langcode`= eng (Default language of the target environment).
1. After the execution completes, the test report can be found in the path ..\testng-report\emailable-report.html

## DSL Execution Logs
1. We can verify the Failure in the Logs mosip-acceptance-tests\ivv-orchestrator\src\logs\mosip-api-test.log


## License
This project is licensed under the terms of [Mozilla Public License 2.0]
