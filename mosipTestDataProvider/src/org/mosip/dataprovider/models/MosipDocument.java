package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
@Data
public class MosipDocument  implements Serializable{
	private static final Logger logger = LoggerFactory.getLogger(MosipDocument.class);

	 private static final long serialVersionUID = 1L;
	String docCategoryCode;
	String docCategoryName;
	String docCategoryLang;
	
	List<MosipDocTypeModel> type;
	List<String> docs;
	
	public String toJSONString() {
		
		ObjectMapper Obj = new ObjectMapper();
		String jsonStr ="";
		try {
				jsonStr = Obj.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		}	
		return jsonStr;
	}
}
