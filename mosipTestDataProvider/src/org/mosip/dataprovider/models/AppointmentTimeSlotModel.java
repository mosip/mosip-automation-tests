package org.mosip.dataprovider.models;

import java.io.Serializable;

public class AppointmentTimeSlotModel  implements Serializable{

	 private static final long serialVersionUID = 1L;
		String fromTime;
		String toTime;


		public String getFromTime() {
			return fromTime;
		}
		public void setFromTime(String fromTime) {
			this.fromTime = fromTime;
		}
		public String getToTime() {
			return toTime;
		}
		public void setToTime(String toTime) {
			this.toTime = toTime;
		}
		public int getAvailability() {
			return availability;
		}
		public void setAvailability(int availability) {
			this.availability = availability;
		}
		int availability;

}
