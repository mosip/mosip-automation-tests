package org.mosip.dataprovider.models;

import java.io.Serializable;

import org.mosip.dataprovider.util.Gender;
import org.mosip.dataprovider.util.Translator;

import lombok.Data;

@Data
public class Name  implements Serializable{
	
	 private static final long serialVersionUID = 1L;
	private String firstName;		
	private String midName;
	private String surName;
	private Gender gender;
	
	public Name translateTo(String langIsoCode) {
		Name n = new Name();
		n.firstName = Translator.translate(langIsoCode, firstName);
		n.midName = Translator.translate(langIsoCode,midName);
		n.surName = Translator.translate(langIsoCode, surName);
		n.gender = gender;
		return n;
	}
	
	public Name() {
		firstName = "";
		midName = "";
		surName = "";
	}
	

}