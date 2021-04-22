package io.mosip.ivv.orchestrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Reporter;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.restassured.response.Response;

public class PacketUtility extends BaseTestCaseUtil {

	public List<String> generateResidents(int n, Boolean bAdult, Boolean bSkipGuardian, String gender,
			String missFields, HashMap<String, String> contextKey) throws RigInternalError {

		String url = baseUrl + props.getProperty("getResidentUrl") + n;
		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		if (bAdult) {
			residentAttrib.put("Age", "RA_Adult");
		} else {
			residentAttrib.put("Age", "RA_Minor");
			residentAttrib.put("SkipGaurdian", bSkipGuardian);
		}
		residentAttrib.put("Gender", gender);
		residentAttrib.put("PrimaryLanguage", "eng");
		residentAttrib.put("Iris", true);
		// added for face biometric related issue
		residentAttrib.put("Finger", true);
		residentAttrib.put("Face", true);
		//
		
		if (missFields != null)
			residentAttrib.put("Miss", missFields);
		jsonReq.put("PR_ResidentAttribute", residentAttrib);
		jsonwrapper.put("requests", jsonReq);

		// Response response = postReqest(url, jsonwrapper.toString(),
		// "GENERATE_RESIDENTS_DATA");
		Response response = postRequestWithQueryParamAndBody(url, jsonwrapper.toString(), contextKey,
				"GENERATE_RESIDENTS_DATA");
		// assertTrue(response.getBody().asString().contains("SUCCESS"),"Unable to get
		// residentData from packet utility");
		if (!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to get residentData from packet utility");
		// assertTrue(response.getBody().asString().contains("Failure"),"Unable to get
		// residentData from packet utility");
		JSONArray resp = new JSONObject(response.getBody().asString()).getJSONArray("response");
		List<String> residentPaths = new ArrayList<>();
		for (int i = 0; i < resp.length(); i++) {
			JSONObject obj = resp.getJSONObject(i);
			String resFilePath = obj.get("path").toString();
			residentPaths.add(resFilePath);
			// residentTemplatePaths.put(resFilePath, null);
		}
		return residentPaths;

	}

	public JSONArray getTemplate(Set<String> resPath, String process, HashMap<String, String> contextKey)
			throws RigInternalError {
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		for (String residentPath : resPath) {

			arr.put(residentPath);
		}
		jsonReq.put("personaFilePath", arr);
		String url = baseUrl + props.getProperty("getTemplateUrl") + process + "/ /";
		// Response templateResponse = postReqest(url, jsonReq.toString(),
		// "GET-TEMPLATE");
		Response templateResponse = postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey,
				"GET-TEMPLATE");
		JSONObject jsonResponse = new JSONObject(templateResponse.asString());
		JSONArray resp = jsonResponse.getJSONArray("packets");
		if ((resp.length() <= 0))
			throw new RigInternalError("Unable to get Template from packet utility");
		return resp;
	}

