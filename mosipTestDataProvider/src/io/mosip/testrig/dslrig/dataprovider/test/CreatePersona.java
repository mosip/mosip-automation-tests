package io.mosip.testrig.dslrig.dataprovider.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.PacketTemplateProvider;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipGenderModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLocationModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.SchemaValidator;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataCallback;
import io.mosip.testrig.dslrig.dataprovider.util.DemographicMissFieldUtil;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class CreatePersona {
	private static final Logger logger = LoggerFactory.getLogger(CreatePersona.class);

	public static JSONObject constructNode(JSONObject identity, String Id, String primLang, String secLang, String primVal, String secVal, Boolean bSimpleType) {
		JSONObject obj = new JSONObject();
		JSONArray array  = new JSONArray();

		obj.put("language", primLang);
		if(primVal != null && primVal.equals(""))
			obj.put("value", JSONObject.NULL);
		else
			obj.put("value", primVal);
		if(bSimpleType){
			array.put(0, obj);

			if(secLang != null) {	
				obj = new JSONObject();
				obj.put("language", secLang);
				if(secVal != null && secVal.equals(""))
					obj.put("value", JSONObject.NULL);
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
	public static List<String> validateIDObject(JSONObject mergedJsonMap,String contextKey){
		Hashtable<Double, Properties> tblschema  = MosipMasterData.getIDSchemaLatestVersion(contextKey);
		List<MosipIDSchema> schema = (List<MosipIDSchema>) tblschema.get( tblschema.keys().nextElement() ).get("schemaList");
		JSONObject identity = mergedJsonMap.getJSONObject("identity");
		List<String> failedSchemaIds = new ArrayList<String>();

		for(MosipIDSchema s: schema) {
			String id = s.getId();
			if(s.getRequired() || s.getInputRequired()) {
				List<SchemaValidator> validators = s.getValidators();
				if(validators != null) {
					if(!identity.has(id)) {
						failedSchemaIds.add(id);
						continue;
					}
					String fValue = "";
					if(s.getType().equals("simpleType")) {
						JSONArray arr = identity.getJSONArray(id);
						if(!arr.isEmpty() && arr.getJSONObject(0).has("value") )
							fValue = arr.getJSONObject(0).get("value").toString();
					}
					else
					{
						fValue  =identity.get(id).toString();
					}

					for(SchemaValidator v: validators) {
						if(v.getType().equals("regex")) {
								String expr = v.getValidator();
								if(!fValue.matches(expr))
									failedSchemaIds.add(id);
						}
					}
				}

			}
		}
		return failedSchemaIds;
	}
	public static JSONObject createIdentity(ResidentModel resident, DataCallback cb,String contextKey) {

		Hashtable<Double,Properties> tbl1 = MosipMasterData.getIDSchemaLatestVersion(contextKey);
		Hashtable<Double,Properties> tbl = MosipMasterData.getPreregIDSchemaLatestVersion(contextKey);
		Double preRegUISpecVersion = tbl.keys().nextElement();
		Double schemaversion = tbl1.keys().nextElement();
		List<MosipIDSchema>  lstSchema =(List<MosipIDSchema>) tbl.get(preRegUISpecVersion).get("schemaList");
		List<String> requiredAttribs = (List<String>) tbl1.get(schemaversion).get("requiredAttributes");
		JSONArray locaitonherirachyArray = (JSONArray)tbl.get(preRegUISpecVersion).get("locaitonherirachy");

		JSONObject identity = new JSONObject();

		Hashtable<String, MosipLocationModel> locations =   resident.getLocation();
		Set<String> locationSet =  locations.keySet();
		Hashtable<String,List<DynamicFieldModel>> dynaFields = resident.getDynaFields();
		Hashtable<String,List<MosipGenderModel>> genderTypes = resident.getGenderTypes();

		identity.put("IDSchemaVersion",schemaversion );
		if(cb != null)
			cb.logDebug("createIdentity:schemaversion=" + schemaversion);

		List<String> lstMissedAttributes = resident.getMissAttributes();
		if (lstMissedAttributes != null && !lstMissedAttributes.isEmpty()) {
			lstMissedAttributes = DemographicMissFieldUtil.expandMissAttributeIds(new ArrayList<>(lstMissedAttributes),
					lstSchema, contextKey);
		}

		for(MosipIDSchema schemaItem: lstSchema) {

			boolean found = false;
			if(cb != null) {
				cb.logDebug(schemaItem.toJSONString());
			}


			if (!CommonUtil.isExists(requiredAttribs, schemaItem.getId()))
				continue;


			if(lstMissedAttributes != null && lstMissedAttributes.stream().anyMatch( v -> v.equalsIgnoreCase(schemaItem.getId()))) {
				continue;
			}


			if( schemaItem.getType() != null &&
					( schemaItem.getType().equals("documentType") || schemaItem.getType().equals("biometricsType"))) {
				continue;
			}
			if(!(schemaItem.getRequired())) {
				continue;
			}
			if(schemaItem.getId().equals("IDSchemaVersion"))
				continue;

			if(schemaItem.getType() == null )
				continue;

			if(PacketTemplateProvider.processDynamicFields(schemaItem, identity, resident,contextKey))
				continue;


			if(schemaItem.getFieldType().equals("dynamic")) {

				if(schemaItem.getId().toLowerCase().contains("residen")  ) {
					String name = resident.getResidentStatus().getCode() ;

					constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
											resident.getSecondaryLanguage(),
											name,
											name,
											schemaItem.getType().equals("simpleType") ? true: false
							);
					continue;				
				}
				else {
					found = false;	

					if(found)
						continue;
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
					continue;
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
					continue;
			}	
			else
			if(schemaItem.getId().toLowerCase().contains("address")) {

				Pair<String,String> addrLines = PacketTemplateProvider.processAddresslines(schemaItem, resident, identity,contextKey);

				constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(),
							addrLines.getValue0(),
							addrLines.getValue1(),
							schemaItem.getType().equals("simpleType") ? true: false
				);
				continue;
			}
			else
			if(schemaItem.getId().toLowerCase().equals("dateofbirth") ||schemaItem.getId().toLowerCase().equals("dob") || schemaItem.getId().toLowerCase().equals("birthdate") ) {
						String strDate= resident.getDob();
						constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(),
								strDate,
								strDate,
								schemaItem.getType().equals("simpleType") ? true: false
						);
						continue;
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
						continue;
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
						continue;

			}
			else
			if(schemaItem.getId().toLowerCase().equals("gender")){
						String primaryValue ="Female";
						if(resident.getGender() == Gender.Male)
							primaryValue = "Male";
						String secValue = primaryValue;


						constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(),
								primaryValue,
								secValue,
								schemaItem.getType().equals("simpleType") ? true: false
						);
						continue;

			}
			else
			{
				found = false;

				for (int i = 0; i < locaitonherirachyArray.length(); i++) {
					JSONArray jsonArray = locaitonherirachyArray.getJSONArray(i);
					for (int j = 0; j < jsonArray.length(); j++) {
						String id = jsonArray.getString(j);
						logger.info(id);

						if (schemaItem.getId().toLowerCase().equals(id.toLowerCase())) {

							String locLevel = getLocatonLevel(j+1,locations);

							constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
									resident.getSecondaryLanguage(), locations.get(locLevel).getCode(),
									locations.get(locLevel).getCode(),
									schemaItem.getType().equals("simpleType") ? true : false);
							found = true;
							break;
						}
					}
				}

			}


				if(found)
					continue;

				String someVal= null;
				List<SchemaValidator>  validators = schemaItem.getValidators();
				if(validators != null) {
					for(SchemaValidator v: validators) {
						if(v.getType().equalsIgnoreCase("regex")) {
							String regexpr = v.getValidator();
							if(regexpr != null && !regexpr.equals(""))
								try {
									someVal = CommonUtil.genStringAsperRegex(regexpr,contextKey);
								} catch (Exception e) {
									logger.error(e.getMessage());
								}
						}
					}
				}
				if(someVal == null)
					someVal = CommonUtil.generateRandomString(schemaItem.getMaximum());

				constructNode(identity, schemaItem.getId(), resident.getPrimaryLanguage(),
						resident.getSecondaryLanguage(),
						someVal,
						someVal,
						schemaItem.getType().equals("simpleType") ? true: false
				);

			}


		return identity;


	}

	private static String getLocatonLevel(int levelCode,Hashtable<String, MosipLocationModel> locations) {
		Enumeration<String> e = locations.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			MosipLocationModel mLocModel = locations.get(key);
			if (mLocModel.getHierarchyLevel() == levelCode) {
				return key;
			}
		}
		return null;
	}

	public static JSONObject createRequestBody(JSONObject requestObject, Boolean bUpdate) throws JSONException {


		JSONObject obj = new JSONObject();
		if(bUpdate)
			obj.put("id", "mosip.pre-registration.demographic.update");
		else
			obj.put("id", "mosip.pre-registration.demographic.create");
		obj.put("version", "1.0");
		obj.put("request", requestObject);
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));

		 return obj;
	}

	public static String sendOtpTo(String to, String langCode, String contextKey) throws JSONException {


		String response ="";
		JSONObject obj = new JSONObject();
		obj.put("id", "mosip.pre-registration.login.sendotp");
		obj.put("version", "1.0");
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		JSONObject req = new JSONObject();
		req.put("userId", to);
		req.put("langCode", langCode);
		obj.put("request", req);

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +"preregistration/v1/login/sendOtp/langcode";

		try {
			JSONObject resp = RestClient.postNoAuthvalidate(url, obj, "sendotp",contextKey);

			response = resp.toString();
		} catch (Exception e) {

			logger.error(e.getMessage());
		}
		return response;
	}

	public static String sendOtpToPhone(String mobile,String contextKey) throws JSONException {
		return sendOtpTo(mobile, "",contextKey);
	}

	public static String validateOTP(String otp, String mobileOrEmailId,String contextKey) throws JSONException {
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +"preregistration/v1/login/validateOtp";
		String response ="";
		JSONObject obj = new JSONObject();
		obj.put("id", "mosip.pre-registration.login.useridotp");
		obj.put("version", "1.0");
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		JSONObject req = new JSONObject();
		req.put("otp",otp);
		req.put("userId",mobileOrEmailId);

		obj.put("request", req);


		try {
			JSONObject resp = RestClient.postNoAuthvalidate (url, obj,"prereg",contextKey);

			response = resp.toString();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return response;

	}

}
