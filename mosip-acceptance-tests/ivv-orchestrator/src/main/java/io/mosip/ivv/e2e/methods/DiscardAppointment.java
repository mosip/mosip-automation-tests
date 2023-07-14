package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.DeleteWithParam;
import io.restassured.response.Response;

public class DiscardAppointment extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(DiscardAppointment.class);
	DeleteWithParam discardAppointment = new DeleteWithParam();

	@Override
	public void run() throws RigInternalError {
		String bookingStatus =null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"Paramter is  missing in step: "+step.getName());
		} else {
			String prid = step.getParameters().get(0);
			if (prid.startsWith("$$")) {
				prid = step.getScenario().getVariables().get(prid);
		}
			
					//String url = BaseTestCase.ApplnURI + props.getProperty("discardAppoinment")+"/"+prid1;
			String url = baseUrl + props.getProperty("discardAppoinment")+"/"+prid;
					HashMap<String, String> map=new HashMap<String, String>();
					map.put("preRegistrationId", prid);
					//Response response =deleteRequestWithoutStep(url, "Discard booking");
					Response response =deleteRequest(url, "Discard booking",step);
					System.out.println(response.toString());
		}
	}

}
