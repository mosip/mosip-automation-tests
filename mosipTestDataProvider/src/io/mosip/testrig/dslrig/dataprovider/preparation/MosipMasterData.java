package io.mosip.testrig.dslrig.dataprovider.preparation;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.testrig.dslrig.dataprovider.ResidentDataProvider;
import io.mosip.testrig.dslrig.dataprovider.models.ApplicationConfigIdSchema;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldModel;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldValueModel;
import io.mosip.testrig.dslrig.dataprovider.models.LocationHierarchyModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipBiometricAttributeModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipBiometricTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocCategoryModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipGenderModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIdentity;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIndividualTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLanguage;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLocationModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipPreRegLoginConfig;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.SchemaRule;
import io.mosip.testrig.dslrig.dataprovider.test.CreatePersona;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public  class MosipMasterData {
	private static final Logger logger = LoggerFactory.getLogger(MosipMasterData.class);
	public static String RUN_CONTEXT = "run_context";

	public static List<MosipBiometricAttributeModel> getBiometricAttrByTypes(String bioType,String lang,String contextKey){
		
		List<MosipBiometricAttributeModel> biotypes =null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"biometricAttributes").toString();
		url = url + lang + "/" + bioType;
		Object o = MosipDataSetup.getCache(url,contextKey);
		if(o != null)
			return( (List<MosipBiometricAttributeModel>) o);
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray langArray = resp.getJSONArray("biometricattributes");
				ObjectMapper objectMapper = new ObjectMapper();
				
				 biotypes = objectMapper.readValue(langArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipBiometricAttributeModel.class));
				
				 MosipDataSetup.setCache(url,  biotypes,contextKey);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
        return  biotypes;
		

	}

	
	public static List<MosipBiometricTypeModel> getBiometricTypes(String contextKey){
		
		List<MosipBiometricTypeModel> biotypes =null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"biometricTypes").toString();
		Object o = MosipDataSetup.getCache(url,contextKey);
		if(o != null)
			return( (List<MosipBiometricTypeModel>) o);
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray langArray = resp.getJSONArray("biometrictypes");
				ObjectMapper objectMapper = new ObjectMapper();
				
				 biotypes = objectMapper.readValue(langArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipBiometricTypeModel.class));
				
				 MosipDataSetup.setCache(url,  biotypes,contextKey);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
        return  biotypes;
		

	}
	public static List<MosipLanguage> getConfiguredLanguages(String contextKey) {
		List<MosipLanguage> langs =null;
			
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"languages").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (List<MosipLanguage>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray langArray = resp.getJSONArray("languages");
				ObjectMapper objectMapper = new ObjectMapper();
				
				langs = objectMapper.readValue(langArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipLanguage.class));
				
				MosipDataSetup.setCache(url, langs,run_context);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
        return langs;
		
	}
	
	public static Hashtable<String,List<DynamicFieldModel>> getAllDynamicFields(String contextKey) {
		
		Hashtable<String,List<DynamicFieldModel>> tblDynaFieldsLang = new Hashtable<String,List<DynamicFieldModel>>();
		
		//List<DynamicFieldModel> lstDynamicFields = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"dynamicFields").toString();
	String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (Hashtable<String,List<DynamicFieldModel>>) o);
	
		int pageno = 0;
		int nPages = 0;
		do {
			try {
				List<DynamicFieldModel> lstDynamicFieldsPart = null;
				String urlQuery = url + "?pageNumber="+ pageno;
				JSONObject resp = RestClient.get(urlQuery,new JSONObject() , new JSONObject(),contextKey);
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
					MosipDataSetup.setCache(url, tblDynaFieldsLang,run_context);
				
					pageno++;
				}
			} catch (Exception e) {
					logger.error(e.getMessage());
			}
		}while(pageno <= nPages);
			
		
		return tblDynaFieldsLang;
	}
	public static HashMap<String,LocationHierarchyModel[]> getAllLocationHierarchies(String contextKey) {
		
		HashMap<String,LocationHierarchyModel[]> locationHierarchies = new HashMap<String,LocationHierarchyModel[]>();
		List<MosipLanguage> langs =  getConfiguredLanguages(contextKey);
		if(langs != null) {
			langs.forEach( (l) ->{
				try {
					LocationHierarchyModel[] locationPerLanguage = getLocationHierarchy(l.getCode(),contextKey);
					locationHierarchies.put(l.getCode(), locationPerLanguage);
				}catch(Exception ex) {
					logger.error("Failed to get LocationHierarchyModel" + ex.getMessage());
				}
			});
        }
		else {
			 logger.error("Failed to get configured languages");
		}
		
		return locationHierarchies;
	}
	public static LocationHierarchyModel[] getLocationHierarchy(String langCode,String contextKey) {
		
		LocationHierarchyModel [] locationHierarchy = null;
		
		List<LocationHierarchyModel> locHierarchy =null;
			
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"locationhierarchy").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (LocationHierarchyModel[]) o);
	
		try {
			JSONObject resp = RestClient.get(url+ langCode,new JSONObject() , new JSONObject(),contextKey);
		//	logger.info(resp.toString());
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
	
				MosipDataSetup.setCache(url, locationHierarchy,run_context);
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return locationHierarchy;
		
	}
	public static List<MosipLocationModel> getImmedeateChildren(String locCode, String langCode,String contextKey){

		List<MosipLocationModel> locList = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
		"v1/masterdata/locations/immediatechildren/";
		url = url+ locCode + "/" + langCode ;
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (List<MosipLocationModel>) o);
	
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
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
				MosipDataSetup.setCache(url, locList,run_context);
			}
		} catch (Exception e) {
			if(RestClient.isDebugEnabled(contextKey))
			     logger.error(e.getMessage());
		}
		return locList;
	
	}
	
	public static MosipPreRegLoginConfig getPreregLoginConfig(String contextKey) {
		MosipPreRegLoginConfig config = new MosipPreRegLoginConfig();
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"loginconfig").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (MosipPreRegLoginConfig) o);


		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			
			
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
				
				MosipDataSetup.setCache(url, config, run_context);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return config;
	}

	public static  ApplicationConfigIdSchema getAppConfigIdSchema(String contextKey) {
		ApplicationConfigIdSchema config = new ApplicationConfigIdSchema();
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(contextKey,"applicaionconfig").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (ApplicationConfigIdSchema) o);


		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			//JSONObject configObject = resp.getJSONObject("response");
			
			if(resp != null) {
				JSONObject idSchemaObject = resp.getJSONObject("idSchema");
				if(idSchemaObject != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					config = objectMapper.readValue(idSchemaObject.toString(), ApplicationConfigIdSchema.class);
					MosipDataSetup.setCache(url, config,run_context);
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return config;
	}

	public  static List<MosipLocationModel> getLocationsByLevel(String level,String contextKey) {
		List<MosipLocationModel> locList = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"locationbylevel").toString();
		url = url+ level ;
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (List<MosipLocationModel>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
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
				MosipDataSetup.setCache(url, locList,run_context);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
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

	public static String getIDSchemaSchemaLatestVersion(String contextKey) {
		String schemaJson = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"idschemaapi").toString();
	    try {
				JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject(),contextKey);

				schemaJson = resp.getString("schemaJson");

	
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	    return schemaJson;
	}
	public static void test1() {
		
		logger.info("Hello");
		
	}
	
	public static Hashtable<Double, Properties> getIDSchemaLatestVersion(String contextKey) {
	    Hashtable<Double, Properties> tbl = new Hashtable<>();
	    String url = VariableManager.getVariableValue(contextKey, "urlBase").toString() +
	                 VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "idschemaapi").toString();
	    String run_context = VariableManager.getVariableValue(contextKey, "urlBase").toString() + RUN_CONTEXT;
	    String process = VariableManager.getVariableValue(contextKey, "process").toString();
	    if (process == null) {
	        process = "NEW";
	    }
	    process = process.toLowerCase().trim() + "Process";
	    Object o = MosipDataSetup.getCache(url,run_context);
	    if (o != null) {
	        return (Hashtable<Double, Properties>) o;
	    }
	    try {
	        JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject(), contextKey);
	        JSONArray idSchema = new JSONArray();
	        double schemaVersion = 0.0;
	        String schemaTitle = "";
	        JSONArray screens = resp.getJSONObject(process).getJSONArray("screens");
	        for (int i = 0; i < screens.length(); i++) {
	            JSONArray fields = screens.getJSONObject(i).getJSONArray("fields");
	            for (int j = 0; j < fields.length(); j++) {
	                idSchema.put(fields.getJSONObject(j));
	            }
	        }
	        logger.info(idSchema.toString());
	        schemaVersion = resp.getDouble("idVersion");
	        schemaTitle = resp.getString("title");
	        // Additional fields
	        idSchema.put(createField("IDSchemaVersion", "ID Schema Version", "number", true));
	        idSchema.put(createField("UIN", "UIN", "string", false));
	        if (idSchema.length() > 0) {
	            List<MosipIDSchema> listSchema = new ArrayList<>();
	            ObjectMapper objectMapper = new ObjectMapper();
	            objectMapper.setSerializationInclusion(Include.NON_NULL);
	            for (int i = 0; i < idSchema.length(); i++) {
	                JSONObject schemaJson = idSchema.getJSONObject(i);
	                if (schemaJson.get("type").equals("array")) {
	                    // Handle nested arrays if necessary
	                    JSONArray nestedArray = schemaJson.getJSONArray("fields");
	                    for (int k = 0; k < nestedArray.length(); k++) {
	                        MosipIDSchema schema = objectMapper.readValue(nestedArray.getJSONObject(k).toString(), MosipIDSchema.class);
	                        listSchema.add(schema);
	                    }
	                } else {
	                    MosipIDSchema schema = objectMapper.readValue(schemaJson.toString(), MosipIDSchema.class);
	                    listSchema.add(schema);
	                }
	            }
	            List<String> requiredAttributes = new ArrayList<>();
	            JSONObject idschemaProps = getIdentityPropsFromIDSchema(resp);
	            if (idschemaProps != null) {
	                Iterator<String> propNames = idschemaProps.keys();
	                while (propNames.hasNext()) {
	                    String key = propNames.next();
	                    requiredAttributes.add(key);
	                }
	                Properties prop = new Properties();
	                prop.put("schemaList", listSchema);
	                prop.put("requiredAttributes", requiredAttributes);
	                tbl.put(schemaVersion, prop);
	                MosipDataSetup.setCache(url, tbl, run_context);
	            }
	        }
	    } catch (Exception e) {
	        logger.error("Error processing ID schema: " + e.getMessage(), e);
	    }
	    return tbl;
	}
	
	private static JSONObject createField(String id, String description, String type, boolean required) {
	    JSONObject field = new JSONObject();
	    field.put("id", id);
	    field.put("inputRequired", false);
	    field.put("type", type);
	    field.put("minimum", 0);
	    field.put("maximum", 0);
	    field.put("description", description);
	    field.put("controlType", "textbox");
	    field.put("fieldType", "default");
	    field.put("format", "none");
	    field.put("validators", new JSONArray());
	    field.put("fieldCategory", "none");
	    field.put("alignmentGroup", "");
	    field.put("contactType", "");
	    field.put("group", "");
	    field.put("required", required);
	    field.put("bioAttributes", new JSONArray());
	    field.put("requiredOn", new JSONArray());
	    field.put("subType", id);
	    return field;
	}
	
	public static Hashtable<Double,Properties>  getPreregIDSchemaLatestVersion(String contextKey) {
		
		Hashtable<Double,Properties> tbl = new Hashtable<Double,Properties> ();
		
		//Hashtable<Double,List<MosipIDSchema>> tbl = new Hashtable<Double,List<MosipIDSchema>> ();
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(
				VariableManager.NS_DEFAULT,
				//"idschemaapi"
				"uiSpec"
				).toString();
		//url="https://qa-double.mosip.net/preregistration/v1/uispec/latest?identitySchemaVersion=0&version=0";
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (Hashtable<Double,Properties>) o);

        try {
			JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject(),contextKey);

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
				
				MosipDataSetup.setCache(url, tbl,run_context);
			}
					
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
        return tbl;
	}
	public static List<MosipDocCategoryModel> getDocumentCategories(String contextKey) {
	List<MosipDocCategoryModel> docCatList = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"documentCategories").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (List<MosipDocCategoryModel>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
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
				MosipDataSetup.setCache(url, newDocTypeList,run_context);
				return newDocTypeList;
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return docCatList;
	}

	public static List<MosipDocTypeModel> getDocumentTypes(String categoryCode,String langCode,String contextKey) {
	
		List<MosipDocTypeModel> docTypeList = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"documentTypes").toString();
		url = url + categoryCode +"/"+ langCode;
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (List<MosipDocTypeModel>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
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
				MosipDataSetup.setCache(url, newDocTypeList,run_context);
				return newDocTypeList;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return docTypeList;
	}
	public static List<MosipDocTypeModel> getMappedDocumentTypes(String categoryCode,String langCode, String contextKey) {
		
		List<MosipDocTypeModel> docTypeList = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"documentTypes").toString();
		url = url + categoryCode +"/"+ langCode;
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (List<MosipDocTypeModel>) o);

		try {
			JSONArray docCatArray = RestClient.getDoc(url,new JSONObject() , new JSONObject(),contextKey);
			
			if(docCatArray != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				docTypeList = objectMapper.readValue(docCatArray.toString(), 
					objectMapper.getTypeFactory().constructCollectionType(List.class, MosipDocTypeModel.class));
	
				List<MosipDocTypeModel> newDocTypeList = new ArrayList<MosipDocTypeModel>();
				for(MosipDocTypeModel m: docTypeList) {
					if(m.getIsActive() )
						newDocTypeList.add(m);
				}
				MosipDataSetup.setCache(url, newDocTypeList,run_context);
				return newDocTypeList;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return docTypeList;
	}
	
	
	public static Hashtable<String, List<MosipIndividualTypeModel>> getIndividualTypesFromDynamicFields(String contextKey) {
		Hashtable<String, List<MosipIndividualTypeModel>> tbl = new Hashtable<String, List<MosipIndividualTypeModel>>();
		Hashtable<String, List<DynamicFieldModel>> tblDyn = MosipMasterData.getAllDynamicFields(contextKey);
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
	public static Hashtable<String, List<MosipIndividualTypeModel>> getIndividualTypes(String contextKey) {
	
		String mosipVersion = "legacy";
		Object obj = VariableManager.getVariableValue(contextKey,"mosip.version");
		if(obj != null)
			mosipVersion = obj.toString();
		if(mosipVersion.equals("1.2")) {
			return getIndividualTypesFromDynamicFields(contextKey);
		}
		return getIndividualTypes_legacy(contextKey);
	}
	public static Hashtable<String, List<MosipIndividualTypeModel>> getIndividualTypes_legacy(String contextKey) {
		
		Hashtable<String, List<MosipIndividualTypeModel>> tbl = new Hashtable<String, List<MosipIndividualTypeModel>>();
		
		List<MosipIndividualTypeModel> indTypeList = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"individualtypes").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (Hashtable<String, List<MosipIndividualTypeModel>>) o);

		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
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
				MosipDataSetup.setCache(url, tbl,run_context);
				//return tbl;
						
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return tbl;

	}
	public static List<MosipGenderModel> getGenderTypes(String lang,String contextKey) {
		List<MosipGenderModel> genderTypeList = Collections.emptyList();

		String mosipVersion = "legacy";
		Object obj = VariableManager.getVariableValue(contextKey,"mosip.version");
		if(obj != null)
			mosipVersion = obj.toString();
		if(mosipVersion.equals("1.2")) {
			genderTypeList = getGenderTypesLTS(lang,contextKey);
		}else {
			String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
					VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"gendertypes").toString();
			String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;			
							Object o = MosipDataSetup.getCache(url,run_context);
							if(o != null)
								return( (List<MosipGenderModel>) o);

							try {
								JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
								JSONArray docCatArray = resp.getJSONArray("genderType");
								
								if(docCatArray != null) {
									ObjectMapper objectMapper = new ObjectMapper();
									genderTypeList = objectMapper.readValue(docCatArray.toString(), 
										objectMapper.getTypeFactory().constructCollectionType(List.class, MosipGenderModel.class));
						
									MosipDataSetup.setCache(url, genderTypeList,run_context);
									return genderTypeList;
											
								}
							} catch (Exception e) {
								logger.error(e.getMessage());
							}
							return genderTypeList;
		}
		return genderTypeList;

		

	}
	private static List<MosipGenderModel> getGenderTypesLTS(String lang, String contextKey) {
		List<MosipGenderModel> genderTypeList = new ArrayList<>();
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"genderTypesByDynamicField").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url +"_"+lang,run_context);
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
			JSONObject resp = RestClient.post(url,jsonReqWrapper,contextKey);
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
				MosipDataSetup.setCache(url +"_"+lang, genderTypeList,run_context);
				return genderTypeList;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
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
	public static ApplicationConfigIdSchema getPreregLocHierarchy(String primLang, int count, String contextKey) throws Exception {

		
		MosipPreRegLoginConfig logincConfig = getPreregLoginConfig(contextKey);
		String countryCode = logincConfig.getMosip_country_code();
		String langCode = logincConfig.getMosip_primary_language();
		
		if(primLang != null )
			langCode = primLang;
	
		LocationHierarchyModel[]  locHiModels = MosipMasterData.getLocationHierarchy(langCode,contextKey);
		
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
		Hashtable<Double,Properties> tblSchema = getIDSchemaLatestVersion(contextKey);
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
		List<MosipLocationModel> rootLocs =  getImmedeateChildren(levelCode, langCode, contextKey);
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
			
			getChildLocations( locSchemaList, langCode,lc.getCode() , levelName, tbl,locHiModels,contextKey);
		}
		
		idschema.setTblLocations(tblList);
			
		return idschema;
	}
	static void getChildLocations(List<MosipIDSchema> locHirachyList, String langCode, String levelCode,
			String levelName, Hashtable<String, MosipLocationModel> tbl, LocationHierarchyModel[]  locHiModels,String contextKey) {

		int idx=0;
		Stack<List<MosipLocationModel>> stk = new Stack<List<MosipLocationModel>>();
		//while(isExists(locHirachyList, levelName))
		while( true ) {
				//MosipLocationModel lcParent = tbl.get(preLevel);
			List<MosipLocationModel> rootLocs =  getImmedeateChildren(levelCode, langCode, contextKey);
			if(rootLocs == null) {
				rootLocs = stk.pop();
				idx++;
			}
			if(rootLocs != null && !rootLocs.isEmpty()) {
				if(idx >= rootLocs.size())
					break;
				MosipLocationModel lc = rootLocs.get(idx );
				stk.push(rootLocs)	;
				tbl.put(lc.getHierarchyName(), lc);
				
				levelCode = lc.getCode();
			//	if(lc.getHierarchyLevel() >= maxLevel)
			//		break;
			}
			else
				break;
		}
			
		
	}

	public static String getIDschemaStringLatest(String contextKey){

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(
				VariableManager.NS_DEFAULT,
				//"individualtypes"
				"idschemaapi"
				).toString();

		

		try {
			JSONObject resp = RestClient.get(url, genQueryParams(), new JSONObject(),contextKey);

			return resp.toString();

		
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "{\"Failed\"}";
		
	}

	public static String postSchema(String id, int version,  JSONArray schema,String contextKey){

		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"idschema").toString();
	

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
			resp = RestClient.post(url, obj,contextKey);
			return resp.toString();
		}
		catch(Exception e){
			logger.error(e.getMessage());
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
	static void testSchemaRule(String contextKey) {
		
		ResidentDataProvider residentProvider = new ResidentDataProvider();
		residentProvider.addCondition(ResidentAttribute.RA_SECONDARY_LANG, "ara")
		.addCondition(ResidentAttribute.RA_Gender, Gender.Any)
		.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult)
		.addCondition(ResidentAttribute.RA_Finger, false);
		List<ResidentModel> lst =  residentProvider.generate(contextKey);
		Hashtable<Double, Properties>  schema = MosipMasterData.getIDSchemaLatestVersion(contextKey);
		Set<Double> schemaIds = schema.keySet();
		Double schemVersion = schema.keySet().iterator().next();
	
		for(ResidentModel r: lst) {
	
			JSONObject jsonObject = CreatePersona.createIdentity(r,null,contextKey);
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
				//logger.info(idschema.toJSONString());
				List<SchemaRule>  rule = idschema.getRequiredOn();
				if(rule != null) {
					for(SchemaRule sr: rule) {
						logger.info("rule:" + sr);
						boolean bval = validateCondn(sr.getExpr(),r);
						logger.info("rule:result=" + bval);
					}
				}
			}
				
		}
	}
	
	public static JSONArray getUiSpecId(String contextKey) {
		JSONArray array= new JSONArray();
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim() +"preregistration/v1/uispec/latest?identitySchemaVersion=0&version=0";
		//String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"uiSpec").toString();
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o = MosipDataSetup.getCache(url,run_context);
		if(o != null)
			return( (JSONArray) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray identityArray = resp.getJSONObject("jsonSpec").getJSONObject("identity").getJSONArray("identity");
				Iterator<Object> iterator = identityArray.iterator();
				while (iterator.hasNext()) {
					JSONObject identityJson=(JSONObject)iterator.next();
					if(identityJson.getBoolean("required") && !identityJson.getString("id").toLowerCase().contains("proof")) {
						array.put(identityJson.getString("id"));
					}
				}
				logger.info("printing Array : "+ array);
				
				
				MosipDataSetup.setCache(url, array, run_context);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return array;
	}
	
	
	
	
	public static void main(String[] args) {
		 String	contextKey="globalvariable";
		VariableManager.setVariableValue(contextKey,"urlBase","https://sandbox.mosip.net/");
		VariableManager.setVariableValue(contextKey,"configpath","config/*/mz/1.1.5/registration-processor-mz.properties");

	
//		MosipDataSetup.getConfig();
	//	List<MosipDeviceModel> devices = MosipDataSetup.getDevices("10002");
//		test1();
		
	//	List<DynamicFieldModel> lstDyn =  MosipMasterData.getAllDynamicFields();
//		List<MosipMachineModel> mach =  MosipDataSetup.getMachineDetail("10082", "eng");
//		MosipDataSetup.getMachineConfig(mach.get(0).getName()) ;
		getMappedDocumentTypes("POA","eng",contextKey);
		
		Hashtable<Double,Properties> tbl1 = getIDSchemaLatestVersion(contextKey);
		 double schemaId = tbl1.keys().nextElement();
	//	 logger.info(schemaId);
		 List<MosipIDSchema> lstSchema = (List<MosipIDSchema>) tbl1.get(schemaId).get("schemaList");
		 List<String> reqdFields = (List<String>) tbl1.get(schemaId).get("requiredAttributes");
					
		
		List<MosipGenderModel> allg = MosipMasterData.getGenderTypes("eng",contextKey);
		allg.forEach( g-> {
			logger.info(g.getCode() +"\t" + g.getGenderName());
		});
		testSchemaRule(contextKey);
		System.exit(1);
		ApplicationConfigIdSchema ss;
		try {
			ss = getPreregLocHierarchy("fra",1,contextKey);
			logger.info(ss.toJSONString());
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		System.exit(0);
		
		 
		//MosipPreRegLoginConfig c1 =  MosipMasterData.getPreregLoginConfig();
		//ApplicationConfigIdSchema idschma =	MosipMasterData.getAppConfigIdSchema();
		//logger.info(idschma.toJSONString());
									 
		


		if(0 ==1) {
			
		HashMap<String,LocationHierarchyModel[]> locHi = getAllLocationHierarchies(contextKey);
		
		List<MosipGenderModel> genderTypes = MosipMasterData.getGenderTypes("hin",contextKey);
		
		List<MosipBiometricTypeModel> bioTypes = getBiometricTypes(contextKey);
		for(MosipBiometricTypeModel bt: bioTypes) {
			
			logger.info(bt.toJSONString());
			List<MosipBiometricAttributeModel> bioAttrs = getBiometricAttrByTypes(bt.getCode(), bt.getLangCode(),contextKey);
			for(MosipBiometricAttributeModel bam: bioAttrs) {
				logger.info(bam.toJSONString());
			}
			
		}
			Hashtable<String, List<MosipIndividualTypeModel>>indTypes =  getIndividualTypes(contextKey);
			List<MosipDocCategoryModel> docCat =getDocumentCategories(contextKey);
			
			 List<MosipDocTypeModel> dcoTypes= getDocumentTypes(docCat.get(0).getCode(),docCat.get(0).getLangCode(),contextKey);
			 
			//test dynamic fields fields;
		
			Hashtable<String,List<DynamicFieldModel>> lstDynF =  getAllDynamicFields(contextKey) ;
			lstDynF.forEach( (k,v)->{
			
				for(DynamicFieldModel dm: v) {
					
					logger.info(dm.getName() );
					
				
				}
		
			});
						
			Hashtable<Double,Properties> tbl = getIDSchemaLatestVersion(contextKey);
			List<MosipIDSchema> lst = null;
			if(tbl != null)
				lst = (List<MosipIDSchema>) tbl.get( tbl.keys().nextElement()).get("schemaList");
			lst.forEach( (sch)->{
				logger.info(sch.getId() + " " + sch.getRequired());
			});
			List<MosipLanguage> langs =  getConfiguredLanguages(contextKey);
			
			// getIDSchemaLatestVersion();
			langs.forEach( (l) ->{
				logger.info(l.getCode() + " "+ l.getName());
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
					List<MosipLocationModel> list = getLocationsByLevel(locHierachies[i].getHierarchyLevelName(), contextKey);
					list.forEach((m) ->{
						logger.info(m.getName());
					});
				}	
			});
			
		}
	}
}