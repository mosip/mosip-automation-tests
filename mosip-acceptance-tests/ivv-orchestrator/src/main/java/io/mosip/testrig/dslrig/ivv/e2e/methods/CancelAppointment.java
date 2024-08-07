package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class CancelAppointment extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(CheckStatus.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String cancelStatus = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "Paramter is  missing in step: " + step.getName());
		} else {
			cancelStatus = step.getParameters().get(0);
			String prid1 = step.getParameters().get(1);
			if (prid1.startsWith("$$")) {
				prid1 = step.getScenario().getVariables().get(prid1);
			}
			if (prid1 != null)
				cancelAppointment(prid1, cancelStatus);
			else {
				this.hasError = true;
				throw new RigInternalError("PRID cannot be null or empty");
			}
		}

	}

	private void cancelAppointment(String prid, String cancelStatus) throws RigInternalError {
		String message = null;
		switch (cancelStatus.toLowerCase()) {
		case "cancel":
			message = "appointment for the selected application has been successfully cancelled";
			break;
		case "nonexisting":
			message = "no data found for the requested pre-registration id";
			break;
		default:
			logger.error("Parameter not supported");
		}
		String url = baseUrl + props.getProperty("cancelAppointment") + prid;

		Response response = putRequest(url, "CancelAppointment", step);
		if (!response.getBody().asString().toLowerCase().contains(message)) {
			this.hasError = true;
			throw new RigInternalError("Unable to CancelAppointment");
		}
	}

}
