package io.mosip.testrig.dslrig.dataprovider.models.setup;

import java.io.Serializable;

import lombok.Data;

@Data
public class MosipRegCenterModel implements Serializable{

	 private static final long serialVersionUID = 1L;

	 String addressLine1;
	 String addressLine2;
	 String addressLine3;
	 String centerEndTime;
	 String	centerStartTime;	
	 String centerTypeCode;
	 String contactPerson;
	 String contactPhone;


	    String holidayLocationCode;

	    String id;
	    boolean isActive;
	    String langCode;
	    String latitude;
	    String locationCode;
	    String longitude;
	    String lunchEndTime;	
	    String lunchStartTime;	
	    String name;
	    String perKioskProcessTime;	
	    String timeZone;
	    String workingHours;

	    String zoneCode;
}
