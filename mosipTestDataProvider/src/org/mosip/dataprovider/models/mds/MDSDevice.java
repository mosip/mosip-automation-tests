package org.mosip.dataprovider.models.mds;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@JsonIgnoreProperties({"error"})
public class MDSDevice {

	String purpose;
	List<Integer> deviceSubId;
	String digitalId;
	String deviceStatus;
	String deviceId;
	String deviceCode;
	String certification;
	String serviceVersion;
	List<String> specVersion;
	String callbackId;
	

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
