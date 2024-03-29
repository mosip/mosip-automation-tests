package io.mosip.testrig.dslrig.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
@Data
@JsonIgnoreProperties({"arguments","errorCode","errorMessageCode"})
public class SchemaValidator implements Serializable {

	 private static final long serialVersionUID = 1L;
	 String type;
	 String validator;
	 String langCode;
}
