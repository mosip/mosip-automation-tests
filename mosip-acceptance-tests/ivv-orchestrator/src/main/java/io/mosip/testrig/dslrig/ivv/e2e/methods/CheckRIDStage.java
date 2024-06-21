package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CheckRIDStage extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(CheckRIDStage.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		JSONObject myJSONObject = null;
		String ridStage = null;
		Boolean flag = false;
		String transactionTypeCode = null;
		String statusCode = null;
		String subStatusCode = null;
		String waitTime = props.getProperty("waitTime");
		int counter = 0;
		JSONObject res = null;
		JSONArray arr = null;

		if (step.getParameters().size() >= 3) {
			ridStage = step.getScenario().getVariables().get(step.getParameters().get(0));
			transactionTypeCode = step.getParameters().get(1);
			statusCode = step.getParameters().get(2);

			if (step.getParameters().size() == 4) {
				subStatusCode = step.getParameters().get(3);
			}
		}

		while (counter < Integer.parseInt(props.getProperty("loopCount"))) {
			Response response = getRequest(baseUrl + props.getProperty("ridStatus") + ridStage, "Get Stages by rid",
					step);

			// Check these two keys statusCode, transactionTypeCode

			res = new JSONObject(response.getBody().asString());
			arr = res.getJSONObject("response").getJSONArray("packetStatusUpdateList");
			for (Object myObject : arr) {
				myJSONObject = (JSONObject) myObject;

				if (transactionTypeCode.equalsIgnoreCase(myJSONObject.getString("transactionTypeCode"))) {
					if (statusCode.equalsIgnoreCase(myJSONObject.getString("statusCode"))) {
						logger.info("matching statusCode");
						if (subStatusCode == null) {
							flag = true;
							break;
						} else if (subStatusCode.equalsIgnoreCase(myJSONObject.getString("subStatusCode"))) {
							logger.info("matching subStatusCode");
							flag = true;
							break;
						}
					}
				}
			}
		   if(flag == true)
				break;
			
			else {
				logger.info("Waiting for " + Long.parseLong(waitTime) / 1000 + " sec to get desired response");
				counter++;
				try {
					Thread.sleep(Long.parseLong(waitTime));
				} catch (NumberFormatException | InterruptedException e) {
					logger.error(e.getMessage());
					Thread.currentThread().interrupt();
				}
		}
		}
		   logger.info(res.toString());
		if (flag == false) {
			this.hasError = true;
			throw new RigInternalError("RESPONSE = doesn't contain " + transactionTypeCode + " " +statusCode);
		}
	}
}
