package org.mosip.dataprovider.models.setup;

import java.io.Serializable;

import lombok.Data;

@Data
public class MosipRegCenterModel implements Serializable{

	 private static final long serialVersionUID = 1L;
	 
	 String addressLine1;
	 String addressLine2;
	 String addressLine3;
	 String centerEndTime;//HH:mm:ss
	 String	centerStartTime;	// "HH:mm:ss",
	 String centerTypeCode;
	 String contactPerson;
	 String contactPhone;
	 
	 /*exceptionalHolidayPutPostDto": [
	      {
	        "exceptionHolidayDate": "string",
	        "exceptionHolidayName": "string",
	        "exceptionHolidayReson": "string"
	      }
	    ], */
	    String holidayLocationCode;
	    
	    String id;
	    boolean isActive;
	    String langCode;
	    String latitude;
	    String locationCode;
	    String longitude;
	    String lunchEndTime;	// "HH:mm:ss",
	    String lunchStartTime;	// "HH:mm:ss",
	    String name;
	    String perKioskProcessTime;	// "HH:mm:ss",
	    String timeZone;
	    String workingHours;
	    /*
	    workingNonWorkingDays": {
	      "fri": true,
	      "mon": true,
	      "sat": true,
	      "sun": true,
	      "thu": true,
	      "tue": true,
	      "wed": true
	    },*/
	    String zoneCode;
}
