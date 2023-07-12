package org.mosip.dataprovider.models;
//skip validators,requiredOn,bioAttributes,controlType,label

import java.io.Serializable;
import java.util.List;

import org.mosip.dataprovider.models.mds.MDSDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
/*@JsonIgnoreProperties({"preview","labelName","transliteration","visible","label","changeAction","locationHierarchy",
	"transliterate","fieldLayout","groupLabel","templateName","conditionalBioAttributes","subtype","tooltip","checksum",
	"exceptionPhotoRequired",
	"parentLocCode","locationHierarchyLevel"
})*/
public class MosipIDSchema  implements Serializable{
	private static final Logger logger = LoggerFactory.getLogger(MosipIDSchema.class);

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
	//List<List<String>> locationHierarchy;
	//String bioAttributes;
	String controlType;
	int maximum;
	int minimum ;
	
	String subType ;
	String id ;
	
	String fieldType ;
	String group;
	
	@JsonAlias({"$ref"})
	String typeRef;
	
	List<SchemaValidator> validators;
	List<SchemaRule> requiredOn;
	List<String> bioAttributes;
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
