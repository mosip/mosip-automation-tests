package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthPartnerProcessor;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.testscripts.BioAuth;
import io.mosip.testscripts.DemoAuth;
import io.mosip.testscripts.MultiFactorAuth;
import io.mosip.testscripts.OtpAuth;

//"e2e_multiFactorAuthentication(faceDevice,phoneNumber,UIN,$$uin,$$personaFilePath)"
public class MultiFactorAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(MultiFactorAuthentication.class);
	private static final String MULTIFACTOR = "idaData/MultiFactorAuth/MultiFactorAuth.yml";
	Properties deviceProp =null;
	Properties uinResidentDataPathFinalProps = new Properties();
	OtpAuth otpAuth=new OtpAuth() ;
	MultiFactorAuth multiFactorAuth = new MultiFactorAuth();
	BioAuth bioAuth = new BioAuth();
	DemoAuth demoAuth = new DemoAuth();
	List<String> demoAuthList = null;
	List<String> bioAuthList = null;
	String individualType = null;
	String individualIdAuth = null;
	String bioResponse = null;
	String demofields=null;
	String multiFactorResponse=null;
	String uins = null;
	String demoResponse = null;

	@Override
	public void run() throws RigInternalError {
		AuthPartnerProcessor.startProcess();
		//uinPersonaProp.put("2310290713", "C:\\\\Users\\\\user\\\\AppData\\\\Local\\\\Temp\\\\residents_8783170256176160783\\\\915849158491584.json");

		List<String> demoFetchList = null;
		TestCaseDTO test = null;
		
		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		}
		
		if(step.getParameters().size() > 1) {
			for(int i=0;i<=step.getParameters().size();i++) {
				if (i == 0) {
					if (!StringUtils.isBlank(step.getParameters().get(i))) {
						bioAuthList = Arrays.asList(step.getParameters().get(i).split("@@"));
					} else {
						bioAuthList = new ArrayList<String>(uinPersonaProp.stringPropertyNames());
					}
				}
				
				if (i == 1) {
					if (!StringUtils.isBlank(step.getParameters().get(i))) {
						demoAuthList = Arrays.asList(step.getParameters().get(i).split("@@"));
						demoFetchList = new ArrayList<String>();
						demoFetchList.add(E2EConstants.DEMOFETCH);
					} else {
						demoAuthList = new ArrayList<String>(uinPersonaProp.stringPropertyNames());
					}
				}
				
				if (i == 2) {
					if (!StringUtils.isBlank(step.getParameters().get(i))) {
						individualType = step.getParameters().get(i);
					} else {
						individualType = uinPersonaProp.stringPropertyNames().iterator().next();
					}
				}
					
				if(i == 3){
					if (step.getParameters().get(i).startsWith("$$")) {
						individualIdAuth = step.getParameters().get(i);
						individualIdAuth = step.getScenario().getVariables().get(individualIdAuth);
					} else if (!step.getParameters().get(i).equals("0")) {
						individualIdAuth = step.getParameters().get(i);  // uin  actual value
					}
					else {
						individualIdAuth = uinPersonaProp.stringPropertyNames().iterator().next();
					}
				}
			}
			Object[] testObj = multiFactorAuth.getYmlTestData(MULTIFACTOR);
			for(Object obj:testObj) {
				test = (TestCaseDTO)obj;
				test = demoAuthE2eTest(demoFetchList, individualIdAuth, test);
				test = bioAuthE2eTest(bioAuthList, individualIdAuth, test);
				test = otpAuthE2eTest(individualIdAuth, test);
			}
			try {
				multiFactorAuth.test(test);
			} catch (AuthenticationTestException e) {
				e.printStackTrace();
			} catch (AdminTestException e) {
				e.printStackTrace();
			}
			
		}
	}

	private TestCaseDTO otpAuthE2eTest(String individualIdAuth, TestCaseDTO test) throws RigInternalError {
		test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", props.getProperty("partnerKey")));
	
		 String input=test.getInput();
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
				 individualIdAuth, "individualId");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
				 individualType, "individualIdType");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
				 individualIdAuth, "sendOtp.individualId");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
				 individualType, "sendOtp.individualIdType");
		 input= input.replace("$PartnerKey$", props.getProperty("partnerKey"));
		test.setInput(input);
		return test;
		
	}

	private TestCaseDTO demoAuthE2eTest(List<String> demoFetchList, String individualIdAuth, TestCaseDTO testInput) throws RigInternalError {
		
		String personFilePathvalue = null;
		if (step.getParameters().size() > 4) {
			String _personFilePath = step.getParameters().get(5);
			if (_personFilePath.startsWith("$$")) {
				_personFilePath = step.getScenario().getVariables().get(_personFilePath);
				personFilePathvalue = _personFilePath;
			}
		} else if (uinPersonaProp.containsKey(individualIdAuth))
			personFilePathvalue = uinPersonaProp.getProperty(individualIdAuth);
		else
			throw new RigInternalError("Persona doesn't exist for the given UIN " + individualIdAuth);
		demoResponse = packetUtility.retrieveBiometric(personFilePathvalue, demoFetchList);
		
		testInput.setEndPoint(testInput.getEndPoint().replace("$PartnerKey$", props.getProperty("partnerKey")));
		
		String demoFieldValueKey=null;
		String demoValue = null;
		String input = null;
		
		for(String demoField : demoAuthList )
		{
			switch(demoField) {
			case "dob":
				demoFieldValueKey=E2EConstants.DEMODOB;
				break;
			case "emailId":
				demoFieldValueKey=E2EConstants.DEMOEMAIL;
				break;
			case "phoneNumber":
				demoFieldValueKey=E2EConstants.DEMOPHONE;
				break;
			case "age":
				demoFieldValueKey=E2EConstants.DEMOAGE;
				break;
			default:
				throw new RigInternalError("Given DEMO doesn't match with the options in the script");
			}
			
			demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey);
			if(demoValue==null)
				throw new RigInternalError("Received null value from Persona for" + demoField);
			input = testInput.getInput();
			
			input = JsonPrecondtion.parseAndReturnJsonContent(input,demoField, "identityRequest.key");
			input = JsonPrecondtion.parseAndReturnJsonContent(input,demoValue, "identityRequest.value");
			//testInput=filterOutTestCase(testObj,testFilterKey);
			testInput.setInput(input);
		}
		return testInput;
	
}


