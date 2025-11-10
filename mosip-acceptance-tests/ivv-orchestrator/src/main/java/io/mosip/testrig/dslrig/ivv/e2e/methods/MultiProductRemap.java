package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileReader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class MultiProductRemap extends BaseTestCaseUtil implements StepInterface {

	KernelAuthentication kernelAuthLib = new KernelAuthentication();
	private static final Logger logger = Logger.getLogger(MultiProductRemap.class);
	String GETREQBODYDEVICEPATH = "src/main/resources/kernel/Device/Search.json";
	
	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String type = "machine";
		String[] arrayValue = null;
		String value = null;
		String regCenterId = null;
		String zoneCode = null;
		String message = null;

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 3) {
			logger.warn("arugemnt is  missing  please pass the argument from DSL sheet");
		} else {
			type = step.getParameters().get(0);

			if (step.getParameters().get(1).split("=").length > 1) {
				arrayValue = step.getParameters().get(1).split("=");
			} else {
				value = step.getParameters().get(1);
			}
			regCenterId = step.getParameters().get(2);
			zoneCode = step.getParameters().get(3);

		}
		String token = kernelAuthLib.getTokenByRole("admin");

		switch (type) {
		case "machine":
			String GETMACHINEURL = System.getProperty("env.endpoint") + props.getProperty("getMachineToRemap") + value
					+ "/eng";
			Response responseMachine = packetUtility.getRequestWithCookiesAndPathParam(GETMACHINEURL, token,
					"Get machine detail by machineId");
			if (responseMachine.getBody().asString().toLowerCase().contains("errorcode")) {
				this.hasError = true;
				logger.error("machineId :[" + value + "] not found");
				throw new RigInternalError("machineId :[" + value + "] not found");
			}
			JSONObject jsonRespMachine = new JSONObject(responseMachine.getBody().asString());
			String machineID = packetUtility.remapMachine(jsonRespMachine.toString(), token, regCenterId, zoneCode);
			if (machineID != null)
				message = machineID.equals(value) ? "Reampped " + type : "Remap Fail " + type;
			else {
				this.hasError = true;
				throw new RigInternalError("Unable to " + message);
			}

			break;

		case "user":
			String GETUSERURL = System.getProperty("env.endpoint") + props.getProperty("getUserToRemap") + value;
			Response responseUser = packetUtility.getRequestWithCookiesAndPathParam(GETUSERURL, token,
					"Get machine detail by machineId");
			if (responseUser.getBody().asString().toLowerCase().contains("errorcode")) {
				this.hasError = true;
				logger.error("machineId :[" + value + "] not found");
				throw new RigInternalError("machineId :[" + value + "] not found");
			}
			JSONObject jsonRespUser = new JSONObject(responseUser.getBody().asString());
			Boolean userStatus = packetUtility.remapUser(jsonRespUser.toString(), token, value, regCenterId, zoneCode,
					step);
			if (userStatus != null)
				message = userStatus ? "Reampped " + type : "Remap Fail " + type;
			else {
				this.hasError = true;
				throw new RigInternalError("Unable to " + message);
			}
			break;

		case "device":
			String getDeviceRequestBody = null;
			String GETDEVICEURL = System.getProperty("env.endpoint") + props.getProperty("getDeviceToRemap");
			JSONParser jsonParser = new JSONParser();
			try {
				File file = new File(GETREQBODYDEVICEPATH);
				FileReader reader = new FileReader(file);
				Object obj = jsonParser.parse(reader);
				org.json.simple.JSONObject jsonRequestInput = (org.json.simple.JSONObject) obj;
				if (arrayValue.length > 0) {
					getDeviceRequestBody = JsonPrecondtion.parseAndReturnJsonContent(
							JSONValue.toJSONString(jsonRequestInput), arrayValue[0], "request.(filters)[0].columnName");
					getDeviceRequestBody = JsonPrecondtion.parseAndReturnJsonContent(getDeviceRequestBody,
							arrayValue[1], "request.(filters)[0].value");

				}

				Response responseDevice = packetUtility.postReqestWithCookiesAndBody(GETDEVICEURL, getDeviceRequestBody,
						token, "Get device detail by deviceId");

				if (responseDevice.getBody().asString().toLowerCase().contains("errorcode")) {
					logger.error("deviceId :[" + arrayValue[1] + "] not found");
					throw new RigInternalError("deviceId :[" + arrayValue[1] + "] not found");
				}
				JSONObject jsonRespDevice = new JSONObject(responseDevice.getBody().asString());
				Boolean deviceStatus = packetUtility.remapDevice(jsonRespDevice.toString(), token, arrayValue[1],
						regCenterId, zoneCode, step);
				if (deviceStatus != null)
					message = deviceStatus ? "Reampped " + type : "Remap Fail " + type;
				else {
					this.hasError = true;
					throw new RigInternalError("Unable to " + message);
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}

			break;
		}
	}

	private static Response getRequestWithPathParam(String url, String token) {
		Response getResponse = given().relaxedHTTPSValidation().cookie("Authorization", token).log().all().when()
				.get(url).then().log().all().extract().response();
		return getResponse;
	}

}
