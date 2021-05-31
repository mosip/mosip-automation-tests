package io.mosip.ivv.orchestrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.Reporter;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.restassured.response.Response;

public class ActivateDeactivateHelper extends BaseTestCaseUtil {
	Logger logger = Logger.getLogger(ActivateDeactivateHelper.class);
	
	public void deActivateRegCenter(Properties prop, String token) throws RigInternalError {
		String status = prop.getProperty("status");
		String value = prop.getProperty("value");
		if (status.equals("false")) {
			String message = "RegCenter [" + value + "] already De-Activate";
			Reporter.log("<pre> <b>" + message + "</b></pre>");
			throw new RigInternalError(message);
		}
		Reporter.log("<pre> <b> assuming  RegCenter is de-active [isActive=false] </b></pre>");
		String url = System.getProperty("env.endpoint") + props.getProperty("getRegistrationCenter");
		JSONObject jsonregCenterReq = packetUtility.regCenterPutrequestBuilder(null, value,
				prop.getProperty("locationCode"), prop.getProperty("zoneCode"), status);
		Response putResponse = packetUtility.putReqestWithCookiesAndBody(url, jsonregCenterReq.toString(), token,
				"Update RegCenter details with status[isActive=" + status + "]");
		if (putResponse.getBody().asString().toLowerCase().contains("errorcode")) {
			throw new RigInternalError("unable to update RegCenter detail");
		}
		JSONObject jsonResp = new JSONObject(putResponse.getBody().asString());
		Boolean regCenterStatus = jsonResp.getJSONObject("response").getBoolean("isActive");
		if (!(regCenterStatus == Boolean.parseBoolean(status))) {
			String message = (status.equals("true")) ? "Activate RegCenter" : "DeActivate RegCenter";
			throw new RigInternalError("Unable to " + message);
		}
	}

	public void activateDeactivateOperator(Properties prop, String token) throws RigInternalError {
		HashMap<String, String> queryparam = new HashMap<>();
		queryparam.put("id", prop.getProperty("value"));
		queryparam.put("isActive", prop.getProperty("status"));
		String url = System.getProperty("env.endpoint") + props.getProperty("usercentermapping");
		Response patchUserResponse = packetUtility.patchRequestWithQueryParm(url, queryparam, token,
				"UpdateUserRegCenterStatus set [isActive=" + prop.getProperty("status") + "]");
		JSONObject patchJsonResp = new JSONObject(patchUserResponse.getBody().asString());
		String responseStatus = patchJsonResp.getJSONObject("response").getString("status");
		if ((responseStatus.equals("Status updated successfully for User"))) {
			throw new RigInternalError("unable to update userid :[" + prop.getProperty("value") + "]");
		}
	}

	// assuming that machineid passed is valid as a response is does not have detail
	// json.we assume that machine is de-active
	public void deActivateMachine(Properties prop, String token) throws RigInternalError {
		String status = prop.getProperty("status");
		String value = prop.getProperty("value");
		Reporter.log("<pre> <b> assuming  machine is de-active [isActive=false] </b></pre>");
		if (status.equals("false")) {
			String message = "Machine [" + value + "] already De-Activate";
			Reporter.log("<pre> <b>" + message + "</b></pre>");
			throw new RigInternalError(message);
		}
		JSONObject jsonPutReq = packetUtility.machineRequestBuilder(null, prop.getProperty("machineSpecId"), value,
				prop.getProperty("zoneCode"), status);
		Boolean machineStatus = packetUtility.updateMachineDetail(jsonPutReq, token, status);
		if (!(machineStatus == Boolean.parseBoolean(status))) {
			String message = (status.equals("true")) ? "Activate machine" : "DeActivate machine";
			throw new RigInternalError("Unable to " + message);
		}
	}

