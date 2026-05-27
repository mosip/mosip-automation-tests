package io.mosip.testrig.dslrig.dataprovider.persona;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.BloodGroupProvider;
import io.mosip.testrig.dslrig.dataprovider.ContactProvider;
import io.mosip.testrig.dslrig.dataprovider.DateOfBirthProvider;
import io.mosip.testrig.dslrig.dataprovider.LocationProvider;
import io.mosip.testrig.dslrig.dataprovider.NameProvider;
import io.mosip.testrig.dslrig.dataprovider.NrcIdProvider;
import io.mosip.testrig.dslrig.dataprovider.ResidentDataProvider;
import io.mosip.testrig.dslrig.dataprovider.models.ApplicationConfigIdSchema;
import io.mosip.testrig.dslrig.dataprovider.models.Contact;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldModel;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldValueModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipGenderModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIndividualTypeModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLanguage;
import io.mosip.testrig.dslrig.dataprovider.models.MosipPreRegLoginConfig;
import io.mosip.testrig.dslrig.dataprovider.models.Name;
import io.mosip.testrig.dslrig.dataprovider.models.NrcId;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.ResidentAttribute;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Demographics-only persona fields (names, locations, DOB, status) without biometric I/O.
 */
public final class PersonaDemographicsBuilder {

	private static final Logger logger = LoggerFactory.getLogger(PersonaDemographicsBuilder.class);
	private static final SecureRandom RAND = new SecureRandom();

	private PersonaDemographicsBuilder() {
	}

	public static final class Context {
		public final int count;
		public final Gender gender;
		public final String primaryLang;
		public final String secLang;
		public final String thirdLang;
		public final Hashtable<String, List<DynamicFieldModel>> dynaFields;
		public final List<MosipGenderModel> genderTypesPrimary;
		public final List<MosipGenderModel> genderTypesSec;
		public final List<MosipGenderModel> genderTypesThird;
		public final List<Name> namesPrimary;
		public final List<Name> namesSec;
		public final List<Contact> contacts;
		public final List<NrcId> nrcIds;
		public final ApplicationConfigIdSchema locations;
		public final ApplicationConfigIdSchema locationsSecLang;
		public final Hashtable<String, List<DynamicFieldValueModel>> bloodGroups;
		public final Hashtable<String, List<MosipIndividualTypeModel>> resStatusList;

		Context(int count, Gender gender, String primaryLang, String secLang, String thirdLang,
				Hashtable<String, List<DynamicFieldModel>> dynaFields, List<MosipGenderModel> genderTypesPrimary,
				List<MosipGenderModel> genderTypesSec, List<MosipGenderModel> genderTypesThird, List<Name> namesPrimary,
				List<Name> namesSec, List<Contact> contacts, List<NrcId> nrcIds, ApplicationConfigIdSchema locations,
				ApplicationConfigIdSchema locationsSecLang,
				Hashtable<String, List<DynamicFieldValueModel>> bloodGroups,
				Hashtable<String, List<MosipIndividualTypeModel>> resStatusList) {
			this.count = count;
			this.gender = gender;
			this.primaryLang = primaryLang;
			this.secLang = secLang;
			this.thirdLang = thirdLang;
			this.dynaFields = dynaFields;
			this.genderTypesPrimary = genderTypesPrimary;
			this.genderTypesSec = genderTypesSec;
			this.genderTypesThird = genderTypesThird;
			this.namesPrimary = namesPrimary;
			this.namesSec = namesSec;
			this.contacts = contacts;
			this.nrcIds = nrcIds;
			this.locations = locations;
			this.locationsSecLang = locationsSecLang;
			this.bloodGroups = bloodGroups;
			this.resStatusList = resStatusList;
		}
	}

