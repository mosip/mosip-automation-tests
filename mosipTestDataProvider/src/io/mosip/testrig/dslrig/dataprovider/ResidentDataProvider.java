package io.mosip.testrig.dslrig.dataprovider;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.DocumentException;

import io.mosip.testrig.dslrig.dataprovider.mds.MDSClient;
import io.mosip.testrig.dslrig.dataprovider.models.ApplicationConfigIdSchema;
import io.mosip.testrig.dslrig.dataprovider.models.BiometricDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.Contact;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldModel;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldValueModel;
import io.mosip.testrig.dslrig.dataprovider.models.IrisDataModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.MosipGenderModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIndividualTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLanguage;
import io.mosip.testrig.dslrig.dataprovider.models.MosipPreRegLoginConfig;
import io.mosip.testrig.dslrig.dataprovider.models.Name;
import io.mosip.testrig.dslrig.dataprovider.models.NrcId;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/*
 * Generate Resident record
 * First Name MiddleName SurName ...
 * 
 * By default all attributes are selected.
 * If not set 'Any' is used
 *  Can set to 'No' to skip the attribute in output
 *  For Finger ->
 *     'All' -> all ten fingers
 *     String[] -> names of fingers - LeftThumb, RightThumb etc
 *  
 */
public class ResidentDataProvider {
	private static final Logger logger = LoggerFactory.getLogger(ResidentDataProvider.class);
	private static SecureRandom  rand = new SecureRandom ();
		Properties attributeList;
	
	
	public ResidentDataProvider() {
		attributeList = new Properties();
		//attributeList.put(ResidentAttribute.RA_Count, 1);
		//attributeList.put(ResidentAttribute.RA_PRIMARAY_LANG, DataProviderConstants.LANG_CODE_ENGLISH);
		//attributeList.put(ResidentAttribute.RA_Country, "PHIL");
		RestClient.clearToken();
	}
	//Attribute Value ->'Any','No' or specific value
	public ResidentDataProvider addCondition(ResidentAttribute attributeName, Object attributeValue) {
		attributeList.put(attributeName, attributeValue);
		return this;
	}
	public static ResidentModel genGuardian(Properties attributes,String contextKey) {
		Properties attributeList = new Properties();
		attributes.forEach( (k,v) ->{
			attributeList.put(k, v);
		});
		//attributeList.put(ResidentAttribute.RA_Count, 1);
		attributeList.put(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);
		attributeList.put(ResidentAttribute.RA_Gender, Gender.Any);
		
		ResidentDataProvider provider = new ResidentDataProvider();
		provider.attributeList = attributeList;
		ResidentModel guardian = provider.generate(contextKey).get(0);
		return guardian;
	}
	public static ResidentModel updateBiometric(ResidentModel model,String bioType,String contextKey) throws Exception {
		boolean bDirty = false;
		
		if(bioType.equalsIgnoreCase("finger")) {
			BiometricDataModel bioData = BiometricDataProvider.updateFingerData(contextKey);
			model.getBiometric().setFingerPrint( bioData.getFingerPrint());
			model.getBiometric().setFingerHash( bioData.getFingerHash());
			model.getBiometric().setFingerRaw(bioData.getFingerRaw());
			bDirty = true;
		}
		else
		if(bioType.equalsIgnoreCase("iris")) {
			List<IrisDataModel> iris = BiometricDataProvider.updateIris(contextKey);
			if(iris != null && !iris.isEmpty()) {
				model.getBiometric().setIris(iris.get(0));
				bDirty = true;
			}
		}
		else
		if(bioType.equalsIgnoreCase("face")) {
			BiometricDataModel bioData = model.getBiometric();
			byte[][] faceData = BiometricDataProvider.updateFaceData(contextKey);
			bioData.setEncodedPhoto(
					Base64.getEncoder().encodeToString(faceData[0]));
			bioData.setRawFaceData(faceData[1]);
		
			bioData.setFaceHash(CommonUtil.getHexEncodedHash( faceData[1]));
			bDirty = true;
		}
		
		  if(bDirty) model.getBiometric().setCbeff(null);
			 
		return model;
	}
	
