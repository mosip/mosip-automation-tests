package io.mosip.testrig.dslrig.dataprovider;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.ibm.icu.text.SimpleDateFormat;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class DateOfBirthProvider {

	private static SecureRandom  rand = new SecureRandom ();
	byte bytes[] = new byte[20];
	
	public static String generateDob(int minAge, int maxAge) {
		byte bytes[] = new byte[20];
		rand.nextBytes(bytes);
		int offset = rand.nextInt(maxAge - minAge) + minAge;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -offset);
		Date dob = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.format(dob);
	}

	public static String generate(ResidentAttribute ageAttribute,String contextKey) {
		if (ageAttribute == ResidentAttribute.RA_Adult) {
			 VariableManager.setVariableValue(contextKey, "AGE_GROUP", "ADULT");
			return generateDob(DataProviderConstants.Age_Adult_Min_Age, DataProviderConstants.Age_Senior_Citizen_Min_Age);
		}
		else if (ageAttribute == ResidentAttribute.RA_Minor) {
			 VariableManager.setVariableValue(contextKey, "AGE_GROUP", "CHILD");
			return generateDob(DataProviderConstants.Age_Minor_Min_Age, DataProviderConstants.Age_Adult_Min_Age);
		}
		else if (ageAttribute == ResidentAttribute.RA_Senior) {
			 VariableManager.setVariableValue(contextKey, "AGE_GROUP", "SENIOR_CITIZEN");
			return generateDob(DataProviderConstants.Age_Senior_Citizen_Min_Age, 100);
		}
		else if (ageAttribute == ResidentAttribute.RA_Infant) {
			 VariableManager.setVariableValue(contextKey, "AGE_GROUP", "CHILD");
			return generateDob(0, 5);
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.format(new Date());
	}

}
