package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.kernel.util.KernelAuthentication;
import io.restassured.response.Response;

public class ActivateDeactivate extends BaseTestCaseUtil implements StepInterface {
	KernelAuthentication kernelAuthLib = new KernelAuthentication();
	Logger logger = Logger.getLogger(ActivateDeactivate.class);

	// Machine [type=machine@@value=10002@@machineSpecId=1001@@zoneCode=SAL@@status=true]
	// RegCenter [type=regcenter@@value=10001@@locationCode=14023@@zoneCode=SAL@@status=true]
	// Operator [type=operator@@value=110124@@status=true]
	
	@Override
	public void run() throws RigInternalError {
		String url = null;
		HashMap<String, String> map = new HashMap<>();
		if (step.getParameters()!=null && step.getParameters().size() == 1) {
			Properties props = PacketUtility.getParamsFromArg(step.getParameters().get(0), "@@");
			for (final String key : props.stringPropertyNames())
				map.put(key, props.getProperty(key));
		} else {
			logger.error("input argument is missing");
			throw new RigInternalError("input argument is missing");
		}
		String token = kernelAuthLib.getTokenByRole("admin");
		switch (map.get("type").toLowerCase()) {
		case E2EConstants.MACHINE:
			url = System.getProperty("env.endpoint") + props.getProperty("getMachine") + map.get("value") + "/eng";
			Response getMachineResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get machine detail by machineId");
			if (!(getMachineResponse.getBody().asString().toLowerCase().contains("errorcode"))) {
				if (map.get("status").equals("true")) {
					Reporter.log("<pre> <b>Machine [" + map.get("value") + "] already Activate</b></pre>");
					logger.error("Machine [" + map.get("value") + "] already Activate");
					throw new RigInternalError("Machine [" + map.get("value") + "] already Activate");
				}
				JSONObject jsonResp = new JSONObject(getMachineResponse.getBody().asString());
				Boolean machineStatus = packetUtility.activateDeActiveMachine(jsonResp.toString(),
						map.get("machineSpecId"), map.get("value"), map.get("zoneCode"), token, map.get("status"));
				if (!(machineStatus == Boolean.parseBoolean(map.get("status")))) {
					String message = (map.get("status").equals("true")) ? "Activate machine" : "DeActivate machine";
					throw new RigInternalError("Unable to " + message);
				}
			} else {
				// assuming that mahineid passed is valid as a response is does not have detail
				// json.we assume that machine is de-active
				Reporter.log("<pre> <b> assuming  machine is de-active [isActive=false] </b></pre>");
				if (map.get("status").equals("false")) {
					Reporter.log("<pre> <b>Machine [" + map.get("value") + "] already De-Activate</b></pre>");
					logger.error("Machine [" + map.get("value") + "] already De-Activate");
					throw new RigInternalError("Machine [" + map.get("value") + "] already De-Activate");
				}
				JSONObject jsonPutReq = packetUtility.machineRequestBuilder(null, map.get("machineSpecId"),
						map.get("value"), map.get("zoneCode"), map.get("status"));
				Boolean machineStatus = packetUtility.updateMachineDetail(jsonPutReq, token,map.get("status"));
				if (!(machineStatus == Boolean.parseBoolean(map.get("status")))) {
					String message = (map.get("status").equals("true")) ? "Activate machine" : "DeActivate machine";
					throw new RigInternalError("Unable to " + message);
				}
			}
			break;

		case E2EConstants.OPERATOR:
			url = System.getProperty("env.endpoint") + props.getProperty("getuserDetail") + map.get("value");
			Response getOperatorResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get operator detail by userid");
			if (!(getOperatorResponse.getBody().asString().toLowerCase().contains("errorcode"))) {
				JSONObject jsonResp = new JSONObject(getOperatorResponse.getBody().asString());
				Boolean isActive = jsonResp.getJSONObject("response").getBoolean("isActive");
				if (isActive == Boolean.parseBoolean(map.get("status"))) {
					String message = (isActive) ? "userid [" + map.get("value") + "] is already Active"
							: "userid [" + map.get("value") + "] is already De-active";
					logger.error(message);
					throw new RigInternalError(message);
				}
				HashMap<String, String> queryparam = new HashMap<>();
				queryparam.put("id", map.get("value"));
				queryparam.put("isActive", map.get("status"));
				url = System.getProperty("env.endpoint") + props.getProperty("usercentermapping");
				Response patchUserResponse = packetUtility.patchRequestWithQueryParm(url, queryparam, token,
						"UpdateUserRegCenterStatus set [isActive="+map.get("status")+"]");
				JSONObject patchJsonResp = new JSONObject(patchUserResponse.getBody().asString());
				String responseStatus = patchJsonResp.getJSONObject("response").getString("status");
				if ((responseStatus.equals("Status updated successfully for User"))) {
					logger.error("unable to update userid :[" + map.get("value") + "]");
					throw new RigInternalError("unable to update userid :[" + map.get("value") + "]");
				}
			} else {
				logger.error("userid :[" + map.get("value") + "] not found");
				throw new RigInternalError("userid :[" + map.get("value") + "] not found");
			}
			break;

		case E2EConstants.PARTNER:
			token = kernelAuthLib.getTokenByRole("partner");
			url = System.getProperty("env.endpoint") + props.getProperty("getPartner") + map.get("value");
			Response getPartnerResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get partner detail by partnerid");
			if (getPartnerResponse.getBody().asString().toLowerCase().contains("errorcode")) {
				logger.error("partnerid :[" + map.get("value") + "] not found");
				throw new RigInternalError("partnerid :[" + map.get("value") + "] not found");
			} else {
				JSONObject jsonpartnerReq = packetUtility.updatePartnerRequestBuilder(map.get("status"));
				Response patchPartnerResponse = packetUtility.patchReqestWithCookiesAndBody(url,
						jsonpartnerReq.toString(), token, "ActivateDeactivatePartner");
				if (patchPartnerResponse.getBody().asString().toLowerCase().contains("errorcode")) {
					logger.error("unable to [" + map.get("status") + "] partner");
					throw new RigInternalError("unable to [" + map.get("status") + "] partner");
				}
			}
			break;

		case E2EConstants.REGCENTER:
			url = System.getProperty("env.endpoint") + props.getProperty("getRegistrationCenter") + map.get("value")
					+ "/eng";
			Response getregCenterResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get RegCenter detail by RegCenterId");
			if (!getregCenterResponse.getBody().asString().toLowerCase().contains("errorcode")) {
				if (map.get("status").equals("true")) {
					Reporter.log("<pre> <b>RegCenter [" + map.get("value") + "] already Active</b></pre>");
					logger.error("RegCenter [" + map.get("value") + "] already Active");
					throw new RigInternalError("RegCenter [" + map.get("value") + "] already Active");
				}
				JSONObject jsonRegcenterResp = new JSONObject(getregCenterResponse.getBody().asString());
				Boolean regCenterStatus = packetUtility.activateDeActiveRegCenter(jsonRegcenterResp.toString(),
						map.get("value"), map.get("locationCode"), map.get("zoneCode"), token, map.get("status"));
				if (!(regCenterStatus == Boolean.parseBoolean(map.get("status")))) {
					String message = (map.get("status").equals("true")) ? "Activate RegCenter" : "DeActivate RegCenter";
					throw new RigInternalError("Unable to " + message);
				}
			} else {
				
				if (map.get("status").equals("false")) {
					Reporter.log("<pre> <b>RegCenter [" + map.get("value") + "] already De-Activate</b></pre>");
					logger.error("RegCenter [" + map.get("value") + "] already De-Activate");
					throw new RigInternalError("RegCenter [" + map.get("value") + "] already De-Activate");
				}
				Reporter.log("<pre> <b> assuming  RegCenter is de-active [isActive=false] </b></pre>");
				url = System.getProperty("env.endpoint") + props.getProperty("getRegistrationCenter");
				JSONObject jsonregCenterReq = packetUtility.regCenterPutrequestBuilder(null, map.get("value"),
						map.get("locationCode"), map.get("zoneCode"), map.get("status"));
				Response putResponse = packetUtility.putReqestWithCookiesAndBody(url, jsonregCenterReq.toString(),
						token, "Update RegCenter details with status[isActive="+map.get("status")+"]");
				if (putResponse.getBody().asString().toLowerCase().contains("errorcode")) {
					logger.error("unable to update RegCenter detail");
					throw new RigInternalError("unable to update RegCenter detail");
				}
				JSONObject jsonResp = new JSONObject(putResponse.getBody().asString());
				Boolean regCenterStatus = jsonResp.getJSONObject("response").getBoolean("isActive");
				if (!(regCenterStatus == Boolean.parseBoolean(map.get("status")))) {
					String message = (map.get("status").equals("true")) ? "Activate RegCenter" : "DeActivate RegCenter";
					throw new RigInternalError("Unable to " + message);
				}
			}
			break;

		default:
			throw new RigInternalError(
					map.get("type") + "is not supported only allowed [machine/operator/partner/device/regcenter]");
		}
	}

}
