package org.mosip.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "country" , "createdAt","updatedAt","geonameid","shape",
	"languages","cities","timezones","provinces" })
public class StateModel  implements Serializable{
	 private static final long serialVersionUID = 1L;
	private String objectId;
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getSubDivisionCode() {
		return subDivisionCode;
	}
	public void setSubDivisionCode(String subDivisionCode) {
		this.subDivisionCode = subDivisionCode;
	}
	public String getSubDivisionType() {
		return subDivisionType;
	}
	public void setSubDivisionType(String subDivisionType) {
		this.subDivisionType = subDivisionType;
	}
	@JsonProperty("Country_Code")
	private String countryCode;
	@JsonProperty("Subdivision_Code")
	private String subDivisionCode;
	
	@JsonProperty("Subdivion_Type")
	private String subDivisionType;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIso2() {
		return iso2;
	}
	public void setIso2(String iso2) {
		this.iso2 = iso2;
	}
	@JsonProperty("Subdivision_Name")
	private String name;
	
	private String iso2;
	
}
