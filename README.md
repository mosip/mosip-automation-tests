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
* Authentication Demo Service `mvn clean install`
* Automation Tests `mvn clean install`
* Acceptance Tests(location: mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator) `mvn clean install`
    - After Successful build will get the jar (ivv-orchestrator-1.2.0.1-SNAPSHOT-jar-with-dependencies.jar)

### To build Packet Utility
* Mosip Test Data Provider `mvn clean install`
* Mosip-Packet-Creator `mvn clean install`
    - After successful build will get the jar (mosip-packet-creator-1.2.0.1-SNAPSHOT.jar)
    - Packet Utility is used to create and uploads the packet which is used by the e2e automation

## Configuration - Packet Utility
1. Download vcredist_x86.exe `https://www.microsoft.com/en-us/download/details.aspx?id=48145`
  ( Note : one time activity it will be installed as a service , no need to do it again for any further update)
1. Download `centralized folder from src/main/resources/dockersupport
	- Under `mosip-packet-creator
			-Biometric Devices= Contains Mockmds specific files.
			-config= application.properties configurations
			-resource
					-config=default.properties
					-mapper=demographic mappings environment specific or default setup.
					-privatekeys=machine specific details for encrypting and signing the packet.
					
1. Set device certificates as per the environment and keep certificate under each modality keys. See [MDSdevicecert.md](https://github.com/mosip/mosip-infra/blob/1.2.0-rc2/deployment/sandbox-v2/docs/MDSdevicecert.md).
1. Place Device p12 file under `centralized\mountvolume\mockmdscert\api-internal.env_context

1. Update `..\resource\config\default.properties with the following details 
        * `packetutilURLBase=http://localhost:8080
1.	Update ..\run.bat as mentioned below
1.	Keep mosip-packet-creator-1.2.0.1-SNAPSHOT.jar and execute run.bat
1.	Verify if the Packet utility is running by hitting `http://localhost:8080/swagger-ui.html#/ `
1.	For any failure in the packet utility verify the logs location: mosip-packet-creator\PacketUtilityRunlog.txt


## Configuration - DSL Orchestrator
1. Build the E2E_Automation acceptance test project and get the jar  `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\target`
2. Take the config folder from the mosip-acceptance test project `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\src\main\resources\config`
3. Take the local folder from the mosip-acceptance test project `mosip-automation-tests\mosip-acceptance-tests\ivv-orchestrator\src\main\resources\local`

1. Command to execute the e2e automation (ivv-orchestrator-1.2.0.1-SNAPSHOT-jar-with-dependencies.jar) utility with below vm arguments
     * java `-Denv.user`=environment name `-Denv.endpoint`=baseurl `-Denv.testLevel`=sanity `-Denv.langcode`=eng -jar ivv-orchestrator-1.2.0.1-SNAPSHOT-jar-with-dependencies.jar
     * `env.user`  =  environment name example qa, qa2, dev
     * `env.endpoint` = base environment
     * `env.testLevel` = sanity
     * `env.langcode`= eng (default language of the target environment)
1. After the execution completes, the test report can be found in the path `..\testng-report\emailable-report.html`

## DSL execution logs
1. We can verify the failure in the logs `mosip-acceptance-tests\ivv-orchestrator\src\logs\mosip-api-test.log`

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).
