package io.mosip.testrig.dslrig.ivv.orchestrator;

import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.testng.Reporter;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.esignet.testscripts.EsignetBioAuth;
import io.mosip.testrig.apirig.esignet.utils.EsignetUtil;
import io.mosip.testrig.apirig.masterdata.utils.MasterDataUtil;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.auth.testscripts.BioAuth;
import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
//import io.mosip.testrig.apirig.testscripts.BioAuthOld;
import io.restassured.response.Response;

public class PacketUtility extends BaseTestCaseUtil {
	static Logger logger = Logger.getLogger(PacketUtility.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	// String constants
	private static final String SKIPGAURDIAN = "SkipGaurdian";
	private static final String GENDER = "Gender";
	private static final String FINGER = "Finger";
	private static final String REQUESTS = "requests";
	private static final String SUCCESS = "success";
	private static final String RESPONSE = "response";
	private static final String PERSONAFILEPATH = "personaFilePath";
	private static final String UPDATERESIDENTURL = "updateResidentUrl";
	private static final String PR_RESIDENTLIST = "PR_ResidentList";
	private static final String GUARDIAN = "guardian";
	private static final String CHILD = "child";
	private static final String URLBASE = "urlBase";
	private static final String MOSIP_TEST_BASEURL = "mosip.test.baseurl";
	private static final String MOSIP_TEST_REGCLIENT_MACHINEID = "mosip.test.regclient.machineid";
	private static final String MOSIP_TEST_REGCLIENT_CENTERID = "mosip.test.regclient.centerid";
	private static final String REGCLIENT_CENTERID = "regclient.centerid";
	private static final String MOSIP_TEST_REGCLIENT_USERID = "mosip.test.regclient.userid";
	private static final String PREREG_OPERATORID = "prereg.operatorId";
	private static final String MOSIP_TEST_REGCLIENT_PASSWORD = "mosip.test.regclient.password";
	private static final String PREREG_PASSWORD = "prereg.password";
	private static final String MOSIP_TEST_REGCLIENT_SUPERVISORID = "mosip.test.regclient.supervisorid";
	private static final String PREREG_PRECONFIGUREDOTP = "prereg.preconfiguredOtp";
	private static final String SETCONTEXT = "SetContext";
	private static final String SCENARIO = "scenario";
	private static final String MACHINEID = "machineid";
	private static final String CENTERID = "centerId";
	private static final String USERID = "userid";
	private static final String PASSWORD = "password";
	private static final String MOSIP_TEST_REGCLIENT_supervisorP = "mosip.test.regclient.supervisorpwd";
	private static final String USERPASSWORD = "userpassword";
	private static final String VALID = "valid";
	private static final String INVALID = "invalid";
	private static final String IPADDRESS = "ipAddress";
	private static final String ISACTIVE = "isActive";
	private static final String LANGCODE = "langCode";
	private static final String MACADDRESS = "macAddress";
	private static final String MACHINESPECID = "machineSpecId";
	private static final String REGCENTERID = "regCenterId";
	private static final String ZONECODE = "zoneCode";
	private static final String INDIVIDUALID = "individualId";
	private static final String BIOSUBTYPE = "bioSubType";
	private static final String BIOTYPE = "bioType";
	private static final String DEVICECODE = "deviceCode";
	private static final String DEVICEPROVIDERID = "deviceProviderID";
	private static final String DEVICESERVICEID = "deviceServiceID";
	private static final String DEVICESERVICEVERSION = "deviceServiceVersion";
	private static final String DEVICEPROVIDER = "deviceProvider";
	private static final String DEVICESUBTYPE = "deviceSubType";
	private static final String MODEL = "model";
	private static final String SERIALNO = "serialNo";
	private static final String ENV_ENDPOINT = "env.endpoint";
	private static final String ERRORCODE = "errorcode";
	private static final String AUTHORIZATION = "Authorization";
	private static final String STRING = "string";
	private static final String METADATA = "metadata";
	private static final String REQUEST = "request";
	private static final String REQUESTTIME = "requesttime";
	private static final String VERSION = "version";
	public static String appointmentDate = "";

	public List<String> generateResidents(int n, Boolean bAdult, Boolean bSkipGuardian, String gender,
			String missFields, HashMap<String, String> contextKey, Scenario.Step step) throws RigInternalError {

		String url = baseUrl + props.getProperty("getResidentUrl") + n;
		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		if (bAdult) {
			residentAttrib.put("Age", "RA_Adult");
		} else {
			residentAttrib.put("Age", "RA_Minor");
			residentAttrib.put(SKIPGAURDIAN, bSkipGuardian);
		}
		residentAttrib.put(GENDER, gender);
		residentAttrib.put("Iris", true);
		// added for face biometric related issue
		residentAttrib.put(FINGER, true);
		residentAttrib.put("Face", true);

		if (missFields != null)
			residentAttrib.put("Miss", missFields);
		jsonReq.put("PR_ResidentAttribute", residentAttrib);
		jsonwrapper.put(REQUESTS, jsonReq);

		Response response = postRequest(url, jsonwrapper.toString(), "GENERATE_RESIDENTS_DATA", step);

		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to get residentData from packet utility");
		}
		JSONArray resp = new JSONObject(response.getBody().asString()).getJSONArray(RESPONSE);
		List<String> residentPaths = new ArrayList<>();
		for (int i = 0; i < resp.length(); i++) {
			JSONObject obj = resp.getJSONObject(i);
			String resFilePath = obj.get("path").toString();
			residentPaths.add(resFilePath);
		}
		return residentPaths;

	}

	public Response generateResident(String ageCategory, Boolean bSkipGuardian, String missFields,
			HashMap<String, String> genderAndBioFlag, Scenario.Step step) throws RigInternalError {

		String url = baseUrl + props.getProperty("getResidentUrl");
		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		if (ageCategory.equalsIgnoreCase("adult")) {
			residentAttrib.put("Age", "RA_Adult");
		}
		if (ageCategory.equalsIgnoreCase("senior")) {
			residentAttrib.put("Age", "RA_Senior");
		} else if (ageCategory.equalsIgnoreCase("minor")) {
			residentAttrib.put("Age", "RA_Minor");
			residentAttrib.put(SKIPGAURDIAN, bSkipGuardian);
		} else if (ageCategory.equalsIgnoreCase("infant")) {
			residentAttrib.put("Age", "RA_Infant");
			residentAttrib.put(SKIPGAURDIAN, bSkipGuardian);
		}

		residentAttrib.put(GENDER, genderAndBioFlag.get(GENDER));
		residentAttrib.put("Iris", genderAndBioFlag.get("Iris"));
		residentAttrib.put(FINGER, genderAndBioFlag.get(FINGER));
		residentAttrib.put("Face", genderAndBioFlag.get("Face"));

		if (missFields != null)
			residentAttrib.put("Miss", missFields);
		jsonReq.put("PR_ResidentAttribute", residentAttrib);
		jsonwrapper.put(REQUESTS, jsonReq);

		Response response = postRequest(url, jsonwrapper.toString(), "GENERATE_RESIDENTS_DATA", step);

		return response;

	}

	public JSONArray getTemplate(Set<String> resPath, String process, HashMap<String, String> contextKey,
			Scenario.Step step, String qualityScore, boolean genarateValidCbeff) throws RigInternalError {
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		for (String residentPath : resPath) {
			arr.put(residentPath);
		}
		jsonReq.put(PERSONAFILEPATH, arr);
		String url = baseUrl + props.getProperty("getTemplateUrl") + process + "/" + qualityScore + "/"
				+ genarateValidCbeff;

		int count = 0;
		int maxRetryCount = Integer.parseInt(props.getProperty("loopCount"));;
		Response templateResponse = null;
		JSONArray resp = null;
		do {
			count++;
			templateResponse = postRequest(url, jsonReq.toString(), "GET-TEMPLATE", step);
			if (!templateResponse.getBody().asString().toLowerCase().contains("packets")) {
				if (count == maxRetryCount) {
					this.hasError = true;
					throw new RigInternalError(templateResponse.getBody().asString());
				} 
				else {
					logger.info("Unable to get biometrics via mds.Retrying...");
					continue;
				}		
			}
			JSONObject jsonResponse = new JSONObject(templateResponse.asString());
			resp = jsonResponse.getJSONArray("packets");
			if ((resp.length() <= 0)) {
				if (count == maxRetryCount) {
					this.hasError = true;
					throw new RigInternalError("Unable to get Template from packet utility");
				} 
					logger.info("Unable to get Template from packet utility.Retrying...");				
			} else {
				break;
			}
		} while (count < maxRetryCount);
		return resp;
	}

