package org.mosip.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
@Data
public class MosipBiometricAttributeModel implements Serializable{

	 private static final long serialVersionUID = 1L;
	 private String biometricTypeCode;
		
	 private String code;
	 private String description;
	 private Boolean isActive;
	 private String langCode;
	 private String name;
	 
	 public String toJSONString() {
			
			ObjectMapper Obj = new ObjectMapper();
			String jsonStr ="";
			try {
					jsonStr = Obj.writeValueAsString(this);
			} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}	
			return jsonStr;
		}
}
