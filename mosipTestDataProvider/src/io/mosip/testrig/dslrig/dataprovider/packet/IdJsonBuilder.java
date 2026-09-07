package io.mosip.testrig.dslrig.dataprovider.packet;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.PacketTemplateProvider;
import io.mosip.testrig.dslrig.dataprovider.models.ContextSchemaDetail;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.MosipGenderModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLocationModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.SchemaValidator;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.test.CreatePersona;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DemographicMissFieldUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * ID packet JSON builder and field processors (extracted from {@code PacketTemplateProvider}).
 */
public final class IdJsonBuilder {

	private static final SecureRandom rand = new SecureRandom();
	private static final Logger logger = LoggerFactory.getLogger(IdJsonBuilder.class);

	private static final String IDENTITY = "identity";
	private static final String BIOMETRICSTYPE = "biometricsType";
	private static final String DOCUMENTTYPE = "documentType";
	private static final String FORMAT = "format";
	private static final String VALUE = "value";
	private static final String DOCFILE = "docFIle=";
	private static final String DTYPE = " dType=";
	private static final String CAT = " cat=";
	private static final String CHECKBOX = "checkbox";
	private static final String SIMPLETYPE = "simpleType";
	private static final String FIRSTNAME = "firstname";
	private static final double INVALID_SCHEMA = 2.5;
	private static final String LASTNAME = "lastname";
	private static final String MIDDLENAME = "middlename";
	private static final String DOCUMENTS = "documents";
	private static final String CBEFF = "cbeff";
	private static final String VERSION = "version";
	private static final String BIO_CBEFF_XML = "_bio_CBEFF.xml";
	private static final String BIO_CBEFF = "_bio_CBEFF";
	private static final String GENERATEIDJSONV2 = "generateIDJsonV2";

	private IdJsonBuilder() {
	}

