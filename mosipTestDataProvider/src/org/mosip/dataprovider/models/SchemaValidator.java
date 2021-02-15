package org.mosip.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
@Data
@JsonIgnoreProperties({"arguments"})
public class SchemaValidator implements Serializable {

	 private static final long serialVersionUID = 1L;
	 String type;
	 String validator;
	
}
