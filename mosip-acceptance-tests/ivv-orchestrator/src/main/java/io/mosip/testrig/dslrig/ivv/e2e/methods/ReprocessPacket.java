package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class ReprocessPacket extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(ReprocessPacket.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
	    String rid = null;

	    if (step.getParameters().size() >= 1) {
	        rid = step.getScenario().getVariables().get(step.getParameters().get(0));
	    }

	    Map<String, Object> registration = getRegistrationRecord(rid);
	    String workflowInstanceId = (String) registration.get("workflow_instance_id");
	    // process column holds NEW / UPDATE / LOST ΓÇö required by securezone notification as reg_type
	    String regType = registration.get("process") != null ? registration.get("process").toString() : null;
	    if (regType == null || regType.isBlank()) {
	        throw new RigInternalError("reg_type/process not found in regprc.registration for rid=" + rid);
	    }

	    JSONObject jsonReq = new JSONObject();
	    jsonReq.put("rid", rid);
	    jsonReq.put("workflowInstanceId", workflowInstanceId);
	    jsonReq.put("regType", regType.trim().toUpperCase());

	    Response response = postRequest(baseUrl + props.getProperty("reprocessPacket"), jsonReq.toString(), "Reprocess the rid", step);

	    String responseBody = response.getBody().asString();
	    logger.info("Response Body: " + responseBody);


	    JSONObject res = new JSONObject(responseBody);

	    if (!res.has("status")) {
	        logger.error("RESPONSE ERROR: 'status' field is missing in response: " + responseBody);
	        throw new RuntimeException("ERROR: Expected 'status' field is missing in the response.");
	    }

	    String expectedStatusMessage = "Packet with registrationId '" + rid + "' has been forwarded to next stage";
	    String actualStatusMessage = res.getString("status").replace("\"", ""); 

	    if (!actualStatusMessage.equals(expectedStatusMessage)) {
	        logger.error("ERROR: Expected status message not found. Actual: " + actualStatusMessage);
	        throw new RuntimeException("ERROR: Expected status message not received. Actual: " + actualStatusMessage);
	    }
	}

	public static Map<String, Object> getRegistrationRecord(String RID) {
		String sqlQuery = "SELECT workflow_instance_id, process FROM regprc.registration where reg_id='" + RID + "'";

		return DBManager.executeQueryAndGetRecord(ConfigManager.getproperty("audit_default_schema"), sqlQuery);
	}

	public static String getWorkflowInstanceId(String RID) {
		return (String) getRegistrationRecord(RID).get("workflow_instance_id");
	}
}
