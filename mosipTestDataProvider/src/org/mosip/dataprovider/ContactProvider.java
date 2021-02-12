package org.mosip.dataprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mosip.dataprovider.models.Contact;
import org.mosip.dataprovider.models.Name;
import org.mosip.dataprovider.util.DataProviderConstants;

public class ContactProvider {

	
	static int MAX_NUM = 9999;
	public static List<Contact> generate(List<Name> names,int count) {
		
		List<Contact> contacts = new ArrayList<Contact>();
		
		Random rand = new Random();
		for(int i=0; i < count; i++) {
			
			Contact contact = new Contact();
			String emailId =  String.format(DataProviderConstants.email_format, names.get(i).getFirstName(),
					 names.get(i).getSurName(),rand.nextInt(MAX_NUM));
			
			contact.setEmailId(emailId);
			contact.setMobileNumber( generateMobileNumber());
			contacts.add(contact);
		}
		return contacts;
	}
	static String generateMobileNumber() {
		
		StringBuilder mobNumber = new StringBuilder();
		Random rand = new Random();
		int idx = rand.nextInt(4);
		
		long val = DataProviderConstants.mobNumber_prefix[idx] * 1000;
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
