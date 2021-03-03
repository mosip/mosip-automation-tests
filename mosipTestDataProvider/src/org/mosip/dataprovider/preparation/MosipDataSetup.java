package org.mosip.dataprovider.preparation;


import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.models.MosipLanguage;
import org.mosip.dataprovider.models.setup.MosipMachineModel;
import org.mosip.dataprovider.models.setup.MosipMachineSpecModel;
import org.mosip.dataprovider.models.setup.MosipMachineTypeModel;
import org.mosip.dataprovider.models.setup.MosipRegistrationCenterTypeModel;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import variables.VariableManager;

public class MosipDataSetup {

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
}
