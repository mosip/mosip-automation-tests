package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class ApplicationConfigIdSchema implements Serializable{
	 private static final long serialVersionUID = 1L;
	 List<MosipIDSchema>  locationHierarchy;
	// List<ApplicationConfigSchemaItem> identity;
	 List<Hashtable<String,MosipLocationModel>> tblLocations ;
	 
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
