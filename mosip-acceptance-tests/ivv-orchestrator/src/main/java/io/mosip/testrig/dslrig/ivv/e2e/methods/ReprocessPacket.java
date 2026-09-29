package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
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
	    String workflowInstanceId = value(registration, "workflow_instance_id");
	    // process column holds NEW / UPDATE / LOST, required by securezone notification as reg_type
	    String regType = value(registration, "process");
	    if (regType == null || regType.isBlank()) {
	        throw new RigInternalError("reg_type/process not found in regprc.registration for rid=" + rid
	        		+ " keys=" + (registration == null ? "null" : registration.keySet()));
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

	public static Map<String, Object> getRegistrationRecord(String RID) throws RigInternalError {
		String sqlQuery = "SELECT workflow_instance_id, process FROM regprc.registration where reg_id='" + RID
				+ "' ORDER BY cr_dtimes DESC";
		Map<String, Object> row = null;
		try {
			row = DBManager.executeQueryAndGetRecord(ConfigManager.getproperty("audit_default_schema"), sqlQuery);
		} catch (RuntimeException e) {
			logger.error("Hibernate registration lookup failed for rid=" + RID + ": " + e.getMessage());
		}
		if (value(row, "process") == null || value(row, "workflow_instance_id") == null) {
			logger.info("Registration lookup via audit connection missed process for rid=" + RID
					+ "; querying mosip_regprc directly");
			row = queryRegprcDirect(RID);
		}
		return row;
	}

	/**
	 * regprc.registration lives in database mosip_regprc. The shared DB helper uses
	 * audit_db_schema, which points at mosip_audit on several environments and then
	 * returns an empty map.
	 */
	private static Map<String, Object> queryRegprcDirect(String rid) throws RigInternalError {
		String host = ConfigManager.getDbServer();
		String port = ConfigManager.getDbPort();
		if (port == null || port.isBlank()) {
			port = "5432";
		}
		String user = ConfigManager.getAuditDbUser();
		String pass = ConfigManager.getAuditDbPass();
		String url = "jdbc:postgresql://" + host + ":" + port + "/mosip_regprc";
		String sql = "SELECT workflow_instance_id, process FROM regprc.registration WHERE reg_id=? ORDER BY cr_dtimes DESC";
		try {
			Class.forName("org.postgresql.Driver");
			try (Connection connection = DriverManager.getConnection(url, user, pass);
					PreparedStatement statement = connection.prepareStatement(sql)) {
				statement.setString(1, rid);
				try (ResultSet rs = statement.executeQuery()) {
					if (!rs.next()) {
						throw new RigInternalError("no regprc.registration row for rid=" + rid + " on " + host + ":"
								+ port + "/mosip_regprc");
					}
					Map<String, Object> row = new HashMap<>();
					row.put("workflow_instance_id", rs.getString("workflow_instance_id"));
					row.put("process", rs.getString("process"));
					return row;
				}
			}
		} catch (RigInternalError e) {
			throw e;
		} catch (Exception e) {
			throw new RigInternalError(
					"regprc lookup failed for rid=" + rid + " via " + url + ": " + e.getMessage());
		}
	}

	private static String value(Map<String, Object> row, String key) {
		if (row == null || key == null) {
			return null;
		}
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null) {
				String text = entry.getValue().toString().trim();
				if (!text.isEmpty()) {
					return text;
				}
			}
		}
		return null;
	}

	public static String getWorkflowInstanceId(String RID) throws RigInternalError {
		return value(getRegistrationRecord(RID), "workflow_instance_id");
	}
}
