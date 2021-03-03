package org.mosip.dataprovider.models;
//skip validators,requiredOn,bioAttributes,controlType,label

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
@Data
@JsonIgnoreProperties({"visible","label","changeAction"})
public class MosipIDSchema  implements Serializable{

	 private static final long serialVersionUID = 1L;
	//Boolean visible;
	String format ;
	String description ;
	String contactType ;
	String alignmentGroup;
	String type ;
	Boolean required ;
	Boolean inputRequired;
	String fieldCategory ;
	//String bioAttributes;
	String controlType;
	int maximum;
	int minimum ;
	
	String subType ;
	String id ;
	
	String fieldType ;
	String group;
	List<SchemaValidator> validators;
	List<SchemaRule> requiredOn;
	List<String> bioAttributes;
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
