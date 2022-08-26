package org.mosip.dataprovider.preparation;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mosip.dataprovider.ResidentDataProvider;
import org.mosip.dataprovider.models.ApplicationConfigIdSchema;

import org.mosip.dataprovider.models.DynamicFieldModel;
import org.mosip.dataprovider.models.DynamicFieldValueModel;
import org.mosip.dataprovider.models.MosipIdentity;
import org.mosip.dataprovider.models.LocationHierarchyModel;
import org.mosip.dataprovider.models.MosipBiometricAttributeModel;
import org.mosip.dataprovider.models.MosipBiometricTypeModel;
import org.mosip.dataprovider.models.MosipDocCategoryModel;
import org.mosip.dataprovider.models.MosipDocTypeModel;
import org.mosip.dataprovider.models.MosipGenderModel;
import org.mosip.dataprovider.models.MosipIDSchema;
import org.mosip.dataprovider.models.MosipIndividualTypeModel;
import org.mosip.dataprovider.models.MosipLanguage;
import org.mosip.dataprovider.models.MosipLocationModel;
import org.mosip.dataprovider.models.MosipPreRegLoginConfig;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.SchemaRule;
import org.mosip.dataprovider.models.setup.MosipDeviceModel;
import org.mosip.dataprovider.models.setup.MosipMachineModel;
import org.mosip.dataprovider.test.CreatePersona;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.Gender;
import org.mosip.dataprovider.util.ResidentAttribute;
import org.mosip.dataprovider.util.RestClient;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import variables.VariableManager;

public  class MosipMasterData {

/*	static {
		VariableManager.Init();
	}
*/
	public static Object getCache(String key) {
		try {
		return VariableManager.getVariableValue(key);
		}catch(Exception e) {
			
		}
		return null;
	}
	public static void setCache(String key, Object value) {
		
		VariableManager.setVariableValue(key,  value);
	}

