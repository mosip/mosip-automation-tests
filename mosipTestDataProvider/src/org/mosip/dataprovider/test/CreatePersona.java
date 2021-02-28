package org.mosip.dataprovider.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.mosip.dataprovider.models.DynamicFieldModel;
import org.mosip.dataprovider.models.MosipGenderModel;
import org.mosip.dataprovider.models.MosipIDSchema;
import org.mosip.dataprovider.models.MosipLocationModel;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.SchemaValidator;
import org.mosip.dataprovider.preparation.MosipMasterData;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataCallback;
import org.mosip.dataprovider.util.RestClient;
import org.mosip.dataprovider.util.Translator;
import org.mvel2.MVEL;

import io.cucumber.core.gherkin.messages.internal.gherkin.internal.com.eclipsesource.json.Json;
import variables.VariableManager;

public class CreatePersona {

	static 	Hashtable<Double,List<MosipIDSchema>> tbl;
	
	public static JSONObject constructNode(JSONObject identity, String Id, String primLang, String secLang, String primVal, String secVal, Boolean bSimpleType) {
		JSONObject obj = new JSONObject();
		JSONArray array  = new JSONArray();
		
		obj.put("language", primLang);
		if(primVal != null && primVal.equals(""))
			obj.put("value", Json.NULL);
		else
			obj.put("value", primVal);
		if(bSimpleType){
			array.put(0, obj);
			
			if(secLang != null) {	
				obj = new JSONObject();
				obj.put("language", secLang);
				if(secVal != null && secVal.equals(""))
					obj.put("value", Json.NULL);
				else
					obj.put("value", secVal);
				array.put(1, obj);
			}
			identity.put(Id, array);
		}
		else
			identity.put(Id, obj.get("value"));
		
		return identity;
	}

