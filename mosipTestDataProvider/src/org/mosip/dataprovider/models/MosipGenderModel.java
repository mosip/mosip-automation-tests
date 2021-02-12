package org.mosip.dataprovider.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class MosipGenderModel  implements Serializable {

	private static final long serialVersionUID = 1L;

	String code;
	String genderName;
	Boolean isActive;
	String langCode;
	
	
}
