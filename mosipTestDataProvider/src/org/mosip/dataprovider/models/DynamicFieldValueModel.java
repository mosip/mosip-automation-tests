package org.mosip.dataprovider.models;

import java.io.Serializable;

public class DynamicFieldValueModel  implements Serializable {

	 private static final long serialVersionUID = 1L;
	String code;
	String value;
	String langCode;
	Boolean active;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	
}
