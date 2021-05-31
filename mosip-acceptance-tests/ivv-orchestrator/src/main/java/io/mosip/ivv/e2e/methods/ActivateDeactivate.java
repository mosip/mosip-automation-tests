package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.ActivateDeactivateHelper;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.kernel.util.KernelAuthentication;
import io.restassured.response.Response;

public class ActivateDeactivate extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(ActivateDeactivate.class);
	KernelAuthentication kernelAuthLib = new KernelAuthentication();
	ActivateDeactivateHelper helper = new ActivateDeactivateHelper();
	

	// Machine [type=machine@@value=10002@@machineSpecId=1001@@zoneCode=SAL@@status=true]
	// RegCenter [type=regcenter@@value=10001@@locationCode=14023@@zoneCode=SAL@@status=true]
	// Operator [type=operator@@value=110124@@status=true]
	// Device [type=device@@id=1001@@status=true]
	// Device-LTS [type=devicelts@@value=1001@@status=true]

	@Override
	public void run() throws RigInternalError {
		String url = null, type = null, value = null, status = null, filterColumnName = null, filterByValue = null;
		Properties prop = null;
		String endPoint = System.getProperty("env.endpoint");
		if (step.getParameters() != null && step.getParameters().size() == 1) {
			prop = PacketUtility.getParamsFromArg(step.getParameters().get(0), "@@");
			if (prop != null && !prop.isEmpty())
				type = prop.getProperty("type");
			status = prop.getProperty("status");
			if (!(type.equalsIgnoreCase("device")))
				value = prop.getProperty("value");
			else {
				for (String key : prop.stringPropertyNames()) {
					if (!key.equals("type") && !key.equals("status")) {
						filterColumnName = key;
						filterByValue = prop.getProperty(filterColumnName);
					}
				}
			}
		} else {
			throw new RigInternalError(helper.missingInputParameter());
		}

		String token = kernelAuthLib.getTokenByRole("admin");
		switch (type.toLowerCase()) {
		case E2EConstants.MACHINE:
			url = endPoint + props.getProperty("getMachine") + value + "/eng";
			Response getMachineResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get machine detail by machineId");
			if (!(getMachineResponse.getBody().asString().toLowerCase().contains("errorcode"))) {
				if (status.equals("true")) {
					String message = "Machine [" + value + "] already Activate";
					Reporter.log("<pre> <b>" + message + "</b></pre>");
					logger.error(message);
					throw new RigInternalError(message);
				}
				JSONObject jsonResp = new JSONObject(getMachineResponse.getBody().asString());
				Boolean machineStatus = packetUtility.activateDeActiveMachine(jsonResp.toString(),
						prop.getProperty("machineSpecId"), value, prop.getProperty("zoneCode"), token, status);
				if (!(machineStatus == Boolean.parseBoolean(status))) {
					String message = (status.equals("true")) ? "Activate machine" : "DeActivate machine";
					throw new RigInternalError("Unable to " + message);
				}
			} else {
				helper.deActivateMachine(prop, token);
			}
			break;

		case E2EConstants.OPERATOR:
			url = endPoint + props.getProperty("getuserDetail") + value;
			Response getOperatorResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get operator detail by userid");
			if (!(getOperatorResponse.getBody().asString().toLowerCase().contains("errorcode"))) {
				JSONObject jsonResp = new JSONObject(getOperatorResponse.getBody().asString());
				Boolean isActive = jsonResp.getJSONObject("response").getBoolean("isActive");
				if (isActive == Boolean.parseBoolean(status)) {
					String message = (isActive) ? "userid [" + value + "] is already Active"
							: "userid [" + value + "] is already De-active";
					logger.error(message);
					throw new RigInternalError(message);
				}
				helper.activateDeactivateOperator(prop, token);
			} else {
				String message = "userid :[" + value + "] not found";
				logger.error(message);
				throw new RigInternalError(message);
			}
			break;

		case E2EConstants.PARTNER:
			token = kernelAuthLib.getTokenByRole("partner");
			url = endPoint + props.getProperty("getPartner") + value;
			Response getPartnerResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get partner detail by partnerid");
			if (getPartnerResponse.getBody().asString().toLowerCase().contains("errorcode")) {
				String message = "partnerid :[" + value + "] not found";
				logger.error(message);
				throw new RigInternalError(message);
			} else {
				JSONObject jsonpartnerReq = packetUtility.updatePartnerRequestBuilder(status);
				Response patchPartnerResponse = packetUtility.patchReqestWithCookiesAndBody(url,
						jsonpartnerReq.toString(), token, "ActivateDeactivatePartner");
				if (patchPartnerResponse.getBody().asString().toLowerCase().contains("errorcode")) {
					logger.error("unable to [" + status + "] partner");
					throw new RigInternalError("unable to [" + status + "] partner");
				}
			}
			break;

		case E2EConstants.REGCENTER:
			url = endPoint + props.getProperty("getRegistrationCenter") + value + "/eng";
			Response getregCenterResponse = packetUtility.getRequestWithCookiesAndPathParam(url, token,
					"Get RegCenter detail by RegCenterId");
			if (!getregCenterResponse.getBody().asString().toLowerCase().contains("errorcode")) {
				if (status.equals("true")) {
					String message = "RegCenter [" + value + "] already Active";
					Reporter.log("<pre> <b>" + message + "</b></pre>");
					logger.error(message);
					throw new RigInternalError(message);
				}
				JSONObject jsonRegcenterResp = new JSONObject(getregCenterResponse.getBody().asString());
				Boolean regCenterStatus = packetUtility.activateDeActiveRegCenter(jsonRegcenterResp.toString(), value,
						prop.getProperty("locationCode"), prop.getProperty("zoneCode"), token, status);
				if (!(regCenterStatus == Boolean.parseBoolean(status))) {
					String message = (status.equals("true")) ? "Activate RegCenter" : "DeActivate RegCenter";
					throw new RigInternalError("Unable to " + message);
				}
			} else {
				helper.deActivateRegCenter(prop, token);
			}
			break;
			
		case E2EConstants.DEVICE:
			JSONObject jsonResp = helper.filterRecordByColumnName(filterColumnName, filterByValue,
					Boolean.valueOf(status), token);
			String deiceJsonReq = helper.buildDeviceRequest(jsonResp, Boolean.valueOf(status));
			url = endPoint + props.getProperty("devices");
			Response deviceResponse = packetUtility.putReqestWithCookiesAndBody(url, deiceJsonReq, token,
					"Device activate/deactivate");
			if (deviceResponse.getBody().asString().toLowerCase().contains("errorcode"))
				throw new RigInternalError("Failed to activate/deactivate device");
			break;
			
		case E2EConstants.DEVICElTS:
			url = endPoint + props.getProperty("devices");
			HashMap<String, String> queryParam = new HashMap<>();
			queryParam.put("id", value);
			queryParam.put("isActive", status);
			Response patchResponse = packetUtility.patchRequestWithQueryParm(url, queryParam, token,
					"Activate/Deactivate Device id: " + value);
			if (!patchResponse.getBody().asString().toLowerCase().contains("status updated successfully for devices"))
				throw new RigInternalError(
						(Boolean.valueOf(status)) ? "Unable to Activate device" : "Unable to Deactivate Device");
			break;
		default:
			throw new RigInternalError(
					type + "is not supported only allowed [machine/operator/partner/device/regcenter]");
		}
	}

}
