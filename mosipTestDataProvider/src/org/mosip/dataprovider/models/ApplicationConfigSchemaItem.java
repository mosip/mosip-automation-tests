package org.mosip.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties({"labelName","validators"})
public class ApplicationConfigSchemaItem implements Serializable{

	 private static final long serialVersionUID = 1L;

	 String id;
	 String description;
	 String controlType;
	 Boolean inputRequired;
	 String fieldType;
	 String	type;
	 Boolean required;
	 
}
