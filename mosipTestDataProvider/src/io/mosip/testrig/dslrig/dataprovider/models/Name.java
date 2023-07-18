package io.mosip.testrig.dslrig.dataprovider.models;

import java.io.Serializable;

import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import lombok.Data;

@Data
public class Name  implements Serializable{
	
	 private static final long serialVersionUID = 1L;
	private String firstName;		
	private String midName;
	private String surName;
	private Gender gender;
	
	public Name translateTo(String langIsoCode,String contextKey) {
		Name n = new Name();
		n.firstName = Translator.translate(langIsoCode, firstName,contextKey);
		n.midName = Translator.translate(langIsoCode,midName,contextKey);
		n.surName = Translator.translate(langIsoCode, surName,contextKey);
		n.gender = gender;
		return n;
	}
	
	public Name() {
		firstName = "";
		midName = "";
		surName = "";
	}
	

}