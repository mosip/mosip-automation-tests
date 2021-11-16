package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthPartnerProcessor;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;
import io.mosip.testscripts.DemoAuth;

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
		String addressResponse=null;
		demoAuth=new DemoAuth();
		String _personFilePath = null;

		AuthPartnerProcessor.startProcess();
		//uinPersonaProp.put("2759239619", "C:\\Users\\NEEHAR~1.GAR\\AppData\\Local\\Temp\\residents_2140454779925252334\\498484984849848.json");

		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			demofields = step.getParameters().get(0);
			if (!StringUtils.isBlank(demofields))
				demofieldList = new ArrayList<>(Arrays.asList(demofields.split("@@")));

		}
		if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else if (step.getParameters().size() > 2) { // "e2e_demoAuthentication(name,$$uin,$$personaFilePath)"
			uins = step.getParameters().get(1);
			_personFilePath = step.getParameters().get(2);
			if (uins.startsWith("$$") && _personFilePath.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				_personFilePath = step.getScenario().getVariables().get(_personFilePath);
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
			}
		} else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());

		Object[] testObj=demoAuth.getYmlTestData(DEMOPATH);
		TestCaseDTO test=(TestCaseDTO)testObj[0];
		test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", props.getProperty("partnerKey")));

		for (String uin : uinList) {
			String personFilePathvalue = null;
			if (step.getParameters().size() > 2) {
				personFilePathvalue = _personFilePath;
			} else if (uinPersonaProp.containsKey(uin))
				personFilePathvalue = uinPersonaProp.getProperty(uin);
			else
				throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);

			List<String> demoFetchList=new ArrayList<String>();
			demoFetchList.add(E2EConstants.DEMOFETCH);
			demoResponse = packetUtility.retrieveBiometric(personFilePathvalue, demoFetchList);
			List<String> addressFetchList=new ArrayList<String>();
			addressFetchList.add(E2EConstants.DEMOADDRESSFETCH);
			addressResponse = packetUtility.retrieveBiometric(personFilePathvalue, addressFetchList);
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
			{	String demoFieldValueKey=null;
			String demoValue=null;

			switch(demoField) {
			case E2EConstants.DEMODOB:
				demoFieldValueKey=E2EConstants.DEMODOB;
				demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
				if(demoValue==null)
					throw  new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
				identityReqJson.put(demoField, demoValue);
				break;

			case E2EConstants.DEMOEMAIL:
				demoFieldValueKey=E2EConstants.DEMOEMAIL;
				demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
				if(demoValue==null)
					throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
				identityReqJson.put(demoField, demoValue);
				break;

			case E2EConstants.DEMOYMLPHONE:
				demoFieldValueKey=E2EConstants.DEMOPHONE;
				demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
				if(demoValue==null)
					throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
				identityReqJson.put(demoField, demoValue);
				break;
				
			case E2EConstants.DEMOADDRESSFETCH:
				String addLine1=null,addLine2=null,addLine3=null;
				try {
					addLine1= JsonPrecondtion.JsonObjSimpleParsing(addressResponse,E2EConstants.DEMOADDRESSFETCH,E2EConstants.DEMOADDRESSLINE1);
					addLine2= JsonPrecondtion.JsonObjSimpleParsing(addressResponse,E2EConstants.DEMOADDRESSFETCH,E2EConstants.DEMOADDRESSLINE2);
					addLine3= JsonPrecondtion.JsonObjSimpleParsing(addressResponse,E2EConstants.DEMOADDRESSFETCH,E2EConstants.DEMOADDRESSLINE3);
				} catch (Exception e) {
					throw new RigInternalError(e.getMessage());
				}
				if(addLine1==null ||addLine2==null ||addLine3==null)
					throw  new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");


				JSONArray addressLine1Array=new JSONArray();
				JSONObject addressLine1Obj=new JSONObject();
				addressLine1Obj.put("language", BaseTestCase.languageList.get(0));
				addressLine1Obj.put("value", addLine1);
				addressLine1Array.put(addressLine1Obj);
				identityReqJson.put(E2EConstants.DEMOADDRESSLINE1, addressLine1Array);

				JSONArray addressLine2Array=new JSONArray();
				JSONObject addressLine2Obj=new JSONObject();
				addressLine2Obj.put("language",  BaseTestCase.languageList.get(0));
				addressLine2Obj.put("value", addLine2);
				addressLine2Array.put(addressLine2Obj);
				identityReqJson.put(E2EConstants.DEMOADDRESSLINE2, addressLine2Array);

				JSONArray addressLine3Array=new JSONArray();
				JSONObject addressLine3Obj=new JSONObject();
				addressLine3Obj.put("language",  BaseTestCase.languageList.get(0));
				addressLine3Obj.put("value", addLine3);
				addressLine3Array.put(addressLine3Obj);
				identityReqJson.put(E2EConstants.DEMOADDRESSLINE3, addressLine3Array);
				break;
			case E2EConstants.DEMONAME:

				String firstNm=null,midNm=null,lastNm=null,fullname=null;
				firstNm=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOFNAME);
				midNm=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOMNAME);
				lastNm=JsonPrecondtion.getValueFromJson(demoResponse,E2EConstants.DEMOFETCH+"."+E2EConstants.DEMOLNAME);
				if(firstNm==null||midNm==null||lastNm==null)
					throw new RigInternalError("Unable to get the Demo value for field " + demoField + " from Persona");
				fullname=firstNm +" "+ midNm + " "+ lastNm ;
				JSONArray nameArray=new JSONArray();
				JSONObject nameObj=new JSONObject();
				nameObj.put("language",  BaseTestCase.languageList.get(0));
				nameObj.put("value", fullname);
				nameArray.put(nameObj);
				identityReqJson.put(demoField, nameArray);
				break;
				
			case E2EConstants.DEMOGENDER:
				demoFieldValueKey=E2EConstants.DEMOGENDER;
				demoValue = JsonPrecondtion.getValueFromJson(demoResponse, E2EConstants.DEMOFETCH+"."+demoFieldValueKey); //array fill all the values
				JSONArray genArray=new JSONArray();
				JSONObject genderObj=new JSONObject();
				genderObj.put("language", BaseTestCase.languageList.get(0));
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
			inputJson.put("identityRequest", identityReqJson.toString());
			test.setInput(inputJson.toString());
			try {
				demoAuth.test(test);
			} catch (AuthenticationTestException | AdminTestException e) {
				throw new RigInternalError(e.getMessage());

			} finally {
				AuthPartnerProcessor.authPartherProcessor.destroyForcibly();
			}
		}

	}
}