	public static Context prepare(Properties attributeList, String contextKey) throws Exception {
		int count = 1;
		Gender gender = (Gender) attributeList.get(ResidentAttribute.RA_Gender);
		String primaryLang = (String) attributeList.get(ResidentAttribute.RA_PRIMARAY_LANG);
		String secLang = (String) attributeList.get(ResidentAttribute.RA_SECONDARY_LANG);
		String thirdLang = (String) attributeList.get(ResidentAttribute.RA_THIRD_LANG);

		Object oAttr = attributeList.get(ResidentAttribute.RA_SCHEMA_VERSION);
		double schemaVersion = parseSchemaVersion(oAttr);
		VariableManager.setVariableValue(contextKey, "schemaVersion", schemaVersion);

		String[] langsRequired = getConfiguredLanguages(contextKey);
		if (langsRequired != null) {
			primaryLang = langsRequired[0];
			if (langsRequired.length > 1) {
				secLang = langsRequired[1];
			}
			if (langsRequired.length > 2) {
				thirdLang = langsRequired[2];
			}
		}

		String overridePrimary = (String) attributeList.get(ResidentAttribute.RA_PRIMARAY_LANG);
		String overrideSec = (String) attributeList.get(ResidentAttribute.RA_SECONDARY_LANG);
		if (overridePrimary != null && !overridePrimary.isEmpty()) {
			primaryLang = overridePrimary;
		}
		if (overrideSec != null && !overrideSec.isEmpty()) {
			secLang = overrideSec;
		}

		if (gender == null) {
			gender = Gender.Any;
		}

		Hashtable<String, List<DynamicFieldModel>> dynaFields = MosipMasterData.getAllDynamicFields(contextKey);
		List<MosipGenderModel> genderTypesPrimary = MosipMasterData.getGenderTypes(primaryLang, contextKey);
		List<MosipGenderModel> genderTypesSec = secLang != null ? MosipMasterData.getGenderTypes(secLang, contextKey)
				: null;
		List<MosipGenderModel> genderTypesThird = thirdLang != null
				? MosipMasterData.getGenderTypes(thirdLang, contextKey)
				: null;

		int maleCount = 0;
		int femaleCount = 0;
		switch (gender) {
			case Any -> {
				maleCount = count / 2;
				femaleCount = count - maleCount;
			}
			case Male -> maleCount = count;
			case Female -> femaleCount = count;
			default -> {
			}
		}

		List<Name> engMaleNames = null;
		List<Name> engFemaleNames = null;
		List<Name> engNames = null;
		if (maleCount > 0) {
			engMaleNames = NameProvider.generateNames(Gender.Male, DataProviderConstants.LANG_CODE_ENGLISH, maleCount,
					null, contextKey);
			engNames = engMaleNames;
		}
		if (femaleCount > 0) {
			engFemaleNames = NameProvider.generateNames(Gender.Female, DataProviderConstants.LANG_CODE_ENGLISH,
					femaleCount, null, contextKey);
			if (engNames != null) {
				engNames.addAll(engFemaleNames);
			} else {
				engNames = engFemaleNames;
			}
		}

		List<Name> namesPrimary = null;
		List<Name> namesSec = null;
		if (primaryLang != null) {
			if (!primaryLang.startsWith(DataProviderConstants.LANG_CODE_ENGLISH)) {
				namesPrimary = NameProvider.generateNames(gender, primaryLang, count, engNames, contextKey);
			} else {
				namesPrimary = engNames;
			}
		}
		if (secLang != null) {
			if (!secLang.startsWith(DataProviderConstants.LANG_CODE_ENGLISH)) {
				namesSec = NameProvider.generateNames(gender, secLang, count, engNames, contextKey);
			} else {
				namesSec = engNames;
			}
		}

		List<Contact> contacts = ContactProvider.generate(engNames, count);
		List<NrcId> nrcIds = NrcIdProvider.generate(count);
		ApplicationConfigIdSchema locations = LocationProvider.generate(primaryLang, count, contextKey);
		ApplicationConfigIdSchema locationsSecLang = secLang != null
				? LocationProvider.generate(secLang, count, contextKey)
				: null;

		Hashtable<String, List<DynamicFieldValueModel>> bloodGroups = null;
		if (dynaFields != null && !dynaFields.isEmpty()) {
			bloodGroups = BloodGroupProvider.generate(count, dynaFields);
		}

		Hashtable<String, List<MosipIndividualTypeModel>> resStatusList = MosipMasterData.getIndividualTypes(contextKey);

		return new Context(count, gender, primaryLang, secLang, thirdLang, dynaFields, genderTypesPrimary,
				genderTypesSec, genderTypesThird, namesPrimary, namesSec, contacts, nrcIds, locations,
				locationsSecLang, bloodGroups, resStatusList);
	}

