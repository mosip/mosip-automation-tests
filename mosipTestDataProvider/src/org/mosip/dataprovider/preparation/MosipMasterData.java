package org.mosip.dataprovider.preparation;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mosip.dataprovider.ResidentDataProvider;
import org.mosip.dataprovider.models.ApplicationConfigIdSchema;

import org.mosip.dataprovider.models.DynamicFieldModel;
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
import org.mosip.dataprovider.test.CreatePersona;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.Gender;
import org.mosip.dataprovider.util.ResidentAttribute;
import org.mosip.dataprovider.util.RestClient;
import org.mvel2.MVEL;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import variables.VariableManager;

public  class MosipMasterData {

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
	
	public static List<DynamicFieldModel> getAllDynamicFields() {
		
		List<DynamicFieldModel> lstDynamicFields = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"dynamicFields").toString();
	
		Object o =getCache(url);
		if(o != null)
			return( (List<DynamicFieldModel>) o);
	
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray dynaFields = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				
				lstDynamicFields = objectMapper.readValue(dynaFields.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, DynamicFieldModel.class));
	
				setCache(url, lstDynamicFields);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lstDynamicFields;
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
					for (LocationHierarchyModel object : locHierarchy) {
						if(object.getIsActive())
						locationHierarchy[object.getHierarchyLevel()] = object;
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
		"/v1/masterdata/locations/immediatechildren/";
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
				config.setMosip_country_code(  resp.getString("mosip.country.code"));
				config.setMosip_id_validation_identity_dateOfBirth(resp.getString("mosip.id.validation.identity.dateOfBirth"));
				config.setMosip_primary_language(resp.getString("mosip.primary-language"));
				config.setPreregistration_documentupload_allowed_file_type(resp.getString("preregistration.documentupload.allowed.file.type"));
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
	public static Hashtable<Double,List<MosipIDSchema>>  getIDSchemaLatestVersion() {
	
		Hashtable<Double,List<MosipIDSchema>> tbl = new Hashtable<Double,List<MosipIDSchema>> ();
		String url = VariableManager.getVariableValue("urlBase").toString() +VariableManager.getVariableValue(
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
			System.out.println(idSchema.toString());
			schemaVersion=	resp.getDouble( "idVersion");
			schemaTitle = resp.getString("title");
			
			if(idSchema != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setSerializationInclusion(Include.NON_NULL);

				List<MosipIDSchema>  listSchema  = new ArrayList<MosipIDSchema>();
				for(int i=0; i < idSchema.length(); i++) {
					MosipIDSchema schema = objectMapper.readValue(idSchema.get(i).toString(), MosipIDSchema.class);
					listSchema.add(schema);
				}
				/*
				List<MosipIDSchema>  listSchema = objectMapper.readValue(idSchema.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipIDSchema.class));
				*/
				tbl.put(schemaVersion, listSchema);
				
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
	
	public static Hashtable<String, List<MosipIndividualTypeModel>> getIndividualTypes() {
		
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
	public static List<MosipGenderModel> getGenderTypes() {
		List<MosipGenderModel> genderTypeList = null;

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
		int maxLevel  = 0;
		
		if(locHiModels != null) {
			maxLevel = Arrays.stream(locHiModels).max(Comparator.comparing(
					LocationHierarchyModel::getHierarchyLevel) ).get().getHierarchyLevel();
			
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
		Hashtable<Double, List<MosipIDSchema>> tblSchema = getIDSchemaLatestVersion();
		List<MosipIDSchema> idSchemaList = tblSchema.get( tblSchema.keys().nextElement());
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
			
			getChildLocations( locSchemaList, langCode,lc.getCode() , levelName, tbl,maxLevel);
		}
		
		idschema.setTblLocations(tblList);
			
		return idschema;
	}
	static void getChildLocations(List<MosipIDSchema> locHirachyList, String langCode, String levelCode,
			String levelName, Hashtable<String, MosipLocationModel> tbl, int maxLevel) {

		int idx=0;
		Stack<List<MosipLocationModel>> stk = new Stack<List<MosipLocationModel>>();
		
		while( isExists(locHirachyList, levelName) ) {
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
				if(lc.getHierarchyLevel() >= maxLevel)
					break;
			}
			else
				break;
		}
			
		
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
		Hashtable<Double, List<MosipIDSchema>>  schema = MosipMasterData.getIDSchemaLatestVersion();
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
			
			
	
			for( MosipIDSchema idschema: schema.get( schemVersion)) {
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
	public static void main(String[] args) {
	
		List<MosipGenderModel> allg =MosipMasterData.getGenderTypes();
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
		
		List<MosipGenderModel> genderTypes = MosipMasterData.getGenderTypes();
		
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
		
			List<DynamicFieldModel> lstDynF =  getAllDynamicFields() ;
			for(DynamicFieldModel dm: lstDynF) {
	
				System.out.println(dm.getName() );
				
			
			}
				
			Hashtable<Double,List<MosipIDSchema>> tbl = getIDSchemaLatestVersion();
			List<MosipIDSchema> lst = null;
			if(tbl != null)
				lst = tbl.get( tbl.keys().nextElement());
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
