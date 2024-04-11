package io.mosip.testrig.dslrig.dataprovider;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ibm.icu.text.SimpleDateFormat;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class DateOfBirthProvider {

	private static SecureRandom rand = new SecureRandom();
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
	 

	public static String generate(ResidentAttribute ageAttribute, String contextKey) {
		String age = VariableManager.getVariableValue(contextKey, "ageCategory").toString();
		String[] keyValuePairs = age.replaceAll("[{}]", "").split(",");
		Map<String, int[]> ageRanges = new HashMap<>();

		for (String pair : keyValuePairs) {
			String[] parts = pair.split(":");
			String key = parts[0].replaceAll("'", "").trim();
			String[] range = parts[1].replaceAll("'", "").trim().split("-");
			int minAge = Integer.parseInt(range[0]);
			int maxAge = Integer.parseInt(range[1]);
			ageRanges.put(key, new int[] { minAge, maxAge });
		}

		int[] ageRange = null;
		String ageGroup = "";

		switch (ageAttribute) {
		case RA_Adult:
			ageGroup = "ADULT";
			ageRange = ageRanges.get("ADULT");
			break;
		case RA_Minor:
			ageGroup = "CHILD";
			ageRange = ageRanges.get("MINOR");
			break;
		case RA_Senior:
			ageGroup = "SENIOR_CITIZEN";
			ageRange = ageRanges.get("ADULT");
			break;
		case RA_Infant:
			ageGroup = "CHILD";
			ageRange = ageRanges.get("INFANT");
			break;
		default:
			break;
		}

		if (ageRange != null) {
			VariableManager.setVariableValue(contextKey, "AGE_GROUP", ageGroup);
			return generateDob(ageRange[0], ageRange[1]);
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.format(new Date());
	}

}
