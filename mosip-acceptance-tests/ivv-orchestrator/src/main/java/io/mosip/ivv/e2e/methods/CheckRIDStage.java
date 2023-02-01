package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CheckRIDStage extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(CheckRIDStage.class);

	@Override
	public void run() throws RigInternalError {

		String rid = null;
		Boolean flag=false;
		HashMap<String, String>map=new HashMap<String, String>();
		HashMap<String, String> context=null;
		 String transactionTypeCode =null;
		   String subStatusCode =null;
			if (step.getParameters().size() >= 3) {
				rid = step.getParameters().get(0);
				transactionTypeCode=step.getParameters().get(1);
				subStatusCode=step.getParameters().get(2);
			}
			 if (rid.startsWith("$$")) {
				 map = step.getScenario().getVariables();
				}
			Response response = getRequest(baseUrl+props.getProperty("ridStatus")+map.get("$$rid"), "Get Stages by rid");
			
		// Check these two keys	subStatusCode,transactionTypeCode
			
		JSONObject res = new JSONObject(response.getBody().asString());
		JSONArray arr=res.getJSONObject("response").getJSONArray("packetStatusUpdateList");
		  for (Object myObject : arr) {

		       		        JSONObject myJSONObject = (JSONObject) myObject;
		     
//		         transactionTypeCode = myJSONObject.getString("transactionTypeCode");
//		         subStatusCode = myJSONObject.getString("subStatusCode");
		        if(transactionTypeCode.equalsIgnoreCase(myJSONObject.getString("transactionTypeCode")) &&
		        	subStatusCode.equalsIgnoreCase(myJSONObject.getString("subStatusCode")))
		        	{
		        	 System.out.println("matching");
		        	 flag=true;
		        	  break;
		        	 
		        	}
		    }
		
		  logger.info(res.toString());
		if (flag.equals(true)) {
			logger.info("RESPONSE= contains" + transactionTypeCode + subStatusCode);
		} else {
			logger.error("RESPONSE= doesn't contain" + arr);
			throw new RuntimeException("RESPONSE= doesn't contain" + transactionTypeCode + subStatusCode);
		}

	}
}