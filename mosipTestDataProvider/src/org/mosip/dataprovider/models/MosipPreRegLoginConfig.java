package org.mosip.dataprovider.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class MosipPreRegLoginConfig implements Serializable {

	 private static final long serialVersionUID = 1L;
	 private String mosip_country_code;
	 private String mosip_primary_language;
	 private String mosip_id_validation_identity_dateOfBirth;
	 
	private String	preregistration_documentupload_allowed_file_type;
	

		
}
