package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppointmentModel implements Serializable{
	 private static final long serialVersionUID = 1L;
	int regCenterId;
	@JsonProperty("centerDetails")
	List<CenterDetailsModel> availableDates;
	public int getRegCenterId() {
		return regCenterId;
	}
	public void setRegCenterId(int regCenterId) {
		this.regCenterId = regCenterId;
	}
	public List<CenterDetailsModel> getAvailableDates() {
		return availableDates;
	}
	public void setAvailableDates(List<CenterDetailsModel> availableDates) {
		this.availableDates = availableDates;
	}


}
