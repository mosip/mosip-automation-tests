package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrunner.MockSMTPListener;

public class GetAdditionalReqId extends BaseTestCaseUtil implements StepInterface { // $$additionalReqId=e2e_getAdditionalReqId(10)

	Logger logger = Logger.getLogger(GetAdditionalReqId.class);

	@Override
	public void run() throws RigInternalError {
		int counter = 0;
		int repeats = 10; // taking from dsl argument
		String email = null;
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 0)
			email = step.getParameters().get(0) + "@mosip.io";
		String url = baseUrl + props.getProperty("getAdditionalInfoReqId");
		Map<Object, Object> m = new HashMap<Object, Object>();
		String additonalInfoRequestId = null;
		while (counter < repeats) {
			m = MockSMTPListener.emailNotificationMapS;
			if (m.get(email) != null) {
//				String html = (String) m.get(email);
//				StringUtils.substringBetween(html, "AdditionalInfoRequestId", "-BIOMETRIC_CORRECTION-1");
				// String
				// arr[]=html.split("AdditionalInfoRequestId","AdditionalInfoRequestId".indexOf(str)
				// ;
				// System.out.println(arr);

				logger.info("*******Checking the email for AdditionalInfoReqId...*******");
				
				additonalInfoRequestId = MockSMTPListener.parseAdditionalReqId((String) m.get(email)).trim()+ "-BIOMETRIC_CORRECTION-1";
				
				// Response response = getRequest(url, "Get addtionalInfoRequestId");
				// String additonalInfoRequestId = response.getBody().asString();
//				additonalInfoRequestId = StringUtils
//						.substringBetween(html, "AdditionalInfoRequestId", "-BIOMETRIC_CORRECTION-1").trim()
//						+ "-BIOMETRIC_CORRECTION-1";
				// additonalInfoRequestId= StringUtils.substringBetween(html,
				// "AdditionalInfoRequestId", "-BIOMETRIC_CORRECTION-1");

			}
			if (additonalInfoRequestId != null && !additonalInfoRequestId.isEmpty()
					&& !additonalInfoRequestId.equals("{Failed}")) {

				logger.info("AdditionalInfoReqId retrieved: " + additonalInfoRequestId);
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().put(step.getOutVarName(), additonalInfoRequestId);
				return;
			}
			counter++;
			try {
				logger.info("waiting for 10 Sec");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				this.hasError=true;
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
		logger.error("AdditionalInfoReqId not found even after " + repeats + " retries");
		this.hasError=true;
		throw new RigInternalError("Failed to retrieve the value for addtionalInfoRequestId from email");

	}

}
