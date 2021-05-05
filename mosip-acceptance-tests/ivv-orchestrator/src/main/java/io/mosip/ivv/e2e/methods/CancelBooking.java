package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

public class CancelBooking extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(CancelBooking.class);

	@Override
	public void run() throws RigInternalError {
		String bookingStatus =null;
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"Paramter is  missing in step: "+step.getName());
		} else {
			bookingStatus =step.getParameters().get(0);
		}
		for (String resDataPath : residentPathsPrid.keySet()) {
			String prid = residentPathsPrid.get(resDataPath);
			if (!StringUtils.isEmpty(prid)) {
				Map<String, String> retrieveBookingByPrid = retrieveBookingByPrid(prid);
				if(!retrieveBookingByPrid.isEmpty())
				cancelBookingByPrid(retrieveBookingByPrid,prid,bookingStatus);
			} else
				throw new RigInternalError("PRID cannot be null or empty");
		}
	}

	private void cancelBookingByPrid(Map<String, String> retrieveBookingByPrid,String prid,String bookingStatus) throws RigInternalError {
		String message=null;
		switch(bookingStatus.toLowerCase()) {
		case "cancel":
			message="appointment for the selected application has been successfully cancelled";
			break;
		case "bookingNotFound":
			message="booking data not found";
			break;
		default:
			logger.error("["+bookingStatus+"]"+" Parameter not supported");
		}
		String url = baseUrl + props.getProperty("cancelBookingByPrid")+prid;
		JSONObject jsonReq = new JSONObject();
		jsonReq.put(E2EConstants.APPOINTMENT_DATE, retrieveBookingByPrid.get("appointment_date"));
		jsonReq.put(E2EConstants.PRE_REGISTRATION_ID, retrieveBookingByPrid.get("pre_registration_id"));
		jsonReq.put(E2EConstants.REGISTRATION_CENTER_ID, retrieveBookingByPrid.get("registration_center_id"));
		jsonReq.put(E2EConstants.TIME_SLOT_FROM, retrieveBookingByPrid.get("time_slot_from"));
		jsonReq.put(E2EConstants.TIME_SLOT_TO, retrieveBookingByPrid.get("time_slot_to"));
		Response response =postRequestWithQueryParamAndBody(url,jsonReq.toString(),contextInuse,"CancelBookingByPrid");
		if (!response.getBody().asString().toLowerCase()
				.contains(message))
			throw new RigInternalError("Unable to CancelAppointment");
	}

	private Map<String, String> retrieveBookingByPrid(String prid) throws RigInternalError {
		Map<String,String> bookingMetadata=new HashMap<String, String>();
		contextKey.put("preregId", prid);
		String url = baseUrl + props.getProperty("retrieveBookingbyPrid");
		Response response = getRequestWithQueryParam(url, contextInuse, "RetrieveBookingByPrid");
		if(response.getBody().asString().equalsIgnoreCase("{}")) {
			logger.info("booking data not found for prid : "+prid);
			return bookingMetadata;
		}
		else if (!response.getBody().asString().contains(prid))
			throw new RigInternalError("Unable to RetrieveBooking for Prid: " + prid);
		JSONObject jsonResp = new JSONObject(response.getBody().asString());
		bookingMetadata=getBookingDetail(jsonResp);
		return bookingMetadata;
	}
	
	
	private Map<String, String> getBookingDetail(JSONObject jsonResponse) {
		String response = jsonResponse.toString();
		Map<String, String> bookingMetadata = new HashMap<>();
		bookingMetadata.put("time_slot_from",
				JsonPrecondtion.getValueFromJson(response, "bookingMetadata.time_slot_from"));
		bookingMetadata.put("time_slot_to", JsonPrecondtion.getValueFromJson(response, "bookingMetadata.time_slot_to"));
		bookingMetadata.put("registration_center_id",
				JsonPrecondtion.getValueFromJson(response, "bookingMetadata.registration_center_id"));
		bookingMetadata.put("appointment_date",
				JsonPrecondtion.getValueFromJson(response, "bookingMetadata.appointment_date"));
		bookingMetadata.put("pre_registration_id",
				JsonPrecondtion.getValueFromJson(response, "preRegistrationId"));
		return bookingMetadata;
	}

}
