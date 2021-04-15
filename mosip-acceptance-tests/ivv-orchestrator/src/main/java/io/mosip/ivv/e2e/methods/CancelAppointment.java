package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

public class CancelAppointment extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(CheckStatus.class);
	
	@Override
	public void run() throws RigInternalError {
		String cancelStatus =null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"Paramter is  missing in step: "+step.getName());
		} else {
			cancelStatus =step.getParameters().get(0);
		}
		for (String resDataPath : residentPathsPrid.keySet()) {
			String prid = residentPathsPrid.get(resDataPath);
			if (!StringUtils.isEmpty(prid))
				cancelAppointment(prid,cancelStatus);
			else
				throw new RigInternalError("PRID cannot be null or empty");
		}

	}

	private void cancelAppointment(String prid,String cancelStatus) throws RigInternalError {
		String message=null;
		switch(cancelStatus.toLowerCase()) {
		case "cancel":
			message="appointment for the selected application has been successfully cancelled";
			break;
		case "nonexisting":
			message="no data found for the requested pre-registration id";
			break;
		default:
			logger.error("Parameter not supported");
		}
		String url = BaseTestCase.ApplnURI + props.getProperty("cancelAppointment") + prid;
		Response response = putReqest(url, "CancelAppointment");
		if (!response.getBody().asString().toLowerCase()
				.contains(message))
			throw new RigInternalError("Unable to CancelAppointment");
	}

}
