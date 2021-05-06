package io.mosip.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
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
import io.restassured.response.Response;

public class DemoAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(DemoAuthentication.class);
	private static final String DEMOPATH = "idaData/DemoAuth/DemoAuth.yml";
	DemoAuth demoAuth ;

	@Override
	public void run() throws RigInternalError {
		String demofields=null;
		List<String> demofieldList = null;
		List<String> uinList = null;
		String uins = null;
		String demoResponse = null;
		demoAuth=new DemoAuth();

		AuthPartnerProcessor.startProcess();
		//uinPersonaProp.put("2759239619", "C:\\Users\\NEEHAR~1.GAR\\AppData\\Local\\Temp\\residents_2140454779925252334\\498484984849848.json");
		
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			demofields = step.getParameters().get(0);  //dob
			if (!StringUtils.isBlank(demofields))
				demofieldList = new ArrayList<>(Arrays.asList(demofields.split("@@")));

		}
		if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());

		List<String> demoFetchList=new ArrayList<String>();
		demoFetchList.add(E2EConstants.DEMOFETCH);


		Object[] testObj=demoAuth.getYmlTestData(DEMOPATH);

		TestCaseDTO test=(TestCaseDTO)testObj[0];
	test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", props.getProperty("partnerKey")));


		for (String uin : uinList) {
			String personFilePathvalue = null;
			if (uinPersonaProp.containsKey(uin))
				personFilePathvalue = uinPersonaProp.getProperty(uin);
			else
				throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);

			
			
			demoResponse = packetUtility.retrieveBiometric(personFilePathvalue, demoFetchList);

			String input=test.getInput();
			 input = JsonPrecondtion.parseAndReturnJsonContent(input,
						uin, "individualId");
			
			JSONObject inputJson = new JSONObject(input);
			
			String identityRequest = null;
			if(inputJson.has("identityRequest")) {
				identityRequest = inputJson.get("identityRequest").toString();
			}
			JSONObject identityReqJson = new JSONObject(identityRequest);
		
			for(String demoField : demofieldList )
			{	String testFilterKey=null,demoFieldValueKey=null;
				String demoValue=null;

				switch(demoField) {
				case "dob":
					demoFieldValueKey=E2EConstants.DEMODOB;
					 demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
					if(demoValue==null)
						throw  new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
					 identityReqJson.put(demoField, demoValue);
								break;
				
				case "emailId":
					demoFieldValueKey=E2EConstants.DEMOEMAIL;
					demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
					if(demoValue==null)
						 throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
					
					identityReqJson.put(demoField, demoValue);
									break;

				case "phoneNumber":
					demoFieldValueKey=E2EConstants.DEMOPHONE;
					demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
					if(demoValue==null)
						 throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
					
					identityReqJson.put(demoField, demoValue);
					
					break;

				case "age":
					demoFieldValueKey=E2EConstants.DEMOAGE;
					demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
					if(demoValue==null)
						 throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
					
					identityReqJson.put(demoField, demoValue);
					
					break;
				case "address":
					testFilterKey="deg";
					//demoFieldValueKey=E2EConstants.DEMOAGE;

					break;
					
				case "name":
					testFilterKey="deg";
					
					String fn=null,mn=null,ln=null,fullname=null;
					fn=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOFNAME);

					mn=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOMNAME);

					ln=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOLNAME);
					
					fullname=fn +" "+ mn + " "+ ln ;
					JSONArray nameArray=new JSONArray();
					JSONObject nameObj=new JSONObject();
					
					nameObj.put("language", "eng");
					nameObj.put("value", fullname);
					nameArray.put(nameObj);
					
					
					if(fullname==null)
						 throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
					
					identityReqJson.put(demoField, nameArray);
					
					
					break;
			/*	case "name1":
					testFilterKey="deg";
					
					String fn=null,mn=null,ln=null,fullname=null;
					fn=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOFNAME);

					mn=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOMNAME);

					ln=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOLNAME);
					
					//fullname=fn +" "+ mn + " "+ ln ;
					JSONArray nameArray1=new JSONArray();
					JSONObject nameObj1=new JSONObject();
					
					nameObj1.put("language", "eng");
					nameObj1.put("value", fn);
					nameObj1.put("value", mn);
					nameObj1.put("value", ln);
					nameArray1.put(nameObj);
					
					
					if(fullname==null)
						 throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
					
					identityReqJson.put(demoField, nameArray1);
					
					
					break;*/
		
				case "gender":
					 demoFieldValueKey=E2EConstants.DEMOGENDER;
					 demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
					JSONArray genArray=new JSONArray();
					JSONObject genderObj=new JSONObject();
					
					genderObj.put("language", "eng");
					genderObj.put("value", demoValue);
					
					genArray.put(genderObj);
					
					if(demoValue==null)
						throw  new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
					
					identityReqJson.put(demoField, genArray);
					break;

				default:
					throw new RigInternalError("Given DEMO doesn't match with the options in the script");
				}	
			}
				inputJson.put("identityRequest",identityReqJson.toString());

				test.setInput(inputJson.toString());
				
						
				try {
					demoAuth.test(test);
				} catch (AuthenticationTestException | AdminTestException e) {
					throw new RigInternalError(e.getMessage());

				}
				finally {
					AuthPartnerProcessor.authPartherProcessor.destroyForcibly();
				}


			
		}

	}
}