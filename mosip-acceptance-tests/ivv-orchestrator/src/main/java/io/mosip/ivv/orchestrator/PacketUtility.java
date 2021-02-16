package io.mosip.ivv.orchestrator;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertTrue;
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
import io.mosip.ivv.e2e.methods.CheckStatus;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class PacketUtility extends BaseTestCaseUtil {
	
	public List<String> generateResidents(int n, Boolean bAdult, Boolean bSkipGuardian, String gender,HashMap<String,String> contextKey) throws RigInternalError {
		
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
		jsonReq.put("PR_ResidentAttribute", residentAttrib);
		jsonwrapper.put("requests", jsonReq);

		//Response response = postReqest(url, jsonwrapper.toString(), "GENERATE_RESIDENTS_DATA");
		Response response = postRequestWithQueryParamAndBody(url, jsonwrapper.toString(),contextKey ,"GENERATE_RESIDENTS_DATA");
		//assertTrue(response.getBody().asString().contains("SUCCESS"),"Unable to get residentData from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to get residentData from packet utility");
		//assertTrue(response.getBody().asString().contains("Failure"),"Unable to get residentData from packet utility");
		JSONArray resp = new JSONObject(response.getBody().asString()).getJSONArray("response");
		List<String> residentPaths= new ArrayList<>();
		for (int i = 0; i < resp.length(); i++) {
			JSONObject obj = resp.getJSONObject(i);
			String resFilePath = obj.get("path").toString();
			residentPaths.add(resFilePath);
			//residentTemplatePaths.put(resFilePath, null);
		}
		return residentPaths;

	}
	
	public JSONArray getTemplate(Set<String> resPath,String process,HashMap<String,String> contextKey) throws RigInternalError {
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		for (String residentPath : resPath) {

			arr.put(residentPath);
		}
		jsonReq.put("personaFilePath", arr);
		String url = baseUrl + props.getProperty("getTemplateUrl") + process + "/ /";
		//Response templateResponse = postReqest(url, jsonReq.toString(), "GET-TEMPLATE");
		Response templateResponse = postRequestWithQueryParamAndBody(url, jsonReq.toString(), contextKey, "GET-TEMPLATE");
		JSONObject jsonResponse = new JSONObject(templateResponse.asString());
		JSONArray resp = jsonResponse.getJSONArray("packets");
		if((resp.length()<=0))
			throw new RigInternalError("Unable to get Template from packet utility");
		return resp;
	}
	
	public  void requestOtp(String resFilePath,HashMap<String,String> contextKey){
		String url = baseUrl+props.getProperty("sendOtpUrl");
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		//postReqest(url,jsonReq.toString(),"Send Otp");
		postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"Send Otp");

	}
	
	public  void verifyOtp(String resFilePath,HashMap<String,String> contextKey) throws RigInternalError{
		String url = baseUrl+props.getProperty("verifyOtpUrl");
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		//Response response =postReqest(url,jsonReq.toString(),"Verify Otp");
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"Verify Otp");
		//assertTrue(response.getBody().asString().contains("VALIDATION_SUCCESSFUL"),"Unable to Verify Otp from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("validation_successful"))
			throw new RigInternalError("Unable to Verify Otp from packet utility");

	}
	
	public  String preReg(String resFilePath,HashMap<String,String> contextKey) throws RigInternalError{
		String url = baseUrl+props.getProperty("preregisterUrl");
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		//Response response =postReqest(url,jsonReq.toString(),"AddApplication");
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"AddApplication");
		String prid = response.getBody().asString();
		//assertTrue((int)prid.charAt(0)>47 && (int)prid.charAt(0)<58 ,"Unable to pre-register from packet utility");
		if(!((int)prid.charAt(0)>47 && (int)prid.charAt(0)<58))
			throw new RigInternalError("Unable to pre-register using packet utility");
		return prid;

	}
	public  void uploadDocuments(String resFilePath, String prid,HashMap<String,String> contextKey){
		String url = baseUrl + "/documents/"+ prid;
		JSONObject jsonReq = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(resFilePath);
		jsonReq.put("personaFilePath", jsonArray);
		//postReqest(url,jsonReq.toString(),"Upload Documents");
		postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"Upload Documents");
	}
	
	public  void bookAppointment(String prid, int nthSlot,HashMap<String,String> contextKey) throws RigInternalError{
		String url = baseUrl + "/bookappointment/"+ prid + "/" + nthSlot;
		JSONObject jsonReq = new JSONObject();
		//Response response =postReqest(url,jsonReq.toString(),"BookAppointment");
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"BookAppointment");
		//assertTrue(response.getBody().asString().contains("Appointment booked successfully") ,"Unable to BookAppointment from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("appointment booked successfully"))
			throw new RigInternalError("Unable to BookAppointment from packet utility");
	}
	
	public  String generateAndUploadPacket(String prid, String packetPath,HashMap<String,String> contextKey) throws RigInternalError{
		String url = baseUrl + "/packet/sync/"+ prid ;
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(packetPath);	
		jsonReq.put("personaFilePath",arr);
		//Response response =postReqest(url,jsonReq.toString(),"Generate And UploadPacket");
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"Generate And UploadPacket");
		JSONObject jsonResp = new JSONObject(response.getBody().asString());
		String rid = jsonResp.getJSONObject("response").getString("registrationId");
		//assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to Generate And UploadPacket from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to Generate And UploadPacket from packet utility");
		return rid;
	}
	
	public  String updateResidentGuardian(String residentFilePath, boolean withRid) throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		List<String> generatedResidentData = generateResidents(1, true,true,"Any",contextKey);
		JSONArray jsonArray=getTemplate(new HashSet<String>(generatedResidentData), "NEW",contextKey);
		JSONObject obj = jsonArray.getJSONObject(0);
		String templatePath = obj.get("path").toString();
		requestOtp(generatedResidentData.get(0),contextKey);
		verifyOtp(generatedResidentData.get(0),contextKey);
		String prid=preReg(generatedResidentData.get(0),contextKey);
		uploadDocuments(generatedResidentData.get(0), prid,contextKey);
		bookAppointment(prid, 1,contextKey);
		String rid=generateAndUploadPacket(prid, templatePath,contextKey);
		// call Dsl step wait her sleep for 2 minute
		String url = null;
		if(withRid)
			url = baseUrl+props.getProperty("updateResidentUrl")+"?RID="+rid;
		else url = baseUrl+props.getProperty("updateResidentUrl");
		
		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put("guardian", generatedResidentData.get(0));
		residentAttrib.put("child", residentFilePath);
		jsonReq.put("PR_ResidentList", residentAttrib);
		jsonwrapper.put("requests", jsonReq);
		Response response =postReqest(url,jsonwrapper.toString(),"Update Resident Guardian");
		//assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to update Resident Guardian from packet utility");
		Reporter.log("<b><u>Generated GuardianPacket with Rid: "+rid+" And linked to child </u></b>");
		if(!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");
		return rid;
		
	}
	public  String updateResidentWithGuardianSkippingPreReg(String residentFilePath,HashMap<String,String> contextKey, boolean withRid) throws RigInternalError {
		Reporter.log("<b><u>Execution Steps for Generating GuardianPacket And linking with Child Resident: </u></b>");
		List<String> generatedResidentData = generateResidents(1, true,true,"Any",contextKey);
		JSONArray jsonArray=getTemplate(new HashSet<String>(generatedResidentData), "NEW",contextKey);
		JSONObject obj = jsonArray.getJSONObject(0);
		String templatePath = obj.get("path").toString();
		String rid=generateAndUploadPacketSkippingPrereg(templatePath,generatedResidentData.get(0),contextKey);
		// call Dsl step wait her sleep for 2 minute
		String url = null;
		if(withRid)
			url = baseUrl+props.getProperty("updateResidentUrl")+"?RID="+rid;
		else url = baseUrl+props.getProperty("updateResidentUrl");
		
		JSONObject jsonwrapper = new JSONObject();
		JSONObject jsonReq = new JSONObject();
		JSONObject residentAttrib = new JSONObject();
		residentAttrib.put("guardian", generatedResidentData.get(0));
		residentAttrib.put("child", residentFilePath);
		jsonReq.put("PR_ResidentList", residentAttrib);
		jsonwrapper.put("requests", jsonReq);
		Response response =postReqest(url,jsonwrapper.toString(),"Update Resident Guardian");
		//assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to update Resident Guardian from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("success"))
			throw new RigInternalError("Unable to update Resident Guardian from packet utility");
		Reporter.log("<b><u>Generated GuardianPacket with Rid: "+rid+" And linked to child </u></b>");
		return rid;
		
	}
	
	public  String generateAndUploadPacketSkippingPrereg(String packetPath, String residentPath,HashMap<String,String> contextKey) throws RigInternalError{
		String url = baseUrl + "/packet/sync/0" ;
		JSONObject jsonReq = new JSONObject();
		JSONArray arr = new JSONArray();
		arr.put(0,packetPath);	
		arr.put(1,residentPath);
		jsonReq.put("personaFilePath",arr);
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextKey,"Generate And UploadPacket");
		JSONObject jsonResp = new JSONObject(response.getBody().asString());
		String rid = jsonResp.getJSONObject("response").getString("registrationId");
		//assertTrue(response.getBody().asString().contains("SUCCESS") ,"Unable to Generate And UploadPacket from packet utility");
		if(!response.getBody().asString().toLowerCase().contains("success"))
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
		Response response =postReqest(url,jsonReq.toString(),"SetContext");
		//Response response = given().contentType(ContentType.JSON).body(jsonReq.toString()).post(url);
		if(!response.getBody().asString().toLowerCase().contains("true"))
			throw new RigInternalError("Unable to set context from packet utility");
		return response.getBody().asString();

	}
	
	
}
