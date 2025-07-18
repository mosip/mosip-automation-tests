package io.mosip.testrig.dslrig.dataprovider;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.mosip.testrig.dslrig.dataprovider.models.Contact;
import io.mosip.testrig.dslrig.dataprovider.models.Name;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;



public class ContactProvider {
	private static SecureRandom  rand = new SecureRandom ();

	
	static int MAX_NUM = 9999;
	
	public static List<Contact> generate(List<Name> names,int count) {
		
		List<Contact> contacts = new ArrayList<Contact>();
		for(int i=0; i < count; i++) {
			
			Contact contact = new Contact();
			String emailId =  String.format(DataProviderConstants.email_format, names.get(i).getFirstName().substring(0, 2),
					 names.get(i).getSurName().substring(0, 2),rand.nextInt(MAX_NUM));
			
			contact.setEmailId(emailId);
			contact.setMobileNumber( generateMobileNumber(rand));
			contacts.add(contact);
		}
		return contacts;
	}
	
//	static String generateMobileNumber(Random rand) {
//		
//		StringBuilder mobNumber = new StringBuilder();
//		//Random rand = new Random();
//		int idx = rand.nextInt(4);
//		
//		long val = (long) DataProviderConstants.mobNumber_prefix[idx] * 1000;
//		val += rand.nextInt(999);
//		mobNumber.append( String.format("%d",val));
//		
//		int nextpart = rand.nextInt(9999 - 1000) + 1000;
//		String nextPart = String.format("%d", nextpart);
//		mobNumber.append(nextPart);
//		nextpart = rand.nextInt(9999 - 1000) + 1000;
//		nextPart = String.format("%d", nextpart);
//		mobNumber.append(nextPart);
//		mobNumber.setLength(DataProviderConstants.mobNumber_maxlen);
//		return mobNumber.toString();
//		
//		
//	}
	
	 public static String generateMobileNumber(Random rand) {
	        StringBuilder mobNumber = new StringBuilder();

	        int idx = rand.nextInt(DataProviderConstants.mobNumber_prefix.length);
	        mobNumber.append(DataProviderConstants.mobNumber_prefix[idx]);

	        for (int i = 1; i < DataProviderConstants.mobNumber_maxlen; i++) {
	            mobNumber.append(rand.nextInt(10));
	        }

	        return mobNumber.toString();
	    }
}