	public String missingInputParameter() {
		StringBuilder builder = new StringBuilder();
		builder.append("input argument is missing for [machine/regcenter/Operator/Device] :").append(
				"\n\t Machine parameter [type=machine@@value=10002@@machineSpecId=1001@@zoneCode=SAL@@status=true]")
				.append(" \n\t RegCenter parameter [type=regcenter@@value=10001@@locationCode=14023@@zoneCode=SAL@@status=true]")
				.append("\n\t Operator parameter [type=operator@@value=110124@@status=true]")
				.append("\n\t Device [type=device@@filterCoumnName=filterbyValue@@status=true]");
		return builder.toString();
	}
	
	//Activate/DeActivate Device--- start
	public JSONObject filterRecordByColumnName(String columnName, String value, boolean isActive, String token)
			throws RigInternalError {
		String searchJsonRequest = "kernel/Device/Search.json";
		JSONObject jsonResp = null;
		try {
			InputStream inputStream = new FileInputStream(
					new File(TestResources.getResourcePath() + searchJsonRequest).getAbsoluteFile());
			JSONTokener tokener = new JSONTokener(inputStream);
			JSONObject jsonObj = new JSONObject(tokener);
			String json = jsonObj.toString();
			json = JsonPrecondtion.parseAndReturnJsonContent(json, columnName, "request.(filters)[0].columnName");
			json = JsonPrecondtion.parseAndReturnJsonContent(json, value, "request.(filters)[0].value");
			String url = System.getProperty("env.endpoint") + props.getProperty("deviceSearch");
			Response filterResponse = packetUtility.postReqestWithCookiesAndBody(url, json, token,
					"Filter/Search device by columnName :" + columnName + " value: " + value);
			if (filterResponse.getBody().asString().toLowerCase().contains("errorcode"))
				throw new RigInternalError("Failed to fetch the device details");
			jsonResp = new JSONObject(filterResponse.getBody().asString());
			String reponseisActive = JsonPrecondtion.getValueFromJson(jsonResp.toString(),
					"response.(data)[0].isActive");
			if (reponseisActive.equals(String.valueOf(isActive))) {
				String message = (isActive) ? "Device is already active" : "Device is already de-active";
				throw new RigInternalError(message);
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		}
		return jsonResp;
	}

	public String buildDeviceRequest(JSONObject jsonResp, Boolean isActiveFlag) {
		String deviceFilePath = "kernel/Device/device.json";
		String json = null;
		try {
			InputStream inputStream = new FileInputStream(
					new File(TestResources.getResourcePath() + deviceFilePath).getAbsoluteFile());
			JSONTokener tokener = new JSONTokener(inputStream);
			JSONObject jsonObj = new JSONObject(tokener);
			json = jsonObj.toString();
			json = JsonPrecondtion.parseAndReturnJsonContent(json,
					JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(data)[0].id"), "request.id");
			json = JsonPrecondtion.parseAndReturnJsonContent(json,
					JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(data)[0].deviceSpecId"),
					"request.deviceSpecId");
			json = JsonPrecondtion.parseAndReturnJsonContent(json, "BOOLEAN:"+isActiveFlag, "request.isActive");
			json = JsonPrecondtion.parseAndReturnJsonContent(json,
					JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(data)[0].macAddress"),
					"request.macAddress");
			json = JsonPrecondtion.parseAndReturnJsonContent(json,
					JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(data)[0].name"), "request.name");
			json = JsonPrecondtion.parseAndReturnJsonContent(json,
					JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(data)[0].regCenterId"),
					"request.regCenterId");
			json = JsonPrecondtion.parseAndReturnJsonContent(json,
					JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(data)[0].serialNum"),
					"request.serialNum");
			json = JsonPrecondtion.parseAndReturnJsonContent(json,
					JsonPrecondtion.getValueFromJson(jsonResp.toString(), "response.(data)[0].zoneCode"),
					"request.zoneCode");
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		}

		return json;

	}

	//Activate/DeActivate Device--- end
}