	public static ResidentModel updateBiometricWithTestPersona(ResidentModel model, ResidentModel testModel,
			String bioType, String contextKey) throws Exception {

		if (bioType.equalsIgnoreCase("finger")) {

			model.getBiometric().setFingerHash(testModel.getBiometric().getFingerHash());
			model.getBiometric().setFingerPrint(testModel.getBiometric().getFingerPrint());
			model.getBiometric().setFingerRaw(testModel.getBiometric().getFingerRaw());
		} else if (bioType.equalsIgnoreCase("iris")) {
			model.getBiometric().setIris(testModel.getBiometric().getIris());
		} else if (bioType.equalsIgnoreCase("face")) {

			model.getBiometric().setEncodedPhoto(testModel.getBiometric().getEncodedPhoto());
			model.getBiometric().setFaceHash(testModel.getBiometric().getFaceHash());
			model.getBiometric().setRawFaceData(testModel.getBiometric().getRawFaceData());
		}
		return model;
	}
	
	private static String[] getConfiguredLanguages(String contextKey) {
		String [] lang_arr = null;
		List<String> langs= new ArrayList<String>();
		List<MosipLanguage> allLang = null;
		try {
			allLang = MosipMasterData.getConfiguredLanguages(contextKey);
		}catch(Exception e) {
			logger.error(e.getMessage());
		}
		
		MosipPreRegLoginConfig  preregconfig = MosipMasterData.getPreregLoginConfig(contextKey);
		if(preregconfig == null) {

			try {
			
				lang_arr = new String[allLang.size()];
				int i=0;
				for(MosipLanguage l: allLang){
					lang_arr[i]= l.getIso2();
					i++;
				}
			}catch(Exception e) {
				logger.error(e.getMessage());
			}
			return lang_arr;
		}
		//check if primary lang is already configured
		String primary_lang = preregconfig.getMosip_primary_language();
		if(primary_lang != null)
			langs.add(primary_lang);
		
		//Step1 : check any mandatory languages configured
		String mandatory_languages_list =preregconfig.getMandatory_languages();
		 String[] mandatlangueages = null;
		if(mandatory_languages_list !=null && !mandatory_languages_list.equals("")) {
			  mandatlangueages = mandatory_languages_list.split(",");
		}
		int minLanguages=Integer.parseInt(preregconfig.getMin_languages_count());
		String opt_lang_list = preregconfig.getOptional_languages();
		String[] opt_langs = null;
		if(opt_lang_list != null && !opt_lang_list.equals("")) {
			opt_langs = opt_lang_list.split(",");
		}
		if(mandatlangueages != null && mandatlangueages.length > 0 ) {
			for(int i=0; i < mandatlangueages.length; i++)
				langs.add( mandatlangueages[i]);
		}
		if(opt_langs != null && opt_langs.length >0) {
			for(int i=0; i < opt_langs.length; i++)
				langs.add( opt_langs[i]);
		}
		//if not enough languags add from the default master datalist
		if(minLanguages > 0  && langs.size() < minLanguages && allLang != null ) {
			int n2add = minLanguages - langs.size();
			for(int i= 0; i < allLang.size() && i < n2add; i++ ) {
				langs.add( allLang.get(i).getIso2());
			}
		}
		lang_arr = new String [ minLanguages > 0 ? minLanguages : langs.size()];
		return langs.toArray(lang_arr);
	}
	
