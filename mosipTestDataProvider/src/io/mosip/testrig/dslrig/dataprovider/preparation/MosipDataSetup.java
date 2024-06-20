package io.mosip.testrig.dslrig.dataprovider.preparation;



import static io.restassured.RestAssured.given;

import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipDeviceModel;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineModel;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineSpecModel;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipRegistrationCenterTypeModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class MosipDataSetup {
	private static final Logger logger = LoggerFactory.getLogger(MosipDataSetup.class);
	private static String RUN_CONTEXT = "run_context";

	public static Properties getConfig(String contextKey) {
		Properties props = new Properties();
		String configPath = "config/*/mz/develop/pre-registration-mz.properties";
		
		try {
			configPath = VariableManager.getVariableValue(contextKey,"configpath").toString();
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() + configPath;
		
		try {
			Response response = given().contentType(ContentType.TEXT).get(url );
    		if(response.getStatusCode() == 200) {
    			props.load(new StringReader(response.getBody().asString()));
    		}
    	} catch (Exception e) {
    		logger.error(e.getMessage());
		}
		return props;
	}
	public static Object getCache(String key,String contextKey) {
	
		try {
			return VariableManager.getVariableValue(contextKey,key);
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	public static void setCache(String key, Object value,String contextKey) {
		
		VariableManager.setVariableValue(contextKey,key,  value);
	}
	
	//GET "https://dev.mosip.net/v1/syncdata/configs/<machine_name>" - machine config
	public static void getMachineConfig(String machineName,String contextKey) {
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(contextKey,"syncdata").toString();
				
		url = url + machineName;
		JSONObject resp;
		try {
			resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("machines");
		
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
	// get all machines
	public static List<MosipMachineModel> getMachineDetail(String machineId, String langCode,String contextKey) {
		
		List<MosipMachineModel> machines = null;
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString()
				+ "v1/masterdata/machines/";
		
		url = url + machineId ;
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o =getCache(url,run_context);
		if(o != null)
			return( (List<MosipMachineModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("machines");
				ObjectMapper objectMapper = new ObjectMapper();
				machines = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipMachineModel.class));
				
				setCache(url, machines,run_context);
			
			}
			
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return machines;
	}
	
	// Get the inActive machine details by machineId (regcenter deactivate scenario) 
public static List<MosipMachineModel> searchMachineDetail(String machineId, String langCode,String contextKey) {
		
		List<MosipMachineModel> machines = null;
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString()
				+ "v1/masterdata/machines/search";
		
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		
		Object o =getCache(url,run_context);
		if(o != null)
			return( (List<MosipMachineModel>) o);
		
		JSONObject pagination = new JSONObject();
		pagination.put("pageStart",0);
		pagination.put("pageFetch",11);

		JSONArray filterArray = new JSONArray();
		JSONObject filters = new JSONObject();
		filters.put("type","equals");
		filters.put("value",machineId);
		filters.put("columnName","id");
		filterArray.put(filters);

		JSONArray sortArray = new JSONArray();
		JSONObject sort = new JSONObject();
		sort.put("sortType","ASC");
		sort.put("sortField","id");
		sortArray.put(sort);

		JSONObject searchRequest = new JSONObject();
		searchRequest.put("languageCode", "eng");
		searchRequest.put("pagination",pagination);
		searchRequest.put("filters",filterArray);
		searchRequest.put("sort",sortArray);

		JSONObject wrapper = new JSONObject();
		wrapper.put("metadata", new JSONObject());
		wrapper.put("version", "string");
		wrapper.put("id", "string");
		wrapper.put("requesttime",  CommonUtil.getUTCDateTime(null));
		wrapper.put("request", searchRequest);
		
		try {
			JSONObject resp = RestClient.post(url,wrapper,contextKey);
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				machines = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipMachineModel.class));
				
				setCache(url, machines,run_context);
			
			}
			
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		return machines;
	}
	//
	
	
	public static void createRegCenterType(MosipRegistrationCenterTypeModel type,String contextKey) {
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"regcentertype").toString();
		
		JSONObject jsonType = new JSONObject();
		jsonType.put("code", type.getCode());
		jsonType.put("descr", type.getDescription());
		jsonType.put("isActive", type.isActive());
		jsonType.put("langCode", type.getLangCode());
		jsonType.put("name", type.getName());

		JSONObject jsonReqWrapper = new JSONObject();
		jsonReqWrapper.put("request", jsonType);
		jsonReqWrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		jsonReqWrapper.put("version", "1.0");
		jsonReqWrapper.put("id", "id.machine");
		jsonReqWrapper.put("metadata", new JSONObject());

		try {
			JSONObject resp = RestClient.post(url,jsonReqWrapper,contextKey);
			if(resp != null) {
				String r = resp.toString();
				logger.info(r);
			}
		} catch (Exception e) {

			logger.error(e.getMessage());
		}

	}

	public static void createMachineType(MosipMachineTypeModel type,String contextKey) {
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"machinetype").toString();
		
		JSONObject jsonType = new JSONObject();
		jsonType.put("code", type.getCode());
		jsonType.put("description", type.getDescription());
		jsonType.put("isActive", type.isActive());
		jsonType.put("langCode", type.getLangCode());
		jsonType.put("name", type.getName());

		JSONObject jsonReqWrapper = new JSONObject();
		jsonReqWrapper.put("request", jsonType);
		jsonReqWrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		jsonReqWrapper.put("version", "1.0");
		jsonReqWrapper.put("id", "id.machine");
		jsonReqWrapper.put("metadata", new JSONObject());

		try {
			JSONObject resp = RestClient.post(url,jsonReqWrapper,contextKey);
			if(resp != null) {
				String r = resp.toString();
				logger.info(r);
			}
		} catch (Exception e) {

			logger.error(e.getMessage());
		}

	}
	public static List<MosipMachineTypeModel> getMachineTypes(String contextKey) {
		List<MosipMachineTypeModel> machineTypes = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"machinetype").toString();
		url = url + "all";
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o =getCache(url,run_context);
		if(o != null)
			return( (List<MosipMachineTypeModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				
				machineTypes = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipMachineTypeModel.class));
				
				setCache(url, machineTypes,run_context);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return machineTypes;
	}
	public static List<MosipRegistrationCenterTypeModel> getRegCenterTypes(String contextKey) {
		List<MosipRegistrationCenterTypeModel> machineTypes = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"regcentertype").toString();
		url = url + "all";
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o =getCache(url,run_context);
		if(o != null)
			return( (List<MosipRegistrationCenterTypeModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				
				machineTypes = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipRegistrationCenterTypeModel.class));
				
				setCache(url, machineTypes,run_context);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return machineTypes;
	}
		
		
	public static void createMachineSpec(MosipMachineSpecModel spec,String contextKey) {
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"machinespec").toString();
		JSONObject jsonSpec = new JSONObject();
		jsonSpec.put("id", spec.getId());
		jsonSpec.put("name", spec.getName());
		jsonSpec.put("brand", spec.getBrand());
		jsonSpec.put("description", spec.getDescription());
		jsonSpec.put("isActive", spec.isActive());
		jsonSpec.put("langCode", spec.getLangCode());
		jsonSpec.put("machineTypeCode", spec.getMachineTypeCode());
		jsonSpec.put("minDriverVersion", spec.getMinDriverversion());
		jsonSpec.put("model", spec.getModel());

		JSONObject jsonReqWrapper = new JSONObject();
		jsonReqWrapper.put("request", jsonSpec);
		jsonReqWrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		jsonReqWrapper.put("version", "1.0");
		jsonReqWrapper.put("id", "id.machine");
		jsonReqWrapper.put("metadata", new JSONObject());

		try {
			JSONObject resp = RestClient.post(url,jsonReqWrapper,contextKey);
			if(resp != null) {
				String r = resp.toString();
				logger.info(r);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		
	}
	public static void createMachine(MosipMachineModel machine,String contextKey) {
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"createmachine").toString();
		
		JSONObject jsonMachine = new JSONObject();
		jsonMachine.put("id", machine.getId());
		jsonMachine.put("ipAddress", machine.getIpAddress());
		jsonMachine.put("isActive", machine.isActive());
		jsonMachine.put("langCode", machine.getLangCode());
		jsonMachine.put("macAddress", machine.getMacAddress());
		jsonMachine.put("machineSpecId", machine.getMachineSpecId());
		jsonMachine.put("name", machine.getName());
		jsonMachine.put("publicKey", machine.getPublicKey());
		jsonMachine.put("regCenterId", machine.getRegCenterId());
		jsonMachine.put("serialNum", machine.getSerialNum());
		jsonMachine.put("signPublicKey", machine.getSignPublicKey());
		jsonMachine.put("validityDateTime", machine.getValidityDateTime());
		jsonMachine.put("zoneCode", machine.getZoneCode());
					
		JSONObject jsonReqWrapper = new JSONObject();
		jsonReqWrapper.put("request", jsonMachine);
		jsonReqWrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		jsonReqWrapper.put("version", "1.0");
		jsonReqWrapper.put("id", "id.machine");
		jsonReqWrapper.put("metadata", new JSONObject());

	
		
		try {
			JSONObject resp = RestClient.post(url,jsonReqWrapper,contextKey);
			if(resp != null) {
				String r = resp.toString();
				logger.info(r);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
	
public static void updateMachine(MosipMachineModel machine,String contextKey) {
		
	
	  String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
	  VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"machine"
	  ).toString();
		JSONObject jsonMachine = new JSONObject();
		jsonMachine.put("id", machine.getId());
		jsonMachine.put("ipAddress", machine.getIpAddress());
		jsonMachine.put("isActive", machine.isActive());
		jsonMachine.put("langCode", machine.getLangCode());
		jsonMachine.put("macAddress", machine.getMacAddress());
		jsonMachine.put("machineSpecId", machine.getMachineSpecId());
		jsonMachine.put("name", machine.getName());
		jsonMachine.put("publicKey", machine.getPublicKey());
		jsonMachine.put("regCenterId", machine.getRegCenterId());
		jsonMachine.put("serialNum", machine.getSerialNum());
		jsonMachine.put("signPublicKey", machine.getSignPublicKey());
		jsonMachine.put("validityDateTime", machine.getValidityDateTime());
		jsonMachine.put("zoneCode", machine.getZoneCode());
					
		JSONObject jsonReqWrapper = new JSONObject();
		jsonReqWrapper.put("request", jsonMachine);
		jsonReqWrapper.put("requesttime", CommonUtil.getUTCDateTime(null));
		jsonReqWrapper.put("version", "1.0");
		jsonReqWrapper.put("id", "id.machine");
		jsonReqWrapper.put("metadata", new JSONObject());

	
		
		try {
			JSONObject resp = RestClient.put(url,jsonReqWrapper,contextKey);
			if(resp != null) {
				String r = resp.toString();
				logger.info(r);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	public static String updatePreRegStatus(String preregId, String statusCode,String contextKey) {
		String response = null;
		
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() + VariableManager.getVariableValue(contextKey,"updatePreRegStatus").toString()
				+ preregId + "?statusCode=" + statusCode;
		try {
			
			JSONObject resp = RestClient.putNoAuth(url, new JSONObject(),"prereg",contextKey);
			if (resp != null) {
				response = resp.getString("response");
				logger.info(response);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return response;

	}


	public static List<MosipDeviceModel> getDevices(String centerId,String contextKey) {
		//GET /v1/masterdata/devices/mappeddevices/1001?direction=DESC&orderBy=createdDateTime&pageNumber=0&pageSize=100
	
		List<MosipDeviceModel> devices = null;
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mappeddevices").toString();
	
		url = url +centerId;
		String run_context = VariableManager.getVariableValue(contextKey,"urlBase").toString() + RUN_CONTEXT;
		Object o =getCache(url,run_context);
		if(o != null)
			return( (List<MosipDeviceModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject(),contextKey);
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				
				devices = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipDeviceModel.class));
				
				setCache(url, devices,run_context);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	
		return devices;
	}
	public static String configureMockABISBiometric(String bdbString, boolean bDuplicate, String[] duplicateBdbs, int delay, String operation,String contextKey,String statusCode,String failureReason) 
			throws JSONException, NoSuchAlgorithmException {
		

		if(operation == null || operation.equals(""))
			operation = "Indentify";
		
		String responseStr = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mockABISsetExpectaion").toString();

		JSONObject req = new JSONObject();
	
		byte[] valBytes=java.util.Base64.getUrlDecoder().decode(bdbString);
		req.put("id", CommonUtil.getSHAFromBytes(valBytes));
		req.put("version","1.0");
		req.put("requesttime",CommonUtil.getUTCDateTime(null) );
		req.put("actionToInterfere",operation );

		req.put("forcedResponse",failureReason);
		req.put("delayInExecution",Integer.toString(delay));
		req.put("errorCode",statusCode);
		
		
		if(!bDuplicate)
			req.put("gallery",JSONObject.NULL);
		else
		{
			JSONObject duprefs = new JSONObject();
			JSONArray arr = new JSONArray();
			for(String s: duplicateBdbs) {
				JSONObject ref = new JSONObject();
				//ref.put("referenceId", CommonUtil.getSHA(bdbString));
				ref.put("referenceId", CommonUtil.getSHAFromBytes(valBytes));
				arr.put(ref);
			}
			duprefs.put("referenceIds", arr);
			req.put("gallery", duprefs);
		}
				
		try {
			JSONObject resp = RestClient.post(url, req,contextKey);
			if(resp != null) {
				responseStr = resp.toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			responseStr = e.getMessage();
		}

		return responseStr;
	}
	
	
	public static String uploadPackets( List<String> packetPaths,String contextKey) {

		String responseStr = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"bulkupload").toString();
		
		String centerID = VariableManager.getVariableValue(contextKey,"mosip.test.regclient.centerid").toString();

		JSONObject req = new JSONObject();
		req.put("category","packet");
		req.put("tableName","");
		req.put("operation","insert");
		req.put("centerId",centerID);
		req.put("process","NEW");
		req.put("source","REGISTRATION_CLIENT");
		req.put("supervisorStatus","APPROVED");
		
		//To do -- We need to mark supervisor status as approved or rejected conditionally 
		VariableManager.setVariableValue(contextKey, "SUPERVISOR_APPROVAL_STATUS", "APPROVED");	
		
	// Need to review these two below tags once the conclusion happens what tags will be set on the packet	
		VariableManager.setVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Finger", "MOSIP-FINGER01-2345678901");
		VariableManager.setVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Face", "MOSIP-FACE01-2345678901");
		
		logger.debug("Tags set while generating the packet: "
				+ VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-supervisorId")
				+ VariableManager.getVariableValue(contextKey, "Biometric_Quality-Iris")
				+ VariableManager.getVariableValue(contextKey, "INTRODUCER_AVAILABILITY")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Finger")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-META_DATA-centerId")
				+ VariableManager.getVariableValue(contextKey, "Biometric_Quality-Face")
				+ VariableManager.getVariableValue(contextKey, "Biometric_Quality-Finger")
				+ VariableManager.getVariableValue(contextKey, "EXCEPTION_BIOMETRICS")
				+ VariableManager.getVariableValue(contextKey, "ID_OBJECT-gender")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-CAPTURED_REGISTERED_DEVICES-Face")
				+ VariableManager.getVariableValue(contextKey, "AGE_GROUP")
				+ VariableManager.getVariableValue(contextKey, "SUPERVISOR_APPROVAL_STATUS")
				+ VariableManager.getVariableValue(contextKey, "META_INFO-OPERATIONS_DATA-officerId")
				+ VariableManager.getVariableValue(contextKey, "ID_OBJECT-residenceStatus"));
		
		try {
			JSONObject resp = RestClient.uploadFiles(url, packetPaths, req,contextKey);
			if(resp != null) {
				responseStr = resp.toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			responseStr = e.getMessage();
		}
		return responseStr;
	}

	public static String deleteMockAbisExpectations(String contextKey) {
		
		String response = "";
		String url = VariableManager.getVariableValue(contextKey,"urlBase").toString().trim()+
		VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "deleteMockAbisExpectations").toString().trim();
				
		try {
			
			response= RestClient.deleteExpectation(url, new JSONObject(),contextKey);
			
		} catch (Exception e) {
			
			logger.error(e.getMessage());
			response = e.getMessage();
		}
		return response;
	}


}
