package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	    String regType = null;

	    if (step.getParameters().size() >= 1) {
	        rid = step.getScenario().getVariables().get(step.getParameters().get(0));
	    }
	    if (step.getParameters().size() >= 2) {
	        regType = step.getParameters().get(1);
	        if (regType != null && regType.startsWith("$$")) {
	            regType = step.getScenario().getVariables().get(regType);
	        }
	    }
	    if (regType == null || regType.isBlank()) {
	        throw new RigInternalError("packet type is required on reprocess for rid=" + rid
	                + ". Pass NEW, UPDATE, or LOST from the scenario.");
	    }

	    String workflowInstanceId = getWorkflowInstanceId(rid);

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
		String sqlQuery = "SELECT workflow_instance_id FROM regprc.registration where reg_id='" + RID
				+ "' ORDER BY cr_dtimes DESC";
		Map<String, Object> row = null;
		try {
			row = DBManager.executeQueryAndGetRecord(ConfigManager.getproperty("audit_default_schema"), sqlQuery);
		} catch (RuntimeException e) {
			logger.error("Hibernate registration lookup failed for rid=" + RID + ": " + e.getMessage());
		}
		if (value(row, "workflow_instance_id") == null) {
			logger.info("Workflow lookup via audit connection missed workflow_instance_id for rid=" + RID
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
		String user = ConfigManager.getAuditDbUser();
		String pass = ConfigManager.getAuditDbPass();
		List<String> ports = candidatePorts();
		Exception last = null;
		String lastUrl = null;
		for (String port : ports) {
			String url = "jdbc:postgresql://" + host + ":" + port + "/mosip_regprc";
			try {
				return queryRegprc(url, user, pass, rid, host, port);
			} catch (RigInternalError noRow) {
				throw noRow;
			} catch (Exception e) {
				last = e;
				lastUrl = url;
				logger.error("regprc connect failed via " + url + ": " + e.getMessage());
			}
		}
		throw new RigInternalError("regprc lookup failed for rid=" + rid + " via " + lastUrl + ": "
				+ (last == null ? "no ports" : last.getMessage()));
	}

	/**
	 * dev2 accepts Postgres on 5433. A blank db-port was defaulting to 5432, and that
	 * port closes the connection, so workflow_instance_id was never read.
	 */
	private static List<String> candidatePorts() {
		List<String> ports = new ArrayList<>();
		addPort(ports, ConfigManager.getDbPort());
		addPort(ports, "5433");
		addPort(ports, "5432");
		return ports;
	}

	private static void addPort(List<String> ports, String port) {
		if (port == null) {
			return;
		}
		String trimmed = port.trim();
		if (trimmed.isEmpty() || ports.contains(trimmed)) {
			return;
		}
		ports.add(trimmed);
	}

	private static Map<String, Object> queryRegprc(String url, String user, String pass, String rid, String host,
			String port) throws Exception {
		Class.forName("org.postgresql.Driver");
		String sql = "SELECT workflow_instance_id FROM regprc.registration WHERE reg_id=? ORDER BY cr_dtimes DESC";
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
				return row;
			}
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