	public static List<MosipBiometricAttributeModel> getBiometricAttrByTypes(String bioType,String lang){
		
		List<MosipBiometricAttributeModel> biotypes =null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"biometricAttributes").toString();
		url = url + lang + "/" + bioType;
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipBiometricAttributeModel>) o);
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray langArray = resp.getJSONArray("biometricattributes");
				ObjectMapper objectMapper = new ObjectMapper();
				
				 biotypes = objectMapper.readValue(langArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipBiometricAttributeModel.class));
				
				setCache(url,  biotypes);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return  biotypes;
		

	}

	//
	public static List<MosipBiometricTypeModel> getBiometricTypes(){
		
		List<MosipBiometricTypeModel> biotypes =null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"biometricTypes").toString();
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipBiometricTypeModel>) o);
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray langArray = resp.getJSONArray("biometrictypes");
				ObjectMapper objectMapper = new ObjectMapper();
				
				 biotypes = objectMapper.readValue(langArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipBiometricTypeModel.class));
				
				setCache(url,  biotypes);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return  biotypes;
		

	}
	public static List<MosipLanguage> getConfiguredLanguages() {
		List<MosipLanguage> langs =null;
			
		String url = VariableManager.getVariableValue("urlBase").toString() +VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"languages").toString();

		Object o =getCache(url);
		if(o != null)
			return( (List<MosipLanguage>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray langArray = resp.getJSONArray("languages");
				ObjectMapper objectMapper = new ObjectMapper();
				
				langs = objectMapper.readValue(langArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipLanguage.class));
				
				setCache(url, langs);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return langs;
		
	}
	
	public static Hashtable<String,List<DynamicFieldModel>> getAllDynamicFields() {
		
		Hashtable<String,List<DynamicFieldModel>> tblDynaFieldsLang = new Hashtable<String,List<DynamicFieldModel>>();
		
		//List<DynamicFieldModel> lstDynamicFields = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"dynamicFields").toString();
	
		Object o =getCache(url);
		if(o != null)
			return( (Hashtable<String,List<DynamicFieldModel>>) o);
	
		int pageno = 0;
		int nPages = 0;
		do {
			try {
				List<DynamicFieldModel> lstDynamicFieldsPart = null;
				String urlQuery = url + "?pageNumber="+ pageno;
				JSONObject resp = RestClient.get(urlQuery,new JSONObject() , new JSONObject());
				if(resp != null) {
					// "pageNo":0,"totalPages":11
					pageno = resp.getInt("pageNo");
					nPages = resp.getInt("totalPages");
					
					JSONArray dynaFields = resp.getJSONArray("data");
					ObjectMapper objectMapper = new ObjectMapper();
					
					lstDynamicFieldsPart = objectMapper.readValue(dynaFields.toString(), 
							objectMapper.getTypeFactory().constructCollectionType(List.class, DynamicFieldModel.class));
		/*			if(lstDynamicFields == null)
						lstDynamicFields = lstDynamicFieldsPart;
					else
						lstDynamicFields.addAll(lstDynamicFieldsPart);
		*/
					if(lstDynamicFieldsPart.size() ==0)
						break;
					for(DynamicFieldModel m: lstDynamicFieldsPart) {
						List<DynamicFieldModel> lst = tblDynaFieldsLang.get(m.getLangCode());
						if(lst == null) {
							lst = new ArrayList<DynamicFieldModel>();
							tblDynaFieldsLang.put(m.getLangCode(), lst);
						}
						lst.add(m);
						
					}
					setCache(url, tblDynaFieldsLang);
				
					pageno++;
				}
			} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}while(pageno <= nPages);
			
		
		return tblDynaFieldsLang;
	}
	public static HashMap<String,LocationHierarchyModel[]> getAllLocationHierarchies() {
		
		HashMap<String,LocationHierarchyModel[]> locationHierarchies = new HashMap<String,LocationHierarchyModel[]>();
		List<MosipLanguage> langs =  getConfiguredLanguages();
		langs.forEach( (l) ->{
//			System.out.println(l.getCode() + " "+ l.getName());
			try {
				LocationHierarchyModel[] locationPerLanguage = getLocationHierarchy(l.getCode());
				locationHierarchies.put(l.getCode(), locationPerLanguage);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		});
		return locationHierarchies;
	}
	public static LocationHierarchyModel[] getLocationHierarchy(String langCode) {
		
		LocationHierarchyModel [] locationHierarchy = null;
		
		List<LocationHierarchyModel> locHierarchy =null;
			
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"locationhierarchy").toString();
		Object o =getCache(url);
		if(o != null)
			return( (LocationHierarchyModel[]) o);
	
		try {
			JSONObject resp = RestClient.get(url+ langCode,new JSONObject() , new JSONObject());
		//	System.out.println(resp.toString());
			if(resp != null) {
				JSONArray langArray = resp.getJSONArray("locationHierarchyLevels");
				ObjectMapper objectMapper = new ObjectMapper();
				locHierarchy = objectMapper.readValue(langArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, LocationHierarchyModel.class));
				if(locHierarchy != null) {
					locationHierarchy = new LocationHierarchyModel[ locHierarchy.size()];
					int index = 0;
					for (LocationHierarchyModel object : locHierarchy) {
						if(object.getIsActive()) {
							locationHierarchy[index] = object;
							index++;
							//if(object.getHierarchyLevel() < locHierarchy.size()-1 )
							//	locationHierarchy[object.getHierarchyLevel()] = object;
						}
					}
				}
	
				setCache(url, locationHierarchy);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return locationHierarchy;
		
	}
	public static List<MosipLocationModel> getImmedeateChildren(String locCode, String langCode){

		List<MosipLocationModel> locList = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
		"v1/masterdata/locations/immediatechildren/";
		url = url+ locCode + "/" + langCode ;

		Object o =getCache(url);
		if(o != null)
			return( (List<MosipLocationModel>) o);
	
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			JSONArray locArray = resp.getJSONArray("locations");
			
			if(locArray != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				locList = objectMapper.readValue(locArray.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, MosipLocationModel.class));
	
				List<MosipLocationModel> newLocList = new ArrayList<MosipLocationModel>(); 
				for(MosipLocationModel lm: locList) {
					if(lm.getIsActive())
						newLocList.add(lm);
				}
				locList = newLocList;
				setCache(url, locList);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return locList;
	
	}
	
	public static MosipPreRegLoginConfig getPreregLoginConfig() {
		MosipPreRegLoginConfig config = new MosipPreRegLoginConfig();
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue("loginconfig").toString();
		Object o =getCache(url);
		if(o != null)
			return( (MosipPreRegLoginConfig) o);


		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			//JSONObject configObject = resp.getJSONObject("response");
			
			if(resp != null) {
				try {
					config.setMosip_country_code(  resp.getString("mosip.country.code"));
				}catch(Exception e) {
					
				}
				try {
					config.setMandatory_languages(resp.getString("mosip.mandatory-languages"));
				}catch(Exception e) {
					
				}
				try {
					config.setMin_languages_count(resp.getString("mosip.min-languages.count"));
				}catch(Exception e) {
					
				}
				try {
					config.setOptional_languages(resp.getString("mosip.optional-languages"));
				}catch(Exception e) {
					
				}
				try {
					config.setMosip_id_validation_identity_dateOfBirth(resp.getString("mosip.id.validation.identity.dateOfBirth"));
				}catch(Exception e) {
					
				}
				
				try {
					config.setMosip_primary_language(resp.getString("mosip.primary-language"));
				}catch(Exception e) {
					
				}
				
				try {
				config.setPreregistration_documentupload_allowed_file_type(resp.getString("preregistration.documentupload.allowed.file.type"));
				}catch(Exception e) {
					
				}
				
				setCache(url, config);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}

	public static  ApplicationConfigIdSchema getAppConfigIdSchema() {
		ApplicationConfigIdSchema config = new ApplicationConfigIdSchema();
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue("applicaionconfig").toString();
		Object o =getCache(url);
		if(o != null)
			return( (ApplicationConfigIdSchema) o);


		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			//JSONObject configObject = resp.getJSONObject("response");
			
			if(resp != null) {
				JSONObject idSchemaObject = resp.getJSONObject("idSchema");
				if(idSchemaObject != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					config = objectMapper.readValue(idSchemaObject.toString(), ApplicationConfigIdSchema.class);
					setCache(url, config);
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}

	public  static List<MosipLocationModel> getLocationsByLevel(String level) {
		List<MosipLocationModel> locList = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"locationbylevel").toString();
		url = url+ level ;

		Object o =getCache(url);
		if(o != null)
			return( (List<MosipLocationModel>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			JSONArray locArray = resp.getJSONArray("locations");
			
			if(locArray != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				locList = objectMapper.readValue(locArray.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, MosipLocationModel.class));
	
				List<MosipLocationModel> newLocList = new ArrayList<MosipLocationModel>(); 
				for(MosipLocationModel lm: locList) {
					if(lm.getIsActive())
						newLocList.add(lm);
				}
				locList = newLocList;
				setCache(url, locList);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return locList;
	}
	
	 static JSONObject genQueryParams() throws JSONException {
		JSONObject queryParams = new JSONObject();
		//?orderBy=desc&pageNumber=0&pageSize=50&sortBy=cr_dtimes"
        queryParams.put("orderBy", "desc");
        queryParams.put("pageNumber", 0);
        queryParams.put("pageSize", 50);
        queryParams.put("sortBy", "cr_dtimes");
        
        return queryParams;
	}
	 /*
	 public static Hashtable<Double,List<MosipIDSchema>>  getIDSchemaLatestVersion_defunct() {
			
		Hashtable<Double,List<MosipIDSchema>> tbl = new Hashtable<Double,List<MosipIDSchema>> ();
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(
				VariableManager.NS_MASTERDATA,
					//"individualtypes"
				"idschemaapi"
				).toString();
		
		Object o =getCache(url);
		if(o != null)
			return( (Hashtable<Double,List<MosipIDSchema>>) o);

	    try {
				JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject());

				//int nSchema = resp.getInt("totalItems");
				JSONArray idSchema = null;
				double schemaVersion = 0.0;
				String schemaTitle = "";
				idSchema = resp.getJSONArray("schema");
				
				String schemaJson = resp.getString("schemaJson");
				System.out.println(idSchema.toString());
				schemaVersion=	resp.getDouble( "idVersion");
				schemaTitle = resp.getString("title");
				List<MosipIDSchema>  listSchema  = new ArrayList<MosipIDSchema>();
				
				if(schemaJson != null && !schemaJson.equals("")) {
					JSONObject schemaObj = new JSONObject(schemaJson);
					JSONObject identityObj = schemaObj.getJSONObject("properties").getJSONObject("identity");
					JSONObject identityProps = identityObj.getJSONObject("properties");
					JSONArray jsonArray = identityObj.getJSONArray("required");

					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.setSerializationInclusion(Include.NON_NULL);

					for(int i = 0; i < jsonArray.length(); i++){
					     String reqdField = jsonArray.getString(i);
					     JSONObject prop = identityProps.getJSONObject(reqdField);
					 	MosipIDSchema schema = objectMapper.readValue(prop.toString(), MosipIDSchema.class);
					 	schema.setRequired(true);
					 	schema.setInputRequired(true);
					 	
					 	schema.setId(reqdField);
					 	if(schema.getTypeRef() != null) {
						 	if(schema.getTypeRef().contains("simpleType"))
						 		schema.setType("simpleType");
						 	else
						 	if(schema.getTypeRef().contains("documentType"))
						 		schema.setType("documentType");
						 	else
							 if(schema.getTypeRef().contains("biometricsType"))
							 	schema.setType("biometricsType");
					 	} 	
						listSchema.add(schema);
					     
					}

					tbl.put(schemaVersion, listSchema);
					
					setCache(url, tbl);
				}
				
						
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return tbl;
		}
*/
	 private static JSONArray getRequiredFileds(JSONObject resp) {

			String schemaJson = resp.getString("schemaJson");

			JSONArray jsonArray = null;
			if(schemaJson != null && !schemaJson.equals("")) {
				//FIX: search and replace \" with "
				
				schemaJson =schemaJson.replaceAll(Pattern.quote("\\\""), "\"");
				
				JSONObject schemaObj = new JSONObject(schemaJson);
				JSONObject identityObj = schemaObj.getJSONObject("properties").getJSONObject("identity");
				JSONObject identityProps = identityObj.getJSONObject("properties");
				jsonArray = identityObj.getJSONArray("required");
			}
			return jsonArray;
				
	 }
	 private static JSONObject getIdentityPropsFromIDSchema(JSONObject resp) {

			String schemaJson = resp.getString("schemaJson");

			JSONObject identityProps = null;
		
			if(schemaJson != null && !schemaJson.equals("")) {
				schemaJson =schemaJson.replaceAll(Pattern.quote("\\\""), "\"");
				
				JSONObject schemaObj = new JSONObject(schemaJson);
				JSONObject identityObj = schemaObj.getJSONObject("properties").getJSONObject("identity");
				identityProps = identityObj.getJSONObject("properties");
				
			}
			return identityProps;
				
	 }

	public static String getIDSchemaSchemaLatestVersion() {
		String schemaJson = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(
				VariableManager.NS_MASTERDATA,
				//"individualtypes"
				"idschemaapi"
				).toString();
	    try {
				JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject());

				schemaJson = resp.getString("schemaJson");

	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return schemaJson;
	}
	public static void test1() {
		
		System.out.println("Hello");
		
	}
	public static Hashtable<Double,Properties>  getIDSchemaLatestVersion() {
	
		Hashtable<Double,Properties> tbl = new Hashtable<Double,Properties> ();
		
		//Hashtable<Double,List<MosipIDSchema>> tbl = new Hashtable<Double,List<MosipIDSchema>> ();
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(
				VariableManager.NS_MASTERDATA,
				//"individualtypes"
				"idschemaapi"
				).toString();
		
		String process=VariableManager.getVariableValue("process").toString();
		if(process == null) {
			process="NEW";
		}
		process = process.toLowerCase().trim() + "Process";
	
		Object o =getCache(url);
		if(o != null)
			return( (Hashtable<Double,Properties>) o);

        try {
			JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject());

			
			//int nSchema = resp.getInt("totalItems");
			JSONArray idSchema = new JSONArray();
			double schemaVersion = 0.0;
			String schemaTitle = "";
			//idSchema = resp.getJSONArray("schema"); //UISpec
			
			 //JSONArray screens = resp.getJSONObject("newProcess").getJSONArray("screens");
			// JSONArray screens = resp.getJSONObject("updateProcess").getJSONArray("screens");
			JSONArray screens = resp.getJSONObject(process).getJSONArray("screens");
				for (int i = 0; i < screens.length(); i++) {
					idSchema.putAll(screens.getJSONObject(i).getJSONArray("fields"));
				}
			 
			System.out.println(idSchema.toString());
		
//			CommonUtil.saveToTemp(idSchema.toString(), "uispec.json");
//			CommonUtil.saveToTemp(resp.getString("schemaJson"), "schemaJson.json");
			
			schemaVersion=	resp.getDouble( "idVersion");
			schemaTitle = resp.getString("title");
			

			
			
			JSONObject schemaVersionJson = new JSONObject();
			schemaVersionJson.put("id", "IDSchemaVersion");
			schemaVersionJson.put("inputRequired", false);
			schemaVersionJson.put("type", "number");
			schemaVersionJson.put("minimum", 0);
			schemaVersionJson.put("maximum", 0);
			schemaVersionJson.put("description", "ID Schema Version");
			schemaVersionJson.put("controlType", "");
			schemaVersionJson.put("fieldType", "default");
			schemaVersionJson.put("format", "none");
			schemaVersionJson.put("validators", new JSONArray());
			schemaVersionJson.put("fieldCategory", "none");
			schemaVersionJson.put("alignmentGroup", "");
			schemaVersionJson.put("contactType", "");
			schemaVersionJson.put("group", "");
			schemaVersionJson.put("required", true);
			schemaVersionJson.put("bioAttributes", new JSONArray());
			schemaVersionJson.put("requiredOn",  new JSONArray());
			schemaVersionJson.put("subType",  "IdSchemaVersion");
			idSchema.put(schemaVersionJson);
			
			JSONObject uinschemaJosn = new JSONObject();
			uinschemaJosn.put("id", "UIN");
			uinschemaJosn.put("inputRequired", false);
			uinschemaJosn.put("type", "string");
			uinschemaJosn.put("minimum", 0);
			uinschemaJosn.put("maximum", 0);
			uinschemaJosn.put("description", "UIN");
			uinschemaJosn.put("controlType", "textbox");
			uinschemaJosn.put("fieldType", "default");
			uinschemaJosn.put("format", "none");
			uinschemaJosn.put("validators", new JSONArray());
			uinschemaJosn.put("fieldCategory", "none");
			uinschemaJosn.put("alignmentGroup", "");
			uinschemaJosn.put("contactType", "");
			uinschemaJosn.put("group", "");
			uinschemaJosn.put("required", false);
			uinschemaJosn.put("bioAttributes", new JSONArray());
			uinschemaJosn.put("requiredOn",  new JSONArray());
			uinschemaJosn.put("subType",  "UIN");
			idSchema.put(uinschemaJosn);
			 
			 
			 
			
			if(idSchema != null) {
				JSONArray reqdFields = getRequiredFileds(resp); //FROM IDSchema
				//JSONObject idschemaProps = getIdentityPropsFromIDSchema(resp);
				
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setSerializationInclusion(Include.NON_NULL);

				List<MosipIDSchema>  listSchema  = new ArrayList<MosipIDSchema>();
				for(int i=0; i < idSchema.length(); i++) {
					
					
					 MosipIDSchema schema = objectMapper.readValue(idSchema.get(i).toString(),
							 MosipIDSchema.class);
					 listSchema.add(schema);
					 /*
					if(schema.getId().toLowerCase().contains("uin") || schema.getId().toLowerCase().contains("rid") )
						listSchema.add(schema);
					else
					{
						for(int ii = 0; ii < reqdFields.length(); ii++){
						     String reqdField = reqdFields.getString(ii);
						     if(reqdField.equals(schema.getId())) {
						    	
						    	 listSchema.add(schema);
						     }
						}
					}*/
				}
				List<String> requiredAttributes = new ArrayList<String>();
				
				JSONObject idschemaProps = getIdentityPropsFromIDSchema(resp);
				Iterator<String> propNames = idschemaProps.keys();
				while(propNames.hasNext()) {
					String key = propNames.next();
					requiredAttributes.add(key);
				}
					
				/*
				for(int i=0; i < reqdFields.length(); i++)
					requiredAttributes.add(reqdFields.getString(i).trim());
				*/
				Properties prop = new Properties();
				prop.put("schemaList", listSchema);
				prop.put("requiredAttributes",requiredAttributes);
				tbl.put(schemaVersion, prop);
				
				setCache(url, tbl);
			}
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return tbl;
	}
	
	public static Hashtable<Double,Properties>  getPreregIDSchemaLatestVersion() {
		
		Hashtable<Double,Properties> tbl = new Hashtable<Double,Properties> ();
		
		//Hashtable<Double,List<MosipIDSchema>> tbl = new Hashtable<Double,List<MosipIDSchema>> ();
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(
				VariableManager.NS_MASTERDATA,
				//"idschemaapi"
				"uiSpec"
				).toString();
		//url="https://qa-double.mosip.net/preregistration/v1/uispec/latest?identitySchemaVersion=0&version=0";
	
		Object o =getCache(url);
		if(o != null)
			return( (Hashtable<Double,Properties>) o);

        try {
			JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject());

				JSONArray identityArray = resp.getJSONObject("jsonSpec").getJSONObject("identity").getJSONArray("identity");
				JSONArray locationHierarchyArray =resp.getJSONObject("jsonSpec").getJSONObject("identity").getJSONArray("locationHierarchy");
			
				if(identityArray != null && locationHierarchyArray != null) {
				
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setSerializationInclusion(Include.NON_NULL);

				List<MosipIDSchema>  identityArraylistSchema  = new ArrayList<MosipIDSchema>();
				for(int i=0; i < identityArray.length(); i++) {
					
					 MosipIDSchema schema = objectMapper.readValue(identityArray.get(i).toString(),
							 MosipIDSchema.class);
					 identityArraylistSchema.add(schema);
				}
				
				JSONArray array = new JSONArray();
				for(int i=0;i<locationHierarchyArray.length();i++) {
					if (locationHierarchyArray.get(i) instanceof JSONArray) {
						array.put(locationHierarchyArray.get(i));
					} 
				}
				if(array.length()==0) {
					array.put(locationHierarchyArray);
				}
				
				Properties prop = new Properties();
				prop.put("locaitonherirachy", array);
				prop.put("schemaList", identityArraylistSchema);
				//tbl.put(0.2, prop);
				tbl.put(resp.getDouble("idSchemaVersion"), prop);
				
				setCache(url, tbl);
			}
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return tbl;
	}
	public static List<MosipDocCategoryModel> getDocumentCategories() {
	List<MosipDocCategoryModel> docCatList = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"documentCategories").toString();
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipDocCategoryModel>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			JSONArray docCatArray = resp.getJSONArray("documentcategories");
			
			if(docCatArray != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				docCatList = objectMapper.readValue(docCatArray.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, MosipDocCategoryModel.class));
				
				List<MosipDocCategoryModel> newDocTypeList = new ArrayList<MosipDocCategoryModel>();
				for(MosipDocCategoryModel m: docCatList) {
					if(m.getIsActive() )
						newDocTypeList.add(m);
				}
				setCache(url, newDocTypeList);
				return newDocTypeList;
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docCatList;
	}

	public static List<MosipDocTypeModel> getDocumentTypes(String categoryCode,String langCode) {
	
		List<MosipDocTypeModel> docTypeList = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"documentTypes").toString();
		url = url + categoryCode +"/"+ langCode;
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipDocTypeModel>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			JSONArray docCatArray = resp.getJSONArray("documents");
			
			if(docCatArray != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				docTypeList = objectMapper.readValue(docCatArray.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, MosipDocTypeModel.class));
	
				List<MosipDocTypeModel> newDocTypeList = new ArrayList<MosipDocTypeModel>();
				for(MosipDocTypeModel m: docTypeList) {
					if(m.getIsActive() )
						newDocTypeList.add(m);
				}
				setCache(url, newDocTypeList);
				return newDocTypeList;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docTypeList;
	}
	public static List<MosipDocTypeModel> getMappedDocumentTypes(String categoryCode,String langCode) {
		
		List<MosipDocTypeModel> docTypeList = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"documentTypes").toString();
		url = url + categoryCode +"/"+ langCode;
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipDocTypeModel>) o);

		try {
			JSONArray docCatArray = RestClient.getDoc(url,new JSONObject() , new JSONObject());
			
			if(docCatArray != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				docTypeList = objectMapper.readValue(docCatArray.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, MosipDocTypeModel.class));
	
				List<MosipDocTypeModel> newDocTypeList = new ArrayList<MosipDocTypeModel>();
				for(MosipDocTypeModel m: docTypeList) {
					if(m.getIsActive() )
						newDocTypeList.add(m);
				}
				setCache(url, newDocTypeList);
				return newDocTypeList;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docTypeList;
	}
	
	
	public static Hashtable<String, List<MosipIndividualTypeModel>> getIndividualTypesFromDynamicFields() {
		Hashtable<String, List<MosipIndividualTypeModel>> tbl = new Hashtable<String, List<MosipIndividualTypeModel>>();
		Hashtable<String, List<DynamicFieldModel>> tblDyn = MosipMasterData.getAllDynamicFields();
		Iterator<String> it = tblDyn.keys().asIterator();
		while(it.hasNext()) {
			String key = it.next();
			List<DynamicFieldModel> dynaFields = tblDyn.get(key);
			for(DynamicFieldModel dfm: dynaFields) {
				if(dfm.getIsActive() && dfm.getName().equals(DataProviderConstants.INDIVIDUAL_TYPE)) {
					for(DynamicFieldValueModel dfmv: dfm.getFieldVal()) {
						MosipIndividualTypeModel im = new MosipIndividualTypeModel();
						im.setCode(dfmv.getCode());
						im.setIsActive(true);
						im.setLangCode(key);
						
						im.setName(dfmv.getValue());
						
						List<MosipIndividualTypeModel> indList = tbl.get(key);
						if(indList == null) {
							indList = new ArrayList<MosipIndividualTypeModel>();
							
							tbl.put(key, indList);
						}
						indList.add(im);
					}
							
				}
			}
		}
		return tbl;
	}
	public static Hashtable<String, List<MosipIndividualTypeModel>> getIndividualTypes() {
	
		String mosipVersion = "legacy";
		Object obj = VariableManager.getVariableValue("mosip.version");
		if(obj != null)
			mosipVersion = obj.toString();
		if(mosipVersion.equals("1.2")) {
			return getIndividualTypesFromDynamicFields();
		}
		return getIndividualTypes_legacy();
	}
	public static Hashtable<String, List<MosipIndividualTypeModel>> getIndividualTypes_legacy() {
		
		Hashtable<String, List<MosipIndividualTypeModel>> tbl = new Hashtable<String, List<MosipIndividualTypeModel>>();
		
		List<MosipIndividualTypeModel> indTypeList = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
		VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"individualtypes").toString();
		
		Object o =getCache(url);
		if(o != null)
			return( (Hashtable<String, List<MosipIndividualTypeModel>>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			JSONArray docCatArray = resp.getJSONArray("data");
			
			if(docCatArray != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				indTypeList = objectMapper.readValue(docCatArray.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, MosipIndividualTypeModel.class));
	
				List<MosipIndividualTypeModel> newList = null;
				for(MosipIndividualTypeModel m: indTypeList) {
					
					if(m.getIsActive() ) {
						newList = tbl.get( m.getLangCode());
						if(newList == null) {
							newList = new ArrayList<MosipIndividualTypeModel>();
							tbl.put(m.getLangCode(), newList);
						}
						newList.add(m);
					}
				}
				setCache(url, tbl);
				//return tbl;
						
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tbl;

	}
	public static List<MosipGenderModel> getGenderTypes(String lang) {
		List<MosipGenderModel> genderTypeList = null;

		String mosipVersion = "legacy";
		Object obj = VariableManager.getVariableValue("mosip.version");
		if(obj != null)
			mosipVersion = obj.toString();
		if(mosipVersion.equals("1.2")) {
			genderTypeList = getGenderTypesLTS(lang);
		}else {
			String url = VariableManager.getVariableValue("urlBase").toString() +
					VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"gendertypes").toString();
							
							Object o =getCache(url);
							if(o != null)
								return( (List<MosipGenderModel>) o);

							try {
								JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
								JSONArray docCatArray = resp.getJSONArray("genderType");
								
								if(docCatArray != null) {
									ObjectMapper objectMapper = new ObjectMapper();
									genderTypeList = objectMapper.readValue(docCatArray.toString(), 
										objectMapper.getTypeFactory().constructCollectionType(List.class, MosipGenderModel.class));
						
									setCache(url, genderTypeList);
									return genderTypeList;
											
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return genderTypeList;
		}
		return genderTypeList;

		

	}
	private static List<MosipGenderModel> getGenderTypesLTS(String lang) {
		List<MosipGenderModel> genderTypeList = new ArrayList<>();
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"genderTypesByDynamicField").toString();
		
		Object o =getCache(url +"_"+lang);
		if(o != null)
			return( (List<MosipGenderModel>) o);
		
		JSONArray filters= new JSONArray();
		JSONObject jsonFilters= new JSONObject();
		jsonFilters.put("columnName", "name");
		jsonFilters.put("type", "contains");
		jsonFilters.put("value", "gender");
		filters.put(jsonFilters);
		JSONArray sort= new JSONArray();
		JSONObject jsonSort= new JSONObject();
		jsonSort.put("sortType", "desc");
		jsonSort.put("sortField", "createdDateTime");
		sort.put(jsonSort);
		JSONObject jsonDynamicField = new JSONObject();
		jsonDynamicField.put("filters", filters);
		jsonDynamicField.put("sort", sort);
		jsonDynamicField.put("languageCode", lang);
					
		JSONObject jsonReqWrapper = new JSONObject();
		jsonReqWrapper.put("request", jsonDynamicField);
		jsonReqWrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		jsonReqWrapper.put("version", "1.0");
		jsonReqWrapper.put("id", JSONObject.NULL);
		jsonReqWrapper.put("metadata", JSONObject.NULL);
		try {
			JSONObject resp = RestClient.post(url,jsonReqWrapper,"admin");
			if(resp!=null) {
				JSONArray genderArray =resp.getJSONArray("data");
				for (int i=0; i<genderArray.length(); i++) {
				    JSONObject genderDataItem = genderArray.getJSONObject(i);
				    String genderName = genderDataItem.getString("name");
				    String langCode = genderDataItem.getString("langCode");
				    Boolean isActive = genderDataItem.getBoolean("isActive");
				    JSONObject fieldValItem =genderDataItem.getJSONObject("fieldVal");
				    String code = fieldValItem.getString("code");
				    String value =fieldValItem.getString("value");
				    MosipGenderModel mgm= new MosipGenderModel();
				    mgm.setGenderName(genderName);
				    mgm.setCode(code);
				    mgm.setIsActive(isActive);
				    mgm.setLangCode(langCode);
				    mgm.setValue(value);
				    genderTypeList.add(mgm);
				}
				setCache(url +"_"+lang, genderTypeList);
				return genderTypeList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return genderTypeList;
		
	}
	public static Boolean isExists(List<MosipIDSchema> lst, String val) {
		for(MosipIDSchema s: lst) {
			if(s.getSubType() != null && s.getSubType().equalsIgnoreCase(val))
			//if(s.getId().equalsIgnoreCase(val))
				return true;
		}
		return false;
	}
	public static ApplicationConfigIdSchema getPreregLocHierarchy(String primLang, int count) throws Exception {

		
		MosipPreRegLoginConfig logincConfig = getPreregLoginConfig();
		String countryCode = logincConfig.getMosip_country_code();
		String langCode = logincConfig.getMosip_primary_language();
		
		if(primLang != null )
			langCode = primLang;
	
		LocationHierarchyModel[]  locHiModels = MosipMasterData.getLocationHierarchy(langCode);
		
		//int maxLevel  = 0;
		
		if(locHiModels != null) {
			//maxLevel = Arrays.stream(locHiModels).max(Comparator.comparing(
			//		LocationHierarchyModel::getHierarchyLevel) ).get().getHierarchyLevel();
			
			/*
			for(LocationHierarchyModel l: locHiModels) {
				maxLevel = Math.max(maxLevel, l.getHierarchyLevel());
			}*/
		}
		if(countryCode == null || countryCode.equals("")) {
			throw new Exception("Missing pre-reg-country-code");
			
		}
		ApplicationConfigIdSchema idschema =  new ApplicationConfigIdSchema(); //getAppConfigIdSchema();
		//"contactType": "Postal"
		Hashtable<Double,Properties> tblSchema = getIDSchemaLatestVersion();
		List<MosipIDSchema> idSchemaList = (List<MosipIDSchema>) tblSchema.get( tblSchema.keys().nextElement()).get("schemaList");
		List<MosipIDSchema> locSchemaList = new ArrayList<MosipIDSchema>();
		for(MosipIDSchema s: idSchemaList) {
			if(s.getRequired() && s.getControlType() != null && s.getControlType().equals("dropdown") &&
					( 
							(s.getContactType() != null && s.getContactType().equals("Postal")) ||
							(s.getGroup() != null && (
									s.getGroup().equals("Location") ||
									s.getGroup().equals("Adresse")))
					)
			) {
				locSchemaList.add(s);
			}
		}
		idschema.setLocationHierarchy(locSchemaList);
		
		String levelCode = countryCode;
		String levelName = "";
		int idx = 0;
		
		List<Hashtable<String, MosipLocationModel>>  tblList = new ArrayList< Hashtable<String, MosipLocationModel>>();
		List<MosipLocationModel> rootLocs =  getImmedeateChildren(levelCode, langCode);
		if(rootLocs == null && rootLocs.isEmpty()) {
			//throw new Exception("Invalid pre-reg-country-code  No locations configured");
			return null;
		}
		int [] idxArray = CommonUtil.generateRandomNumbers(count, rootLocs.size()-1, 0);
				
		for(int i=0; i < count; i++) {
			Hashtable<String, MosipLocationModel> tbl = new Hashtable<String, MosipLocationModel>();
			tblList.add(i, tbl);
			idx = idxArray[i];
			
			MosipLocationModel lc = rootLocs.get(idx);
			levelName = lc.getHierarchyName();
			
			tbl.put(levelName, lc);
			
			getChildLocations( locSchemaList, langCode,lc.getCode() , levelName, tbl,locHiModels);
		}
		
		idschema.setTblLocations(tblList);
			
		return idschema;
	}
	static void getChildLocations(List<MosipIDSchema> locHirachyList, String langCode, String levelCode,
			String levelName, Hashtable<String, MosipLocationModel> tbl, LocationHierarchyModel[]  locHiModels) {

		int idx=0;
		Stack<List<MosipLocationModel>> stk = new Stack<List<MosipLocationModel>>();
		//while(isExists(locHirachyList, levelName))
		while( true ) {
				//MosipLocationModel lcParent = tbl.get(preLevel);
			List<MosipLocationModel> rootLocs =  getImmedeateChildren(levelCode, langCode);
			if(rootLocs == null) {
				rootLocs = stk.pop();
				idx++;
			}
			if(rootLocs != null && !rootLocs.isEmpty()) {
				if(idx >= rootLocs.size())
					break;
				MosipLocationModel lc = rootLocs.get(idx );
				stk.push(rootLocs)	;
				levelName = lc.getHierarchyName();
				tbl.put(levelName, lc);
				
				levelCode = lc.getCode();
			//	if(lc.getHierarchyLevel() >= maxLevel)
			//		break;
			}
			else
				break;
		}
			
		
	}

	public static String getIDschemaStringLatest(){

		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(
				VariableManager.NS_MASTERDATA,
				//"individualtypes"
				"idschemaapi"
				).toString();

		

		try {
			JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject());

			return resp.toString();

		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{\"Failed\"}";
		
	}

	public static String postSchema(String id, int version,  JSONArray schema){

		String url = VariableManager.getVariableValue("urlBase").toString() +
		VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"idschema").toString();
	

		JSONObject request = new JSONObject();
		request.put("description", "new ID schema post");
		request.put("effectiveFrom", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		request.put("schema", schema);
		request.put("schemaVersion", version);
		request.put("title", "test");


		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("metadata", new JSONObject());
		obj.put("request", request);
		obj.put("requesttime", CommonUtil.getUTCDateTime(LocalDateTime.now()));
		obj.put("version", "1.0");



		JSONObject resp;
		try{
			resp = RestClient.post(url, obj);
			return resp.toString();
		}
		catch(Exception e){
			e.printStackTrace();
			return "{\"post failed\"}";
		}

	}
	
	public static boolean executeMVEL(String expression, Object json) {
		try {
			Map context = new HashMap();
			context.put("identity", json);
			VariableResolverFactory resolverFactory = new MapVariableResolverFactory(context);
			return MVEL.evalToBoolean(expression, resolverFactory);
		} catch (Throwable t) {
			//LOGGER.error("Failed to evaluate mvel expr", t);
			
		}
		return false;
	}

	static Boolean validateCondn(String cndnexpr, Object inputObject) {
		return MVEL.evalToBoolean(cndnexpr,inputObject);
	}
	static void testSchemaRule() {
		
		ResidentDataProvider residentProvider = new ResidentDataProvider();
		residentProvider.addCondition(ResidentAttribute.RA_Count, 1)
		.addCondition(ResidentAttribute.RA_SECONDARY_LANG, "ara")
		.addCondition(ResidentAttribute.RA_Gender, Gender.Any)
		.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult)
		.addCondition(ResidentAttribute.RA_Finger, false);
		List<ResidentModel> lst =  residentProvider.generate();
		Hashtable<Double, Properties>  schema = MosipMasterData.getIDSchemaLatestVersion();
		Set<Double> schemaIds = schema.keySet();
		Double schemVersion = schema.keySet().iterator().next();
	
		for(ResidentModel r: lst) {
	
			JSONObject jsonObject = CreatePersona.crateIdentity(r,null);
			MosipIdentity identity = new MosipIdentity();
			identity.setIsChild(false);
			identity.setIsLost(false);
			identity.setIsNew(true);
			identity.setIsUpdate(false);

			identity.setUpdatableFieldGroups("");
			identity.setUpdatableFields("");
			r.setIdentity(identity);
			
			
	
			List<MosipIDSchema> lstSchema = (List<MosipIDSchema>) schema.get( schemVersion).get("schemaList");
			for( MosipIDSchema idschema:lstSchema ) {
				//System.out.println(idschema.toJSONString());
				List<SchemaRule>  rule = idschema.getRequiredOn();
				if(rule != null) {
					for(SchemaRule sr: rule) {
						System.out.println("rule:" + sr);
						boolean bval = validateCondn(sr.getExpr(),r);
						System.out.println("rule:result=" + bval);
					}
				}
			}
				
		}
	}
	
	public static JSONArray getUiSpecId() {
		JSONArray array= new JSONArray();
		String url = VariableManager.getVariableValue("urlBase").toString().trim() +"preregistration/v1/uispec/latest?identitySchemaVersion=0&version=0";
		//String url = VariableManager.getVariableValue("urlBase").toString() +VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"uiSpec").toString();

		Object o =getCache(url);
		if(o != null)
			return( (JSONArray) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray identityArray = resp.getJSONObject("jsonSpec").getJSONObject("identity").getJSONArray("identity");
				Iterator<Object> iterator = identityArray.iterator();
				while (iterator.hasNext()) {
					JSONObject identityJson=(JSONObject)iterator.next();
					if(identityJson.getBoolean("required") && !identityJson.getString("id").toLowerCase().contains("proof")) {
						array.put(identityJson.getString("id"));
					}
				}
				System.out.println("printing Array : "+ array);
				
				
				setCache(url, array);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}
	
	
	
	
	public static void main(String[] args) {
		VariableManager.setVariableValue("urlBase","https://sandbox.mosip.net/");
		VariableManager.setVariableValue("configpath","config/*/mz/1.1.5/registration-processor-mz.properties");

	
//		MosipDataSetup.getConfig();
	//	List<MosipDeviceModel> devices = MosipDataSetup.getDevices("10002");
//		test1();
		
	//	List<DynamicFieldModel> lstDyn =  MosipMasterData.getAllDynamicFields();
//		List<MosipMachineModel> mach =  MosipDataSetup.getMachineDetail("10082", "eng");
//		MosipDataSetup.getMachineConfig(mach.get(0).getName()) ;
		getMappedDocumentTypes("POA","eng");
		
		Hashtable<Double,Properties> tbl1 = getIDSchemaLatestVersion();
		 double schemaId = tbl1.keys().nextElement();
		 System.out.println(schemaId);
		 List<MosipIDSchema> lstSchema = (List<MosipIDSchema>) tbl1.get(schemaId).get("schemaList");
		 List<String> reqdFields = (List<String>) tbl1.get(schemaId).get("requiredAttributes");
					
		
		List<MosipGenderModel> allg = MosipMasterData.getGenderTypes("eng");
		allg.forEach( g-> {
			System.out.println(g.getCode() +"\t" + g.getGenderName());
		});
		testSchemaRule();
		System.exit(1);
		ApplicationConfigIdSchema ss;
		try {
			ss = getPreregLocHierarchy("fra",1);
			System.out.println(ss.toJSONString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
		
		 
		//MosipPreRegLoginConfig c1 =  MosipMasterData.getPreregLoginConfig();
		//ApplicationConfigIdSchema idschma =	MosipMasterData.getAppConfigIdSchema();
		//System.out.println(idschma.toJSONString());
									 
		


		if(0 ==1) {
			
		HashMap<String,LocationHierarchyModel[]> locHi = getAllLocationHierarchies();
		
		List<MosipGenderModel> genderTypes = MosipMasterData.getGenderTypes("hin");
		
		List<MosipBiometricTypeModel> bioTypes = getBiometricTypes();
		for(MosipBiometricTypeModel bt: bioTypes) {
			
			System.out.println(bt.toJSONString());
			List<MosipBiometricAttributeModel> bioAttrs = getBiometricAttrByTypes(bt.getCode(), bt.getLangCode());
			for(MosipBiometricAttributeModel bam: bioAttrs) {
				System.out.println(bam.toJSONString());
			}
			
		}
			Hashtable<String, List<MosipIndividualTypeModel>>indTypes =  getIndividualTypes();
			List<MosipDocCategoryModel> docCat =getDocumentCategories();
			
			 List<MosipDocTypeModel> dcoTypes= getDocumentTypes(docCat.get(0).getCode(),docCat.get(0).getLangCode());
			 
			//test dynamic fields fields;
		
			Hashtable<String,List<DynamicFieldModel>> lstDynF =  getAllDynamicFields() ;
			lstDynF.forEach( (k,v)->{
			
				for(DynamicFieldModel dm: v) {
					
					System.out.println(dm.getName() );
					
				
				}
		
			});
						
			Hashtable<Double,Properties> tbl = getIDSchemaLatestVersion();
			List<MosipIDSchema> lst = null;
			if(tbl != null)
				lst = (List<MosipIDSchema>) tbl.get( tbl.keys().nextElement()).get("schemaList");
			lst.forEach( (sch)->{
				System.out.println(sch.getId() + " " + sch.getRequired());
			});
			List<MosipLanguage> langs =  getConfiguredLanguages();
			
			// getIDSchemaLatestVersion();
			langs.forEach( (l) ->{
				System.out.println(l.getCode() + " "+ l.getName());
				//for(int level=0; level < 5; level++)
				
				//getRootLocations(l.getCode());
				//getChildrenLocations("KTA",l.getCode());
				
				//getLocationHierarchy(l.getCode());
			});
			
			
			//HashMap<String,LocationHierarchyModel[]> locHi = getAllLocationHierarchies();
			Set<String> langSet = locHi.keySet();
			langSet.forEach( (langcode) ->{
				LocationHierarchyModel[] locHierachies = locHi.get(langcode);
				for(int i=0; i < locHierachies.length; i++) {
					List<MosipLocationModel> list = getLocationsByLevel(locHierachies[i].getHierarchyLevelName());
					list.forEach((m) ->{
						System.out.println(m.getName());
					});
				}	
			});
			
		}
	}
}
