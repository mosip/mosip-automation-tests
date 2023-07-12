package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import org.mosip.dataprovider.models.mds.MDSDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class ApplicationConfigIdSchema implements Serializable{
	private static final Logger logger = LoggerFactory.getLogger(ApplicationConfigIdSchema.class);
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
					
				logger.error(e.getMessage());
			}	
			return jsonStr;
		}
}
