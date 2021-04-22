package io.mosip.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import io.restassured.response.Response;

public class BiometricAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(BiometricAuthentication.class);
	private static final String BIOMETRIC_FACE = "ida/BioAuth/BioAuth.yml";
	Properties deviceProp = new Properties();
	Properties uinResidentDataPathFinalProps = new Properties();
	private String FACE;
	private String LEFT_EYE;
	private String RIGHT_EYE;
	private String RIGHT_INDEX;
	private String RIGHT_LITTLE;
	private String RIGHT_RING;
	private String RIGHT_MIDDLE;
	private String LEFT_INDEX;
	private String LEFT_LITTLE;
	private String LEFT_RING;
	private String LEFT_MIDDLE;
	private String LEFT_THUMB;
	private String RIGHT_THUMB;

	@Override
	public void run() throws RigInternalError {
		// AuthPartnerProcessor.startProcess();
		// residentTemplatePaths.put("C:\\Users\\ALOK~1.KUM\\AppData\\Local\\Temp\\residents_7104149890494368630\\322232223222.json",
		// null);

		String bioResponse = null;
		String modality = null;
		String deviceInfoFilePath = null;
		String uinPersonaFileName = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size()<2) {
			logger.error("Parameter is  missing from DSL step");
			//assertTrue(false, "Modality paramter is  missing in step: " + step.getName());
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			modality = step.getParameters().get(0);
			deviceInfoFilePath = step.getParameters().get(1);
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExeternalResourcePath()
						+ properties.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else
				throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
		}
		List<String> retriveAttributeList = (modality != null) ? Arrays.asList(modality.split("@@")) : null;
		// for (String resDataPath : residentTemplatePaths.keySet()) {
		if (step.getParameters().size() == 3) {
			uinPersonaFileName = step.getParameters().get(2);
			uinPersonaFileName = TestRunner.getExeternalResourcePath()
					+ properties.getProperty("ivv.path.deviceinfo.folder") + uinPersonaFileName + ".properties";
			uinResidentDataPathFinalProps = AdminTestUtil.getproperty(uinPersonaFileName);
		}else 
			uinResidentDataPathFinalProps=uinPersonaProp;
		
		for (String uinKey : uinResidentDataPathFinalProps.stringPropertyNames()) {
			String personFilePathvalue = uinResidentDataPathFinalProps.getProperty(uinKey);

			// String
			// resDataPath="C:\\Users\\ALOK~1.KUM\\AppData\\Local\\Temp\\residents_5352399691359944231\\669866986698.json";
			bioResponse = retrieveBiometric(personFilePathvalue, retriveAttributeList);
			writeToFile(bioResponse);
			if (bioResponse != null && !bioResponse.isEmpty() && retriveAttributeList.size() > 0) {
				for (int i = 0; i < retriveAttributeList.size(); i++) {
					String modalityType = retriveAttributeList.get(i);
					switch (modalityType) {
					case E2EConstants.FACE:
						FACE = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.FACE);
						break;
					case E2EConstants.LEFT_EYE:
						LEFT_EYE = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.LEFT_EYE);
						break;
					case E2EConstants.RIGHT_EYE:
						RIGHT_EYE = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.RIGHT_EYE);
						break;
					case E2EConstants.RIGHT_INDEX:
						RIGHT_INDEX = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.RIGHT_INDEX);
						break;
					case E2EConstants.RIGHT_LITTLE:
						RIGHT_LITTLE = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.RIGHT_LITTLE);
						break;
					case E2EConstants.RIGHT_RING:
						RIGHT_RING = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.RIGHT_RING);
						break;
					case E2EConstants.RIGHT_MIDDLE:
						RIGHT_MIDDLE = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.RIGHT_MIDDLE);
						break;
					case E2EConstants.LEFT_INDEX:
						LEFT_INDEX = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.LEFT_INDEX);
						break;
					case E2EConstants.LEFT_LITTLE:
						LEFT_LITTLE = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.LEFT_LITTLE);
						break;
					case E2EConstants.LEFT_RING:
						LEFT_RING = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.LEFT_RING);
						break;
					case E2EConstants.LEFT_MIDDLE:
						LEFT_MIDDLE = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.LEFT_MIDDLE);
						break;
					case E2EConstants.LEFT_THUMB:
						LEFT_THUMB = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.LEFT_THUMB);
						break;
					case E2EConstants.RIGHT_THUMB:
						RIGHT_THUMB = JsonPrecondtion.getValueFromJson(bioResponse, E2EConstants.RIGHT_THUMB);
						break;
					default:
						logger.warn("Biometric type: " + modalityType + " not found.");
						// }
					}
				}
			}
			String fileName = BIOMETRIC_FACE;
			BioAuth bioAuth = new BioAuth();
			bioAuth.isInternal = false;
			Object[] casesList = bioAuth.getYmlTestData(fileName);
			Object[] testCaseList = filterBioTestCases(casesList, retriveAttributeList);
			logger.info("No. of TestCases in Yml file : " + testCaseList.length);
			for (Object object : testCaseList) {
				TestCaseDTO test = (TestCaseDTO) object;
				test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", deviceProp.getProperty("partnerKey")));
				String input = test.getInput();
				// for (String uin : uinReqIds.keySet()) {
				 input=input.replace("$UIN$", "8529632541");
				//input = input.replace("$UIN$", uinKey);
			
				// input=input.replace("$UIN$", "605932539189");
				////input = input.replace("$IdType$", "UIN");
				// input=input.replace("$bioSubType$", deviceProp.getProperty("bioSubType"));
				// input=input.replace("$bioType$", deviceProp.getProperty("bioType"));
				////input = input.replace("$deviceCode$", deviceProp.getProperty("deviceCode"));
				////input = input.replace("$deviceServiceVersion$", deviceProp.getProperty("deviceServiceVersion"));
				////input = input.replace("$deviceProvider$", deviceProp.getProperty("deviceProvider"));
				////input = input.replace("$deviceProviderID$", deviceProp.getProperty("deviceProviderID"));
				// input=input.replace("$deviceProviderId$",
				// deviceProp.getProperty("deviceProviderId"));
				// input=input.replace("$deviceSubType$",
				// deviceProp.getProperty("deviceSubType"));
				////input = input.replace("$make$", deviceProp.getProperty("make"));
				////input = input.replace("$model$", deviceProp.getProperty("model"));
				////input = input.replace("$serialNo$", deviceProp.getProperty("serialNo"));
				// input=input.replace("$type$", deviceProp.getProperty("type"));
				test.setInput(input);
				//setBioData(test, retriveAttributeList);
				Reporter.log("<b><u>" + test.getTestCaseName() + "</u></b>");
				// }
				try {
					bioAuth.test(test);
				} catch (AuthenticationTestException | AdminTestException e) {
					logger.error(retriveAttributeList + " authentication failed: " + e.getMessage());
					throw new RigInternalError(e.getMessage());
				}finally {
					// AuthPartnerProcessor.authPartherProcessor.destroyForcibly();
				}
			}
		}

		
	}

	private String retrieveBiometric(String resFilePath, List<String> retriveAttributeList) throws RigInternalError {
		String url = baseUrl + props.getProperty("getPersonaData");
		JSONObject jsonReqInner = new JSONObject();
		if (retriveAttributeList != null && !(retriveAttributeList.isEmpty()))
			jsonReqInner.put("retriveAttributeList", retriveAttributeList);
		jsonReqInner.put("personaFilePath", resFilePath);
		JSONArray jsonReq = new JSONArray();
		jsonReq.put(0, jsonReqInner);
		Response response = getReqest(url, jsonReq.toString(), "Retrive BiometricData");
		if (response.getBody().asString().equals(""))
			throw new RigInternalError(
					"Unable to retrive BiometricData " + retriveAttributeList + " from packet utility");
		return response.getBody().asString();

	}

	private Response getReqest(String url, String body, String opsToLog) {
		Response apiResponse = getRequestWithbody(url, body, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
		return apiResponse;
	}

	private static Response getRequestWithbody(String url, String body, String contentHeader, String acceptHeader) {
		logger.info("RESSURED: Sending a GET request to " + url);
		Response getResponse = given().relaxedHTTPSValidation().accept("*/*").contentType("application/json").log()
				.all().when().body(body).get(url).then().extract().response();
		logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}

	private void setBioData(TestCaseDTO test, List<String> retriveAttributeList) {
		String bioValue = "$bioValue$";
		String bioCase = null;
		String testCaseName = test.getTestCaseName().toLowerCase();
		for (String bioType : retriveAttributeList) {
			if (testCaseName.contains(bioType.toLowerCase()))
				bioCase = bioType;
			break;
		}
		switch (bioCase) {
		case E2EConstants.FACE:
			test.setInput(test.getInput().replace(bioValue, FACE));
			break;
		case E2EConstants.LEFT_EYE:
			test.setInput(test.getInput().replace(bioValue, LEFT_EYE));
			break;
		case E2EConstants.RIGHT_EYE:
			test.setInput(test.getInput().replace(bioValue, RIGHT_EYE));
			break;
		case E2EConstants.RIGHT_INDEX:
			test.setInput(test.getInput().replace(bioValue, RIGHT_INDEX));
			break;
		case E2EConstants.RIGHT_LITTLE:
			test.setInput(test.getInput().replace(bioValue, RIGHT_LITTLE));
			break;
		case E2EConstants.RIGHT_RING:
			test.setInput(test.getInput().replace(bioValue, RIGHT_RING));
			break;
		case E2EConstants.RIGHT_MIDDLE:
			test.setInput(test.getInput().replace(bioValue, RIGHT_MIDDLE));
			break;
		case E2EConstants.LEFT_INDEX:
			test.setInput(test.getInput().replace(bioValue, LEFT_INDEX));
			break;
		case E2EConstants.LEFT_LITTLE:
			test.setInput(test.getInput().replace(bioValue, LEFT_LITTLE));
			break;
		case E2EConstants.LEFT_RING:
			test.setInput(test.getInput().replace(bioValue, LEFT_RING));
			break;
		case E2EConstants.LEFT_MIDDLE:
			test.setInput(test.getInput().replace(bioValue, LEFT_MIDDLE));
			break;
		case E2EConstants.LEFT_THUMB:
			test.setInput(test.getInput().replace(bioValue, LEFT_THUMB));
			break;
		case E2EConstants.RIGHT_THUMB:
			test.setInput(test.getInput().replace(bioValue, RIGHT_THUMB));
			break;
		}
	}
	
	
	
	 public static void writeToFile(String data) {
	        String text = data;
	        BufferedWriter output = null;
	        try {
	            File file = new File("biometricData.txt");
	            output = new BufferedWriter(new FileWriter(file));
	            output.write(text);
	        } catch ( IOException e ) {
	            e.printStackTrace();
	        } finally {
	          if ( output != null ) {
	            try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	          }
	        }
	    }

}