	public void requestOtp(String resFilePath, HashMap<String, String> map, String emailOrPhone, Scenario.Step step)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("sendOtpUrl") + emailOrPhone;
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put(PERSONAFILEPATH, jsonArray);
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), map, "Send Otp", step);
		if (!response.getBody().asString().toLowerCase().contains("email request submitted")) {
			this.hasError = true;
			throw new RigInternalError("Unable to Send OTP");
		}

	}

	public void verifyOtp(String resFilePath, HashMap<String, String> contextKey, String emailOrPhone,
			Scenario.Step step, String otp) throws RigInternalError {
		String url = baseUrl + props.getProperty("verifyOtpUrl") + emailOrPhone + "/" + otp;
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put(PERSONAFILEPATH, jsonArray);
		Response response = postRequest(url, jsonReq.toString(), "Verify Otp", step);
		if (!response.getBody().asString().toLowerCase().contains("validation_successful")) {
			this.hasError = true;

			throw new RigInternalError("Unable to Verify Otp from packet utility");
		}
	}

	public String preReg(String resFilePath, HashMap<String, String> contextKey, Scenario.Step step)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("preregisterUrl");
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put(PERSONAFILEPATH, jsonArray);
		Response response = postRequest(url, jsonReq.toString(), "AddApplication", step);
		String prid = response.getBody().asString();
		if (!((int) prid.charAt(0) > 47 && (int) prid.charAt(0) < 58)) {
			this.hasError = true;
			throw new RigInternalError("Unable to pre-register using packet utility");
		}
		return prid;

	}

	public void uploadDocuments(String resFilePath, String prid, HashMap<String, String> map, Scenario.Step step) {
		String url = baseUrl + "/prereg/documents/" + prid;
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put(PERSONAFILEPATH, jsonArray);
		postRequestWithQueryParamAndBody(url, jsonReq.toString(), map, "Upload Documents", step);
	}

	public String updatePreRegStatus(String prid, String status, HashMap<String, String> map, Scenario.Step step)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("updatePreRegStatus") + prid + "?statusCode=" + status;
		Response response = putRequestWithQueryParam(url, map, "UpdatePreRegStatus", step);
		GlobalMethods.ReportRequestAndResponse("", "", url, "", response.toString());
		return (response.getBody().asString());

	}

	public void preRegStatusInValidResponse(String response) throws RigInternalError {
		if (!response.isEmpty()) {
			this.hasError = true;
			throw new RigInternalError("Expectations :  Empty response");
		} else {
			// Reporter.log(response);

			GlobalMethods.ReportRequestAndResponse("", "", "", "", response.toString());
			logger.info(response);
		}
	}

	public void preRegStatusValidResponse(String response) throws RigInternalError {
		if (!response.toLowerCase().contains("status_updated_sucessfully")) {
			this.hasError = true;
			Reporter.log("STATUS_NOT_UPDATED_SUCESSFULLY");
			throw new RigInternalError("Unable to updatePreRegStatus from packet utility");
		} else {
			// Reporter.log(response);
			GlobalMethods.ReportRequestAndResponse("", "", "", "", response.toString());
			logger.info(response);
		}
	}

	public String bookAppointment(String prid, int nthSlot, HashMap<String, String> contextKey, boolean bookOnHolidays,
			Scenario.Step step) throws RigInternalError {
		String url = baseUrl + "/prereg/appointment/" + prid + "/" + nthSlot + "/" + bookOnHolidays;
		JSONObject jsonReq = new JSONObject();
		Response response = postRequest(url, jsonReq.toString(), "BookAppointment", step);
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonReq.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains("appointment booked successfully")) {
			this.hasError = true;
			logger.info("bookAppointment Response is:" + response + " url: " + url);
			throw new RigInternalError("Unable to BookAppointment from packet utility");
		}

		String json = response.getBody().asString();

		JSONObject jsonObject = new JSONObject(json);
		appointmentDate = jsonObject.getString("appointmentDate");
		step.getScenario().getVidPersonaProp().put("appointmentDate", appointmentDate);

		// JSONObject responseObject = response.getJSONObject("response");
		// JSONArray data = responseObject.getJSONArray("locations");

		// appointmentDate =response.getBody().
		return response.getBody().asString();
	}

	public String generateAndUploadPacket(String prid, String packetPath, HashMap<String, String> map,
			String responseStatus, Scenario.Step step) throws RigInternalError {
		String rid = null;
		String url = baseUrl + "/packet/sync/" + prid + "/" + true + "/" + true;
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(packetPath);
		jsonReq.put(PERSONAFILEPATH, arr);
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), map, "Generate And UploadPacket",
				step);

		if (!(response.getBody().asString().toLowerCase().contains("failed"))) {
			JSONObject jsonResp = new JSONObject(response.getBody().asString());
			rid = jsonResp.getJSONObject(RESPONSE).getString("registrationId");
			GlobalMethods.ReportRequestAndResponse("", "", url, jsonReq.toString(), response.getBody().asString());
		}
		if (!response.getBody().asString().toLowerCase().contains(responseStatus)) {
			this.hasError = true;
			throw new RigInternalError("Unable to Generate And UploadPacket from packet utility");
		}
		return rid;
	}

	public String updateResidentRid(String personaFilePath, String rid, Scenario.Step step) throws RigInternalError {
		String url = baseUrl + props.getProperty(UPDATERESIDENTURL);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("RID", rid);

		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();

		residentAttrib.put("rid", personaFilePath);

		jsonReq.put(PR_RESIDENTLIST, residentAttrib);

		jsonwrapper.put(REQUESTS, jsonReq);

		Response response = postRequestWithQueryParamAndBody(url, jsonwrapper.toString(), map,
				"link Resident data with RID", step);
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonwrapper.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to add Resident RID in resident data");
		}
		String ret = response.getBody().asString();
		return ret;

	}

	public String updateResidentUIN(String personaFilePath, String uin, Scenario.Step step) throws RigInternalError {
		String url = baseUrl + props.getProperty(UPDATERESIDENTURL);

		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();

		residentAttrib.put("uin", personaFilePath);

		jsonReq.put(PR_RESIDENTLIST, residentAttrib);

		jsonwrapper.put(REQUESTS, jsonReq);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("UIN", uin);
		Response response = postRequestWithQueryParamAndBody(url, jsonwrapper.toString(), map,
				"link Resident data with UIN", step);
//		GlobalMethods.ReportRequestAndResponse("", "", url, jsonwrapper.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to add UIN in resident data");
		}
		String ret = response.getBody().asString();
		return ret;

	}

	public String updateResidentGuardian_old(String residentFilePath, String withRidOrUin, String missingFields,
			String parentEmailOrPhone, Scenario.Step step, String qualityScore, boolean genarateValidCbeff)
			throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		List<String> generatedResidentData = generateResidents(1, true, true, "Any", missingFields,
				step.getScenario().getCurrentStep(), step);
		JSONArray jsonArray = getTemplate(new HashSet<String>(generatedResidentData), "NEW",
				step.getScenario().getCurrentStep(), step, qualityScore, genarateValidCbeff);
		JSONObject obj = jsonArray.getJSONObject(0);
		String templatePath = obj.get("path").toString();
		requestOtp(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getCurrentStep(),
				parentEmailOrPhone, step);
		verifyOtp(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getCurrentStep(),
				parentEmailOrPhone, step, "111111");
		String prid = preReg(step.getScenario().getGeneratedResidentData().get(0), step.getScenario().getCurrentStep(),
				step);
		uploadDocuments(step.getScenario().getGeneratedResidentData().get(0), prid, step.getScenario().getCurrentStep(),
				step);
		bookAppointment(prid, 1, step.getScenario().getCurrentStep(), false, step);
		String rid = generateAndUploadPacket(prid, templatePath, step.getScenario().getCurrentStep(), SUCCESS, step);

		String url = baseUrl + props.getProperty(UPDATERESIDENTURL);

		if (withRidOrUin.equalsIgnoreCase("rid"))
			updateResidentRid(step.getScenario().getGeneratedResidentData().get(0), rid, step);
		else if (withRidOrUin.equalsIgnoreCase("uin")) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
			String identityUrl = baseUrl + props.getProperty("getIdentityUrl");
			Response response = getRequest(identityUrl + rid, "Get uin by rid :" + rid, step);
			GlobalMethods.ReportRequestAndResponse("", "", identityUrl + rid, "", response.getBody().asString());
			String uin = response.asString();
			updateResidentUIN(step.getScenario().getGeneratedResidentData().get(0), uin, step);
		}

		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put(GUARDIAN, step.getScenario().getGeneratedResidentData().get(0));
		residentAttrib.put(CHILD, residentFilePath);
		jsonReq.put(PR_RESIDENTLIST, residentAttrib);
		jsonwrapper.put(REQUESTS, jsonReq);
		Response response = postRequest(url, jsonwrapper.toString(), "Update Resident Guardian", step);
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonwrapper.toString(), response.getBody().asString());
		Reporter.log("<b><u>Generated GuardianPacket with Rid: " + rid + " And linked to child </u></b>");
		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");
		}
		return rid;

	}

	public String updateResidentGuardian(String residentFilePath, Scenario.Step step) throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put(GUARDIAN, step.getScenario().getGeneratedResidentData().get(0));
		residentAttrib.put(CHILD, residentFilePath);
		jsonReq.put(PR_RESIDENTLIST, residentAttrib);
		jsonwrapper.put(REQUESTS, jsonReq);
		String url = baseUrl + props.getProperty(UPDATERESIDENTURL);
		Response response = postRequest(url, jsonwrapper.toString(), "Update Resident Guardian", step);
		Reporter.log("<b><u>Generated GuardianPacket with Rid: " + step.getScenario().getRid_updateResident()
				+ " And linked to child </u></b>");
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonwrapper.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");
		}
		return step.getScenario().getRid_updateResident();

	}

	public String updateResidentWithGuardianSkippingPreReg_old(String residentFilePath,
			HashMap<String, String> contextKey, String withRidOrUin, String missingFields, Scenario.Step step,
			boolean getRidFromSync, String qualityScore, boolean genarateValidCbeff, String invalidMachineFlag) throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		List<String> generatedResidentData = generateResidents(1, true, true, "Any", missingFields, contextKey, step);
		JSONArray jsonArray = getTemplate(new HashSet<String>(generatedResidentData), "NEW", contextKey, step,
				qualityScore, genarateValidCbeff);
		JSONObject obj = jsonArray.getJSONObject(0);
		String templatePath = obj.get("path").toString();
		String rid = generateAndUploadPacketSkippingPrereg(templatePath,
				step.getScenario().getGeneratedResidentData().get(0), null, contextKey, SUCCESS, step, getRidFromSync,invalidMachineFlag);

		String url = baseUrl + props.getProperty(UPDATERESIDENTURL);

		if (withRidOrUin.equalsIgnoreCase("rid"))
			updateResidentRid(step.getScenario().getGeneratedResidentData().get(0), rid, step);
		else if (withRidOrUin.equalsIgnoreCase("uin")) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
			String identityUrl = baseUrl + props.getProperty("getIdentityUrl");
			Response response = getRequest(identityUrl + rid, "Get uin by rid :" + rid, step);
			String uin = response.asString();
			updateResidentUIN(step.getScenario().getGeneratedResidentData().get(0), uin, step);
		}

		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put(GUARDIAN, step.getScenario().getGeneratedResidentData().get(0));
		residentAttrib.put(CHILD, residentFilePath);
		jsonReq.put(PR_RESIDENTLIST, residentAttrib);
		jsonwrapper.put(REQUESTS, jsonReq);
		Response response = postRequest(url, jsonwrapper.toString(), "Update Resident Guardian", step);
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonwrapper.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");

		}
		Reporter.log("<b><u>Generated GuardianPacket with Rid: " + rid + " And linked to child </u></b>");
		return rid;

	}

	public String updateResidentWithGuardianSkippingPreReg(String guardianPersonaFilePath, String childPersonaFilePath,
			HashMap<String, String> contextKey, Scenario.Step step) throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put(GUARDIAN, guardianPersonaFilePath);
		residentAttrib.put(CHILD, (childPersonaFilePath != null) ? childPersonaFilePath
				: step.getScenario().getGeneratedResidentData().get(0));
		jsonReq.put(PR_RESIDENTLIST, residentAttrib);
		jsonwrapper.put(REQUESTS, jsonReq);
		String url = baseUrl + props.getProperty(UPDATERESIDENTURL);
		Response response = postRequest(url, jsonwrapper.toString(), "Update Resident Guardian", step);
