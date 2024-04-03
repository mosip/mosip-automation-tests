# Automation Tests

## Overview
This repo contains test framework for end2end testing of MOSIP functionality.  The following functionality is covered
1. Registration 
1. Pre-registration + registration 
1. Authentication

## Components

1. API automation [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests/)
1. E2E automation (this repo)
1. Packet Generation tool (this repo)
   

## Execution flow	
![](docs/test-orchestrator.png)

## Prerequisites

For Windows/Linux

* Java (11) and Maven  software should be installed on the machine from where the automation tests will be executed
* Git bash
   
## Repository details
Below are repository details of various modules used for the automation

### DSL Orchestrator
* Authentication Demo Service in [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests/).
* Automation tests in [mosip-functional-tests](https://github.com/mosip/mosip-functional-tests/).
* [IVV Orchestrator](mosip-acceptance-tests/ivv-orchestrator/)

### Packet Utility(Packet Generation tool)
* [Mosip Test Data Provider](mosipTestDataProvider)
* [Mosip-Packet-Creator](mosip-packet-creator)

## Build and run
### To build end to end automation 
* Authentication Demo Service `mvn clean install -Dgpg.skip`
* Automation Tests `mvn clean install -Dgpg.skip`
* Acceptance Tests(location: mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator) `mvn clean install -Dgpg.skip -Dgpg.skip`
    - After Successful build will get the jar (dslrig-ivv-orchestrator-1.2.1-develop-SNAPSHOT-jar-with-dependencies.jar)

### To build Packet Utility
* Mosip Test Data Provider `mvn clean install -Dgpg.skip`
* Mosip-Packet-Creator `mvn clean install -Dgpg.skip`
    - After successful build will get the jar (dslrig-packetcreator-1.2.1-develop-SNAPSHOT.jar)
    - Packet Utility is used to create and uploads the packet which is used by the e2e automation

## Configuration - Packet Utility
1. Download `centralized folder from src/main/resources/dockersupport
1. Under `mosip-packet-creator
1. Biometric Devices= Contains Mockmds specific files.
1. config= application.properties configurations
1. config=default.properties
1. mapper=demographic mappings environment specific or default setup.
1. privatekeys=machine specific details for encrypting and signing the packet.					
1.	Update ..\run.bat as mentioned below
1.	Keep mosip-packet-creator-1.2.0.1-develop-SNAPSHOT.jar and execute run.bat
1.	Verify if the Packet utility is running by hitting `http://localhost:8080/v1/packetcreator/swagger-ui.html#/`
1.	For any failure in the packet utility verify the logs location: mosip-packet-creator\PacketUtilityRunlog.txt


## Configuration - DSL Orchestrator
1. Build the E2E_Automation acceptance test project and get the jar  `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\target`
2. Take the config folder from the mosip-acceptance test project `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\src\main\resources\config`
3. Update kernel properties secret keys based on the env details.
1. Update kernel file property `scenariosToExecute=2` update scenario number for execution and keep this empty to run entire full suite
1. Command to execute the e2e automation (dslrig-ivv-orchestrator-1.2.1-develop-SNAPSHOT-jar-with-dependencies.jar) utility with below vm arguments
     * java `-Denv.user`=environment name `-Denv.endpoint`=baseurl -jar dslrig-ivv-orchestrator-1.2.1-develop-SNAPSHOT-jar-with-dependencies.jar
     * `env.user`  =  environment name example qa, qa2, dev
     * `env.endpoint` = base environment
1. After the execution completes, the test report can be found in the path `..\testng-report\emailable-report.html`

## DSL execution logs
1. We can verify the failure in the logs `mosip-acceptance-tests\ivv-orchestrator\src\logs\mosip-api-test.log`


## Docker setup build
1. Deploy Auth demo service
	-Use these branches of code.
	`https://github.com/mosip/mosip-functional-tests/tree/release-1.2.0.1`
	`https://github.com/mosip/mosip-helm/tree/1.2.0.1/charts/authdemo`
1. Deploy Packet creator
	-Use these branches of code.
	`https://github.com/mosip/mosip-automation-tests/tree/release-1.2.0.1`
	`https://github.com/mosip/mosip-helm/tree/1.2.0.1/charts/packetcreator`
1. Deploy Dsl testrig
	-Use these branches of code.
	`https://github.com/mosip/mosip-automation-tests/tree/release-1.2.0.1`
	`https://github.com/mosip/mosip-helm/tree/1.2.0.1/charts/dslorchestrator`
1. Orchestrator Config maps setup
![](docs/configmaps1.png)
![](docs/configmaps2.png)
![](docs/configmaps3.png)
1. To run particular scenario `scenariosToExecute=2` update scenario number for execution and keep this empty to run entire full suite
1.Scenario sheet gets picked internally placed at this path `https://github.com/mosip/mosip-automation-tests/blob/release-1.2.0.1/mosip-acceptance-tests/ivv-orchestrator/src/main/resources/config/scenarios.json`
1. Report gets generated on minio in the mentioned S2 bucket folder in configmaps. For example pick similar to below two reports one is testng report other is extent report.
  -DSL-api-internal.qa-release.mosip.net-full-run-1707833456032-report_T-161_P-1_S-63_F-97
  -ExtentReport-DSL-api-internal.qa-release.mosip.net-full-run-1707833456032-report_T-161_P-1_S-63_F-97

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
