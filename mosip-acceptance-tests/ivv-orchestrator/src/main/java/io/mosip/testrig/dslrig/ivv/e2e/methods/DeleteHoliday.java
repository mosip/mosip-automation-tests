package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.GlobalConstants;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class DeleteHoliday extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(DeleteHoliday.class);
	String holidayId = "";

	@Override
	public void run() throws RigInternalError {

		if (step.getParameters().size() == 1) {
			String holidayId = step.getParameters().get(0);
			if (holidayId.startsWith("$$"))
				holidayId = step.getScenario().getVariables().get(holidayId);

			Response response = null;
			String url = BaseTestCase.ApplnURI + props.getProperty("holidayCreation");
			JSONObject jsonReq = new JSONObject();
			jsonReq.put("isActive", "false");
			jsonReq.put("holidayId", holidayId);

			try {
				response = BaseTestCaseUtil.patchWithQueryParamAndCookie(url, jsonReq.toString(),
						GlobalConstants.AUTHORIZATION, "admin", null);
				if (response != null) {
					JSONObject jsonObject = new JSONObject(response);
					String status = jsonObject.getJSONObject("response").getString("status");
					logger.info(status);
				}
			} catch (Exception e) {
				logger.error(e);
				this.hasError = true;
			}
		}
	}
}