//		GlobalMethods.ReportRequestAndResponse("", "", url, jsonwrapper.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");
		}
		Reporter.log("<b><u>Generated GuardianPacket And linked to child </u></b>");
		return step.getScenario().getRid_updateResident();

	}

	public String generateAndUploadPacketWrongHash(String packetPath, String residentPath, String additionalInfoReqId,
			HashMap<String, String> contextKey, String responseStatus, Scenario.Step step, boolean getRidFromSync, String invalidMachineFlag)
			throws RigInternalError {

		String url = baseUrl + "/packet/sync/01/" + true; // 01 -- to generate wrong hash
		return getRID(url, packetPath, residentPath, additionalInfoReqId, contextKey, responseStatus, step,
				getRidFromSync, true, invalidMachineFlag);
	}

	public String generateAndUploadPacketSkippingPrereg(String packetPath, String residentPath,
			String additionalInfoReqId, HashMap<String, String> contextKey, String responseStatus, Scenario.Step step,
			boolean getRidFromSync, String invalidMachineFlag) throws RigInternalError {

		String url = baseUrl + "/packet/sync/0/" + getRidFromSync; // 0 -- to skip prereg
		return getRID(url, packetPath, residentPath, additionalInfoReqId, contextKey, responseStatus, step,
				getRidFromSync, true,invalidMachineFlag);

	}

	public String generateAndUploadWithInvalidCbeffPacketSkippingPrereg(String packetPath, String residentPath,
			String additionalInfoReqId, HashMap<String, String> contextKey, String responseStatus, Scenario.Step step,
			boolean getRidFromSync, String invalidMachineFlag) throws RigInternalError {

		String url = baseUrl + "/packet/sync/0/" + getRidFromSync; // 0 -- to skip prereg
		return getRID(url, packetPath, residentPath, additionalInfoReqId, contextKey, responseStatus, step,
				getRidFromSync, false,invalidMachineFlag);

	}

	public String getRID(String url, String packetPath, String residentPath, String additionalInfoReqId,
			HashMap<String, String> contextKey, String responseStatus, Scenario.Step step, boolean getRidFromSync,
			boolean genarateValidCbeff,String invalidMachineFlag) throws RigInternalError {
		String rid = null;
		if (genarateValidCbeff)
			url += "/1"; // 1 --- to generateValid Cbeff
		else
			url += "/0"; // 0 --- to generateInValid Cbeff
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(0, packetPath);
		arr.put(1, residentPath);
		jsonReq.put(PERSONAFILEPATH, arr);
		jsonReq.put("additionalInfoReqId", additionalInfoReqId);

		int count = 0;
		int maxRetryCount = Integer.parseInt(props.getProperty("loopCount"));

		do {
	  		count++;
			Response response = postRequest(url, jsonReq.toString(), "Generate And UploadPacket", step);
			
			if (invalidMachineFlag.contentEquals("invalidMachine") && response.getBody().asString().toLowerCase().contains("failed to sign data")) {
				return "";
			}
			if (!response.getBody().asString().toLowerCase().contains(responseStatus)
					|| response.getBody().asString().toLowerCase().contains("failed")) {
				if (count == maxRetryCount) {
					this.hasError = true;
					throw new RigInternalError("Unable to Generate And UploadPacket from packet utility");
				} else {
					logger.info("Unable to generate and upload packet . Retrying...");
				    continue;
				}
			}

			else {
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
				rid = jsonResp.getJSONObject(RESPONSE).getString("registrationId");
				break;
			}

		} while (count < maxRetryCount);

		return rid;
	}

	public String createContext(String key, String baseUrl, Scenario.Step step) throws RigInternalError {
		String url = this.baseUrl + "/servercontext/" + key;

		JSONObject jsonReq = new JSONObject();
		jsonReq.put(URLBASE, baseUrl);
		jsonReq.put(MOSIP_TEST_BASEURL, baseUrl);
		jsonReq.put(MOSIP_TEST_REGCLIENT_MACHINEID, E2EConstants.MACHINE_ID);
		jsonReq.put(MOSIP_TEST_REGCLIENT_CENTERID, E2EConstants.CENTER_ID);
		jsonReq.put(REGCLIENT_CENTERID, E2EConstants.CENTER_ID);
		jsonReq.put(MOSIP_TEST_REGCLIENT_USERID, E2EConstants.USER_ID);
		jsonReq.put(PREREG_OPERATORID, E2EConstants.USER_ID);
		jsonReq.put(MOSIP_TEST_REGCLIENT_PASSWORD, E2EConstants.USER_PASSWD);
		jsonReq.put(PREREG_PASSWORD, E2EConstants.USER_PASSWD);
		jsonReq.put(MOSIP_TEST_REGCLIENT_SUPERVISORID, E2EConstants.SUPERVISOR_ID);
		jsonReq.put(PREREG_PRECONFIGUREDOTP, E2EConstants.PRECONFIGURED_OTP);
		Response response = postRequest(url, jsonReq.toString(), SETCONTEXT, step);
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonReq.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains("true")) {
			this.hasError = true;
			throw new RigInternalError("Unable to set context from packet utility");
		}
		return response.getBody().asString();

	}

	public String createContexts(String key, String userAndMachineDetailParam, 
			Boolean generatePrivateKey, String status, String envbaseUrl, Scenario.Step step) throws RigInternalError {
		String url = this.baseUrl + "/context/server/";
		Map<String, String> map = new HashMap<String, String>();
		if (userAndMachineDetailParam != null && !userAndMachineDetailParam.isEmpty()) {
			String[] details = userAndMachineDetailParam.split("@@");
			for (String detail : details) {
				String detailData[] = detail.split("=");
				String keys = detailData[0].trim();
				String value = detailData[1].trim();
				map.put(keys, value);
			}

		}
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("enableDebug", dslConfigManager.getEnableDebug());
		logger.info("Running suite with enableDebug : " + dslConfigManager.getEnableDebug());
		jsonReq.put("baselang", BaseTestCase.getLanguageList().get(0));

		if (status != null) {
			String[] parts = status.split("@@");
			status = parts[0];
			if (parts.length == 2 && parts[0].equals("null")) {
				int a = 0;
				a = Integer.parseInt(parts[1]);
				jsonReq.put(SCENARIO, a + ":" + step.getScenario().getDescription());
			} else {
				jsonReq.put(SCENARIO, step.getScenario().getId() + ":" + step.getScenario().getDescription());
			}
		} else {
			jsonReq.put(SCENARIO, step.getScenario().getId() + ":" + step.getScenario().getDescription());
		}

		jsonReq.put(URLBASE, envbaseUrl);
		jsonReq.put(MOSIP_TEST_BASEURL, envbaseUrl);
		jsonReq.put(MOSIP_TEST_REGCLIENT_MACHINEID,
				(map.get(MACHINEID) != null) ? map.get(MACHINEID) : E2EConstants.MACHINE_ID);
		jsonReq.put(MOSIP_TEST_REGCLIENT_CENTERID,
				(map.get(CENTERID) != null) ? map.get(CENTERID) : E2EConstants.CENTER_ID);
		jsonReq.put(REGCLIENT_CENTERID, (map.get(CENTERID) != null) ? map.get(CENTERID) : E2EConstants.CENTER_ID);
		jsonReq.put(MOSIP_TEST_REGCLIENT_USERID, (map.get(USERID) != null) ? map.get(USERID) : E2EConstants.USER_ID);
		jsonReq.put(PREREG_OPERATORID, (map.get(USERID) != null) ? map.get(USERID) : E2EConstants.USER_ID);
		jsonReq.put(MOSIP_TEST_REGCLIENT_PASSWORD,
				(map.get(PASSWORD) != null) ? map.get(PASSWORD) : E2EConstants.USER_PASSWD);
		jsonReq.put(PREREG_PASSWORD, (map.get(PASSWORD) != null) ? map.get(PASSWORD) : E2EConstants.USER_PASSWD);
		jsonReq.put(MOSIP_TEST_REGCLIENT_SUPERVISORID,
				(map.get("supervisorid") != null) ? map.get("supervisorid") : E2EConstants.SUPERVISOR_ID);
		jsonReq.put(PREREG_PRECONFIGUREDOTP, E2EConstants.PRECONFIGURED_OTP);
		jsonReq.put("Male", "MLE");
		jsonReq.put("Female", "FLE");
		jsonReq.put("Other", "OTH");
		jsonReq.put("generatePrivateKey", generatePrivateKey);
		jsonReq.put(MOSIP_TEST_REGCLIENT_supervisorP,
				(map.get(USERPASSWORD) != null) ? map.get(USERPASSWORD) : E2EConstants.USER_PASSWD);
		if (status != null && !status.isBlank()) {
			jsonReq.put("machineStatus", status);
		}
		/*
		 * if (mosipVersion != null && !mosipVersion.isEmpty()) {
		 * jsonReq.put("mosip.version", mosipVersion); }
		 */
		Response response = postRequest(url, jsonReq.toString(), SETCONTEXT, step);
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonReq.toString(), response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains("true")) {
			this.hasError = true;
			throw new RigInternalError(response.getBody().asString());
		}
		return response.getBody().asString();

	}

	public String createContexts(String negative, String key, HashMap<String, String> map, 

			Boolean generatePrivateKey, String status, String envbaseUrl, Scenario.Step step, boolean invalidCertFlag,
			String consent, boolean changeSupervisorNameToDiffCase, String invalidEncryptedHashFlag,
			String invalidCheckSum , String invalidIdSchemaFlag ,String skipBiometricClassification,String skipApplicantDocuments, String invalidDateFlag, String invalidOfficerIDFlag) throws RigInternalError {
		String url = this.baseUrl + "/context/server"; // this.baseUrl + "/context/server/" + key?contextKey=Ckey
		logger.info("packet utility base url : " + url);

		String centerId = CENTERID + map.get("appendedkey");

		JSONObject jsonReq = new JSONObject();

		if (status != null) {
			String[] parts = status.split("@@");
			status = parts[0];
			if (parts.length == 2 && parts[0].equals("null")) {
				int a = 0;
				a = Integer.parseInt(parts[1]);
				jsonReq.put(SCENARIO, a + ":" + step.getScenario().getDescription());
			} else {
				jsonReq.put(SCENARIO, step.getScenario().getId() + ":" + step.getScenario().getDescription());
			}
		} else {
			jsonReq.put(SCENARIO, step.getScenario().getId() + ":" + step.getScenario().getDescription());
		}
		
		//Add age category from actuator
		jsonReq.put("ageCategory", MasterDataUtil.getValueFromRegprocActuator("/mosip/mosip-config/registration-processor-default.properties", "mosip.regproc.packet.classifier.tagging.agegroup.ranges"));
		// id json mapping
		jsonReq.put("IDSchemaVersion", getValueFromIdJson("IDSchemaVersion"));
		jsonReq.put("uin", getValueFromIdJson("uin"));
		jsonReq.put("name", getValueFromIdJson("name"));
		jsonReq.put("dob", getValueFromIdJson("dob"));
		jsonReq.put("gender", getValueFromIdJson("gender"));
		jsonReq.put("emailId", getValueFromIdJson("emailId"));
		jsonReq.put("individualBiometrics", getValueFromIdJson("individualBiometrics"));
		jsonReq.put("introducerBiometrics", getValueFromIdJson("introducerBiometrics"));
		jsonReq.put("introducerUIN", getValueFromIdJson("introducerUIN"));
		jsonReq.put("introducerRID", getValueFromIdJson("introducerRID"));
		jsonReq.put("introducerName", getValueFromIdJson("introducerName"));
		jsonReq.put("invalidCheckSum", invalidCheckSum);
		jsonReq.put("invalidIdSchemaFlag", invalidIdSchemaFlag);
		jsonReq.put("skipBiometricClassificationFlag", skipBiometricClassification);
		jsonReq.put("skipApplicantDocumentsFlag", skipApplicantDocuments);
		jsonReq.put("invalidDateFlag", invalidDateFlag);
		jsonReq.put("invalidOfficerIDFlag", invalidOfficerIDFlag);
		jsonReq.put("invalidEncryptedHashFlag", invalidEncryptedHashFlag);
		jsonReq.put("changeSupervisorNameToDiffCase", changeSupervisorNameToDiffCase);
		jsonReq.put("consent", consent);
		jsonReq.put("invalidCertFlag", invalidCertFlag);
		jsonReq.put("enableDebug", dslConfigManager.getEnableDebug());
		logger.info("Running suite with enableDebug : " + dslConfigManager.getEnableDebug());
		jsonReq.put(URLBASE, envbaseUrl);
		jsonReq.put(MOSIP_TEST_BASEURL, envbaseUrl);
		jsonReq.put(MOSIP_TEST_REGCLIENT_MACHINEID,
				(map.get(MACHINEID) != null) ? map.get(MACHINEID) : E2EConstants.MACHINE_ID);

		jsonReq.put(MOSIP_TEST_REGCLIENT_CENTERID,
				(map.get(centerId) != null) ? map.get(centerId) : E2EConstants.CENTER_ID);

		jsonReq.put(REGCLIENT_CENTERID, (map.get(centerId) != null) ? map.get(centerId) : E2EConstants.CENTER_ID);

		jsonReq.put(PREREG_PRECONFIGUREDOTP, E2EConstants.PRECONFIGURED_OTP);
		jsonReq.put("Male", "MLE");
		jsonReq.put("Female", "FLE");
		jsonReq.put("Other", "OTH");
		jsonReq.put("generatePrivateKey", generatePrivateKey);
		
		jsonReq.put("langCode", BaseTestCase.languageCode);

		jsonReq.put("validUIN", (map.get("$$uin") != null) ? map.get("$$uin") : "createnew");

		if (status != null && !status.isBlank())
			jsonReq.put("machineStatus", status);
		/*
		 * if (mosipVersion != null && !mosipVersion.isEmpty())
		 * 
		 * jsonReq.put("mosip.version", mosipVersion);
		 */

		if (!negative.contains("@@")) // This is to null supervisor,operator details
		{
			jsonReq.put(MOSIP_TEST_REGCLIENT_SUPERVISORID,
					(map.get(USERID) != null) ? map.get(USERID) : E2EConstants.SUPERVISOR_ID);
			jsonReq.put(MOSIP_TEST_REGCLIENT_USERID,
					(map.get(USERID) != null) ? map.get(USERID) : E2EConstants.USER_ID);

			jsonReq.put(MOSIP_TEST_REGCLIENT_PASSWORD,
					(map.get(USERPASSWORD) != null) ? map.get(USERPASSWORD) : E2EConstants.USER_PASSWD);

			jsonReq.put(PREREG_OPERATORID, (map.get(USERID) != null) ? map.get(USERID) : E2EConstants.USER_ID);

			jsonReq.put(PREREG_PASSWORD,
					(map.get(USERPASSWORD) != null) ? map.get(USERPASSWORD) : E2EConstants.USER_PASSWD);
			jsonReq.put(MOSIP_TEST_REGCLIENT_supervisorP,
					(map.get(USERPASSWORD) != null) ? map.get(USERPASSWORD) : E2EConstants.USER_PASSWD);
		} else if (negative.contains("@@")) { // to verify permutation and combination for metadata operationdata
			String supervOpertoDetails[] = negative.split("@@");
			// For supervisorid
			if (supervOpertoDetails[0].equalsIgnoreCase("null")) {
			} else if (supervOpertoDetails[0].equalsIgnoreCase(VALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_SUPERVISORID,
						(map.get(USERID) != null) ? map.get(USERID) : E2EConstants.USER_ID);
			else if (supervOpertoDetails[0].equalsIgnoreCase(INVALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_SUPERVISORID, supervOpertoDetails[0]);

			// For supervisorpwd
			if (supervOpertoDetails[1].equalsIgnoreCase("null")) // Don't add to the map
			{
			} else if (supervOpertoDetails[1].equalsIgnoreCase(VALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_supervisorP,
						(map.get(USERPASSWORD) != null) ? map.get(USERPASSWORD) : E2EConstants.USER_PASSWD);
			else if (supervOpertoDetails[1].equalsIgnoreCase(INVALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_supervisorP, supervOpertoDetails[1]);

			// For operatorid
			if (supervOpertoDetails[2].equalsIgnoreCase("null")) // Don't add to the map
			{
			} else if (supervOpertoDetails[2].equalsIgnoreCase(VALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_USERID,
						(map.get(USERID) != null) ? map.get(USERID) : E2EConstants.USER_ID);
			else if (supervOpertoDetails[2].equalsIgnoreCase(INVALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_USERID, supervOpertoDetails[2]);

			// For operatorpwd
			if (supervOpertoDetails[3].equalsIgnoreCase("null")) // Don't add to the map
			{
			} else if (supervOpertoDetails[3].equalsIgnoreCase(VALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_PASSWORD,
						(map.get(USERPASSWORD) != null) ? map.get(USERPASSWORD) : E2EConstants.USER_PASSWD);
			else if (supervOpertoDetails[3].equalsIgnoreCase(INVALID))
				jsonReq.put(MOSIP_TEST_REGCLIENT_PASSWORD, supervOpertoDetails[3]);

			// For officerBiometricFileName
			if (supervOpertoDetails.length > 4) {
				if (supervOpertoDetails[4].equalsIgnoreCase("null")) {
				} // Don't add to the map
				else
					jsonReq.put("mosip.test.regclient.officerBiometricFileName", supervOpertoDetails[4]);
			}

			// For supervisorBiometricFileName
			if (supervOpertoDetails.length > 5) {
				if (supervOpertoDetails[5].equalsIgnoreCase("null")) {
				} // Don't add to the map
				else
					jsonReq.put("mosip.test.regclient.supervisorBiometricFileName", supervOpertoDetails[5]);
			}
		}

		JSONObject JO = new JSONObject(map);

		Response response = postRequest(url, mergeJSONObjects(JO, jsonReq, step).toString(), SETCONTEXT, step);

		if (!response.getBody().asString().toLowerCase().contains("true")) {
			this.hasError = true;
			throw new RigInternalError(response.getBody().asString());
		}
		return response.getBody().asString();

	}

	// get value specific to key from actuator
	private String getValueFromIdJson(String key) {
		String value = AdminTestUtil.getValueFromAuthActuator("json-property", key);
		String result = value.replaceAll("\\[|\\]", "").replaceAll("\"", "");
		return result;
	}

	public JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2, Scenario.Step step) {
		JSONObject mergedJSON = new JSONObject();
		try {
			mergedJSON = new JSONObject(json1, JSONObject.getNames(json1));
			for (String crunchifyKey : JSONObject.getNames(json2)) {
				mergedJSON.put(crunchifyKey, json2.get(crunchifyKey));
			}
		} catch (JSONException e) {
			// RunttimeException: Constructs a new runtime exception with the specified
			// detail message.
			// The cause is not initialized, and may subsequently be initialized by a call
			// to initCause.
			this.hasError = true;
			throw new RuntimeException("JSON Exception" + e);
		}
		return mergedJSON;
	}

	@SuppressWarnings("unused")
	private JSONObject createPayload(String publicKey, String machineId) {
		JSONObject jsonMachine = new JSONObject();
		jsonMachine.put("id", machineId);
		jsonMachine.put(IPADDRESS, "192.168.0.412");
		jsonMachine.put(ISACTIVE, true);
		jsonMachine.put(LANGCODE, "eng");
		jsonMachine.put(MACADDRESS, "A4-BB-6D-0F-B4-D0");
		jsonMachine.put(MACHINESPECID, "1001");
		jsonMachine.put("name", "Auto-1");
		jsonMachine.put("publicKey", publicKey);
		jsonMachine.put(REGCENTERID, "10002");
		jsonMachine.put("serialNum", "FB5962911686");
		jsonMachine.put("signPublicKey", publicKey);
		jsonMachine.put(ZONECODE, "NTH");
		return jsonMachine;
	}

	public String updateDemoOrBioDetail(String resFilePath, String testPersona,  List<String> attributeList, List<String> missAttributeList,
			List<String> updateAttributeList, Scenario.Step step) throws RigInternalError {
		String url = baseUrl + props.getProperty("updatePersonaData");
		JSONObject jsonReqInner = new JSONObject();
		JSONObject updateAttribute = new JSONObject();
		if (missAttributeList != null)
			jsonReqInner.put("missAttributeList", missAttributeList);
		jsonReqInner.put(PERSONAFILEPATH, resFilePath);
		jsonReqInner.put("testPersonaPath", testPersona);
		if (attributeList != null && !(attributeList.isEmpty()))
			jsonReqInner.put("regenAttributeList", attributeList);
		
	    // Handle the special case for "updateLargeDocInPersona"
	    if (step.getParameters().size() > 2 && step.getParameters().get(2).equalsIgnoreCase("updateLargeDocInPersona")) {
	        String docPathValue = "yourDocPathValue"; 
	        String poaValue = String.format("{\"typeCode\": \"DOC016\",\"docPath\": \"%s\"}", docPathValue);
	        updateAttribute.put("POA", poaValue);
	    }
	    else {
		if (updateAttributeList != null && !(updateAttributeList.isEmpty())) {
			String langcode = null;
			for (String keys : updateAttributeList) {
				String[] arr = keys.split("=");
				if (arr.length > 1) {
					if (arr[0].trim().equalsIgnoreCase(LANGCODE)) {
						langcode = arr[1].trim();
						continue;
					}
					if (arr[0].trim().equalsIgnoreCase("residencestatus")) {
						if (StringUtils.isEmpty(langcode)) {

							this.hasError = true;
							throw new RigInternalError("LangCode is missing in paramter");

						}
						updateAttribute.put(arr[0].trim(), langcode + "=" + arr[1].trim());
					} else
						updateAttribute.put(arr[0].trim(),
								(arr[0].trim().equalsIgnoreCase("email")
										? (arr[1].trim().equalsIgnoreCase("testmosip") ? "dslautomation@mosip.io"
												: arr[1].trim() + "@mosip.io")
										: arr[1].trim()));
				}
				// Pass phone and email as empty
				else {
					String key = arr[0].trim();
					updateAttribute.put(key, "");
				}
			}
		}
	    }
	    jsonReqInner.put("updateAttributeList", updateAttribute);
		JSONArray jsonReq = new JSONArray();
		jsonReq.put(0, jsonReqInner);
		Response response = putRequestWithBody(url, jsonReq.toString(), "Update DemoOrBioDetail", step);
		if (!response.getBody().asString().toLowerCase().contains("sucess")) {

			this.hasError = true;
			throw new RigInternalError("Unable to update DemoOrBioDetail " + attributeList + " from packet utility");
		}
		return response.getBody().asString();

	}

	public void updateBioException(String resFilePath, List<String> exceptionatt, Scenario.Step step)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("updatePersonabioexceptions");

		JSONObject jsonObject = new JSONObject();

		// Create exceptions array
		JSONArray exceptionsArray = new JSONArray();

		// Finger:Left IndexFinger@@Finger:Right IndexFinger@@Iris:Left

		String[] modalitytye = null;
		for (String s : exceptionatt) {
			modalitytye = s.split(":");

			JSONObject addmodality = new JSONObject();
			addmodality.put("exceptionType", "Temporary");
			addmodality.put("reason", "Temporary");
			addmodality.put("type", modalitytye[0]);
			addmodality.put("subType", modalitytye[1]);

			exceptionsArray.put(addmodality);

		}

		// Add exceptions array and personaFilePath to the JSON object
		jsonObject.put("exceptions", exceptionsArray);
		jsonObject.put(PERSONAFILEPATH, resFilePath);

		// Print JSON object
		logger.info(jsonObject.toString());

		Response response = putRequestWithBody(url, jsonObject.toString(), step);
//		GlobalMethods.ReportRequestAndResponse("", "", url, jsonObject.toString(), response.getBody().asString());
		if (!(response.getStatusCode() == 200)) {
			this.hasError = true;
			throw new RigInternalError("Unable to update bio exception  from packet utility");
		}
	}

	public String packetSync(String personaPath, HashMap<String, String> map, Scenario.Step step,
			boolean expectedToPass) throws RigInternalError {

		String url = baseUrl + props.getProperty("packetsyncUrl");
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(personaPath);
		jsonReq.put(PERSONAFILEPATH, arr);
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), map, "Packet Sync:", step);
		if (expectedToPass == false) {
			if (response.getBody().asString().contains("RPR-PKR-016")) {
				return response.getBody().asString();
			} else if (response.getBody().asString().contains("RPR-PKR-009")) {
				return response.getBody().asString();
			} else {
				this.hasError = true;
				throw new RigInternalError("Unable to do sync packet from packet utility");
			}
		}

		if (!response.getBody().asString().toLowerCase().contains("packet has reached")) {
			this.hasError = true;
			throw new RigInternalError("Unable to do sync packet from packet utility");
		}

		return response.getBody().asString();
	}

	public void bioAuth(String modility, String bioValue, String uin, Properties deviceProps, TestCaseDTO test,
			BioAuth bioAuth, Scenario.Step step) throws RigInternalError {
		if(test.getTestCaseName().contains("EKYC")) {
			test.setEndPoint(test.getEndPoint().replace("$kycPartnerKey$", kycPartnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$kycPartnerName$", kycPartnerId));
		}else {
			test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", partnerKeyUrl));
			test.setEndPoint(test.getEndPoint().replace("$PartnerName$", partnerId));
		}
		
//		test.setEndPoint(test.getEndPoint().replace("uinnumber", uin));
		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, INDIVIDUALID);
		logger.info("After UIN: "+ input);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(BIOSUBTYPE), "identityRequest."+BIOSUBTYPE);
		logger.info("After BioSubType: "+ input);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(BIOTYPE), "identityRequest."+BIOTYPE);
		logger.info("After BioSubType: "+ input);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICECODE), "identityRequest."+DEVICECODE);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICEPROVIDERID),
				"identityRequest."+DEVICEPROVIDERID);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESERVICEID),
				"identityRequest."+DEVICESERVICEID);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESERVICEVERSION),
				"identityRequest."+DEVICESERVICEVERSION);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICEPROVIDER),
				"identityRequest."+DEVICEPROVIDER);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESUBTYPE), "identityRequest."+DEVICESUBTYPE);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("make"), "identityRequest.make");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(MODEL), "identityRequest."+MODEL);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(SERIALNO), "identityRequest."+SERIALNO);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("type"), "identityRequest.type");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, bioValue, "identityRequest.bioValue");
		test.setInput(input);
		Reporter.log("<b><u>" + test.getTestCaseName() + "_" + modility + "</u></b>");

		try {
			bioAuth.test(test);
		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		} finally {

		}
	}

	public void esignetBioAuth(String modility, String bioValue, String uin, String transactionId,
			Properties deviceProps, TestCaseDTO test, EsignetBioAuth esignetBioAuth, String input, Scenario.Step step)
			throws RigInternalError {

		input = JsonPrecondtion.parseAndReturnJsonContent(input, uin, INDIVIDUALID);

		input = JsonPrecondtion.parseAndReturnJsonContent(input, transactionId, "transactionId");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(BIOSUBTYPE),
				"identityRequest.bioSubType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(BIOTYPE),
				"identityRequest.bioType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICECODE),
				"identityRequest.deviceCode");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICEPROVIDERID),
				"identityRequest.deviceProviderID");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESERVICEID),
				"identityRequest.deviceServiceID");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESERVICEVERSION),
				"identityRequest.deviceServiceVersion");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICEPROVIDER),
				"identityRequest.deviceProvider");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESUBTYPE),
				"identityRequest.deviceSubType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("make"),
				"identityRequest.make");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(MODEL),
				"identityRequest.model");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(SERIALNO),
				"identityRequest.serialNo");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("type"),
				"identityRequest.type");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, getAuthTransactionId(transactionId),
				"identityRequest.transactionId");

		input = JsonPrecondtion.parseAndReturnJsonContent(input, bioValue, "identityRequest.bioValue");
		test.setInput(input);
		Reporter.log("<b><u>" + test.getTestCaseName() + "_" + modility + "</u></b>");

		try {
			esignetBioAuth.test(test);
		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		} finally {

		}
	}
	
	private String getAuthTransactionId(String oidcTransactionId) {
	    final String transactionId = oidcTransactionId.replaceAll("_|-", "");
	    String lengthOfTransactionId =  EsignetUtil.getValueFromEsignetActuator("/mosip/mosip-config/esignet-default.properties", "mosip.esignet.auth-txn-id-length");
	   int authTransactionIdLength = lengthOfTransactionId != null ? Integer.parseInt(lengthOfTransactionId): 0;
	    final byte[] oidcTransactionIdBytes = transactionId.getBytes();
	    final byte[] authTransactionIdBytes = new byte[authTransactionIdLength];
	    int i = oidcTransactionIdBytes.length - 1;
	    int j = 0;
	    while(j < authTransactionIdLength) {
	        authTransactionIdBytes[j++] = oidcTransactionIdBytes[i--];
	        if(i < 0) { i = oidcTransactionIdBytes.length - 1; }
	    }
	    return new String(authTransactionIdBytes);
	}

	public String retrieveBiometric(String resFilePath, List<String> retriveAttributeList, Scenario.Step step)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("getPersonaData");
		JSONObject jsonReqInner = new JSONObject();
		if (retriveAttributeList != null && !(retriveAttributeList.isEmpty()))
			jsonReqInner.put("retriveAttributeList", retriveAttributeList);
		jsonReqInner.put(PERSONAFILEPATH, resFilePath);
		JSONArray jsonReq = new JSONArray();
		jsonReq.put(0, jsonReqInner);
		Response response = getRequest(url, jsonReq.toString(), "Retrive BiometricData", step);
		if (response.getBody().asString().equals("")) {
			this.hasError = true;
			throw new RigInternalError(
					"Unable to retrive BiometricData " + retriveAttributeList + " from packet utility");
		}
		logger.info("Response : " + response.getBody().asString());

		return response.getBody().asString();

	}

	private Response getRequest(String url, String body, String opsToLog, Scenario.Step step) {
		Response apiResponse = getRequestWithbody(url, body, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
				step);
		return apiResponse;
	}

	private Response getRequestWithbody(String url, String body, String contentHeader, String acceptHeader,
			Scenario.Step step) {
		logger.info("RESSURED: Sending a GET request to " + url);
		logger.info("REQUEST: Sending a GET request to " + url);
		url = addContextToUrl(url, step);
		Response getResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			getResponse = given().relaxedHTTPSValidation().accept("*/*").contentType("application/json").log().all()
					.when().body(body).get(url).then().extract().response();
			logger.info("REST-ASSURED: The response Time is: " + getResponse.time());
		} else {
			getResponse = given().relaxedHTTPSValidation().accept("*/*").contentType("application/json").when()
					.body(body).get(url).then().extract().response();
		}
		/*
		 * GlobalMethods.ReportRequestAndResponse(null,
		 * getResponse.getHeaders().asList().toString(), url, body,
		 * getResponse.asString(),true);
		 */
		return getResponse;
	}

	public static Properties getParamsFromArg(String argVal, String pattern) {
		Properties props = new Properties();

		String[] attr = argVal.split(pattern);
		if (attr != null) {
			for (String s : attr) {
				String[] arr = s.split("=");
				if (arr.length > 1) {
					props.put(arr[0].trim(), arr[1].trim());
				}
			}
		}
		return props;
	}

	public static List<String> getParamsArg(String argVal, String pattern) {
		List<String> list = new ArrayList<>();

		String[] attr = argVal.split(pattern);
		if (attr != null) {
			for (String s : attr) {
				list.add(s);
			}
		}
		return list;
	}

	public void serverResourceStatusManager(String responsePattern, String status, Scenario.Step step)
			throws RigInternalError {
		String respnseStatus = "";
		HashMap<String, String> getHMapQParam = createGetRequest();
		String url = baseUrl + props.getProperty("statusCheck");
		Response getResponse = getRequestWithQueryParam(url, getHMapQParam, "Get server status", step);
		if (getResponse == null) {
			this.hasError = true;
			throw new RigInternalError("Packet utility get method doesn't return any response");
		}
		respnseStatus = getResponse.getBody().asString();
		if (!respnseStatus.isEmpty()) {
			if (respnseStatus.toLowerCase().contains(responsePattern.toLowerCase())) {
				HashMap<String, String> putHMapQParam = createPutReqeust(status);
				putRequestWithQueryParam(url, putHMapQParam, "Update server key", step);
			} else {
				this.hasError = true;
				throw new RigInternalError("execution status alrady in use");
			}
		} else {
			this.hasError = true;
			throw new RigInternalError("got empty status");
		}
	}

	private HashMap<String, String> createGetRequest() {
		HashMap<String, String> getHMapQParam = new HashMap<>();
		getHMapQParam.put("key", "automation_key");
		return getHMapQParam;
	}

	private HashMap<String, String> createPutReqeust(String status) {
		HashMap<String, String> putHMapQParam = new HashMap<>();
		putHMapQParam.put("key", "automation_key");
		putHMapQParam.put("status", status);
		return putHMapQParam;
	}

	public void setMockabisExpectaion(JSONArray jsonreq, HashMap<String, String> contextKey, Scenario.Step step)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("mockAbis");
		Response response = postRequestWithQueryParamAndBody(url, jsonreq.toString(), contextKey, "Mockabis Expectaion",
				step);
