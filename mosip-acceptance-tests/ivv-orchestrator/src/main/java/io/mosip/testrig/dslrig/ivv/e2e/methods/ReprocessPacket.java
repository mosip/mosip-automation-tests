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

	    JSONObject jsonReq = new JSONObject();
	    jsonReq.put("rid", rid);
	    jsonReq.put("workflowInstanceId", getWorkflowInstanceId(rid));

	    Response response = postRequest(baseUrl + props.getProperty("reprocessPacket"), jsonReq.toString(), "Reprocess the rid", step);

	    String responseBody = response.getBody().asString();
	    logger.info("Response Body: " + responseBody);

	    if (responseBody == null || responseBody.isBlank()) {
	        throw new RigInternalError("Reprocess returned an empty response for rid=" + rid);
	    }

	    JSONObject res;
	    try {
	        res = new JSONObject(responseBody);
	    } catch (Exception ex) {
	        throw new RigInternalError("Reprocess returned a non-JSON response for rid=" + rid);
	    }

	    if (!res.has("status")) {
	        throw new RigInternalError("Reprocess response missing 'status' field for rid=" + rid);
	    }

	    String expectedStatusMessage = "Packet with registrationId '" + rid + "' has been forwarded to next stage";
	    String actualStatusMessage = res.getString("status").replace("\"", ""); 

	    if (!actualStatusMessage.equals(expectedStatusMessage)) {
	        throw new RigInternalError("Unexpected reprocess status for rid=" + rid
	                + ". Expected: " + expectedStatusMessage + " Actual: " + actualStatusMessage);
	    }
	}

	public static String  getWorkflowInstanceId(String RID) {
		String sqlQuery = "SELECT * FROM regprc.registration where reg_id='"+RID+"'";

		Map<String, Object> response = DBManager
				.executeQueryAndGetRecord(ConfigManager.getproperty("audit_default_schema"), sqlQuery);
		return (String) response.get("workflow_instance_id");
	}
}
