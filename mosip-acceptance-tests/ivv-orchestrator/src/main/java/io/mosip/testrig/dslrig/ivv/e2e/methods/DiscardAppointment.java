package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.testscripts.DeleteWithParam;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class DiscardAppointment extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(DiscardAppointment.class);
	DeleteWithParam discardAppointment = new DeleteWithParam();

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String bookingStatus = null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "Paramter is  missing in step: " + step.getName());
		} else {
			String prid = step.getParameters().get(0);
			if (prid.startsWith("$$")) {
				prid = step.getScenario().getVariables().get(prid);
			}
			String url = baseUrl + props.getProperty("discardAppoinment") + "/" + prid;
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("preRegistrationId", prid);
			Response response = deleteRequest(url, "Discard booking", step);
			logger.info(response.toString());
		}
	}

}
