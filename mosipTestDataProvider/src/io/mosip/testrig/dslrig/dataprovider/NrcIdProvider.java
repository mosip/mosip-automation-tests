package io.mosip.testrig.dslrig.dataprovider;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.mosip.testrig.dslrig.dataprovider.models.NrcId;

public class NrcIdProvider {
	private static SecureRandom  rand = new SecureRandom ();


	static int MAX_NUM = 9999;

	public static List<NrcId> generate(int count) {

		List<NrcId> nrcIds = new ArrayList<NrcId>();
		for(int i=0; i < count; i++) {

			NrcId nrcId = new NrcId();
			nrcId.setNrcId(generateNrcId(rand));
			nrcIds.add(nrcId);
		}
		return nrcIds;
	}

	static String generateNrcId(Random rand) {
		return generateSixDigitNumber(rand) + "/" + generateTwoDigitNumber(rand) + "/" + generateOneDigitNumber(rand);
	}

	private static String generateSixDigitNumber(Random rand) {
		return String.format("%06d", rand.nextInt(1000000));
	}

	private static String generateTwoDigitNumber(Random rand) {
		return String.format("%02d", rand.nextInt(100));
	}

	private static String generateOneDigitNumber(Random rand) {
		return String.valueOf(rand.nextInt(10));
	}
}
