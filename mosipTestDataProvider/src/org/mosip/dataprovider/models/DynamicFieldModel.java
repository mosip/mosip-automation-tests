package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
@Data
public class DynamicFieldModel  implements Serializable{

	/*
	 * In 1.1.6 active is changed to isActive
	 */
	 private static final long serialVersionUID = 1L;
	String id;
	String name;
	String langCode;
	String dataType;
	String description;
	String createdBy;
	String updatedBy;
	String createdOn;
	String updatedOn;
	@JsonAlias({"isActive","active"})
	Boolean isActive;

	List<DynamicFieldValueModel> fieldVal;
	
	
}
