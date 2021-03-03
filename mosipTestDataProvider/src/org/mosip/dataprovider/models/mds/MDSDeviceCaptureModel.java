package org.mosip.dataprovider.models.mds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class MDSDeviceCaptureModel {

	String bioType;
	String bioSubType;
	String qualityScore;
	String bioValue;
	String deviceServiceVersion;
	String deviceCode;
	String hash;
	
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
