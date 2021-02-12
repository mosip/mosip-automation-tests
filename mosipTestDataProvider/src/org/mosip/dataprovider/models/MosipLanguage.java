package org.mosip.dataprovider.models;
/*
 * {"nativeName":"English","code":"eng","name":"English","family":"Indo-European","isActive":true}
 */

import java.io.Serializable;

import lombok.Data;
@Data
public class MosipLanguage  implements Serializable{

	 private static final long serialVersionUID = 1L;
	String nativeName;
	String code;
	String name;
	String family;
	String iso2;
	Boolean isActive;
	
}
