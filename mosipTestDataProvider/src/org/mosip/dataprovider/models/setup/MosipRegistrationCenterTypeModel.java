package org.mosip.dataprovider.models.setup;

import java.io.Serializable;

import org.junit.Ignore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties({"createdBy,updatedBy"})
public class MosipRegistrationCenterTypeModel implements Serializable {

	 private static final long serialVersionUID = 1L;
	 
	 String		code;
	 String		description;
	 boolean 	isActive;
	 boolean	isDeleted;
	 String  	langCode;
	 String	 	name;
 
}
