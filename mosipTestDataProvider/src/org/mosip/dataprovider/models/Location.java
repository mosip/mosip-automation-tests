package org.mosip.dataprovider.models;

import java.io.Serializable;

import org.mosip.dataprovider.util.Translator;

public class Location  implements Serializable{
	 private static final long serialVersionUID = 1L;
		public String getAddressLine1() {
			return addressLine1;
		}
		public void setAddressLine1(String addressLine1) {
			this.addressLine1 = addressLine1;
		}
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getZipcode() {
			return zipcode;
		}
		public void setZipcode(String zipcode) {
			this.zipcode = zipcode;
		}
		private String addressLine1;
		private String country;
		private String state;
		private String city;
		private String zipcode;
		
		public Location() {
			addressLine1 = country = state = city = zipcode = "";
		}
		//Assume from English
		public Location translateTo(String langIsoCode, String contextKey) {
			Location l = new Location();
			l.addressLine1 = Translator.translate(langIsoCode, addressLine1, contextKey);
			l.country = Translator.translate(langIsoCode, country, contextKey);
			l.state = Translator.translate(langIsoCode, state, contextKey);
			l.city = Translator.translate(langIsoCode, city, contextKey);
			l.zipcode = Translator.translate(langIsoCode, zipcode, contextKey);
			
			return l;
		}

}
