package io.mosip.testrig.dslrig.dataprovider.models;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
@Data
public class AppointmentModel implements Serializable{
	private static final Logger logger = LoggerFactory.getLogger(AppointmentModel.class);
	 private static final long serialVersionUID = 1L;
	String regCenterId;
	@JsonProperty("centerDetails")
	List<CenterDetailsModel> availableDates;

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
