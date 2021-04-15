package org.mosip.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
@Data
public class IrisDataModel implements Serializable{
	 private static final long serialVersionUID = 1L;
	 String left;
	 String right;
	 String leftHash;
	 String rightHash;
	 byte[] rawLeft;
	 byte[] rawRight;
	 
	 public String toJSONString() {
			
			ObjectMapper mapper = new ObjectMapper();
		//	mapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
			
			String jsonStr ="";
			try {
					jsonStr = mapper.writeValueAsString(this);
			} catch (JsonProcessingException e) {
					
					e.printStackTrace();
			}	
			return jsonStr;
		}
}
