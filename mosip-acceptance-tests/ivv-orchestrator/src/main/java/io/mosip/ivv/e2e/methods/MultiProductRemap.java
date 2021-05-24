package io.mosip.ivv.e2e.methods;

import static io.restassured.RestAssured.given;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.kernel.util.KernelAuthentication;
import io.restassured.response.Response;

public class MultiProductRemap extends BaseTestCaseUtil implements StepInterface {
	
	KernelAuthentication kernelAuthLib = new KernelAuthentication();
	Logger logger = Logger.getLogger(MultiProductRemap.class);
	
	@Override
	public void run() throws RigInternalError {
		String type = "machine";
		String value = "10002";
		String regCenterId = null;
		String zoneCode = null;
		String message = null;
		
		if (step.getParameters() == null || step.getParameters().isEmpty() ||step.getParameters().size()<3) {
			logger.warn("arugemnt is  missing  please pass the argument from DSL sheet");
		} else {
			type=step.getParameters().get(0);
			value=step.getParameters().get(1);
			regCenterId = step.getParameters().get(2);
			zoneCode = step.getParameters().get(3);
			
		}
		String token = kernelAuthLib.getTokenByRole("admin");
		
		switch (type) {
			case "machine":
				String GETMACHINEURL = System.getProperty("env.endpoint") + props.getProperty("getMachineToRemap") + value + "/eng";
				Response responseMachine = packetUtility.getRequestWithCookiesAndPathParam(GETMACHINEURL, token,
						"Get machine detail by machineId");
				if (responseMachine.getBody().asString().toLowerCase().contains("errorcode")) {
					logger.error("machineId :[" + value + "] not found");
					throw new RigInternalError("machineId :[" + value + "] not found");
				}
				JSONObject jsonRespMachine = new JSONObject(responseMachine.getBody().asString());
				String machineID = packetUtility.remapMachine(jsonRespMachine.toString(), token, regCenterId, zoneCode);
				if (machineID != null)
					message = machineID.equals(value) ? "Reampped "+type : "Remap Fail "+type;
				else
					throw new RigInternalError("Unable to " + message);
			break;
				
			case "user":
				String GETUSERURL = System.getProperty("env.endpoint") + props.getProperty("getUserToRemap") + value;
				Response responseUser = packetUtility.getRequestWithCookiesAndPathParam(GETUSERURL, token,
						"Get machine detail by machineId");
				if (responseUser.getBody().asString().toLowerCase().contains("errorcode")) {
					logger.error("machineId :[" + value + "] not found");
					throw new RigInternalError("machineId :[" + value + "] not found");
				}
				JSONObject jsonRespUser = new JSONObject(responseUser.getBody().asString());
				Boolean userStatus = packetUtility.remapUser(jsonRespUser.toString(), token, value, regCenterId, zoneCode);
				if (userStatus != null)
					message = userStatus ? "Reampped "+type : "Remap Fail "+type;
				else
					throw new RigInternalError("Unable to " + message);
			break;
		}
	}
	
	
	private static Response getRequestWithPathParam(String url, String token) {
		Response getResponse = given().relaxedHTTPSValidation().cookie("Authorization", token).log().all().when()
				.get(url).then().log().all().extract().response();
		return getResponse;
	}

}
