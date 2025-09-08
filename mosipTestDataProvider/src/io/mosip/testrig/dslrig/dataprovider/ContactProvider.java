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
	
	public static List<Contact> generate(List<Name> names, int count) {
	    List<Contact> contacts = new ArrayList<>();

	    if (names == null) {
	        names = new ArrayList<>();
	    }


	    for (int i = 0; i < count; i++) {
	        Name name;
	        if (i < names.size()) {
	            name = names.get(i);
	        } else {
	            // Add default name if list is too small
	            name = new Name();
	            name.setFirstName("John");
	            name.setSurName("Doe");
	            names.add(name);
	        }

	        // Ensure safe substring
	        String first = name.getFirstName().length() >= 2 ? 
	            name.getFirstName().substring(0, 2) : name.getFirstName();
	        String last = name.getSurName().length() >= 2 ? 
	            name.getSurName().substring(0, 2) : name.getSurName();

	        String emailId = String.format(
	            DataProviderConstants.email_format,
	            first,
	            last,
	            rand.nextInt(MAX_NUM)
	        );

	        Contact contact = new Contact();
	        contact.setEmailId(emailId);
	        contact.setMobileNumber(generateMobileNumber(rand));
	        contacts.add(contact);
	    }

	    return contacts;
	}

	
	static String generateMobileNumber(Random rand) {
		
		StringBuilder mobNumber = new StringBuilder();
		//Random rand = new Random();
		int idx = rand.nextInt(4);
		
		long val = (long) DataProviderConstants.mobNumber_prefix[idx] * 1000;
		val += rand.nextInt(999);
		mobNumber.append( String.format("%d",val));
		
		int nextpart = rand.nextInt(9999 - 1000) + 1000;
		String nextPart = String.format("%d", nextpart);
		mobNumber.append(nextPart);
		nextpart = rand.nextInt(9999 - 1000) + 1000;
		nextPart = String.format("%d", nextpart);
		mobNumber.append(nextPart);
		mobNumber.setLength(DataProviderConstants.mobNumber_maxlen);
		return mobNumber.toString();
		
		
	}
}
