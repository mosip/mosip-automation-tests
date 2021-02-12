package org.mosip.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "continent" , "createdAt","updatedAt","geonameid","shape",
	"languages","cities","timezones","provinces" })
public class CountryModel  implements Serializable {
	 private static final long serialVersionUID = 1L;
	private String objectId; 
	private String name;	
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String id) {
		this.objectId = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIso3() {
		return iso3;
	}
	public void setIso3(String iso3) {
		this.iso3 = iso3;
	}
	public String getIso2() {
		return iso2;
	}
	public void setIso2(String iso2) {
		this.iso2 = iso2;
	}
	public String getPhonecode() {
		return phonecode;
	}
	public void setPhonecode(String phonecode) {
		this.phonecode = phonecode;
	}
	public String getCapital() {
		return capital;
	}
	public void setCapital(String capital) {
		this.capital = capital;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getNativeLanguage() {
		return nativeLanguage;
	}
	public void setNativeLanguage(String nativeLanguage) {
		this.nativeLanguage = nativeLanguage;
	}
	public String getEmoji() {
		return emoji;
	}
	public void setEmoji(String emoji) {
		this.emoji = emoji;
	}
	public String getEmojiU() {
		return emojiU;
	}
	public void setEmojiU(String emojiU) {
		this.emojiU = emojiU;
	}
	private	 String iso3;
	@JsonProperty("code")
	private String iso2;
	@JsonProperty("phone")
	private	String  phonecode;
	private String	capital;
	private	String  currency;
	
	@JsonProperty("native")
	private	String  nativeLanguage;
	
	private	String  emoji;
	private	String  emojiU;
	
}
