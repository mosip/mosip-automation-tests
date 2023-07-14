package org.mosip.dataprovider.util;

import java.io.IOException;

import org.mosip.dataprovider.CSVHelper;
import org.mosip.dataprovider.models.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.Transliterator;

import variables.VariableManager;

public class Translator {
	private static final Logger logger = LoggerFactory.getLogger(Translator.class);
	static String IDlookupFile;
	
	public static void main(String[] args) {
	        String text = "Mohandas Karamchand Ghandhi";
	        //Translated text: Hallo Welt!
	        System.out.println("Text:" + text + ",Translated text: " + translate( "heb", text,"contextKey"));
	}

	static String getLanguageID(String langIsoCode,String contextKey) {
	
		String v ="Any-Any";
		
		try {
			IDlookupFile =VariableManager.getVariableValue(contextKey,"mountPath").toString()+VariableManager.getVariableValue(contextKey,"mosip.test.persona.datapath").toString()+"Address/lang-isocode-transid.csv";
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
			logger.error(e.getMessage());
		}
		return v;
	}
	
	public static Name translateName(String toLanguageIsoCode, Name name,String contextKey) {
		Name langName = new Name();
		String lang_from_to = getLanguageID(toLanguageIsoCode,contextKey);
		
			
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
	public static String translate(String toLanguageIsoCode, String text,String contextKey) {
		
		String lang_from_to = getLanguageID(toLanguageIsoCode,contextKey);
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