private TestCaseDTO bioAuthE2eTest(List<String> bioAuthList, String uin, TestCaseDTO test) throws RigInternalError {
	String deviceInfoFilePath = null;
	for(String bioAuth:bioAuthList) {
		deviceInfoFilePath = bioAuth;
		if (!StringUtils.isBlank(deviceInfoFilePath)) {
			deviceInfoFilePath = TestRunner.getExeternalResourcePath()
					+ props.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
			deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
		} else
			throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
		String personFilePathvalue = null;
			if (step.getParameters().size() > 4) {
				String _personFilePath = step.getParameters().get(5);
				if (_personFilePath.startsWith("$$")) {
					_personFilePath = step.getScenario().getVariables().get(_personFilePath);
					personFilePathvalue = _personFilePath;
				}
			} else if (uinPersonaProp.containsKey(uin))
				personFilePathvalue = uinPersonaProp.getProperty(uin);
			else
				throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);
		
		String bioType=null, bioSubType=null;
		List<String> modalityList = new ArrayList<>();
		String modalityToLog = null;
		String modalityKeyTogetBioValue = null;
		if(deviceProp != null) {
			bioType= deviceProp.getProperty("bioType");
			bioSubType=	deviceProp.getProperty("bioSubType");
			switch(bioType) {
			case E2EConstants.FACEBIOTYPE:
				modalityList.add(E2EConstants.FACEFETCH);
				modalityToLog = bioType;
				modalityKeyTogetBioValue = E2EConstants.FACEFETCH;
				break;
			case E2EConstants.IRISBIOTYPE:
				modalityList.add(E2EConstants.IRISFETCH);
				modalityToLog = bioSubType+"_"+bioType;
				modalityKeyTogetBioValue = (bioSubType.equalsIgnoreCase("left"))? E2EConstants.LEFT_EYE:E2EConstants.RIGHT_EYE;
				break;
			case E2EConstants.FINGERBIOTYPE:
				modalityList.add(E2EConstants.FINGERFETCH);
				modalityToLog = bioSubType;
				modalityKeyTogetBioValue = bioSubType;
				break;
			default:
				throw new RigInternalError("Given BIO Type in device property file is not valid");
			}
		}
		
		multiFactorResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList);
		System.out.println("saddjha");
		if (multiFactorResponse != null && !multiFactorResponse.isEmpty() && modalityKeyTogetBioValue!= null) {
			String bioValue = JsonPrecondtion.getValueFromJson(multiFactorResponse, modalityKeyTogetBioValue);
			test = bioAuth(modalityToLog, bioValue, uin, deviceProp, test, this.bioAuth);
			System.out.println(test);
		}
	}
	System.out.println(test);
		return test;
		
	}
	private TestCaseDTO bioAuth(String modalityToLog, String bioValue, String uin, Properties deviceProps, TestCaseDTO test,
			BioAuth bioAuth2) {
		
		 String input = test.getInput();
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,uin, "individualId");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("bioSubType"), "identityRequest.bioSubType");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("bioType"), "identityRequest.bioType");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("deviceCode"), "identityRequest.deviceCode");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("deviceProviderID"), "identityRequest.deviceProviderID");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("deviceServiceID"), "identityRequest.deviceServiceID");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("deviceServiceVersion"), "identityRequest.deviceServiceVersion");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("deviceProvider"), "identityRequest.deviceProvider");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("deviceSubType"), "identityRequest.deviceSubType");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("make"), "identityRequest.make");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("model"), "identityRequest.model");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("serialNo"), "identityRequest.serialNo");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("type"), "identityRequest.type");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					deviceProps.getProperty("individualIdType"), "individualIdType");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input, bioValue, "identityRequest.bioValue");
		 test.setInput(input); 
		 
		 Reporter.log("<b><u>" + test.getTestCaseName()+"_"+ modalityToLog + "</u></b>");
		
	 return test;
	}


}
