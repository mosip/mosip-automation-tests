package org.mosip.dataprovider.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties({ "location" , "createdAt","updatedAt" })
public class CityModel  implements Serializable{
	 private static final long serialVersionUID = 1L;
	 
	public class Country {
		private String objectId;
		public String getObjectId() {
			return objectId;
		}
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}
		public String get__type() {
			return __type;
		}
		public void set__type(String __type) {
			this.__type = __type;
		}
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		private String  __type;
		private String  className;
	}
	private Country country;
	public Country getCountry() {
		return country;
	}
	public void setCountry(Country country) {
		this.country = country;
	}
	private String objectId;
	private int cityId;
	
	private String name;
	
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
/*
{
"objectId": "JJfZWgGdZu",
"location": {
    "__type": "GeoPoint",
    "latitude": 42.54499,
    "longitude": 1.51483
},
"cityId": 3040132,
"name": "la Massana",
"country": {
    "__type": "Pointer",
    "className": "Continentscountriescities_Country",
    "objectId": "sv7fjDVISU"
},
"createdAt": "2019-12-09T21:04:56.736Z",
"updatedAt": "2019-12-09T21:04:56.736Z"
}*/