	/*
	 * 
	 * GENERATE METHOD 
	 * 
	 * 
	 * 
	 */
	public List<ResidentModel> generate(String contextKey) {
		
		List<ResidentModel> residents = new ArrayList<ResidentModel>();
		
		int count = 1;
		Gender gender =  (Gender) attributeList.get(ResidentAttribute.RA_Gender);
		String primary_lang = (String) attributeList.get(ResidentAttribute.RA_PRIMARAY_LANG);
		String sec_lang = (String) attributeList.get(ResidentAttribute.RA_SECONDARY_LANG);
		String override_primary_lan = primary_lang;
		String override_sec_lang = sec_lang;
		String third_lang = (String) attributeList.get(ResidentAttribute.RA_THIRD_LANG);
		
		Object oAttr = attributeList.get(ResidentAttribute.RA_SCHEMA_VERSION);
		double schemaVersion = (oAttr == null) ? 0: (double)oAttr;
		VariableManager.setVariableValue(contextKey,"schemaVersion", schemaVersion);


		String[] langsRequired = getConfiguredLanguages(contextKey);
		if(langsRequired != null) {
			primary_lang = langsRequired[0];
			if(langsRequired.length > 1)
				sec_lang = langsRequired[1];
			if(langsRequired.length > 2)
				third_lang = langsRequired[2];
			
		}
		
		//override if specified
		if(override_primary_lan != null && !override_primary_lan.equals(""))
			primary_lang = override_primary_lan;
		
		if(override_sec_lang != null && !override_sec_lang.equals(""))
			sec_lang = override_sec_lang;
		
		oAttr = attributeList.get(ResidentAttribute.RA_Iris);
		boolean bIrisRequired = true;
		
		if(oAttr != null) {
			bIrisRequired = (boolean)oAttr;
		}
		if(gender == null)
			gender  = Gender.Any;
		List<Name> names_sec = null;
		List<Name> names_primary =null;
		
		Hashtable<String,List<DynamicFieldModel>> dynaFields = MosipMasterData.getAllDynamicFields(contextKey);
		 
		List<MosipGenderModel> genderTypes_primary = MosipMasterData.getGenderTypes(primary_lang,contextKey);
		List<MosipGenderModel> genderTypes_sec = null;
		List<MosipGenderModel> genderTypes_third = null;
		
		if(sec_lang != null)
			genderTypes_sec = MosipMasterData.getGenderTypes(sec_lang,contextKey);

		if(third_lang != null)
			genderTypes_third = MosipMasterData.getGenderTypes(third_lang,contextKey);

		//generate mix of both genders
		int maleCount =0,femaleCount = 0;
		
		switch(gender) {
			case  Any:
				maleCount = count/2;
				femaleCount = count-maleCount;
				break;
			case Male:
				maleCount = count;
				break;
			case Female:
				femaleCount = count;
				break;
			default:
				break;
				
		}
		List<Name> eng_male_names = null;
		List<Name> eng_female_names = null;
		List<Name> eng_names = null;
		
		if(maleCount >0) {
			eng_male_names = NameProvider.generateNames(Gender.Male,  DataProviderConstants.LANG_CODE_ENGLISH, maleCount, null,contextKey);
			eng_names = eng_male_names;
		}
		if(femaleCount > 0) {
			eng_female_names = NameProvider.generateNames(Gender.Female,  DataProviderConstants.LANG_CODE_ENGLISH, femaleCount, null,contextKey);
			if(eng_names != null)
				eng_names.addAll(eng_female_names);
			else
				eng_names = eng_female_names;
		}
		
		if(primary_lang != null) {
			if(!primary_lang.startsWith( DataProviderConstants.LANG_CODE_ENGLISH)) {
				names_primary = NameProvider.generateNames(gender, primary_lang, count, eng_names,contextKey);
			}
			else
				names_primary = eng_names;

		}
		if(sec_lang != null) {
			if(!sec_lang.startsWith( DataProviderConstants.LANG_CODE_ENGLISH)) {
				names_sec = NameProvider.generateNames(gender, sec_lang, count, eng_names,contextKey);
			}
			else
				names_sec = eng_names;

		}

		List<Contact> contacts = ContactProvider.generate(eng_names, count);
		List<NrcId> nrcIds = NrcIdProvider.generate( count);
		ApplicationConfigIdSchema locations = LocationProvider.generate(primary_lang, count,contextKey);
		ApplicationConfigIdSchema locations_secLang  = null;
		if(sec_lang != null)
			locations_secLang = LocationProvider.generate(sec_lang, count, contextKey);
		
		Hashtable<String,List<DynamicFieldValueModel>> bloodGroups = null;
		if(dynaFields != null && !dynaFields.isEmpty())
			 bloodGroups = BloodGroupProvider.generate(count, dynaFields);

		Hashtable<String, List<MosipIndividualTypeModel>> resStatusList =  MosipMasterData.getIndividualTypes(contextKey);
		
	//	int [] idxes = CommonUtil.generateRandomNumbers(count,DataProviderConstants.MAX_PHOTOS,0);
		
		List<IrisDataModel> irisList = null;
		try {
			if(bIrisRequired)
				irisList = BiometricDataProvider.generateIris(count,contextKey);
			logger.info("irisList : "+irisList.toString());
		} catch (  Exception e1) {
			logger.info("irisList catch : "+irisList.toString());
			logger.error(e1.getMessage());
		}
		
		//Random rand = new Random();
		for(int i=0; i < count; i++) {
			Gender res_gender = names_primary.get(i).getGender();
			ResidentModel res= new ResidentModel();
			res.setPrimaryLanguage(primary_lang);
			res.setSecondaryLanguage(sec_lang);
			res.setDynaFields(dynaFields);
			res.setName(names_primary.get(i));
			res.setThirdLanguage(third_lang);
			
			res.getGenderTypes().put(primary_lang, genderTypes_primary);
			if(sec_lang != null)
				res.getGenderTypes().put(sec_lang, genderTypes_sec);
			if(third_lang != null)
				res.getGenderTypes().put(third_lang, genderTypes_third);
			
			if(attributeList.containsKey(ResidentAttribute.RA_MissList)) {
				res.setMissAttributes( (List<String>) attributeList.get(ResidentAttribute.RA_MissList));
			}
			if(attributeList.containsKey(ResidentAttribute.RA_InvalidList)) {
				res.setInvalidAttributes( (List<String>) attributeList.get(ResidentAttribute.RA_InvalidList));
			}
			res.setGender(res_gender);
			if(names_sec != null) {
				res.setName_seclang(names_sec.get(i));
			}
		
			if(bloodGroups != null && !bloodGroups.isEmpty())
				res.setBloodgroup(bloodGroups.get(res.getPrimaryLanguage()).get(i));
			res.setContact(contacts.get(i));
			res.setNrcId(nrcIds.get(i));
			res.setDob( DateOfBirthProvider.generate((ResidentAttribute) attributeList.get(ResidentAttribute.RA_Age),contextKey));
			ResidentAttribute age =  (ResidentAttribute) attributeList.get(ResidentAttribute.RA_Age);
			Boolean skipGaurdian = false;
			if(age == ResidentAttribute.RA_Minor)  {
				res.setMinor(true);
				if(attributeList.containsKey(ResidentAttribute.RA_SKipGaurdian))
					skipGaurdian =   Boolean.valueOf(attributeList.get(ResidentAttribute.RA_SKipGaurdian).toString());
				if(!skipGaurdian)
					res.setGuardian( genGuardian(attributeList, contextKey));
			}
			
			else if(age == ResidentAttribute.RA_Infant )  {
				res.setInfant(true);
				if(attributeList.containsKey(ResidentAttribute.RA_SKipGaurdian))
					skipGaurdian =   Boolean.valueOf(attributeList.get(ResidentAttribute.RA_SKipGaurdian).toString());
				if(!skipGaurdian)
					res.setGuardian( genGuardian(attributeList, contextKey));
			}
			
			res.setAppConfigIdSchema( locations);
			res.setAppConfigIdSchema_secLang(locations_secLang);
			
			res.setLocation(  locations.getTblLocations().get(i));
			String [] addr = new String[ DataProviderConstants.MAX_ADDRESS_LINES];
			String addrFmt = "#%d, %d Street, %d block, lane #%d" ;//+ schemaItem.getId();
			for(int ii=0; ii< DataProviderConstants.MAX_ADDRESS_LINES; ii++) {
				String addrLine = String.format(addrFmt, (10+ rand.nextInt(999)),
					(1 + rand.nextInt(99)),
					(1 + rand.nextInt(10)), ii+1
					);
				addr[ii] = addrLine;
			}

			String primLang = res.getPrimaryLanguage();
			if(!primLang.toLowerCase().startsWith("en"))
			{
				
				String [] addrP = new String[ DataProviderConstants.MAX_ADDRESS_LINES];

				for(int ii=0; ii< DataProviderConstants.MAX_ADDRESS_LINES; ii++) {
					
					addrP[ii] = Translator.translate(primLang, addr[ii],contextKey);
				}
				res.setAddress(addrP);
			}
			else
				res.setAddress(addr);
			//res.setLocation(locations.get(res.getPrimaryLanguage()));
			if(res.getSecondaryLanguage() != null) {
				res.setLocation_seclang (  locations_secLang.getTblLocations().get(i));
				String[] addr_sec = new String[DataProviderConstants.MAX_ADDRESS_LINES];
				for(int ii=0; ii< DataProviderConstants.MAX_ADDRESS_LINES; ii++) {
					addr_sec[ii] = Translator.translate(res.getSecondaryLanguage(), addr[ii],contextKey);
				}	
				res.setAddress_seclang(addr_sec);
			}
			//	res.setLocation_seclang(locations.get(res.getPrimaryLanguage()));
			
			List<MosipIndividualTypeModel> lstResStatusPrimLang = resStatusList.get( res.getPrimaryLanguage());
			int indx =0;
			if(lstResStatusPrimLang != null) {
				for(MosipIndividualTypeModel itm: lstResStatusPrimLang) {
					if(itm.getCode().equals("NFR")) {
						res.setResidentStatus(itm);
						break;
					}
					indx++;
				}
				if(res.getResidentStatus() == null) {
					indx = rand.nextInt(lstResStatusPrimLang.size());
					res.setResidentStatus(lstResStatusPrimLang.get(indx));
				}
			}
			if(res.getSecondaryLanguage() != null) {
				List<MosipIndividualTypeModel> lstResStatusSecLang = resStatusList.get( res.getSecondaryLanguage());
				if(lstResStatusSecLang != null) {
					for(MosipIndividualTypeModel itm: lstResStatusSecLang) {
						if(itm.getCode().equals(lstResStatusPrimLang.get(indx).getCode())){
							res.setResidentStatus_seclang(itm);
							break;
						}
					}
				}	
				if(res.getResidentStatus_seclang() == null) {
					res.setResidentStatus_seclang(res.getResidentStatus());
				}
			}
			Object bFinger = attributeList.get(ResidentAttribute.RA_Finger);
			Boolean skip =  (bFinger == null ? false: !(Boolean)bFinger);
			res.setSkipFinger(skip);
			bFinger = attributeList.get(ResidentAttribute.RA_Photo);
			skip =  (bFinger == null ? false: !(Boolean)bFinger);
			res.setSkipFace(skip);
			bFinger = attributeList.get(ResidentAttribute.RA_Iris);
			skip =  (bFinger == null ? false: !(Boolean)bFinger);
			res.setSkipIris(skip);
			
			
			BiometricDataModel bioData =null;
			try {
				bioData = BiometricDataProvider.getBiometricData(bFinger == null ? true: (Boolean)bFinger,contextKey);
			} catch (IOException e2) {
				logger.error(e2.getMessage());
			}
			if(bIrisRequired)
				bioData.setIris(irisList.get(i));
			
			Object bOFace = attributeList.get(ResidentAttribute.RA_Photo);
			boolean bFace = ( bOFace == null ? true: (boolean)bOFace);
			if(bFace) {
			//	byte[][] faceData = PhotoProvider.getPhoto(idxes[i], res_gender.name(),contextKey );
				
				byte[][] faceData = PhotoProvider.getPhoto(contextKey );
				
				bioData.setEncodedPhoto(
						Base64.getEncoder().encodeToString(faceData[0]));
				bioData.setRawFaceData(faceData[1]);
			
				try {
					bioData.setFaceHash(CommonUtil.getHexEncodedHash( faceData[1]));
				} catch (Exception e1) {
				//logger.error(e1.getMessage());
				}
			}
//			res.setEncodedPhoto( );

			res.setBiometric(bioData);
		
			oAttr = attributeList.get(ResidentAttribute.RA_Document);
			boolean bDocRequired = ( oAttr == null ? true: (boolean)oAttr);
			
			if(bDocRequired) {
				try {
					res.setDocuments(DocumentProvider.generateDocuments(res,contextKey));
				} catch (DocumentException | IOException  | ParseException e) {
					
					logger.error(e.getMessage());
				}
			}
			
			for(MosipDocument doc: res.getDocuments()) {
				String id = doc.getDocCategoryCode();
				int index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size()-1, 0)[0];
				res.getDocIndexes().put(id,index);
			}
			residents.add(res);
		}
		return residents;
	}

	public static void main(String[] args) throws Exception {
		
		ResidentDataProvider residentProvider = new ResidentDataProvider();
		residentProvider
		.addCondition(ResidentAttribute.RA_SECONDARY_LANG, "ara")
		.addCondition(ResidentAttribute.RA_Gender, Gender.Any)
		.addCondition(ResidentAttribute.RA_Age, ResidentAttribute.RA_Adult);
		
		List<ResidentModel> lst =  residentProvider.generate("contextKey");
		MDSClient cli = new MDSClient(0);
		
		for(ResidentModel r: lst) {
			logger.info(r.toJSONString());
	
			cli.createProfile("C:\\Mosip.io\\gitrepos\\mosip-mock-services\\MockMDS\\target\\Profile\\", "tst1", r,"contextKey","Registration");
			
		}
		
	}
}