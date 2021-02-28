package org.mosip.dataprovider.models.mds;

import java.util.Hashtable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class MDSRCaptureModel {

	Hashtable<String, List<MDSDeviceCaptureModel>> lstBiometrics;

	public MDSRCaptureModel() {
		lstBiometrics = new Hashtable<String, List<MDSDeviceCaptureModel>>();
	}
	public String toJSONString() {
		
		ObjectMapper mapper = new ObjectMapper();

		String jsonStr ="";
		try {
				jsonStr = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
				
				e.printStackTrace();
		}	
		return jsonStr;
	}
}