	public void requestOtp(String resFilePath, HashMap<String, String> contextKey, String emailOrPhone) {
		String url = baseUrl + props.getProperty("sendOtpUrl") + emailOrPhone;
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		// postReqest(url,jsonReq.toString(),"Send Otp");
		postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey, "Send Otp");

	}

	public void verifyOtp(String resFilePath, HashMap<String, String> contextKey, String emailOrPhone)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("verifyOtpUrl") + emailOrPhone;
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		// Response response =postReqest(url,jsonReq.toString(),"Verify Otp");
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey, "Verify Otp");
		// assertTrue(response.getBody().asString().contains("VALIDATION_SUCCESSFUL"),"Unable
		// to Verify Otp from packet utility");
		if (!response.getBody().asString().toLowerCase().contains("validation_successful"))
			throw new RigInternalError("Unable to Verify Otp from packet utility");

	}

	public String preReg(String resFilePath, HashMap<String, String> contextKey) throws RigInternalError {
		String url = baseUrl + props.getProperty("preregisterUrl");
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		// Response response =postReqest(url,jsonReq.toString(),"AddApplication");
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey, "AddApplication");
		String prid = response.getBody().asString();
		// assertTrue((int)prid.charAt(0)>47 && (int)prid.charAt(0)<58 ,"Unable to
		// pre-register from packet utility");
		if (!((int) prid.charAt(0) > 47 && (int) prid.charAt(0) < 58))
			throw new RigInternalError("Unable to pre-register using packet utility");
		return prid;

	}

	public String updateApplication(String resFilePath, HashMap<String, String> residentPathsPrid,
			HashMap<String, String> contextKey) throws RigInternalError {
		String url = baseUrl + props.getProperty("updateApplication") + residentPathsPrid.get(resFilePath);
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		Response response = putRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey, "UpdateApplication");
		String prid = response.getBody().asString();
		if (!((int) prid.charAt(0) > 47 && (int) prid.charAt(0) < 58))
			throw new RigInternalError("Unable to updateApplication using packet utility");
		return prid;

	}

	public void uploadDocuments(String resFilePath, String prid, HashMap<String, String> contextKey) {
		String url = baseUrl + "/prereg/documents/" + prid;
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		// postReqest(url,jsonReq.toString(),"Upload Documents");
		postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey, "Upload Documents");
	}

	public void bookAppointment(String prid, int nthSlot, HashMap<String, String> contextKey, boolean bookOnHolidays)
			throws RigInternalError {
		//String url = baseUrl + "/bookappointment/" + prid + "/" + nthSlot + "/" + bookOnHolidays;
		String url = baseUrl + "/prereg/appointment/" + prid + "/" + nthSlot + "/" + bookOnHolidays;
		JSONObject jsonReq = new JSONObject();
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey, "BookAppointment");
		if (!response.getBody().asString().toLowerCase().contains("appointment booked successfully"))
			throw new RigInternalError("Unable to BookAppointment from packet utility");
	}

	public String generateAndUploadPacket(String prid, String packetPath, HashMap<String, String> contextKey)
			throws RigInternalError {
		String url = baseUrl + "/packet/sync/" + prid;
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(packetPath);
		jsonReq.put("personaFilePath", arr);
		// Response response =postReqest(url,jsonReq.toString(),"Generate And
		// UploadPacket");
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey,
				"Generate And UploadPacket");
		JSONObject jsonResp = new JSONObject(response.getBody().asString());
		String rid = jsonResp.getJSONObject("response").getString("registrationId");
		// assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to
		// Generate And UploadPacket from packet utility");
		if (!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to Generate And UploadPacket from packet utility");
		return rid;
	}

	public String updateResidentRid(String personaFilePath, String rid) throws RigInternalError {
		String url = baseUrl + props.getProperty("updateResidentUrl") + "?RID=" + rid;

		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();

		residentAttrib.put("rid", personaFilePath);

		jsonReq.put("PR_ResidentList", residentAttrib);

		jsonwrapper.put("requests", jsonReq);

		Response response = postReqest(url, jsonwrapper.toString(), "link Resident data with RID");

		if (!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to add Resident RID in resident data");
		String ret = response.getBody().asString();
		return ret;

	}

	public String updateResidentGuardian(String residentFilePath, boolean withRid, String missingFields,
			String parentEmailOrPhone) throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		/*
		 * String missingField=null; //boolean isGaurdianVal=false;
		 * if(isGaurdianValid!=null) { //missingFields=isGaurdianValid.split("@@");
		 * //isGaurdianVal=Boolean.parseBoolean(missingFields[0]);
		 * missingField=missingFields[0]; }
		 */
		// List<String> generatedResidentData = generateResidents(1,
		// true,true,"Any",null,contextKey);
		List<String> generatedResidentData = generateResidents(1, true, true, "Any", missingFields, contextKey);
		JSONArray jsonArray = getTemplate(new HashSet<String>(generatedResidentData), "NEW", contextKey);
		JSONObject obj = jsonArray.getJSONObject(0);
		String templatePath = obj.get("path").toString();
		requestOtp(generatedResidentData.get(0), contextKey, parentEmailOrPhone);
		verifyOtp(generatedResidentData.get(0), contextKey, parentEmailOrPhone);
		String prid = preReg(generatedResidentData.get(0), contextKey);
		uploadDocuments(generatedResidentData.get(0), prid, contextKey);
		bookAppointment(prid, 1, contextKey, false);
		String rid = generateAndUploadPacket(prid, templatePath, contextKey);

		String url = baseUrl + props.getProperty("updateResidentUrl");

		if (withRid)
			updateResidentRid(generatedResidentData.get(0), rid);

		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put("guardian", generatedResidentData.get(0));
		residentAttrib.put("child", residentFilePath);
		jsonReq.put("PR_ResidentList", residentAttrib);
		jsonwrapper.put("requests", jsonReq);
		Response response = postReqest(url, jsonwrapper.toString(), "Update Resident Guardian");
		// assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to
		// update Resident Guardian from packet utility");
		Reporter.log("<b><u>Generated GuardianPacket with Rid: " + rid + " And linked to child </u></b>");
		if (!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");
		return rid;

	}

	public String updateResidentWithGuardianSkippingPreReg(String residentFilePath, HashMap<String, String> contextKey,
			boolean withRid, String missingFields) throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		/*
		 * String missingField=null; boolean isGaurdianVal=false; String
		 * []missingFields=null; if(isGaurdianValid!=null&&!isGaurdianValid.isEmpty()) {
		 * missingFields=isGaurdianValid.split("@@");
		 * isGaurdianVal=Boolean.parseBoolean(missingFields[0]); if(isGaurdianVal)
		 * missingField=missingFields[1]; }
		 */
		// List<String> generatedResidentData = generateResidents(1,
		// true,true,"Any",null,contextKey);
		List<String> generatedResidentData = generateResidents(1, true, true, "Any", missingFields, contextKey);
		JSONArray jsonArray = getTemplate(new HashSet<String>(generatedResidentData), "NEW", contextKey);
		JSONObject obj = jsonArray.getJSONObject(0);
		String templatePath = obj.get("path").toString();
		String rid = generateAndUploadPacketSkippingPrereg(templatePath, generatedResidentData.get(0), contextKey);

		String url = baseUrl + props.getProperty("updateResidentUrl");

		if (withRid)
			updateResidentRid(generatedResidentData.get(0), rid);

		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put("guardian", generatedResidentData.get(0));
		residentAttrib.put("child", residentFilePath);
		jsonReq.put("PR_ResidentList", residentAttrib);
		jsonwrapper.put("requests", jsonReq);
		Response response = postReqest(url, jsonwrapper.toString(), "Update Resident Guardian");
		// assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to
		// update Resident Guardian from packet utility");
		if (!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");
		Reporter.log("<b><u>Generated GuardianPacket with Rid: " + rid + " And linked to child </u></b>");
		return rid;

	}

	public String generateAndUploadPacketSkippingPrereg(String packetPath, String residentPath,
			HashMap<String, String> contextKey) throws RigInternalError {
		String url = baseUrl + "/packet/sync/0";
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(0, packetPath);
		arr.put(1, residentPath);
		jsonReq.put("personaFilePath", arr);
		Response response = postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey,
				"Generate And UploadPacket");
		JSONObject jsonResp = new JSONObject(response.getBody().asString());
		String rid = jsonResp.getJSONObject("response").getString("registrationId");
		// assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to
		// Generate And UploadPacket from packet utility");
		if (!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to Generate And UploadPacket from packet utility");
		return rid;
	}

	public String createContext(String key, String baseUrl) throws RigInternalError {
		String url = this.baseUrl + "/servercontext/" + key;
		JSONObject jsonReq = new JSONObject();
		jsonReq.put("urlBase", baseUrl);
		jsonReq.put("mosip.test.baseurl", baseUrl);
		jsonReq.put("mosip.test.regclient.machineid", E2EConstants.MACHINE_ID);
		jsonReq.put("mosip.test.regclient.centerid", E2EConstants.CENTER_ID);
		jsonReq.put("regclient.centerid", E2EConstants.CENTER_ID);
		jsonReq.put("mosip.test.regclient.userid", E2EConstants.USER_ID);
		jsonReq.put("prereg.operatorId", E2EConstants.USER_ID);
		jsonReq.put("mosip.test.regclient.password", E2EConstants.USER_PASSWD);
		jsonReq.put("prereg.password", E2EConstants.USER_PASSWD);
		jsonReq.put("mosip.test.regclient.supervisorid", E2EConstants.SUPERVISOR_ID);
		jsonReq.put("prereg.preconfiguredOtp", E2EConstants.PRECONFIGURED_OTP);
		Response response = postReqest(url, jsonReq.toString(), "SetContext");
		// Response response =
		// given().contentType(ContentType.JSON).body(jsonReq.toString()).post(url);
		if (!response.getBody().asString().toLowerCase().contains("true"))
			throw new RigInternalError("Unable to set context from packet utility");
		return response.getBody().asString();

	}

	public String updateBiometric(String resFilePath, List<String> attributeList, List<String> missAttributeList)
			throws RigInternalError {
		String url = baseUrl + props.getProperty("updatePersonaData");
		JSONObject jsonReqInner = new JSONObject();
		if (missAttributeList != null && !(missAttributeList.isEmpty()))
			jsonReqInner.put("missAttributeList", missAttributeList);
		jsonReqInner.put("personaFilePath", resFilePath);
		if (attributeList != null && !(attributeList.isEmpty()))
			jsonReqInner.put("regenAttributeList", attributeList);
		JSONArray jsonReq = new JSONArray();
		jsonReq.put(0, jsonReqInner);
		Response response = postReqest(url, jsonReq.toString(), "Update BiometricData");
		if (!response.getBody().asString().toLowerCase().contains("sucess"))
			throw new RigInternalError("Unable to update BiometricData " + attributeList + " from packet utility");
		return response.getBody().asString();

	}
	
	public String packetSync(String personaPath, HashMap<String,String> contextKey) throws RigInternalError {
		String url = baseUrl + props.getProperty("packetsyncUrl");
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(personaPath);	
		jsonReq.put("personaFilePath",arr);
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"Packet Sync:");
		if(!response.getBody().asString().toLowerCase().contains("packet has reached"))
			throw new RigInternalError("Unable to do sync packet from packet utility");
		return response.getBody().asString();
	}
	
	
}
