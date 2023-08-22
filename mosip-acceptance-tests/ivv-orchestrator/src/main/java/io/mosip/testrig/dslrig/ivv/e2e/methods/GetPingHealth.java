package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.SkipException;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class GetPingHealth extends BaseTestCaseUtil implements StepInterface {
	  private static final Logger logger = Logger.getLogger(GetPingHealth.class);
	  
		static {
			if (ConfigManager.IsDebugEnabled())
				logger.setLevel(Level.ALL);
			else
				logger.setLevel(Level.ERROR);
		}

	@Override
	public void run() throws RigInternalError {

		String modules = null,uri=null;
		if (step.getParameters().isEmpty() || step.getParameters().size() < 1) {
		
			modules = "";
		} else {
			
			if (step.getParameters().size() == 1)
				modules = step.getParameters().get(0);
			else
				modules = "";
		}
		if(modules.length()>0 && modules.equalsIgnoreCase("packetcreator")) {
			
			// Check packet creator up or not..
			String packetcreatorUri=baseUrl +"/actuator/health";
			String serviceStatus = checkActuatorNoAuth(packetcreatorUri);
			
			if (serviceStatus.equalsIgnoreCase("UP") == false) {
				this.hasError=true;
				throw new SkipException("Packet creator Not responding");
				
			}
		
		}
		else {
		uri=baseUrl + "/ping/"+ ConfigManager.IseSignetDeployed();
		
		Response response = getRequest(uri, "Health Check",step);
		JSONObject res = new JSONObject(response.asString());
		logger.info(res.toString());
		if (res.get("status").equals(true)) {
			logger.info("RESPONSE=" + res.toString());
		} else {
			logger.error("RESPONSE=" + res.toString());
			this.hasError=true;
			throw new SkipException("Health check status" + res.toString());
		}
		}
	}
	
	public static String checkActuatorNoAuth(String actuatorURL) {
		Response response =null;
		response = given().contentType(ContentType.JSON).get(actuatorURL);
		if(response != null && 	response.getStatusCode() == 200 ) {
			logger.info(response.getBody().asString());        	
			JSONObject jsonResponse = new JSONObject(response.getBody().asString());
			return jsonResponse.getString("status");
		}
		return "No Response";
	}
}