	package io.mosip.testrig.dslrig.ivv.e2e.methods;

	import org.apache.log4j.Level;
	import org.apache.log4j.Logger;
	import org.json.JSONObject;

	import io.mosip.testrig.apirig.kernel.util.ConfigManager;
	import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
	import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
	import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
	import io.restassured.response.Response;

public class ResetContextData extends BaseTestCaseUtil implements StepInterface{
		public static Logger logger = Logger.getLogger(ResetContextData.class);

		static {
			if (ConfigManager.IsDebugEnabled())
				logger.setLevel(Level.ALL);
			else
				logger.setLevel(Level.ERROR);
		}

		@Override
		public void run() throws RigInternalError {
			Response response = null;

			String clearBaseUrlNamespaceData = baseUrl +  props.getProperty("resetContextData");
			response = getRequest(clearBaseUrlNamespaceData, "Clear baseUrl namespace data", step);
			if (response != null && response.getStatusCode() == 200) {
				String responseString = response.getBody().asString();
				if (!responseString.contains("true")) {
					logger.info(responseString);
					this.hasError = true;
					throw new RigInternalError("Clearing baseUrl namespace data from variable manager failed");
				}
			} 
		}
	}