	public static ResidentModel populate(int index, Context ctx, Properties attributeList, String contextKey)
			throws Exception {
		Gender resGender = ctx.namesPrimary.get(index).getGender();
		ResidentModel res = new ResidentModel();
		res.setPrimaryLanguage(ctx.primaryLang);
		res.setSecondaryLanguage(ctx.secLang);
		res.setThirdLanguage(ctx.thirdLang);
		res.setDynaFields(ctx.dynaFields);
		res.setName(ctx.namesPrimary.get(index));

		res.getGenderTypes().put(ctx.primaryLang, ctx.genderTypesPrimary);
		if (ctx.secLang != null) {
			res.getGenderTypes().put(ctx.secLang, ctx.genderTypesSec);
		}
		if (ctx.thirdLang != null) {
			res.getGenderTypes().put(ctx.thirdLang, ctx.genderTypesThird);
		}

		if (attributeList.containsKey(ResidentAttribute.RA_MissList)) {
			res.setMissAttributes((List<String>) attributeList.get(ResidentAttribute.RA_MissList));
		}
		if (attributeList.containsKey(ResidentAttribute.RA_InvalidList)) {
			res.setInvalidAttributes((List<String>) attributeList.get(ResidentAttribute.RA_InvalidList));
		}

		res.setGender(resGender);
		if (ctx.namesSec != null) {
			res.setName_seclang(ctx.namesSec.get(index));
		}

		if (ctx.bloodGroups != null && !ctx.bloodGroups.isEmpty()) {
			res.setBloodgroup(ctx.bloodGroups.get(res.getPrimaryLanguage()).get(index));
		}
		res.setContact(ctx.contacts.get(index));
		res.setNrcId(ctx.nrcIds.get(index));
		res.setDob(DateOfBirthProvider.generate((ResidentAttribute) attributeList.get(ResidentAttribute.RA_Age),
				contextKey));

		ResidentAttribute age = (ResidentAttribute) attributeList.get(ResidentAttribute.RA_Age);
		boolean skipGuardian = false;
		if (age == ResidentAttribute.RA_Minor) {
			res.setMinor(true);
			if (attributeList.containsKey(ResidentAttribute.RA_SKipGaurdian)) {
				skipGuardian = Boolean.parseBoolean(attributeList.get(ResidentAttribute.RA_SKipGaurdian).toString());
			}
			if (!skipGuardian) {
				res.setGuardian(ResidentDataProvider.genGuardian(attributeList, contextKey));
			}
		} else if (age == ResidentAttribute.RA_Infant) {
			res.setInfant(true);
			if (attributeList.containsKey(ResidentAttribute.RA_SKipGaurdian)) {
				skipGuardian = Boolean.parseBoolean(attributeList.get(ResidentAttribute.RA_SKipGaurdian).toString());
			}
			if (!skipGuardian) {
				res.setGuardian(ResidentDataProvider.genGuardian(attributeList, contextKey));
			}
		}

		res.setAppConfigIdSchema(ctx.locations);
		res.setAppConfigIdSchema_secLang(ctx.locationsSecLang);
		res.setLocation(ctx.locations.getTblLocations().get(index));

		String[] addr = new String[DataProviderConstants.MAX_ADDRESS_LINES];
		String addrFmt = "#%d, %d Street, %d block, lane #%d";
		for (int ii = 0; ii < DataProviderConstants.MAX_ADDRESS_LINES; ii++) {
			addr[ii] = String.format(addrFmt, (10 + RAND.nextInt(999)), (1 + RAND.nextInt(99)), (1 + RAND.nextInt(10)),
					ii + 1);
		}

		String primLang = res.getPrimaryLanguage();
		if (!primLang.toLowerCase().startsWith("en")) {
			String[] addrP = new String[DataProviderConstants.MAX_ADDRESS_LINES];
			for (int ii = 0; ii < DataProviderConstants.MAX_ADDRESS_LINES; ii++) {
				addrP[ii] = Translator.translate(primLang, addr[ii], contextKey);
			}
			res.setAddress(addrP);
		} else {
			res.setAddress(addr);
		}

		if (res.getSecondaryLanguage() != null) {
			res.setLocation_seclang(ctx.locationsSecLang.getTblLocations().get(index));
			String[] addrSec = new String[DataProviderConstants.MAX_ADDRESS_LINES];
			for (int ii = 0; ii < DataProviderConstants.MAX_ADDRESS_LINES; ii++) {
				addrSec[ii] = Translator.translate(res.getSecondaryLanguage(), addr[ii], contextKey);
			}
			res.setAddress_seclang(addrSec);
		}

		applyResidentStatus(res, ctx, index);
		applySkipFlagsFromAttributes(res, attributeList);
		return res;
	}

