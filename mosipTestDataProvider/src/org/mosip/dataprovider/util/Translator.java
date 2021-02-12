package org.mosip.dataprovider.util;

import java.io.IOException;

import org.mosip.dataprovider.CSVHelper;
import org.mosip.dataprovider.models.Name;

import com.ibm.icu.text.Transliterator;

public class Translator {
	static String IDlookupFile =DataProviderConstants.RESOURCE+"Address/lang-isocode-transid.csv";
	
	public static void main(String[] args) {
	        String text = "Mohandas Karamchand Ghandhi";
	        //Translated text: Hallo Welt!
	        System.out.println("Text:" + text + ",Translated text: " + translate( "heb", text));
	}

	static String getLanguageID(String langIsoCode) {
	
		String v ="Any-Any";
		
		try {
			CSVHelper csv = new CSVHelper(IDlookupFile);
			String[] rec;
			csv.open();
			while((rec=csv.readRecord()) != null) {
				if(rec[0].toLowerCase().equals(langIsoCode.toLowerCase())){
					String val = rec[2].trim();
					if(val != "")
						v = val;
					break;
				}
			}
			csv.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return v;
	}
	
	public static Name translateName(String toLanguageIsoCode, Name name) {
		Name langName = new Name();
		String lang_from_to = getLanguageID(toLanguageIsoCode);
		
			
		Transliterator toTrans = Transliterator.getInstance(lang_from_to);
		
		if(name.getMidName() != null)
			langName.setMidName(toTrans.transliterate(name.getMidName()));
		if(name.getFirstName() != null)
			langName.setFirstName(toTrans.transliterate(name.getFirstName()));
		if(name.getSurName() != null)
			langName.setSurName(toTrans.transliterate(name.getSurName()));
		langName.setGender(name.getGender());
		
		return langName;
	}
	public static String translate(String toLanguageIsoCode, String text) {
		
		String lang_from_to = getLanguageID(toLanguageIsoCode);
		//Enumeration<String> idSet = Transliterator.getAvailableIDs();
		//String lang_from_to = fromLanguage+ "-"+ toLanguage;
		//Boolean bFound = false;
		/*
		for( Iterator<String> it = idSet.asIterator(); it.hasNext();) {
			String id = it.next();
			System.out.println(id);
			if(id.equals(lang_from_to)) {
				
				bFound = true;
				break;
			}	
		}*/
	
		Transliterator toTrans = Transliterator.getInstance(lang_from_to);
		return toTrans.transliterate(text);
	}
}

