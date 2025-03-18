package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
	    
	    JSONObject jsonReq = new JSONObject();
	    jsonReq.put("rid", rid);
	    jsonReq.put("workflowInstanceId", getWorkflowInstanceId(rid));

	    Response response = postRequest(baseUrl + props.getProperty("reprocessPacket"), jsonReq.toString(), "Reprocess the rid", step);

	    String responseBody = response.getBody().asString();
	    logger.info("Response Body: " + responseBody);

	    // Validate the expected response format
	    JSONObject res = new JSONObject(responseBody);
	    
	    if (!res.has("status")) {
	        logger.error("RESPONSE ERROR: 'status' field is missing in response: " + responseBody);
	        throw new RuntimeException("ERROR: Expected 'status' field is missing in the response.");
	    }

	    String expectedStatusMessage = "Packet with registrationId '" + rid + "' has been forwarded to next stage";
	    String actualStatusMessage = res.getString("status").replace("\"", ""); // Remove escaped quotes if present

	    if (!actualStatusMessage.equals(expectedStatusMessage)) {
	        logger.error("ERROR: Expected status message not found. Actual: " + actualStatusMessage);
	        throw new RuntimeException("ERROR: Expected status message not received. Actual: " + actualStatusMessage);
	    }
	}
	
	public static String  getWorkflowInstanceId(String RID) {
		String sqlQuery = "SELECT * FROM regprc.registration where reg_id='"+RID+"'";

		Map<String, Object> response = DBManager
				.executeQueryAndGetRecord(ConfigManager.getproperty("audit_default_schema"), sqlQuery);
		return (String) response.get("workflow_instance_id");
	}
}