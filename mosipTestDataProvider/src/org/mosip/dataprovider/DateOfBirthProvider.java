package org.mosip.dataprovider;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.ResidentAttribute;

import com.ibm.icu.text.SimpleDateFormat;

public class DateOfBirthProvider {

	public static String generateDob(int minAge, int maxAge) {
		
		
		Random rand = new Random();
		int offset =  rand.nextInt(maxAge - minAge) + minAge;
		Calendar calendar = Calendar.getInstance();
	    calendar.add(Calendar.YEAR, -offset);
	    Date dob =  calendar.getTime();
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");  
		return formatter.format(dob);

	}
	public static String generate(ResidentAttribute ageAttribute) {
		if(ageAttribute == ResidentAttribute.RA_Adult)
			return generateDob(DataProviderConstants.Age_Adult_Min_Age, 100);
		else if(ageAttribute == ResidentAttribute.RA_Minor)
			return generateDob(0, DataProviderConstants.Age_Minor_Max_Age);
		else if(ageAttribute == ResidentAttribute.RA_Senior)
			return generateDob( DataProviderConstants.Age_Senior_Citizen_Min_Age, 100);
			
		 SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");  
		 return formatter.format(new Date());
	
	}
		
}