	private static void applySkipFlagsFromAttributes(ResidentModel res, Properties attributeList) {
		Object finger = attributeList.get(ResidentAttribute.RA_Finger);
		res.setSkipFinger(finger == null ? false : !(Boolean) finger);
		Object photo = attributeList.get(ResidentAttribute.RA_Photo);
		res.setSkipFace(photo == null ? false : !(Boolean) photo);
		Object iris = attributeList.get(ResidentAttribute.RA_Iris);
		res.setSkipIris(iris == null ? false : !(Boolean) iris);
	}

	private static void applyResidentStatus(ResidentModel res, Context ctx, int index) {
		List<MosipIndividualTypeModel> lstResStatusPrimLang = ctx.resStatusList.get(res.getPrimaryLanguage());
		int indx = 0;
		if (lstResStatusPrimLang != null) {
			for (MosipIndividualTypeModel itm : lstResStatusPrimLang) {
				if (itm.getCode().equals("NFR")) {
					res.setResidentStatus(itm);
					break;
				}
				indx++;
			}
			if (res.getResidentStatus() == null) {
				indx = RAND.nextInt(lstResStatusPrimLang.size());
				res.setResidentStatus(lstResStatusPrimLang.get(indx));
			}
		}
		if (res.getSecondaryLanguage() != null && lstResStatusPrimLang != null && !lstResStatusPrimLang.isEmpty()) {
			List<MosipIndividualTypeModel> lstResStatusSecLang = ctx.resStatusList.get(res.getSecondaryLanguage());
			if (lstResStatusSecLang != null) {
				final int chosen = Math.min(indx, lstResStatusPrimLang.size() - 1);
				String primaryCode = lstResStatusPrimLang.get(chosen).getCode();
				for (MosipIndividualTypeModel itm : lstResStatusSecLang) {
					if (itm.getCode().equals(primaryCode)) {
						res.setResidentStatus_seclang(itm);
						break;
					}
				}
			}
			if (res.getResidentStatus_seclang() == null) {
				res.setResidentStatus_seclang(res.getResidentStatus());
			}
		}
	}

	private static String[] getConfiguredLanguages(String contextKey) {
		String[] langArr = null;
		List<String> langs = new ArrayList<>();
		List<MosipLanguage> allLang = null;
		try {
			allLang = MosipMasterData.getConfiguredLanguages(contextKey);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		MosipPreRegLoginConfig preregconfig = MosipMasterData.getPreregLoginConfig(contextKey);
		if (preregconfig == null) {
			try {
				langArr = new String[allLang.size()];
				int i = 0;
				for (MosipLanguage l : allLang) {
					langArr[i] = l.getIso2();
					i++;
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			return langArr;
		}

		String primaryLang = preregconfig.getMosip_primary_language();
		if (primaryLang != null) {
			langs.add(primaryLang);
		}

		String mandatoryLanguagesList = preregconfig.getMandatory_languages();
		String[] mandatLanguages = null;
		if (mandatoryLanguagesList != null && !mandatoryLanguagesList.equals("")) {
			mandatLanguages = mandatoryLanguagesList.split(",");
		}
		int minLanguages = Integer.parseInt(preregconfig.getMin_languages_count());
		String optLangList = preregconfig.getOptional_languages();
		String[] optLangs = null;
		if (optLangList != null && !optLangList.equals("")) {
			optLangs = optLangList.split(",");
		}
		if (mandatLanguages != null && mandatLanguages.length > 0) {
			for (int i = 0; i < mandatLanguages.length; i++) {
				langs.add(mandatLanguages[i]);
			}
		}
		if (optLangs != null && optLangs.length > 0) {
			for (int i = 0; i < optLangs.length; i++) {
				langs.add(optLangs[i]);
			}
		}
		if (minLanguages > 0 && langs.size() < minLanguages && allLang != null) {
			int n2add = minLanguages - langs.size();
			for (int i = 0; i < allLang.size() && i < n2add; i++) {
				langs.add(allLang.get(i).getIso2());
			}
		}
		langArr = new String[minLanguages > 0 ? minLanguages : langs.size()];
		return langs.toArray(langArr);
	}

	static double parseSchemaVersion(Object oAttr) {
		if (oAttr == null) {
			return 0;
		}
		if (oAttr instanceof Number number) {
			return number.doubleValue();
		}
		try {
			return Double.parseDouble(oAttr.toString().trim());
		} catch (NumberFormatException e) {
			logger.debug("Invalid schema version '{}', using 0", oAttr);
			return 0;
		}
	}
}