	public static boolean processGender(MosipIDSchema s, ResidentModel resident, JSONObject identity,
			Hashtable<String, List<MosipGenderModel>> genderTypesLang,
			Hashtable<String, List<DynamicFieldModel>> dynaFields, String contextKey) {

		boolean processed = false;

		if ((s.getSubType() != null
				&& s.getSubType().toLowerCase().equals(VariableManager.getVariableValue(contextKey, "gender")))
				|| s.getId().toLowerCase().equals(VariableManager.getVariableValue(contextKey, "gender"))) {

			String primLang = resident.getPrimaryLanguage();
			String secLan = resident.getSecondaryLanguage();

			Gender resGen = resident.getGender();
			String genderCode = null;
			String primVal = "";
			String secVal = "";


			Object obj = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, resGen.name());
			if (obj != null) {
				genderCode = obj.toString();
				primVal = secVal = genderCode;
				VariableManager.setVariableValue(contextKey, "ID_OBJECT-gender", genderCode);
			}

			if (dynaFields != null) {
				DynamicFieldModel dfmPrim = null;
				String primaryValue = "";
				String secValue = "";

				for (DynamicFieldModel dfm : dynaFields.get(primLang)) {
					if (dfm.getIsActive() && dfm.getName().equals(s.getId())) {
						for (int i = 0; i < dfm.getFieldVal().size(); i++) {
							if (dfm.getFieldVal().get(i).getValue().equalsIgnoreCase(resGen.name())) {
								primaryValue = dfm.getFieldVal().get(i).getCode();
								dfmPrim = dfm;
								VariableManager.setVariableValue(contextKey, "ID_OBJECT-gender", primaryValue);
								break;
							}
						}

					}
				}
				secValue = primaryValue;

				if (secLan != null)
					for (DynamicFieldModel dfm1 : dynaFields.get(secLan)) {
						if (dfm1.getIsActive() && dfm1.getName().equals(s.getId())) {

							for (int i = 0; i < dfm1.getFieldVal().size(); i++) {
								if (dfm1.getFieldVal().get(i).getValue().equalsIgnoreCase(resGen.name())) {
									secValue = dfm1.getFieldVal().get(i).getCode();
									VariableManager.setVariableValue(contextKey, "ID_OBJECT-gender", secValue);
									break;
								}
							}
						}
					}

				CreatePersona.constructNode(identity, s.getId(), resident.getPrimaryLanguage(),
						resident.getSecondaryLanguage(), primaryValue, secValue,
						s.getType().equals(SIMPLETYPE) ? true : false);
				processed = true;

			}


		}
		return processed;

	}

	public static Pair<String, String> processAddresslines(MosipIDSchema s, ResidentModel resident, JSONObject identity,
			String contextKey) {
		byte bytes[] = new byte[20];
		rand.nextBytes(bytes);
		String addr = null;
		String addr_sec = "";

		if (s.getControlType().equals(CHECKBOX)) {
			addr = "Y";
			addr_sec = "Y";
		} else {
			String[] addressLines = resident.getAddress();
			int index = 0;
			if (s.getId().toLowerCase().contains("line1"))
				index = 0;
			else if (s.getId().toLowerCase().contains("line2"))
				index = 1;
			else if (s.getId().toLowerCase().contains("line3"))
				index = 2;
			else if (s.getId().toLowerCase().contains("line4"))
				index = 3;

			if (index > -1)
				addr = addressLines[index];
			if (addr == null) {
				addr = "#%d, %d Street, %d block";
				addr = String.format(addr, (100 + rand.nextInt(999)), (1 + rand.nextInt(99)), (1 + rand.nextInt(10)));

				if (resident.getSecondaryLanguage() != null)
					addr_sec = Translator.translate(resident.getSecondaryLanguage(), addr, contextKey);
			} else {
				if (resident.getSecondaryLanguage() != null) {
					addr_sec = resident.getAddress_seclang()[index];
					if (addr_sec == null)
						addr_sec = Translator.translate(resident.getSecondaryLanguage(), addr, contextKey);

				}

			}
			if (s.getMaximum() > 0 && addr.length() >= s.getMaximum())
				addr = addr.substring(0, s.getMaximum() - 1);
		}
		Pair<String, String> retVal = new Pair<String, String>(addr, addr_sec);
		return retVal;

	}

	public static String generateDefaultAttributes(MosipIDSchema schemaItem, ResidentModel resident,
			JSONObject identity, String contextKey) {
		byte bytes[] = new byte[20];
		rand.nextBytes(bytes);
		String someVal = null;
		List<SchemaValidator> validators = schemaItem.getValidators();
		if (validators != null) {
			for (SchemaValidator v : validators) {
				if (v.getType().equalsIgnoreCase("regex")) {
					String regexpr = v.getValidator();
					if (regexpr != null && !regexpr.equals(""))
						try {
							someVal = CommonUtil.genStringAsperRegex(regexpr, contextKey);
						} catch (Exception e) {
							logger.error(e.getMessage());
						}
				}
			}
		}
		if (someVal == null)
			someVal = CommonUtil.generateRandomString(schemaItem.getMaximum());
		return someVal;

	}

	public static Boolean updateFromAdditionalAttribute(JSONObject identity, MosipIDSchema s, ResidentModel resident,
			String contextKey) {
		Boolean bRet = false;
		Hashtable<String, String> addtnAttr = resident.getAddtionalAttributes();
		if (addtnAttr == null)
			return bRet;
		Enumeration<String> keys = addtnAttr.keys();

		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = addtnAttr.get(key);

			if (s.getId().equalsIgnoreCase(key)) {
				if (s.getType().equals(SIMPLETYPE)) {

					JSONArray jsonO = null;
					try {
						jsonO = new JSONArray(value);
						identity.put(s.getId(), jsonO);
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
					if (jsonO == null) {
						String secValue = Translator.translate(resident.getSecondaryLanguage(), value, contextKey);
						CreatePersona.constructNode(identity, s.getId(), resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), value, secValue, true);
					}
				} else
					identity.put(s.getId(), value);
				bRet = true;
				break;
			}
		}
		return bRet;
	}

	public static Boolean processDynamicFields(MosipIDSchema s, JSONObject identity, ResidentModel resident,
			String contextKey) {

		String primaryLanguage = resident.getPrimaryLanguage();
		String secLanguage = resident.getSecondaryLanguage();
		Hashtable<String, List<DynamicFieldModel>> dynaFields = resident.getDynaFields();
		Hashtable<String, List<MosipGenderModel>> genderTypes = resident.getGenderTypes();
		boolean found = false;
		Object processValue = VariableManager.getVariableValue(contextKey, "process");
		String flow = processValue != null ? processValue.toString() : "";

		if (s.getFieldType().equals("dynamic")) {

			found = processGender(s, resident, identity, genderTypes, dynaFields, contextKey);
			if (found)
				return found;

			if (dynaFields != null) {
				DynamicFieldModel dfmPrim = null;
				String primaryValue = "";
				String secValue = "";

				for (DynamicFieldModel dfm : dynaFields.get(primaryLanguage)) {
					if (dfm.getIsActive() && dfm.getName().equals(s.getId())) {
						primaryValue = dfm.getFieldVal().get(0).getCode();
						if (s.getId().contains("residenceStatus"))
							VariableManager.setVariableValue(contextKey, "ID_OBJECT-residenceStatus", primaryValue);
						dfmPrim = dfm;
						break;
					}
				}
				secValue = primaryValue;

				if (secLanguage != null)
					for (DynamicFieldModel dfm1 : dynaFields.get(secLanguage)) {
						if (dfm1.getIsActive() && dfm1.getName().equals(s.getId())) {
							secValue = dfm1.getFieldVal().get(0).getCode();
							if (s.getId().contains("residenceStatus"))
								VariableManager.setVariableValue(contextKey, "ID_OBJECT-residenceStatus", secValue);
							break;
						}
					}
				if (flow.equals("CRVS_NEW")) {

				} else {
					CreatePersona.constructNode(identity, s.getId(), resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(), primaryValue, secValue,
							s.getType().equals(SIMPLETYPE) ? true : false);
					found = true;
				}
			}

		}
		return found;
	}

	public static String generateIDJson(ResidentModel resident, HashMap<String, String[]> fileInfo, String contextKey,
			Properties prop, JSONObject preregResponse, String purpose, ContextSchemaDetail contextSchemaDetail,
			String qualityScore, boolean genarateValidCbeff, String process) {

		String idjson = "";

		JSONObject identity = new JSONObject();

		String primaryLanguage = resident.getPrimaryLanguage();
		String secLanguage = resident.getSecondaryLanguage();
		Hashtable<String, MosipLocationModel> locations = resident.getLocation();
		Hashtable<String, MosipLocationModel> locations_seclang = resident.getLocation_seclang();

		Set<String> locationSet = locations.keySet();

		Set<String> locationSet_sec = null;
		if (locations_seclang != null)
			locationSet_sec = locations_seclang.keySet();
		List<String> lstMissedAttributes = resident.getMissAttributes();
		if (lstMissedAttributes != null && !lstMissedAttributes.isEmpty()) {
			lstMissedAttributes = DemographicMissFieldUtil.expandMissAttributeIds(new ArrayList<>(lstMissedAttributes),
					contextSchemaDetail.getSchema(), contextKey);
		}

		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			RestClient.logInfo(contextKey, s.toJSONString());

			if (!CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId()))
				continue;

			if (lstMissedAttributes != null
					&& lstMissedAttributes.stream().anyMatch(v -> v.equalsIgnoreCase(s.getId()))) {
				continue;
			}

			if (PacketJsonSupport.isResidentUinField(s.getId(), contextKey)) {
				if (!PacketJsonSupport.isNewRegistrationProcess(process)) {
					String uin = resident.getUIN();
					if (uin != null && !uin.trim().equals("")) {
						identity.put(s.getId(), uin.trim());
					}
				}
				continue;
			}
			if (!s.getRequired() && !s.getInputRequired()) {
				continue;
			}

			if (!s.getRequired() && !(s.getRequiredOn() != null && s.getRequiredOn().size() > 0)) {
				continue;
			}
			

			if (VariableManager.getVariableValue(contextKey, "invalidIdSchemaFlag").toString().equals("invalidIdSchema")
					&& s.getId().equals(VariableManager.getVariableValue(contextKey, "IDSchemaVersion"))) {
				identity.put(s.getId(), Double.valueOf(INVALID_SCHEMA));
				continue;
			}

			if (VariableManager.getVariableValue(contextKey, "invalidIdSchemaFlag").toString().equals("oldIdSchema")
					&& s.getId().equals(VariableManager.getVariableValue(contextKey, "IDSchemaVersion"))) {
				identity.put(s.getId(),MosipMasterData.getIDSchemaOldVersion(contextKey));
				continue;
			}

			if (VariableManager.getVariableValue(contextKey, "IDSchemaVersion") != null
					&& s.getId().equals(VariableManager.getVariableValue(contextKey, "IDSchemaVersion"))) {
				identity.put(s.getId(), contextSchemaDetail.getSchemaVersion());
				continue;
			}

			if (s.getId().contains("residenceStatus")) {
				VariableManager.setVariableValue(contextKey, "ID_OBJECT-residenceStatus", resident.getResidentStatus().getCode());
			}

			if (updateFromAdditionalAttribute(identity, s, resident, contextKey)) {
				continue;
			}
			if (processDynamicFields(s, identity, resident, contextKey))
				continue;
			if (s.getId().equals("nrcId")) {
				identity.put(s.getId(),resident.getNrcId().getNrcId());
				continue;
			}
				String primaryValue = "";
				String secValue = "";
				if (VariableManager.getVariableValue(contextKey, "name") != null
						&& VariableManager.getVariableValue(contextKey, "name").toString().contains(s.getId())) {
					primaryValue = resident.getName().getFirstName() + " " + resident.getName().getMidName() + " "
							+ resident.getName().getSurName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getFirstName() + " "
								+ resident.getName_seclang().getMidName() + " "
								+ resident.getName_seclang().getSurName();
				} else if (prop.getProperty(FIRSTNAME) != null && s.getId().equals(prop.getProperty(FIRSTNAME))) {
					primaryValue = resident.getName().getFirstName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getFirstName();
				} else if (prop.getProperty(LASTNAME) != null && s.getId().equals(prop.getProperty(LASTNAME))) {
					primaryValue = resident.getName().getSurName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getSurName();
				} else if (prop.getProperty(MIDDLENAME) != null && s.getId().equals(prop.getProperty(MIDDLENAME))) {
					primaryValue = resident.getName().getMidName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getMidName();
				} else if (VariableManager.getVariableValue(contextKey, "dob") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "dob"))) {
					primaryValue = resident.getDob();
					secValue = primaryValue;
				} else if (prop.getProperty("addressgroup") != null
						&& s.getId().equals(prop.getProperty("addressgroup"))) {
					if (s.getControlType().equals(CHECKBOX)) {
						primaryValue = "Y";
						if (secLanguage != null)
							secValue = "Y";
					} else {
						Pair<String, String> addrLines = processAddresslines(s, resident, identity, contextKey);
						primaryValue = addrLines.getValue0();
						secValue = addrLines.getValue1();
					}
				} else if (s.getId().contains("residenceStatus")) {
					primaryValue = resident.getResidentStatus().getCode();
					secValue = primaryValue;
				} else if (VariableManager.getVariableValue(contextKey, "emailId") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "emailId"))) {
					primaryValue = resident.getContact().getEmailId();
				}

				else if (s.getId().toLowerCase().contains("blood")) {
					primaryValue = resident.getBloodgroup().getCode();
					secValue = primaryValue;
				} else if (VariableManager.getVariableValue(contextKey, "individualBiometrics") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "individualBiometrics"))) {
					if(!VariableManager.getVariableValue(contextKey, "skipBiometricClassificationFlag").toString().contentEquals("skipBiometricClassification"))
					{
						JSONObject o = new JSONObject();
						o.put(FORMAT, CBEFF);
						o.put(VERSION, 1.0f);
						String[] v = fileInfo.get(PacketTemplateProvider.RID_FOLDER);
						v[1] = s.getId() + BIO_CBEFF_XML;
						fileInfo.put(PacketTemplateProvider.RID_FOLDER, v);
						o.put(VALUE, s.getId() + BIO_CBEFF);
						identity.put(s.getId(), o);
					}
					String outFile = fileInfo.get(PacketTemplateProvider.RID_FOLDER)[0] + "/"
							+ fileInfo.get(PacketTemplateProvider.RID_FOLDER)[1];
					try {
						List<String> missAttribs = resident.getMissAttributes();
						List<String> bioAttrib = new ArrayList<>(s.getBioAttributes());
						if (missAttribs != null && !missAttribs.isEmpty()) {
							bioAttrib.removeAll(missAttribs);
							resident.getBiometric().setCbeff(null);
						}		
						if (resident.getFilteredBioAttribtures() == null)
							resident.setFilteredBioAttribtures(bioAttrib);
						if (resident.getSkipFace()) {
							bioAttrib.removeAll(List.of("face"));
							resident.getBiometric().setCbeff(null);
						}
						if (resident.getSkipIris()) {
							bioAttrib.removeAll(List.of("leftEye", "rightEye"));
							resident.getBiometric().setCbeff(null);
						}
						if (resident.getSkipFinger()) {
							bioAttrib.removeAll(List.of(DataProviderConstants.schemaFingerNames));
							resident.getBiometric().setCbeff(null);
						}
						RestClient.logInfo(contextKey, "Before Cbeff Generation contextkey=" + contextKey + " fileinfo="
								+ fileInfo + " outFile=" + outFile);

						boolean bret = false;

						bret = PacketCbeffGenerator.generateCBEFF(resident, bioAttrib, outFile, contextKey, purpose,
								qualityScore, missAttribs, genarateValidCbeff, process);

						if (bret == false)
							return "Failed to generate biometric via mds";

						if (prop.containsKey("mosip.test.regclient.officerBiometricFileName")) {
							RestClient.logInfo(contextKey, preregResponse.toString());
							JSONArray getArray = preregResponse.getJSONObject("response").getJSONArray(DOCUMENTS);
							JSONObject objects = getArray.getJSONObject(0);
							String value = (String) objects.get(VALUE);
							byte[] decoded = Base64.getUrlDecoder().decode(value);
							String decodedcbeff = new String(decoded, StandardCharsets.UTF_8);
							resident.getBiometric().setCbeff(decodedcbeff);
							bret = PacketCbeffGenerator.generateCBEFF(resident, bioAttrib,
									fileInfo.get(PacketTemplateProvider.RID_FOLDER)[0] + "/"
											+ prop.get("mosip.test.regclient.officerBiometricFileName") + ".xml",
											contextKey, purpose, qualityScore, missAttribs, genarateValidCbeff, process);
							if (bret == false)
								return "";
						}
						if (prop.containsKey("mosip.test.regclient.supervisorBiometricFileName")) {
							bret = PacketCbeffGenerator.generateCBEFF(resident, bioAttrib,
									fileInfo.get(PacketTemplateProvider.RID_FOLDER)[0] + "/"
											+ prop.get("mosip.test.regclient.supervisorBiometricFileName") + ".xml",
											contextKey, purpose, qualityScore, missAttribs, genarateValidCbeff, process);

							if (bret == false)
								return "";
						}

					} catch (Exception e) {
						logger.error(GENERATEIDJSONV2, e);
					}
					continue;
				} else if ((VariableManager.getVariableValue(contextKey, "introducerBiometrics") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "introducerBiometrics"))))

				{
					if ((resident.isMinor() || resident.isInfant()) && resident.getGuardian() != null) {
						JSONObject o = new JSONObject();
						o.put(FORMAT, CBEFF);
						o.put(VERSION, 1.0f);
						String[] v = fileInfo.get(PacketTemplateProvider.RID_FOLDER);
						v[2] = s.getId() + BIO_CBEFF_XML;
						fileInfo.put(PacketTemplateProvider.RID_FOLDER, v);
						o.put(VALUE, s.getId() + BIO_CBEFF);

						String outFile = fileInfo.get(PacketTemplateProvider.RID_FOLDER)[0] + "/" + v[2];
						try {

							List<String> missAttribs = resident.getMissAttributes();
							List<String> bioAttrib = new ArrayList<>(s.getBioAttributes());
							if (missAttribs != null && !missAttribs.isEmpty()) {
								bioAttrib.removeAll(missAttribs);
								resident.getBiometric().setCbeff(null);
							}
							if (resident.getGuardian().getFilteredBioAttribtures() == null) {
								resident.getGuardian().setFilteredBioAttribtures(bioAttrib);
							}

							boolean bret = PacketCbeffGenerator.generateCBEFF(resident.getGuardian(), bioAttrib, outFile,
									contextKey, purpose, qualityScore, missAttribs, genarateValidCbeff, process);

							if (bret == false)
								return "";

							identity.put(s.getId(), o);

						} catch (Exception e) {
							logger.error(GENERATEIDJSONV2, e);
							return "";
						}

					} else if (resident.getGuardian() != null) {
						String primValue = null;
						String secGValue = null;
						if (resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();
						if (resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
							secGValue = resident.getGuardian().getName_seclang().getFirstName();

						PacketJsonSupport.updateSimpleType(s.getId(), identity, primValue, secGValue,
								resident.getPrimaryLanguage(), resident.getSecondaryLanguage(),
								resident.getThirdLanguage(), contextKey);

					}
					continue;
				} else if (s.getType().equals(BIOMETRICSTYPE)) {
					continue;
				} else if (s.getType().equals(DOCUMENTTYPE)) {

					if (resident.getDocuments() == null)
						continue;
					int index = 0;
					for (MosipDocument doc : resident.getDocuments()) {

						if (CommonUtil.isExists(lstMissedAttributes, doc.getDocCategoryCode()))
							continue;
						index = 0;
						if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {

							if (resident.getDocIndexes() != null
									&& resident.getDocIndexes().containsKey(doc.getDocCategoryCode())) {
								index = resident.getDocIndexes().get(doc.getDocCategoryCode());
							}
							if (index < 0 || index >= doc.getDocs().size() || index >= doc.getType().size()) {
								index = 0;
							}

							String docFile = doc.getDocs().get(index);
							logger.info(
									"Template doc selection -> schemaSubtype: {}, schemaId: {}, docCategory: {}, selectedIndex: {}, docType: {}, sourcePath: {}",
									s.getSubType(),
									s.getId(),
									doc.getDocCategoryCode(),
									index,
									doc.getType().get(index).getDocTypeCode(),
									docFile);
							RestClient.logInfo(contextKey,
									DOCFILE + docFile + DTYPE + s.getSubType() + CAT + s.getId());
							JSONObject o = new JSONObject();
							o.put(FORMAT, "pdf");
							o.put("type", doc.getType().get(index).getDocTypeCode());
							String[] v = fileInfo.get(PacketTemplateProvider.RID_FOLDER);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(PacketTemplateProvider.RID_FOLDER, v);
							o.put(VALUE, s.getId());

							identity.put(s.getId(), o);
							String outFile = fileInfo.get(PacketTemplateProvider.RID_FOLDER)[0] + "/"
									+ fileInfo.get(PacketTemplateProvider.RID_FOLDER)[1];
							try {

								if(!VariableManager.getVariableValue(contextKey, "skipApplicantDocumentsFlag").toString().contentEquals("skipApplicantDocuments"))  
									CommonUtil.copyFileWithBuffer(Paths.get(docFile), Paths.get(outFile));
							} catch (Exception e) {
								logger.error(GENERATEIDJSONV2, e);
							}
							break;
						}

					}
					continue;
				} else if (prop.getProperty("identitynumber") != null
						&& s.getId().equals(prop.getProperty("identitynumber"))) {
					List<SchemaValidator> validators = s.getValidators();
					if (validators != null) {
						primaryValue = generateDefaultAttributes(s, resident, identity,contextKey);
					} else {
						primaryValue = resident.getId();
					}

					identity.put(s.getId(), primaryValue);
					continue;
				}
				for (String locKey : locationSet) {
					MosipLocationModel locModel = locations.get(locKey);

					if (s.getId().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase())
							|| s.getSubType().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase())) {
						primaryValue = locModel.getCode();

						break;
					}
				}
				if (locations_seclang != null)
					for (String locKey : locationSet_sec) {
						MosipLocationModel locModel = locations_seclang.get(locKey);

						if (s.getId().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase())) {
							secValue = locModel.getCode();
							break;
						}
					}

				if ((primaryValue == null || primaryValue.equals("")) 
				        && (!s.getId().equals("dob") && !s.getId().equals("dateOfBirth"))) {
					primaryValue = generateDefaultAttributes(s, resident, identity, contextKey);
					if (secLanguage != null) {
						secValue = Translator.translate(secLanguage, primaryValue, contextKey);
					}
				}

				if (s.getType().equals(SIMPLETYPE)) {

					PacketJsonSupport.updateSimpleType(s.getId(), identity, primaryValue, secValue, primaryLanguage,
							secLanguage, resident.getThirdLanguage(), contextKey);

				} else {
					if (primaryValue.equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), primaryValue);
				}

		}
		if (PacketJsonSupport.isNewRegistrationProcess(process)) {
			PacketJsonSupport.omitResidentUin(identity, contextKey);
		}
		JSONObject retObject = new JSONObject();
		retObject.put(IDENTITY, identity);
		idjson = retObject.toString();
		return idjson;
	}
}
