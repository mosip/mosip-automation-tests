package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class BookSpecificSlotForPrid extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(BookSpecificSlotForPrid.class);
	String appointment_date ="";
	//String pre_registration_id ="";
	String registration_center_id ="";
	String time_slot_from ="";
	String time_slot_to ="";

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().isEmpty() ||step.getParameters().size()<3) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"Paramter is  missing in step: "+step.getName());
		} else {
			appointment_date =step.getParameters().get(0);
			//pre_registration_id =step.getParameters().get(1);
			registration_center_id =step.getParameters().get(1);
			time_slot_from =step.getParameters().get(2);
			time_slot_to =step.getParameters().get(3);
		}
		for (String resDataPath : residentPathsPrid.keySet()) {
			String prid = residentPathsPrid.get(resDataPath);
			if (!StringUtils.isEmpty(prid)) {
				bookSlotForPrid(prid);
			} else
				throw new RigInternalError("PRID cannot be null or empty");
		}
	}
	
	private void bookSlotForPrid(String prid) throws RigInternalError {
		String url = baseUrl + props.getProperty("bookSpecificSlotForPrid")+prid;
		JSONObject jsonReq = new JSONObject();
		jsonReq.put(E2EConstants.APPOINTMENT_DATE, appointment_date);
		jsonReq.put(E2EConstants.PRE_REGISTRATION_ID, prid);
		jsonReq.put(E2EConstants.REGISTRATION_CENTER_ID, registration_center_id);
		jsonReq.put(E2EConstants.TIME_SLOT_FROM, time_slot_from);
		jsonReq.put(E2EConstants.TIME_SLOT_TO, time_slot_to);
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextInuse,"BookSlotForPrid");
		if (!response.getBody().asString().toLowerCase()
				.contains("appointment booked successfully"))
			throw new RigInternalError("Unable to Book Appointment for Prid :"+prid);
	}

}
