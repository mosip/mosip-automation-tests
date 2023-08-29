package io.mosip.testrig.dslrig.dataprovider.test.prereg;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.testrig.dslrig.dataprovider.models.AppointmentModel;
import io.mosip.testrig.dslrig.dataprovider.models.AppointmentTimeSlotModel;
import io.mosip.testrig.dslrig.dataprovider.models.CenterDetailsModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.test.CreatePersona;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataCallback;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;



public class PreRegistrationSteps {
	private static final Logger logger = LoggerFactory.getLogger(PreRegistrationSteps.class);

	private static JSONObject matchApplication(JSONArray arr, String preregId) {
		for(int i=0; i < arr.length() ; i++)
			if(arr.getJSONObject(i).getString("preRegistrationId").equals(preregId))
				return arr.getJSONObject(i);
		
		return new JSONObject();
	}
	public static String getApplications(String status, String preregId,String contextKey) {


		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +
		VariableManager.getVariableValue( contextKey,"postapplication").toString().trim();
		JSONArray newArray = new JSONArray();

		try {
		//	JSONObject resp = RestClient.getNoAuth (url, new JSONObject(),new JSONObject(),contextKey);
			JSONObject resp = RestClient.getAdminPreReg (url, new JSONObject(),new JSONObject(),contextKey);
			
			String strCount = resp.getString("totalRecords");
			int count =0;
			if(strCount != null && !strCount.equals(""))
				count = Integer.parseInt(strCount);
			if(count >0 && status == null) {
				if(preregId == null || preregId.equals(""))
					return resp.getJSONArray("basicDetails").toString();
				return matchApplication(resp.getJSONArray("basicDetails"),preregId).toString();
			}
			
			if(count > 0) {	
				JSONArray arr = resp.getJSONArray("basicDetails");
				
				
				for(int j=0; j < arr.length(); j++) {
					if( arr.getJSONObject(j).has("statusCode")) {
						String curstatus = arr.getJSONObject(j).getString("statusCode");
						if(curstatus.equals(status)) {
							newArray.put( arr.getJSONObject(j));
						}
					}
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		if(preregId == null || preregId.equals(""))
			return  newArray.toString();
		return matchApplication(newArray,preregId).toString();
	}
	//"/preregistration/v1/applications";
	public static String postApplication(ResidentModel resident, DataCallback cb,String contextKey) throws JSONException {
		String result = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "postapplication").toString().trim();
		
		JSONArray requiredFieldsArray=MosipMasterData.getUiSpecId(contextKey);
		JSONObject identity = CreatePersona.createIdentity(resident,cb,contextKey);
		JSONObject demoData = new JSONObject();
		demoData.put("identity",identity);
		JSONObject reqObject = new JSONObject();
		reqObject.put("demographicDetails", demoData);
		reqObject.put("requiredFields", requiredFieldsArray);
		JSONObject reqBody = CreatePersona.createRequestBody(reqObject,false);
		reqObject.put("langCode",resident.getPrimaryLanguage());//resident.getPrimaryLanguare());
		//RestClient client = annotation.getRestClient();
		
		try {
			JSONObject resp = RestClient.postNoAuth (url, reqBody,"prereg",contextKey);
			result = resp.get("preRegistrationId").toString();
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return  result;
	}
	public static String putApplication(ResidentModel resident, String preregId,String contextKey) {
		String result = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +
				VariableManager.getVariableValue(contextKey, "postapplication").toString().trim();
		url = url + "/"+ preregId;
		JSONObject identity = CreatePersona.createIdentity(resident,null,contextKey);
		JSONObject demoData = new JSONObject();
		demoData.put("identity",identity);
		JSONObject reqObject = new JSONObject();
		reqObject.put("demographicDetails", demoData);
		JSONObject reqBody = CreatePersona.createRequestBody(reqObject,true);
		reqObject.put("langCode","eng");//resident.getPrimaryLanguare());
		//RestClient client = annotation.getRestClient();
		
		try {
			//JSONObject resp = RestClient.putNoAuth(url, reqBody,contextKey);
			JSONObject resp = RestClient.putAdminPrereg(url, reqBody,contextKey);
			result = resp.get("preRegistrationId").toString();
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return  result;
		
	}
	
	public static AppointmentModel getAppointments(String contextKey) {
		AppointmentModel appointmentSlot = new AppointmentModel();

		String base = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim();
		String api = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "appointmentslots").toString().trim();
		
		
		String centerId = VariableManager.getVariableValue(contextKey, "mosip.test.prereg.centerid").toString().trim();
		if(centerId.equalsIgnoreCase("automatic"))
		centerId = VariableManager.getVariableValue(contextKey, "mosip.test.regclient.centerid").toString().trim();
		
		
		String url =  base + api + centerId;
		logger.info("BookAppointment:" + url);

		try {
			//JSONObject resp = RestClient.get(url, new JSONObject(), new JSONObject(),contextKey);
			JSONObject resp = RestClient.getNoAuth(url, new JSONObject(), new JSONObject(),contextKey);
			 //JSONObject resp = RestClient.getAdminPreReg(url, new JSONObject(), new JSONObject(),contextKey);
			if(resp != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				appointmentSlot = objectMapper.readValue(resp.toString(),  AppointmentModel.class);	
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}		
		return appointmentSlot;
	}
	public static String cancelAppointment(String preregId, String startTime, String toTime, String appointmentDate, String centerId,String contextKey) {
		String response = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "postappointment").toString().trim();
		JSONObject obj = new JSONObject();
		JSONObject requestObject = new JSONObject();

		obj.put("id", "mosip.pre-registration.booking.book");
		obj.put("version", "1.0");
		obj.put("request", requestObject);
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		requestObject.put("registration_center_id",centerId);
		requestObject.put("appointment_date",appointmentDate);
		requestObject.put("time_slot_from",startTime);
		requestObject.put("time_slot_to",toTime);
		requestObject.put("pre_registration_id",preregId);
		
		try {
			url = url + "/" + preregId;
			//JSONObject resp = RestClient.putNoAuth(url, obj,contextKey); 
			JSONObject resp = RestClient.putAdminPrereg(url, obj,contextKey); 
			response = resp.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
			response = e.getMessage();
		}
		return response;
			
	}
	public static String deleteApplication(String preregId,String contextKey) {
	
		String response = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "deleteApplication").toString().trim();
		System.out.println("url is:");
		url = url + "/" + preregId;		
		try {
			JSONObject resp = RestClient.deleteNoAuth(url, new JSONObject(),contextKey);
			response = resp.toString();
		} catch (Exception e) {
			
			logger.error(e.getMessage());
			response = e.getMessage();
		}
		return response;
	}

	public static String discardBooking(HashMap<String, String> map,String contextKey) {
		
	String response = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+
		VariableManager.getVariableValue( VariableManager.NS_DEFAULT,"postappointment").toString().trim();
		url = url + "?preRegistrationId=" + map.get("preRegistrationId");
		try {
			JSONObject resp = RestClient.deleteNoAuth(url, new JSONObject(),contextKey);
			//JSONObject resp = RestClient.deleteNoAuthWithQueryParam(url, new JSONObject().put("map", map));
			response = resp.toString();
		} catch (Exception e) {
			
			logger.error(e.getMessage());
			response = e.getMessage();
		}
		
		return response;
	}
	
	public static String updatePreRegAppointment(String prid,String contextKey) {
		
		String response = "";
			String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+
			VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "postappointment").toString().trim();
		url = url + "/" + prid;
			try {
				JSONObject resp = RestClient.putPreRegStatus(url, new JSONObject(),contextKey);
				//JSONObject resp = RestClient.deleteNoAuthWithQueryParam(url, new JSONObject().put("map", map));
				response = resp.toString();
			} catch (Exception e) {
				
				logger.error(e.getMessage());
				response = e.getMessage();
			}
			
			return response;
		}
		
		
		
		
	
	
	
	
	
	public static String bookAppointment(String preRegId, String appointmentDate, String centerId, AppointmentTimeSlotModel slot, String contextKey) throws JSONException {

		String result ="";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "postappointment").toString().trim();

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
				JSONObject resp = RestClient.postNoAuth(url, obj,"prereg",contextKey);

				if(resp != null) {
					
					resp.put("appointmentDate", appointmentDate);
					result = resp.toString();
				
					//ObjectMapper objectMapper = new ObjectMapper();
					//appointmentSlot = objectMapper.readValue(resp.toString(),  AppointmentModel.class);	
				}
			

				
			} catch (Exception e) {
				logger.error(e.getMessage());
			}		
			return result;
	}
	public static JSONObject UploadDocument(String docCatCode,String docTypCode, String langCode, String docFilePath, String preRegID, String contextKey) throws JSONException {
		JSONObject response=null;
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +
				VariableManager.getVariableValue(contextKey,"uploaddocument").toString().trim();
				
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
			response = RestClient.uploadFile(url + preRegID,docFilePath,obj,contextKey);
			
		} catch (Exception e) {
		//	logger.error(e.getMessage());
		}
		
		
		return response;
	}
	public static void main(String[] args) throws JSONException {

	
				
		String preRegID ="24728640730673";
		Boolean bBooked = false;
		//CreatePersona.sendOtpTo("sanath@mailinator.com");
		CreatePersona.validateOTP("111111", "sanath@mailinator.com","contextKey");
		AppointmentModel res = getAppointments("contextKey");
		System.out.println(res.getRegCenterId());
		for( CenterDetailsModel a: res.getAvailableDates()) {
			if(!a.getHoliday()) {
				for(AppointmentTimeSlotModel ts: a.getTimeslots()) {
					if(ts.getAvailability() > 0) {
						bookAppointment(preRegID,a.getDate(),res.getRegCenterId(),ts,"contextKey");
						bBooked = true;
						break;
					}
				}
			}
			if(bBooked) break;
		}
	}
}
