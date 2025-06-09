package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class GetHandleDetailsFromSchema extends BaseTestCaseUtil implements StepInterface {
	private static final Logger logger = Logger.getLogger(GetHandleDetailsFromSchema.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		Map<String, JSONObject> selectedHandles = new LinkedHashMap<>();
		
	    KernelAuthentication kernelAuthLib = new KernelAuthentication();
	    String token = kernelAuthLib.getTokenByRole(GlobalConstants.ADMIN);
	    String url = AdminTestUtil.getSchemaURL();

	    Response response = RestClient.getRequestWithCookie(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON,
	            GlobalConstants.AUTHORIZATION, token);

	    org.json.JSONObject responseJson = new org.json.JSONObject(response.asString());
	    org.json.JSONObject schemaData = (org.json.JSONObject) responseJson.get(GlobalConstants.RESPONSE);

	    String schemaJsonData = schemaData.getString(GlobalConstants.SCHEMA_JSON);
	    String schemaFile = schemaJsonData;

	    try {
	        JSONObject schemaFileJson = new JSONObject(schemaFile);
	        JSONObject schemaPropsJson = schemaFileJson.getJSONObject("properties");
	        JSONObject schemaIdentityJson = schemaPropsJson.getJSONObject("identity");
	        JSONObject identityPropsJson = schemaIdentityJson.getJSONObject("properties");
	        JSONArray requiredPropsArray = schemaIdentityJson.getJSONArray("required");

	        for (int i = 0, size = requiredPropsArray.length(); i < size; i++) {
	            String eachRequiredProp = requiredPropsArray.getString(i);

	            if (!identityPropsJson.has(eachRequiredProp)) {
	                continue;
	            }

	            JSONObject eachPropDataJson = (JSONObject) identityPropsJson.get(eachRequiredProp);
	            
	            if(eachPropDataJson.optBoolean("handle", false)){
            		selectedHandles.put(eachRequiredProp, eachPropDataJson);
            	}
	            
	        }
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	    }

			if (step.getOutVarName() != null)
				step.getScenario().getObjectVariables().put(step.getOutVarName(), selectedHandles);
		}
	
}
