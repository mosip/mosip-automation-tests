# Automation Tests

## Overview
This repo contains test framework for end2end testing of MOSIP functionality.  The following functionality is covered:
1. Registration 
1. Pre-registration + registration 
1. Authentication

## Components

1. API automation (mosip-functional-tests)
     `https://github.com/mosip/mosip-functional-tests/blob/1.2.0-rc2/README.md`
1. E2E automation (mosip-automation-tests)
1. Packet Generation tool (mosip-automation-tests)

## Execution flow	
![](docs/test-orchestrator.png)

## Prerequisites

1. For Windows
   * Java (11) and Maven  software should be installed on the machine from where the automation tests will be executed
   * Git bash
   
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
* Acceptance Tests(location: mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator) `mvn clean install`
    - After Successful build will get the jar (ivv-orchestrator-0.1.1-SNAPSHOT-jar-with-dependencies.jar)

### To build Packet Utility Project Sequence:
* Mosip Test Data Provider `mvn clean install`
* Mosip-Packet-Creator `mvn clean install`
    - After successful build will get the jar (mosip-packet-creator-0.0.1-SNAPSHOT.jar)
    - Packet Utility is used to create and uploads the packet which is used by the e2e automation.


## Configuration - Packet Utility
1. Download (deploy folder)[https://github.com/mosip/mosip-automation-tests/tree/1.2.0-rc2/deploy]
1. Download (vcredist_x86.exe)[https://www.microsoft.com/en-us/download/details.aspx?id=48145]
  ( Note : one time activity it will be installed as a service , no need to do it again for any further update)
1. Download and build (MockMDS)[https://github.com/mosip/mosip-mock-services/tree/master/MockMDS]
1. Start Mock mds by executing batch file (location: deploy\mockmds\run.bat) and verify its running on port 4501(default port)
1. Update ..\config\application.properties with the following details
      * `mosip.test.baseurl`=https://qa.mosip.net
      * `server.port`=8080
      * `mosip.test.temp`=/temp/ (create empty 'temp` folder inside current directory e.g. D:\temp)
1. Update ..\resource\config\default.properties with the following details 
      * `urlBase`=https://qa.mosip.net -is the base url of the target MOSIP server.
1.	Update ..\run.bat as mentioned below
	`spring.config.location` should have the absolute path of application.properties, e.g.
    `-Dspring.config.location=C:\Users\Downloads\deploy\config\application.properties`
1.	Execute run.bat
1.	Verify if the Packet utility is running by hitting :  `http://localhost:8080/swagger-ui.html#/ `
1.	For any failure in the packet utility verify the logs Location: deploy\runlog.txt
1. Deploy folder structure looks like

  ![](docs/deploy-folder-structure.png)


## Configuration - DSL Orchestrator
1. Build the E2E_Automation acceptance test project and get the jar  `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\target`
2. Take the config folder from the mosip-acceptance test project `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\src\main\resources\config`
3. Take the local folder from the mosip-acceptance test project `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\src\main\resources\local`
4. End to end folder structure looks like below.
![](docs/e2efolder-structure.png)
1. Command to execute the e2e automation (ivv-orchestrator-0.1.1-SNAPSHOT-jar-with-dependencies.jar) utility with below vm arguments
     * java `-Denv.user`=environment name `-Denv.endpoint`=baseurl `-Denv.testLevel`=smoke `-DscenarioSheet`=<scenariosheetname> `-Denv.langcode`=eng -jar ivv-orchestrator-0.1.1-SNAPSHOT-jar-with-dependencies.jar
     * `env.user`  =  environment name example qa, qa2, dev
     * `env.endpoint` = base environment
     * `scenarioSheet` = scenariosheet.csv ( which we want to execute)
     * `env.testLevel` = smoke
     * `env.langcode`= eng (default language of the target environment).
1. After the execution completes, the test report can be found in the path `..\testng-report\emailable-report.html`

## DSL execution logs
1. We can verify the failure in the logs `mosip-acceptance-tests\ivv-orchestrator\src\logs\mosip-api-test.log`


## License
This project is licensed under the terms of [Mozilla Public License 2.0]
