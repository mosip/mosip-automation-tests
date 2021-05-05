package org.mosip.dataprovider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mosip.dataprovider.models.Name;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.Gender;
import org.mosip.dataprovider.util.Translator;

import variables.VariableManager;

public class NameProvider {

	private static String resourceName_male = DataProviderConstants.RESOURCE+"Names/%s/boy_names.csv";
	private static String resourceName_female = DataProviderConstants.RESOURCE+ "Names/%s/girl_names.csv";
	private static String resourceName_surname =DataProviderConstants.RESOURCE+ "Names/%s/surnames.csv";
	
	static String[] getSurNames(String lang, int count) {
		String resPath = String.format(resourceName_surname, lang);
		String [] values = new String[count];
		int i=0;
		try {
			CSVHelper helper;
			helper = new CSVHelper(resPath);
			int recCount = 200; // helper.getRecordCount();
			int[] recNos = CommonUtil.generateRandomNumbers(count, recCount,0);
		
			helper.open();
			List<String[]> recs = helper.readRecords(recNos);
			for(String[] r: recs) {
				if(lang.equals("en"))
					values[i] = CommonUtil.toCaptialize(r[0]);
				else
					values[i] =  r[0];
				
				i++;
			}
			
			helper.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return values;
		
	}
	public static List<Name> generateNames(Gender gender,String lang, int count,List<Name> engNames ){
	
		List<Name> names = null;
		if(engNames == null) 
			names = generateNamesWrapper(gender,  count);
		else names = engNames;
			
		if(!lang.startsWith("en")) {
			List<Name> namesLang = new ArrayList<Name>();
			for(Name name: names) {
				Name langName = Translator.translateName( lang, name);
				namesLang.add(langName);
			}
			names = namesLang;
		}
		
		return names;
	}
	static List<Name> generateNamesWrapper(Gender gender, int count){
		/*
		syntheticnames=true
				syntheticmidname=true

				syntheticfirstnamelen=50
				syntheticmidnamelen=50
				syntheticlastnamelen=50
		 */
		Object objAttr = VariableManager.getVariableValue("syntheticnames");
		boolean bValue = objAttr == null ? false :  Boolean.parseBoolean(objAttr.toString());
		if(bValue) {
		
			return generateSynthNames(gender, count);
		}
		else
		{
			return generateNames(gender,count);
		}
		
	}
	static String genRandomWord(int len) {
		String w = new String("");
		
		int[] charName = CommonUtil.generateRandomNumbers(len, (int)'z',(int)'a');
		for(int c: charName )
			w +=  (char)c;
		
		return w;
	}
	static List<Name> generateSynthNames(Gender gender, int count){
		String lang ="en"; 
		List<Name> names = new ArrayList<Name>();
		Object objAttr = VariableManager.getVariableValue("syntheticmidname");
		boolean bValue = objAttr == null ? false : Boolean.parseBoolean(objAttr.toString());
		objAttr = VariableManager.getVariableValue("syntheticfirstnamelen");
		int fNameLen = objAttr == null ? 30: Integer.parseInt(objAttr.toString());
	
		objAttr = VariableManager.getVariableValue("syntheticmidnamelen");
		
		int mNameLen = objAttr == null ? 30: Integer.parseInt(objAttr.toString());
		
		objAttr = VariableManager.getVariableValue("syntheticlastnamelen");
		int lNameLen = objAttr == null ? 30: Integer.parseInt(objAttr.toString());
		
		for(int i=0; i < count; i++) {
			Name n = new Name();
			n.setFirstName( genRandomWord(30));
			if(bValue)
				n.setMidName( genRandomWord(30));
			n.setSurName( genRandomWord(30));
			n.setGender(gender);
			names.add(n);
		}
		
		return names;
		
	}
	 
	static List<Name> generateNames(Gender gender, int count){
	
		String lang ="en"; 
		List<Name> names = new ArrayList<Name>();
		
		String resPath = "";
		Gender recGender = Gender.Female;
		
		if(gender == Gender.Male) {
			resPath = String.format(resourceName_male, lang);
			recGender = Gender.Male;
		}
		else
			resPath = String.format(resourceName_female, lang);
		
		try {
			CSVHelper helper;
			
			helper = new CSVHelper(resPath);
		
			int recCount = helper.getRecordCount();
			int[] recNos = CommonUtil.generateRandomNumbers(count, recCount,0);
			helper.open();
			
			List<String[]> recs = helper.readRecords( recNos);
			
			String[] surNames = getSurNames(lang, count); 
			Name name = new Name();
			int i=0;
			for(String[] r: recs) {
				if(lang.equals("en")) {
					name.setFirstName(CommonUtil.toCaptialize(r[1]));
					name.setSurName( CommonUtil.toCaptialize(surNames[i]));
				}
				else {
					name.setFirstName(r[1]);
					name.setSurName(surNames[i]);
				}
				name.setGender(recGender);
				names.add(name);
				i++;
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return names;
	}
}
