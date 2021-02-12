package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CenterDetailsModel  implements Serializable {

	 private static final long serialVersionUID = 1L;	
	String date;
	Boolean holiday;
	
	public Boolean getHoliday() {
		return holiday;
	}
	public void setHoliday(Boolean holiday) {
		this.holiday = holiday;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public List<AppointmentTimeSlotModel> getTimeslots() {
		return timeslots;
	}
	public void setTimeslots(List<AppointmentTimeSlotModel> timeslots) {
		this.timeslots = timeslots;
	}
	@JsonProperty("timeSlots")
	List<AppointmentTimeSlotModel> timeslots;


}
