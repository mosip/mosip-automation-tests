package org.mosip.dataprovider.preparation;



import java.security.MessageDigest;

import static io.restassured.RestAssured.given;

import java.io.Reader;
import java.io.StringReader;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mosip.dataprovider.models.setup.MosipDeviceModel;
import org.mosip.dataprovider.models.setup.MosipMachineModel;
import org.mosip.dataprovider.models.setup.MosipMachineSpecModel;
import org.mosip.dataprovider.models.setup.MosipMachineTypeModel;
import org.mosip.dataprovider.models.setup.MosipRegistrationCenterTypeModel;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.core.gherkin.messages.internal.gherkin.internal.com.eclipsesource.json.Json;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import variables.VariableManager;

public class MosipDataSetup {

	public static Properties getConfig() {
		Properties props = new Properties();
		//https://sandbox.mosip.net/config/*/mz/1.1.4/print-mz.properties
		//https://dev.mosip.net/config/*/mz/develop/registration-processor-mz.properties
		String configPath = "config/*/mz/develop/pre-registration-mz.properties";
		
		try {
			configPath = VariableManager.getVariableValue("configpath").toString();
		}catch(Exception e) {}
		
		String url = VariableManager.getVariableValue("urlBase").toString() + configPath;
		
		try {
			Response response = given().contentType(ContentType.TEXT).get(url );
    		if(response.getStatusCode() == 200) {
    			props.load(new StringReader(response.getBody().asString()));
    		}
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props;
	}
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
	
	//GET "https://dev.mosip.net/v1/syncdata/configs/<machine_name>" - machine config
	public static void getMachineConfig(String machineName) {
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue("syncdata").toString();
				
		url = url + machineName;
		JSONObject resp;
		try {
			resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("machines");
		
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	// get all machines
	public static List<MosipMachineModel> getMachineDetail(String machineId, String langCode) {
		
		List<MosipMachineModel> machines = null;
		String url = VariableManager.getVariableValue("urlBase").toString() +
		VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"machinedetail").toString();
		url = url + machineId + "/" + langCode;
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipMachineModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("machines");
				ObjectMapper objectMapper = new ObjectMapper();
				machines = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipMachineModel.class));
				
				setCache(url, machines);
			
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return machines;
	}
	public static void createRegCenterType(MosipRegistrationCenterTypeModel type) {
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"regcentertype").toString();
		
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
			JSONObject resp = RestClient.post(url,jsonReqWrapper);
			if(resp != null) {
				String r = resp.toString();
				System.out.println(r);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public static void createMachineType(MosipMachineTypeModel type) {
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"machinetype").toString();
		
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
			JSONObject resp = RestClient.post(url,jsonReqWrapper);
			if(resp != null) {
				String r = resp.toString();
				System.out.println(r);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	public static List<MosipMachineTypeModel> getMachineTypes() {
		List<MosipMachineTypeModel> machineTypes = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"machinetype").toString();
		url = url + "all";
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipMachineTypeModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				
				machineTypes = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipMachineTypeModel.class));
				
				setCache(url, machineTypes);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return machineTypes;
	}
	public static List<MosipRegistrationCenterTypeModel> getRegCenterTypes() {
		List<MosipRegistrationCenterTypeModel> machineTypes = null;
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"regcentertype").toString();
		url = url + "all";
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipRegistrationCenterTypeModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				
				machineTypes = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipRegistrationCenterTypeModel.class));
				
				setCache(url, machineTypes);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return machineTypes;
	}
		
		
	public static void createMachineSpec(MosipMachineSpecModel spec) {
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"machinespec").toString();
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
			JSONObject resp = RestClient.post(url,jsonReqWrapper);
			if(resp != null) {
				String r = resp.toString();
				System.out.println(r);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	public static void createMachine(MosipMachineModel machine) {
		
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"createmachine").toString();
		
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
			JSONObject resp = RestClient.post(url,jsonReqWrapper);
			if(resp != null) {
				String r = resp.toString();
				System.out.println(r);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static List<MosipDeviceModel> getDevices(String centerId) {
		//GET /v1/masterdata/devices/mappeddevices/1001?direction=DESC&orderBy=createdDateTime&pageNumber=0&pageSize=100
	
		List<MosipDeviceModel> devices = null;
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue(VariableManager.NS_MASTERDATA,"mappeddevices").toString();
	
		url = url +centerId;
		
		Object o =getCache(url);
		if(o != null)
			return( (List<MosipDeviceModel>) o);
		
		try {
			JSONObject resp = RestClient.get(url,new JSONObject() , new JSONObject());
			if(resp != null) {
				JSONArray typeArray = resp.getJSONArray("data");
				ObjectMapper objectMapper = new ObjectMapper();
				
				devices = objectMapper.readValue(typeArray.toString(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, MosipDeviceModel.class));
				
				setCache(url, devices);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return devices;
	}
	public static String configureMockABISBiometric(String bdbString, boolean bDuplicate, String[] duplicateBdbs, int delay, String operation) 
			throws JSONException, NoSuchAlgorithmException {
		

		if(operation == null || operation.equals(""))
			operation = "Indentify";
		
		String responseStr = "";
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue("mockABISsetExpectaion").toString();

		JSONObject req = new JSONObject();
		//req.put("id", CommonUtil.getSHA(bdbString));
		byte[] valBytes=java.util.Base64.getUrlDecoder().decode(bdbString);
		req.put("id", CommonUtil.getSHAFromBytes(valBytes));
		req.put("version","1.0");
		req.put("requesttime",CommonUtil.getUTCDateTime(null) );
		req.put("actionToInterfere",operation );
		req.put("forcedResponse","Duplicate" );
		
		req.put("delayInExecution",Integer.toString(delay) );
		
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
			JSONObject resp = RestClient.post(url, req);
			if(resp != null) {
				responseStr = resp.toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseStr = e.getMessage();
		}

		return responseStr;
	}
	
	
	public static String uploadPackets( List<String> packetPaths) {

		String responseStr = "";
		String url = VariableManager.getVariableValue("urlBase").toString() +
				VariableManager.getVariableValue("bulkupload").toString();

		JSONObject req = new JSONObject();
		req.put("category","packet");
		req.put("tableName","packet");
		req.put("operation","");
		
		
		try {
			JSONObject resp = RestClient.uploadFiles(url, packetPaths, req);
			if(resp != null) {
				responseStr = resp.toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			responseStr = e.getMessage();
		}
		return responseStr;
	}


}
