package org.mosip.dataprovider.models;

import java.io.Serializable;

public class CountryLookup  implements Serializable{
	 private static final long serialVersionUID = 1L;
	private String iso2;
	public CountryLookup() {
		
	}
	public String getIso2() {
		return iso2;
	}
	public void setIso2(String iso2) {
		this.iso2 = iso2;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	private String objectId;
}