//		GlobalMethods.ReportRequestAndResponse("", "", url, jsonreq.toString(), response.getBody().asString());
		logger.info("****" + response.getBody().asString());
		if (!response.getBody().asString().toLowerCase().contains(SUCCESS)) {
			this.hasError = true;
			throw new RigInternalError("Unable to set mockabis expectaion from packet utility");
		}
	}

	//// Activate/DeActivate machine--- start
	public Boolean activateDeActiveMachine(String jsonInput, String machineSpecId, String machineid, String zoneCode,
			String token, String status, Scenario.Step step) throws RigInternalError {

		JSONObject jsonPutReq = machineRequestBuilder(jsonInput, machineSpecId, machineid, zoneCode, status);
		Boolean isActive = updateMachineDetail(jsonPutReq, token, status, step);
		return isActive;
	}

	public Boolean updateMachineDetail(JSONObject jsonPutReq, String token, String status, Scenario.Step step)
			throws RigInternalError {
		String url = System.getProperty(ENV_ENDPOINT) + props.getProperty("getMachine");
		Response puttResponse = putReqestWithCookiesAndBody(url, jsonPutReq.toString(), token,
				"Update machine detail with status[isActive=" + status + "]", step);

		GlobalMethods.ReportRequestAndResponse("", "", url, jsonPutReq.toString(), puttResponse.getBody().asString());

		if (puttResponse.getBody().asString().toLowerCase().contains(ERRORCODE)) {
			logger.error("unable to update machine detail");
			this.hasError = true;
			throw new RigInternalError("unable to update machine detail");
		}

		JSONObject jsonResp = new JSONObject(puttResponse.getBody().asString());
		Boolean isActive = jsonResp.getJSONObject(RESPONSE).getBoolean(ISACTIVE);
		return isActive;
	}

	public Response putReqestWithCookiesAndBody(String url, String body, String token, String opsToLog,
			Scenario.Step step) {

		Response puttResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").log().all().when().cookie(AUTHORIZATION, token).put(url).then().log().all().extract()
					.response();
		} else {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").when().cookie(AUTHORIZATION, token).put(url).then().extract().response();
		}

		GlobalMethods.ReportRequestAndResponse("", "", url, body, puttResponse.getBody().asString());

		return puttResponse;
	}

	public Response postReqestWithCookiesAndBody(String url, String body, String token, String opsToLog) {
		Response posttResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			posttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").log().all().when().cookie(AUTHORIZATION, token).post(url).then().log().all()
					.extract().response();
		} else {
			posttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").when().cookie(AUTHORIZATION, token).post(url).then().extract().response();
		}

		GlobalMethods.ReportRequestAndResponse("", "", url, body, posttResponse.getBody().asString());

		return posttResponse;
	}

	public Response patchReqestWithCookiesAndBody(String url, String body, String token, String opsToLog) {
		Response puttResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").log().all().when().cookie(AUTHORIZATION, token).patch(url).then().log().all()
					.extract().response();
		} else {
			puttResponse = given().relaxedHTTPSValidation().body(body).contentType(MediaType.APPLICATION_JSON)
					.accept("*/*").when().cookie(AUTHORIZATION, token).patch(url).then().extract().response();
		}
		GlobalMethods.ReportRequestAndResponse("", "", url, body, puttResponse.getBody().asString());
		return puttResponse;
	}

	public Response patchRequestWithQueryParm(String url, HashMap<String, String> queryParam, String token,
			String opsToLog) {

		Response patchResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			patchResponse = given().relaxedHTTPSValidation().queryParams(queryParam)
					.contentType(MediaType.APPLICATION_JSON).cookie(AUTHORIZATION, token).accept("*/*").log().all()
					.when().patch(url).then().log().all().extract().response();
		} else {
			patchResponse = given().relaxedHTTPSValidation().queryParams(queryParam)
					.contentType(MediaType.APPLICATION_JSON).cookie(AUTHORIZATION, token).accept("*/*").when()
					.patch(url).then().extract().response();
		}
		GlobalMethods.ReportRequestAndResponse("", "", url, "", patchResponse.getBody().asString());
		return patchResponse;
	}

	/* Remap User--- start */
	public Boolean remapUser(String jsonInput, String token, String value, String regCenterId, String zoneCode,
			Scenario.Step step) throws RigInternalError {
		regCenterId = !regCenterId.equals("0") ? regCenterId
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.regCenterId");
		String PUTUSERURL = System.getProperty(ENV_ENDPOINT) + props.getProperty("putUserToRemap") + value + "/eng/"
				+ regCenterId;
		String updatedRegCenter = updateToRemapUser(PUTUSERURL, token, step);
		return updatedRegCenter.equals(regCenterId) ? true : false;
	}

	/**** Remap Device ****/
	public Boolean remapDevice(String jsonInput, String token, String value, String regCenterId, String zoneCode,
			Scenario.Step step) throws RigInternalError {
		regCenterId = !regCenterId.equals("0") ? regCenterId
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].regCenterId");
		zoneCode = !zoneCode.equals("0") ? zoneCode
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].zoneCode");
		String PUTUSERURL = System.getProperty(ENV_ENDPOINT) + props.getProperty("putDeviceToRemap");
		JSONObject jsonPutReq = requestBuilderDeviceRemap(jsonInput, zoneCode, regCenterId);
		Response response = putReqestWithCookiesAndBody(PUTUSERURL, JSONValue.toJSONString(jsonPutReq), token,
				"Remap device to different registration center", step);
		GlobalMethods.ReportRequestAndResponse("", "", PUTUSERURL, "", response.getBody().asString());

		return JsonPrecondtion.getValueFromJson(response.getBody().asString(), "response.regCenterId")
				.equals(regCenterId) ? true : false;
	}

	/* Remap Machine--- start */
	public String remapMachine(String jsonInput, String token, String regCenterId, String zoneCode)
			throws RigInternalError {
		regCenterId = !regCenterId.equals("0") ? regCenterId
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].regCenterId");
		String url = System.getProperty(ENV_ENDPOINT) + props.getProperty("getRegistrationCenter") + regCenterId
				+ "/eng";
		Response getResponse = getRequestWithCookiesAndPathParam(url, token, "Get zoneCode by regCenterId");
		GlobalMethods.ReportRequestAndResponse("", "", url, "", getResponse.getBody().asString());

		if (getResponse.getBody().asString().toLowerCase().contains(ERRORCODE)) {
			logger.error("zoneCode not found for  :[" + regCenterId + "]");
			this.hasError = true;
			throw new RigInternalError("zoneCode not found for  :[" + regCenterId + "]");
		}
		JSONObject jsonResp = new JSONObject(getResponse.getBody().asString());
		zoneCode = !zoneCode.equals("0") ? zoneCode
				: JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(registrationCenters)[0].zoneCode");
		JSONObject jsonPutReq = requestBuilderMachineRemap(jsonInput, zoneCode, regCenterId);
		String updatedMachineID = updateToRemapMachine(jsonPutReq, token, step);
		return updatedMachineID;
	}

	private JSONObject requestBuilderMachineRemap(String jsonInput, String zoneCode, String regCenterId) {
		JSONObject jsonOutterReq = new JSONObject();
		jsonOutterReq.put("id", STRING);
		jsonOutterReq.put(METADATA, new JSONObject());
		JSONObject jsonInnerReq = new JSONObject();
		jsonInnerReq.put("id", JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].id"));
		jsonInnerReq.put("name", JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].name"));
		jsonInnerReq.put(MACADDRESS, JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].macAddress"));
		jsonInnerReq.put(IPADDRESS, JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].ipAddress"));
		jsonInnerReq.put(MACHINESPECID,
				JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].machineSpecId"));
		jsonInnerReq.put(LANGCODE, "eng");
		jsonInnerReq.put(REGCENTERID, JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].macAddress"));
		jsonInnerReq.put(ZONECODE, zoneCode);
		jsonInnerReq.put(REGCENTERID, regCenterId);
		jsonInnerReq.put(ISACTIVE, true);
		jsonOutterReq.put(REQUEST, jsonInnerReq);
		jsonOutterReq.put(REQUESTTIME, getCurrentDateAndTimeForAPI());
		jsonOutterReq.put(VERSION, STRING);
		return jsonOutterReq;
	}

	private JSONObject requestBuilderDeviceRemap(String jsonInput, String zoneCode, String regCenterId) {
		JSONObject jsonOutterReq = new JSONObject();
		jsonOutterReq.put("id", STRING);
		jsonOutterReq.put(METADATA, new JSONObject());
		JSONObject jsonInnerReq = new JSONObject();
		jsonInnerReq.put("deviceSpecId",
				JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].deviceSpecId"));
		jsonInnerReq.put("id", JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].id"));
		jsonInnerReq.put(IPADDRESS, JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].ipAddress"));
		jsonInnerReq.put(ISACTIVE, true);
		jsonInnerReq.put(LANGCODE, "eng");
		jsonInnerReq.put(MACADDRESS, JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].macAddress"));
		jsonInnerReq.put("name", JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].name"));
		jsonInnerReq.put(REGCENTERID, regCenterId);
		jsonInnerReq.put("serialNum", JsonPrecondtion.getValueFromJson(jsonInput, "response.(data)[0].serialNum"));
		jsonInnerReq.put(ZONECODE, zoneCode);
		jsonOutterReq.put(REQUEST, jsonInnerReq);
		jsonOutterReq.put(REQUESTTIME, getCurrentDateAndTimeForAPI());
		jsonOutterReq.put(VERSION, STRING);
		return jsonOutterReq;
	}

	public Response getRequestWithCookiesAndPathParam(String url, String token, String opsToLog) {
		Response getResponse = given().relaxedHTTPSValidation().cookie(AUTHORIZATION, token).log().all().when().get(url)
				.then().log().all().extract().response();

		return getResponse;
	}

	public JSONObject machineRequestBuilder(String jsonInput, String machineSpecId, String machineid, String zoneCode,
			String status) {
		JSONObject jsonOutterReq = new JSONObject();
		jsonOutterReq.put("id", STRING);
		jsonOutterReq.put(METADATA, new JSONObject());
		JSONObject jsonInnerReq = new JSONObject();
		jsonInnerReq.put("id", JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].id"));
		jsonInnerReq.put("name", JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].name"));
		jsonInnerReq.put(MACADDRESS, JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].macAddress"));
		jsonInnerReq.put(IPADDRESS, JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].ipAddress"));
		jsonInnerReq.put(MACHINESPECID,
				JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].machineSpecId"));
		jsonInnerReq.put(LANGCODE, "eng");
		jsonInnerReq.put(REGCENTERID, JsonPrecondtion.getValueFromJson(jsonInput, "response.(machines)[0].macAddress"));
		jsonInnerReq.put(ZONECODE, zoneCode);
		jsonInnerReq.put(ISACTIVE, true);
		jsonOutterReq.put(REQUEST, jsonInnerReq);
		jsonOutterReq.put(REQUESTTIME, getCurrentDateAndTimeForAPI());
		jsonOutterReq.put(VERSION, STRING);
		return jsonOutterReq;
	}

	public String getCurrentDateAndTimeForAPI() {
		return javax.xml.bind.DatatypeConverter.printDateTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
	}

	public JSONObject updatePartnerRequestBuilder(String status) throws RigInternalError {
		List<String> statusList = Arrays.asList("Active", " De-activate");
		if (!(statusList.contains(status))) {
			logger.error(status + " is not supported only allowed status[Active/De-Active]");
			this.hasError = true;
			throw new RigInternalError(status + " is not supported only allowed status[Active/De-Active]");
		}
		JSONObject jsonOutterReq = new JSONObject();
		jsonOutterReq.put("id", STRING);
		jsonOutterReq.put(METADATA, new JSONObject());
		JSONObject jsonInnerReq = new JSONObject();
		jsonInnerReq.put("status", status); // status can be Active and De-Active
		jsonOutterReq.put(REQUEST, jsonInnerReq);
		jsonOutterReq.put(REQUESTTIME, getCurrentDateAndTimeForAPI());
		jsonOutterReq.put(VERSION, STRING);
		return jsonOutterReq;
	}

	// Activate/DeActivate RegCenter--- start
	public Boolean activateDeActiveRegCenter(String jsonInput, String id, String locationCode, String zoneCode,
			String token, String status, Scenario.Step step) throws RigInternalError {
		JSONObject jsonPutReq = regCenterPutrequestBuilder(jsonInput, id, locationCode, zoneCode, status, step);
		String url = System.getProperty(ENV_ENDPOINT) + props.getProperty("getRegistrationCenter");
		Response puttResponse = putReqestWithCookiesAndBody(url, jsonPutReq.toString(), token,
				"Update RegCenter details with status[isActive=]" + status, step);

		GlobalMethods.ReportRequestAndResponse("", "", url, "", puttResponse.getBody().asString());

		if (puttResponse.getBody().asString().toLowerCase().contains(ERRORCODE)) {
			logger.error("unable to update RegCenter detail");
			this.hasError = true;
			throw new RigInternalError("unable to update RegCenter detail");
		}
		JSONObject jsonResp = new JSONObject(puttResponse.getBody().asString());
		Boolean isActive = jsonResp.getJSONObject(RESPONSE).getBoolean(ISACTIVE);
		return isActive;
	}

	public JSONObject regCenterPutrequestBuilder(String jsonInput, String id, String locationCode, String zoneCode,
			String status, Scenario.Step step) {
		JSONObject jsonOutterReq = new JSONObject();
		JSONObject jsonInnerReq = new JSONObject();
		jsonOutterReq.put("id", STRING);
		jsonOutterReq.put(METADATA, new JSONObject());
		jsonInnerReq.put("addressLine1", (jsonInput == null) ? "addressLine1"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].addressLine1"));
		jsonInnerReq.put("centerEndTime", (jsonInput == null) ? "17:00:00"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].centerEndTime"));
		jsonInnerReq.put("centerStartTime", (jsonInput == null) ? "09:00:00"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].centerStartTime"));
		jsonInnerReq.put("centerTypeCode", (jsonInput == null) ? "REG"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].centerTypeCode"));
		jsonInnerReq.put("holidayLocationCode", (jsonInput == null) ? "KTA"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].holidayLocationCode"));
		jsonInnerReq.put("id", id);
		jsonInnerReq.put(ISACTIVE, status);
		jsonInnerReq.put(LANGCODE, "eng");
		jsonInnerReq.put("latitude", (jsonInput == null) ? "35.405692"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].latitude"));
		jsonInnerReq.put("locationCode", locationCode);
		jsonInnerReq.put("longitude", (jsonInput == null) ? "-5.433368"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].longitude"));
		jsonInnerReq.put("name", (jsonInput == null) ? "name1"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].name"));
		jsonInnerReq.put("perKioskProcessTime", (jsonInput == null) ? "00:15:00"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].perKioskProcessTime"));
		jsonInnerReq.put("workingHours", (jsonInput == null) ? "8:00:00"
				: JsonPrecondtion.getValueFromJson(jsonInput, "response.(registrationCenters)[0].workingHours"));
		JSONObject jsonArrayPutPostDtoJsonReq = new JSONObject();
		JSONArray exceptionalHolidayPutPostDtoJsonReq = new JSONArray();
		jsonArrayPutPostDtoJsonReq.put("exceptionHolidayDate", "2021-01-01");
		jsonArrayPutPostDtoJsonReq.put("exceptionHolidayName", "New year");
		jsonArrayPutPostDtoJsonReq.put("exceptionHolidayReson", "New year eve");
		exceptionalHolidayPutPostDtoJsonReq.put(jsonArrayPutPostDtoJsonReq);
		jsonInnerReq.put("exceptionalHolidayPutPostDto", exceptionalHolidayPutPostDtoJsonReq);
		jsonInnerReq.put(ZONECODE, zoneCode);
		jsonOutterReq.put(REQUEST, jsonInnerReq);
		jsonOutterReq.put(REQUESTTIME, getCurrentDateAndTimeForAPI());
		jsonOutterReq.put(VERSION, STRING);
		return jsonOutterReq;
	}

	// Activate/DeActivate RegCenter--- end

	private String updateToRemapMachine(JSONObject jsonPutReq, String token, Scenario.Step step)
			throws RigInternalError {
		String url = System.getProperty(ENV_ENDPOINT) + props.getProperty("putMachineToRemap");
		Response puttResponse = putReqestWithCookiesAndBody(url, jsonPutReq.toString(), token, "Update machine detail",
				step);
		GlobalMethods.ReportRequestAndResponse("", "", url, jsonPutReq.toString(), puttResponse.getBody().asString());

		if (puttResponse.getBody().asString().toLowerCase().contains(ERRORCODE)) {
			logger.error("unable to update machine detail");
			this.hasError = true;
			throw new RigInternalError("unable to update machine detail");
		}
		JSONObject jsonResp = new JSONObject(puttResponse.getBody().asString());
		String machineID = jsonResp.getJSONObject(RESPONSE).getString("id");
		return machineID;
	}

	private String updateToRemapUser(String url, String token, Scenario.Step step) throws RigInternalError {
		Response puttResponse = putReqestWithCookiesAndNoBody(url, token, "Update user detail", step);
		GlobalMethods.ReportRequestAndResponse("", "", url, "", puttResponse.getBody().asString());

		if (puttResponse.getBody().asString().toLowerCase().contains(ERRORCODE)) {
			logger.error("unable to update user detail");
			this.hasError = true;
			throw new RigInternalError("unable to update user detail");
		}
		JSONObject jsonResp = new JSONObject(puttResponse.getBody().asString());
		String regCenterId = jsonResp.getJSONObject(RESPONSE).getString(REGCENTERID);
		return regCenterId;
	}

	public Response putReqestWithCookiesAndNoBody(String url, String token, String opsToLog, Scenario.Step step) {
		Response puttResponse = null;
		if (dslConfigManager.IsDebugEnabled()) {
			puttResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON).accept("*/*").log()
					.all().when().cookie(AUTHORIZATION, token).put(url).then().log().all().extract().response();
		} else {
			puttResponse = given().relaxedHTTPSValidation().contentType(MediaType.APPLICATION_JSON).accept("*/*").when()
					.cookie(AUTHORIZATION, token).put(url).then().extract().response();
		}
		GlobalMethods.ReportRequestAndResponse("", "", url, "", puttResponse.getBody().asString());

		return puttResponse;
	}

	public void operatorOnboardAuth(String modility, String bioValue, String user, TestCaseDTO test, BioAuth bioAuth,
			String individualIdType, Properties deviceProps, Scenario.Step step) throws RigInternalError {

		test.setEndPoint(test.getEndPoint().replace("$PartnerKey$", deviceProps.getProperty("partnerKey")));
		String input = test.getInput();
		input = JsonPrecondtion.parseAndReturnJsonContent(input, user, INDIVIDUALID);
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(BIOSUBTYPE),
				"identityRequest.bioSubType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(BIOTYPE),
				"identityRequest.bioType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICECODE),
				"identityRequest.deviceCode");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICEPROVIDERID),
				"identityRequest.deviceProviderID");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESERVICEID),
				"identityRequest.deviceServiceID");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESERVICEVERSION),
				"identityRequest.deviceServiceVersion");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICEPROVIDER),
				"identityRequest.deviceProvider");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(DEVICESUBTYPE),
				"identityRequest.deviceSubType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("make"),
				"identityRequest.make");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(MODEL),
				"identityRequest.model");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty(SERIALNO),
				"identityRequest.serialNo");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, deviceProps.getProperty("type"),
				"identityRequest.type");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, individualIdType, "individualIdType");
		input = JsonPrecondtion.parseAndReturnJsonContent(input, bioValue, "identityRequest.bioValue");
		test.setInput(input);
		Reporter.log("<b><u>" + test.getTestCaseName() + "_" + modility + "</u></b>");

		try {
			bioAuth.test(test);
		} catch (AuthenticationTestException | AdminTestException e) {
			this.hasError = true;
			throw new RigInternalError(e.getMessage());
		} finally {

		}
	}

	public static String getJsonFromTemplate(String input, String template) {
		return getJsonFromTemplate(input, template, true);

	}

	public static String getJsonFromTemplate(String input, String template, boolean readFile) {
		String resultJson = null;
		try {
			Handlebars handlebars = new Handlebars();
			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> map = gson.fromJson(input, type);
			String templateJsonString;
			if (readFile) {
				templateJsonString = new String(
						Files.readAllBytes(Paths.get(AdminTestUtil.getResourcePath() + template + ".hbs")), "UTF-8");
			} else {
				templateJsonString = template;
			}

			Template compiledTemplate = handlebars.compileInline(templateJsonString);
			Context context = Context.newBuilder(map).build();
			resultJson = compiledTemplate.apply(context);
		} catch (Exception e) {
		}
		return resultJson;
	}

	public static String signJWKKey(String clientId, RSAKey jwkKey) {
		String tempUrl = BaseTestCase.ApplnURI.replace("api-internal", "esignet") + "/v1/esignet/oauth/token";
		String clientAssertionToken = "";
		// Create RSA-signer with the private key
		JWSSigner signer;

		try {
			signer = new RSASSASigner(jwkKey);

			// Prepare JWT with claims set
			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(clientId)//
					.audience(tempUrl)//
					.issuer(clientId)//
					.issueTime(new Date()).expirationTime(new Date(new Date().getTime() + 180 * 1000)).build();

			SignedJWT signedJWT = new SignedJWT(
					new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwkKey.getKeyID()).build(), claimsSet);

			// Compute the RSA signature
			signedJWT.sign(signer);
			clientAssertionToken = signedJWT.serialize();
		} catch (Exception e) {
		}
		return clientAssertionToken;
	}

	public static int getActuatorDelay() {
		String sequence = null;

		sequence = BaseTestCaseUtil.getRegprocWaitFromActuator();
		String[] numbers = sequence.split(",");
		int commonDifference = Integer.parseInt(numbers[1]) - Integer.parseInt(numbers[0]);

		// Convert wait time from regproc actuator in seconds

		int waitFromActuator = commonDifference * 60;
		return waitFromActuator;
	}

	public static void closeOutputStream(FileOutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.flush();
				outputStream.close();
			} catch (IOException e) {
				// Handle the exception
			}
		}
	}

	public static void closeFileReader(FileReader fileReader) {
		if (fileReader != null) {
			try {
				fileReader.close();
			} catch (IOException e) {
				// Handle the exception
			}
		}
	}

	public static void closeInputStream(FileInputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// Handle the exception
			}
		}
	}

	public static void closeZipInputStream(ZipInputStream zipInputStream) {
		if (zipInputStream != null) {
			try {
				zipInputStream.close();
			} catch (IOException e) {
				// Handle the exception
			}
		}
	}

}