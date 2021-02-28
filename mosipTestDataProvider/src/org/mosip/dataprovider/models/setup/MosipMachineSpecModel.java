package org.mosip.dataprovider.models.setup;

import java.io.Serializable;

import lombok.Data;

@Data
public class MosipMachineSpecModel implements Serializable {
	 private static final long serialVersionUID = 1L;
	
	 String		brand;
	 String 	description;
	 String 	id;
	 boolean	isActive;
	 String		langCode;
	 String   	machineTypeCode;
	 String   	minDriverversion;
	 String   	model;
	 String   	name;
}