	 static Boolean validateCondn(String cndnexpr, Object inputObject) {
		return MVEL.evalToBoolean(cndnexpr,inputObject);
	}
	public static List<String> validateIDObject(JSONObject mergedJsonMap){
		Hashtable<Double, List<MosipIDSchema>> tblschema  = MosipMasterData.getIDSchemaLatestVersion();
		List<MosipIDSchema> schema = tblschema.get( tblschema.keys().nextElement() );
		JSONObject identity = mergedJsonMap.getJSONObject("identity");
		List<String> failedSchemaIds = new ArrayList<String>();
		
		for(MosipIDSchema s: schema) {
			if(s.getRequired() || s.getInputRequired()) {
				List<SchemaValidator> validators = s.getValidators();
				if(validators != null) {
					if(!identity.has(s.getId())) {
						failedSchemaIds.add(s.getId());
						continue;
					}
					String fValue = "";
					if(s.getType().equals("simpleType")) {
						JSONArray arr = identity.getJSONArray(s.getId());
						fValue = arr.getJSONObject(0).get("value").toString();
					}
					else
					{
						fValue  =identity.get(s.getId()).toString();
					}
					
					for(SchemaValidator v: validators) {
						if(v.getType().equals("regex")) {
								String expr = v.getValidator();
								if(!fValue.matches(expr))
									failedSchemaIds.add(s.getId());
						}
					}
				}
				//validate requiredon
			}
		}
		return failedSchemaIds;
	}
	public static JSONObject crateIdentity(ResidentModel resident, DataCallback cb) {

		tbl = MosipMasterData.getIDSchemaLatestVersion();
		Double schemaversion = tbl.keys().nextElement();
		List<MosipIDSchema>  lstSchema =tbl.get(schemaversion);
		
		JSONObject identity = new JSONObject();

		Hashtable<String, MosipLocationModel> locations =   resident.getLocation();
		Set<String> locationSet =  locations.keySet();
		List<DynamicFieldModel> dynaFields = resident.getDynaFields();
		List<MosipGenderModel> genderTypes = resident.getGenderTypes();
		
		identity.put("IDSchemaVersion",schemaversion );
		if(cb != null)
			cb.logDebug("createIdentity:schemaversion=" + schemaversion);
		//ApplicationConfigSchemaItem schemaItem = null;
		List<String> lstMissedAttributes = resident.getMissAttributes();
		
		for(MosipIDSchema schemaItem: lstSchema) {

			if(cb != null) {
				cb.logDebug(schemaItem.toJSONString());
			}
			if(lstMissedAttributes != null && lstMissedAttributes.stream().anyMatch( v -> v.equalsIgnoreCase(schemaItem.getId()))) {
				continue;
			}
			//skip document types
			//"type": "documentType","type": "biometricsType",
			if( schemaItem.getType() != null &&
					( schemaItem.getType().equals("documentType") || schemaItem.getType().equals("biometricsType"))) {
				continue;
			}
			if(!schemaItem.getRequired() && !schemaItem.getInputRequired()) {
				continue;
			}
			if(schemaItem.getType() != null &&
				( schemaItem.getContactType() != null  || schemaItem.getGroup() != null)
					
			){
				if(schemaItem.getControlType().equals("dropdown")) {
					if(schemaItem.getFieldType().equals("dynamic")) {
						for(DynamicFieldModel dfm: dynaFields) {
							if(dfm.getActive() && dfm.getId().equals(schemaItem.getId())) {

								constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(), resident.getSecondaryLanguage(),
											dfm.getFieldVal().get(0).getValue(),
											dfm.getFieldVal().get(0).getValue(),
											schemaItem.getType().equals("simpleType") ? true: false
											);
								break;
							}
						}
					}
					//known lookups
					else {
						if(schemaItem.getSubType().toLowerCase().equals("gender")  ) {
							
							String primLang = resident.getPrimaryLanguage();
							String secLan = resident.getSecondaryLanguage();
							String resGen = resident.getGender();
							String primVal = "";
							String secVal = "";
							for(MosipGenderModel g: genderTypes) {
								if(!g.getIsActive())
									continue;
								if(g.getGenderName().equals(resGen)) {

									if(g.getLangCode().equals(primLang) ) {
										primVal = g.getCode();
									}
									else
									if(secLan != null && g.getLangCode().equals(secLan) ) {
											
										secVal =  g.getCode();
									
									}
								}
							}
							if(secVal.equals(""))
								secVal = primVal;
							constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(), resident.getSecondaryLanguage(),
									primVal,
									secVal,
									schemaItem.getType().equals("simpleType") ? true: false
							);
	
							
						}
						else
						if(schemaItem.getId().toLowerCase().contains("residen")  ) {
							String name = resident.getResidentStatus().getCode() ;

							constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
											resident.getSecondaryLanguage(),
											name,
											name,
											schemaItem.getType().equals("simpleType") ? true: false
							);
									
						}
						/* check with location details */
						else {
							
							for(String locLevel: locationSet) {
				
								if(schemaItem.getSubType().toLowerCase().contains(locLevel.toLowerCase())) {
									
			//					if(schemaItem.getId().toLowerCase().contains(locLevel.toLowerCase())) {
									constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
											resident.getSecondaryLanguage(),
											locations.get(locLevel).getCode(),
											locations.get(locLevel).getCode(),
											schemaItem.getType().equals("simpleType") ? true: false
									);
									break;
								}
							}
						}
					}
				}
				else 
				if(schemaItem.getId().toLowerCase().equals("fullname")) {
					String name = resident.getName().getFirstName();
					if(resident.getName().getMidName() != null && !resident.getName().getMidName().equals(""))
						name = name + " " + resident.getName().getMidName();
					name = name +  " "+ resident.getName().getSurName();
					name = name.trim();
					
					String name_sec="";
					if(resident.getSecondaryLanguage() != null) {
					
						name_sec = resident.getName_seclang().getFirstName();
						if(resident.getName_seclang().getMidName() != null && !resident.getName_seclang().getMidName().equals(""))
							name_sec = name_sec + " " + resident.getName_seclang().getMidName();
						name_sec = name_sec +  " "+ resident.getName_seclang().getSurName();
						name_sec = name_sec.trim();
						
						name_sec = resident.getName_seclang().getFirstName() +" "+ resident.getName_seclang().getMidName() + " "+ resident.getName_seclang().getSurName();
					}
					constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
						resident.getSecondaryLanguage(),
						name,
						name_sec,
						schemaItem.getType().equals("simpleType") ? true: false
					);
				}
				else
				if(schemaItem.getId().toLowerCase().equals("firstname") ||
						schemaItem.getId().toLowerCase().equals("lastname") ||
						schemaItem.getId().toLowerCase().equals("middlename") 
				) {
					
					String name = "";
					String name_sec="";
					
					if(schemaItem.getId().toLowerCase().equals("firstname")) {
						name = resident.getName().getFirstName() ;
						if(resident.getSecondaryLanguage() != null)
							name_sec = resident.getName_seclang().getFirstName();
					}
					else
					if(schemaItem.getId().toLowerCase().equals("lastname")) {
						name = resident.getName().getSurName() ;
						if(resident.getSecondaryLanguage() != null)
							name_sec = resident.getName_seclang().getSurName();
					}	
					else
					if(schemaItem.getId().toLowerCase().equals("middlename")) {
							name = resident.getName().getMidName() ;
							if(resident.getSecondaryLanguage() != null)
								name_sec = resident.getName_seclang().getMidName();
					}
					constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(),
							name,
							name_sec,
							schemaItem.getType().equals("simpleType") ? true: false
					);
				}	
				else
				if(schemaItem.getId().toLowerCase().contains("address")) {
				
					String addr = "";
					String addr_sec="";
					Random rand = new Random();
					
					addr = "#%d, %d Street, %d block" ;//+ schemaItem.getId();
					addr = String.format(addr, (100+ rand.nextInt(999)),
							(1 + rand.nextInt(99)),
							(1 + rand.nextInt(10))
							);
					if(schemaItem.getMaximum() > 0 && addr.length() >= schemaItem.getMaximum() )
						addr = addr.substring(0,schemaItem.getMaximum() -1);
					
					if(resident.getSecondaryLanguage() != null)
						addr_sec =Translator.translate(resident.getSecondaryLanguage(),addr);
					constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(),
							addr,
							addr_sec,
							schemaItem.getType().equals("simpleType") ? true: false
					);
				}
				else
				if(schemaItem.getId().toLowerCase().equals("dateofbirth") ||schemaItem.getId().toLowerCase().equals("dob") || schemaItem.getId().toLowerCase().equals("birthdate") ) {
						
						//should be informat yyyy/mm/dd
						//SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");  
						String strDate= resident.getDob();
						constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(),
								strDate,
								strDate,
								schemaItem.getType().equals("simpleType") ? true: false
						);
				}
				else
				if(schemaItem.getId().toLowerCase().contains("phone") || schemaItem.getId().toLowerCase().contains("mobile") ) {
					String mobileNo =   resident.getContact().getMobileNumber();
					constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(),
							mobileNo,
							mobileNo,
							schemaItem.getType().equals("simpleType") ? true: false
					);
					
				}
				else
				if(schemaItem.getId().toLowerCase().contains("email") || schemaItem.getId().toLowerCase().contains("mail") ) {
						
						String emailId =   resident.getContact().getEmailId();
						constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(),
								emailId,
								emailId,
								schemaItem.getType().equals("simpleType") ? true: false
						);
				}
				else
				if(schemaItem.getId().toLowerCase().contains("referenceidentity") ) {
					
						String id = resident.getId();	
						constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(),
								id,
								id,
								schemaItem.getType().equals("simpleType") ? true: false
						);
						
				}
			}
			else
			if(schemaItem.getRequired() || schemaItem.getInputRequired()) {
				String someVal = CommonUtil.generateRandomString(schemaItem.getMaximum());
				if(schemaItem.getId().equals("IDSchemaVersion"))
					someVal = Double.toString(schemaversion);
				
				constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
						resident.getSecondaryLanguage(),
						someVal,
						someVal,
						schemaItem.getType().equals("simpleType") ? true: false
				);
		
			}
		}
		

		return identity;
		
		
	}
		/*
	public static JSONObject crateIdentityOld(ResidentModel resident) throws JSONException {
		//columns which are not arrays -IDSchemaVersion,Phone, email
		
		JSONObject identity = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
	
		Double schemaversion = tbl.keys().nextElement();
		List<MosipIDSchema> schemas = tbl.get(schemaversion);
		
		identity.put("IDSchemaVersion", schemaversion);

		List<MosipLocationModel> locations =   resident.getLocation();
		List<MosipLocationModel> locations_seclang =   resident.getLocation_seclang();
		
		for(MosipIDSchema schema:schemas ) {

			if(schema.getId().equals("fullName") && schema.getRequired()) {
				String name = resident.getName().getFirstName() +" " + resident.getName().getMidName()+ " "+ resident.getName().getSurName();
		
				obj.put("language", resident.getPrimaryLanguage());
				obj.put("value", name);
				array.put(0, obj);
	
				if(resident.getSecondaryLanguage() != null) {	
					name = resident.getName_seclang().getFirstName() +" "+ resident.getName_seclang().getMidName() + " "+ resident.getName_seclang().getSurName();
					obj = new JSONObject();
					obj.put("language", resident.getSecondaryLanguage());
					obj.put("value", name);
					array.put(1, obj);
				}
				identity.put("fullName", array);
			}
			if(schema.getId().equals("dateOfBirth") && schema.getRequired()) {
				
				//should be informat yyyy/mm/dd
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");  
				String strDate= formatter.format(resident.getDob());
				String gender ="FML";
				identity.put("dateOfBirth", strDate);
				if(resident.getGender().equals("Male"))
					gender = "MLE";
				
				array = new JSONArray();
				obj = new JSONObject();
				obj.put("language", resident.getPrimaryLanguage());
				obj.put("value", gender);
				array.put(0, obj);
				if(resident.getSecondaryLanguage() != null) {	
				
					obj = new JSONObject();
					obj.put("language", resident.getSecondaryLanguage());
					obj.put("value", gender);
					array.put(1, obj);
				}
				identity.put("gender", array);
			}
			if(schema.getId().equals("addressLine1") && schema.getRequired()) {
					
				String addr = "#11, 127th Main, Golden Heights";
				array = new JSONArray();
				obj = new JSONObject();
				obj.put("language", resident.getPrimaryLanguage());
				obj.put("value", addr);
				array.put(0, obj);
				
				if(resident.getSecondaryLanguage() != null) {	
					
					obj = new JSONObject();
					obj.put("language", resident.getSecondaryLanguage());
					obj.put("value", Translator.translate(resident.getSecondaryLanguage(),addr));
					array.put(1, obj);
				}
				identity.put("addressLine1", array);
			}
			if(schema.getId().equals("addressLine2") && schema.getRequired()) {
				
				String addr = "abcd area";
				array = new JSONArray();
				obj = new JSONObject();
				obj.put("language", resident.getPrimaryLanguage());
				obj.put("value", addr);
				array.put(0, obj);
				
				if(resident.getSecondaryLanguage() != null) {	
					
					obj = new JSONObject();
					obj.put("language", resident.getSecondaryLanguage());
					obj.put("value", Translator.translate(resident.getSecondaryLanguage(),addr));
					array.put(1, obj);
				}
				identity.put("addressLine2", array);
			}
			if(schema.getId().equals("addressLine3") && schema.getRequired()) {
				
				String addr = "xyz county";
				array = new JSONArray();
				obj = new JSONObject();
				obj.put("language", resident.getPrimaryLanguage());
				obj.put("value", addr);
				array.put(0, obj);
				
				if(resident.getSecondaryLanguage() != null) {	
					
					obj = new JSONObject();
					obj.put("language", resident.getSecondaryLanguage());
					obj.put("value", Translator.translate(resident.getSecondaryLanguage(),addr));
					array.put(1, obj);
				}
				identity.put("addressLine3", array);
			}
			if(schema.getId().equals("residenceStatus") && schema.getRequired()) {
						
				//Resident status
				array = new JSONArray();
				obj = new JSONObject();
				obj.put("language", resident.getPrimaryLanguage());
				obj.put("value", resident.getResidentStatus());
				array.put(0, obj);
					
				if(resident.getSecondaryLanguage() != null) {	
					
					obj = new JSONObject();
					obj.put("language", resident.getSecondaryLanguage());
					obj.put("value", resident.getResidentStatus_seclang());
					array.put(1, obj);
				}
				identity.put("residenceStatus", array);
			}
			// construct as per location hierarchy
			array = new JSONArray();
			Boolean bFound= false;
			for(MosipLocationModel locModel: locations) {
				
				System.out.println("Schema.id="+ schema.getId() + "== locModel[" + locModel.getHierarchyLevel() + "]=" +locModel.getHierarchyName());
			
				if(schema.getId().equalsIgnoreCase(locModel.getHierarchyName()) && schema.getRequired() ) {
	
			
					obj = new JSONObject();
					obj.put("language", locModel.getLangCode());
					obj.put("value", locModel.getName());
					array.put(0, obj);
					bFound= true;
					break;
				}
			}
			if(locations_seclang != null)			
			for(MosipLocationModel locModel: locations_seclang) {
				
				if(schema.getId().equalsIgnoreCase(locModel.getHierarchyName()) && schema.getRequired() ) {
					obj = new JSONObject();
					obj.put("language", locModel.getLangCode());
					obj.put("value", locModel.getName());
					array.put(1, obj);
					break;
				}
			}
			if(bFound) {
				if(schema.getId().equals("postalCode")) {
					JSONObject objPostal= array.getJSONObject(0);
					identity.put(schema.getId(), objPostal.get("value"));
				}
				else
					identity.put(schema.getId(), array);
			}
		
		}
		identity.put("phone",  resident.getContact().getMobileNumber());
		identity.put("email",  resident.getContact().getEmailId());
		identity.put("referenceIdentityNumber",  resident.getId());	
		return identity;
		
	}
	*/
	public static JSONObject createRequestBody(JSONObject requestObject) throws JSONException {
		
		
		JSONObject obj = new JSONObject();
		obj.put("id", "mosip.pre-registration.demographic.create");
		obj.put("version", "1.0");
		obj.put("request", requestObject);
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		
		 return obj;
	}
	/*
	 * {"id":"mosip.pre-registration.login.sendotp",
	 *    "request":{"userId":"9845024662"},
	 *    "version":"1.0","requesttime":"2020-12-05T10:01:50.763Z"
	 *    }
	 */
	public static String sendOtpTo(String to) throws JSONException {
		//urlBase
		//https://dev.mosip.net//preregistration/v1/login/sendOtp
		String response ="";
		JSONObject obj = new JSONObject();
		obj.put("id", "mosip.pre-registration.login.sendotp");
		obj.put("version", "1.0");
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		JSONObject req = new JSONObject();
		req.put("userId", to);	
		obj.put("request", req);
		//RestClient client = annotation.getRestClient();
		String url = VariableManager.getVariableValue("urlBase").toString().trim() +"preregistration/v1/login/sendOtp";
	//	url = "https://dev.mosip.net/preregistration/v1/login/sendOtp";
		try {
			JSONObject resp = RestClient.postNoAuth(url, obj);
			response = resp.toString();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return response;
	}
	
	public static String sendOtpToPhone(String mobile) throws JSONException {
		return sendOtpTo(mobile);
	}
	/*
	 * {"id":"mosip.pre-registration.login.useridotp",
	 *  "request":{
	 *        "otp":"111111","userId":"abc.efg@gmail.com"
	 *        },
	 *        "version":"1.0",
	 *        "requesttime":"2020-12-05T10:32:52.541Z"
	 *   }
	 */
	public static String validateOTP(String otp, String mobileOrEmailId) throws JSONException {
		String url = VariableManager.getVariableValue("urlBase").toString().trim() +"/preregistration/v1/login/validateOtp";
		String response ="";
		JSONObject obj = new JSONObject();
		obj.put("id", "mosip.pre-registration.login.useridotp");
		obj.put("version", "1.0");
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		JSONObject req = new JSONObject();
		req.put("otp",otp);
		req.put("userId",mobileOrEmailId);
		
		obj.put("request", req);
		
		//RestClient client = annotation.getRestClient();

		
		try {
			JSONObject resp = RestClient.postNoAuth (url, obj);
			response = resp.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return response;
		
	}
		
}
