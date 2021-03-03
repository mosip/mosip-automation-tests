package org.mosip.dataprovider.test.prereg;

import java.time.LocalDateTime;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mosip.dataprovider.models.AppointmentModel;
import org.mosip.dataprovider.models.AppointmentTimeSlotModel;
import org.mosip.dataprovider.models.CenterDetailsModel;


import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.test.CreatePersona;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataCallback;
import org.mosip.dataprovider.util.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import variables.VariableManager;



public class PreRegistrationSteps {

	//"/preregistration/v1/applications";
	public static String postApplication(ResidentModel resident, DataCallback cb) throws JSONException {
		String result = "";
		String url = VariableManager.getVariableValue("urlBase").toString().trim() +
		VariableManager.getVariableValue( "postapplication").toString().trim();
		
		JSONObject identity = CreatePersona.crateIdentity(resident,cb);
		JSONObject demoData = new JSONObject();
		demoData.put("identity",identity);
		JSONObject reqObject = new JSONObject();
		reqObject.put("demographicDetails", demoData);
		JSONObject reqBody = CreatePersona.createRequestBody(reqObject);
		reqObject.put("langCode","eng");//resident.getPrimaryLanguare());
		//RestClient client = annotation.getRestClient();
		
		try {
			JSONObject resp = RestClient.postNoAuth (url, reqBody);
			result = resp.get("preRegistrationId").toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  result;
	}
	public static AppointmentModel getAppointments() {
		AppointmentModel appointmentSlot = new AppointmentModel();

		String base = VariableManager.getVariableValue("urlBase").toString().trim();
		String api = VariableManager.getVariableValue( "appointmentslots").toString().trim();
		String centerId = VariableManager.getVariableValue( "centerId").toString().trim();
		String url =  base + api + centerId;


		try {
			JSONObject resp = RestClient.getNoAuth(url, new JSONObject(), new JSONObject());

			if(resp != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				appointmentSlot = objectMapper.readValue(resp.toString(),  AppointmentModel.class);	
			}
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return appointmentSlot;
	}
	public static String bookAppointment(String preRegId, String appointmentDate, int centerId, AppointmentTimeSlotModel slot) throws JSONException {

		String result ="";
			String url = VariableManager.getVariableValue("urlBase").toString().trim()+
			VariableManager.getVariableValue( "postappointment").toString().trim();

			JSONObject obj = new JSONObject();
			JSONObject requestObject = new JSONObject();
			JSONArray bookingRequestObject = new JSONArray();
			JSONObject booking = new JSONObject();
			obj.put("id", "mosip.pre-registration.booking.book");
			obj.put("version", "1.0");
			obj.put("request", requestObject);
			obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
			
			requestObject.put("bookingRequest",bookingRequestObject);
			booking.put("preRegistrationId", preRegId);
			booking.put("appointment_date",appointmentDate);
			booking.put("registration_center_id",centerId);
			booking.put("time_slot_from",slot.getFromTime());
			booking.put("time_slot_to",slot.getToTime());
			bookingRequestObject.put(0, booking);

			try {
				JSONObject resp = RestClient.postNoAuth(url, obj);

				if(resp != null) {
					result = resp.toString();
				
					//ObjectMapper objectMapper = new ObjectMapper();
					//appointmentSlot = objectMapper.readValue(resp.toString(),  AppointmentModel.class);	
				}
			

				
			} catch (Exception e) {
				e.printStackTrace();
			}		
			return result;
	}
	public static JSONObject UploadDocument(String docCatCode,String docTypCode, String langCode, String docFilePath, String preRegID) throws JSONException {
		JSONObject response=null;
		String url = VariableManager.getVariableValue("urlBase").toString().trim() +
				VariableManager.getVariableValue("uploaddocument").toString().trim();
				
		JSONObject obj = new JSONObject();
		JSONObject requestObject = new JSONObject();
		obj.put("id", "mosip.pre-registration.document.upload");
		obj.put("version", "1.0");
		obj.put("request", requestObject);
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		requestObject.put("docCatCode", docCatCode);
		requestObject.put("docTypCode", docTypCode);
		requestObject.put("langCode", langCode);
		
		try {
			response = RestClient.uploadFile(url + preRegID,docFilePath,obj);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		
		
		return response;
	}
	public static void main(String[] args) throws JSONException {

	
				
		String preRegID ="30491084523580";
		Boolean bBooked = false;
		CreatePersona.sendOtpTo("sanath@mailinator.com");
		CreatePersona.validateOTP("111111", "sanath@mailinator.com");
		AppointmentModel res = getAppointments();
		System.out.println(res.getRegCenterId());
		for( CenterDetailsModel a: res.getAvailableDates()) {
			if(!a.getHoliday()) {
				for(AppointmentTimeSlotModel ts: a.getTimeslots()) {
					if(ts.getAvailability() > 0) {
						bookAppointment(preRegID,a.getDate(),res.getRegCenterId(),ts);
						bBooked = true;
						break;
					}
				}
			}
			if(bBooked) break;
		}
	}
}
