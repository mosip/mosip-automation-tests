package io.mosip.testrig.dslrig.dataprovider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.core.gherkin.messages.internal.gherkin.internal.com.eclipsesource.json.Json;
import io.mosip.mock.sbi.exception.SBIException;
import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import io.mosip.testrig.dslrig.dataprovider.models.ContextSchemaDetail;
import io.mosip.testrig.dslrig.dataprovider.models.DocumentDto;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.MosipGenderModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLocationModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.SchemaRule;
import io.mosip.testrig.dslrig.dataprovider.models.SchemaValidator;
import io.mosip.testrig.dslrig.dataprovider.models.mds.MDSRCaptureModel;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.test.CreatePersona;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/*
 * Generate Packet structure for a given Resident record
 */

@SuppressWarnings("unchecked")
public class PacketTemplateProvider {
	private static SecureRandom rand = new SecureRandom();
	private static final Logger logger = LoggerFactory.getLogger(PacketTemplateProvider.class);

	public static String RID_FOLDER = "rid_id";
	public static String RID_EVIDENCE = "rid_evidence";
	public static String RID_OPTIONAL = "rid_optional";

	private static final String DOMAIN_NAME = ".mosip.net";

	// String constants
	private static final String ID_JSON = "/ID.json";
	private static final String PACKET_META_INFO_JSON = "/packet_meta_info.json";
	private static final String EVIDENCE = "evidence";
	private static final String IDENTITY = "identity";
	private static final String BIOMETRICSTYPE = "biometricsType";
	private static final String DOCUMENTTYPE = "documentType";
	private static final String FORMAT = "format";
	private static final String VALUE = "value";
	private static final String DTYPE = " dType=";
	private static final String CAT = " cat=";
	private static final String DOCFILE = "docFIle=";
	private static final String INTRODUCERDETAILS = "introducerdetails";
	private static final String GUARDIANDETAILS = "guardiandetails";
	private static final String INTRODUCERNAME = "introducername";
	private static final String PARENTORGUARDIANNAME = "parentOrGuardianname";
	private static final String LANGUAGE = "language";
	private static final String CHECKBOX = "checkbox";
	private static final String SIMPLETYPE = "simpleType";
	private static final String INTRODUCER = "introducer";
	private static final String APPLICANT_AUTH = "applicant-auth";
	private static final String GENDER = "gender";
	private static final String FULLNAME = "fullname";
	private static final String FIRSTNAME = "firstname";
	private static final String LASTNAME = "lastname";
	private static final String MIDDLENAME = "middlename";
	private static final String DATEOFBIRTH = "dateofbirth";
	private static final String DOCUMENTS = "documents";
	private static final String EMAIL = "email";
	private static final String CBEFF = "cbeff";
	private static final String VERSION = "version";
	private static final String BIO_CBEFF_XML = "_bio_CBEFF.xml";
	private static final String BIO_CBEFF = "_bio_CBEFF";
	private static final String GENERATEIDJSONV2 = "generateIDJsonV2";
	private static final String LABEL = "label";

	public ContextSchemaDetail getSchema(String contextKey) {
		ContextSchemaDetail contextSchemaDetail = new ContextSchemaDetail();
		contextSchemaDetail.setAllSchema(MosipMasterData.getIDSchemaLatestVersion(contextKey));
		contextSchemaDetail.setSchemaVersion(contextSchemaDetail.getAllSchema().keys().nextElement());
		contextSchemaDetail.setSchema((List<MosipIDSchema>) contextSchemaDetail.getAllSchema()
				.get(contextSchemaDetail.getSchemaVersion()).get("schemaList"));
		contextSchemaDetail.setRequiredAttribs((List<String>) contextSchemaDetail.getAllSchema()
				.get(contextSchemaDetail.getSchemaVersion()).get("requiredAttributes"));
		return contextSchemaDetail;
	}

	// generate un encrypted template
	public String generate(String source, String process, ResidentModel resident, String packetFilePath,
			String preregId, String machineId, String centerId, String contextKey, Properties props,
			JSONObject preregResponse, String purpose, String qualityScore, boolean genarateValidCbeff)
			throws IOException {
		final HashMap<String, String[]> fileInfo = new HashMap<String, String[]>();
		String rootFolder = packetFilePath;
		String ridFolder = "";
		Path path = Paths.get(rootFolder);
		RestClient.logInfo(contextKey, "path1:" + path);
		ContextSchemaDetail contextSchemaDetail = getSchema(contextKey);

		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		String sourceFolder = rootFolder + File.separator + source.toUpperCase();
		path = Paths.get(sourceFolder);
		RestClient.logInfo(contextKey, "path2:" + path);
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		String processFolder = sourceFolder + File.separator + process.toUpperCase();
		RestClient.logInfo(contextKey, "processFolder:" + processFolder);
		path = Paths.get(processFolder);
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		String nextFolder = processFolder + File.separator + RID_FOLDER;
		ridFolder = nextFolder;
		fileInfo.put(RID_FOLDER, (new String[] { ridFolder, "", "" }));

		path = Paths.get(nextFolder);
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		nextFolder = processFolder + File.separator + RID_EVIDENCE;
		String rid_evidence_folder = nextFolder;
		fileInfo.put(RID_EVIDENCE, (new String[] { rid_evidence_folder, "", "" }));

		path = Paths.get(nextFolder);
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		nextFolder = processFolder + File.separator + RID_OPTIONAL;
		String rid_optional_folder = nextFolder;
		path = Paths.get(nextFolder);
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}

		String idJson = null;
		try {
			idJson = generateIDJson(resident, fileInfo, contextKey, props, preregResponse, purpose, contextSchemaDetail,
					qualityScore, genarateValidCbeff);

			if (idJson != null && idJson.equalsIgnoreCase("Failed to generate biometric via mds"))
				return "Failed to generate biometric via mds";

		} catch (Throwable e) {
			logger.error("generate", e);
			return e.getMessage();
		}
		JSONObject processMVEL = processMVEL(resident, idJson, process, contextSchemaDetail, contextKey);
		idJson = processMVEL.toString();
		Files.write(Paths.get(ridFolder + ID_JSON), idJson.getBytes());
		String metadataJson = generateMetaDataJson(resident, preregId, machineId, centerId, fileInfo, contextKey,
				contextSchemaDetail);
		Files.write(Paths.get(ridFolder + PACKET_META_INFO_JSON), metadataJson.getBytes());

		// Generate evidence json

		String evidenceJson = generateEvidenceJson(resident, fileInfo, contextKey, props, contextSchemaDetail);
		Files.write(Paths.get(rid_evidence_folder + ID_JSON), evidenceJson.getBytes());
		Files.write(Paths.get(rid_evidence_folder + PACKET_META_INFO_JSON), metadataJson.getBytes());

		// copy the dummy jsons to optional also

		Files.write(Paths.get(rid_optional_folder + ID_JSON), evidenceJson.getBytes());
		Files.write(Paths.get(rid_optional_folder + PACKET_META_INFO_JSON), metadataJson.getBytes());

		idJson = genRID_PacketTypeJson(source, process, "id", contextSchemaDetail);
		Files.write(Paths.get(processFolder + File.separator + "/rid_id.json"), idJson.getBytes());
		idJson = genRID_PacketTypeJson(source, process, EVIDENCE, contextSchemaDetail);
		Files.write(Paths.get(processFolder + File.separator + "/rid_evidence.json"), idJson.getBytes());
		idJson = genRID_PacketTypeJson(source, process, "optional", contextSchemaDetail);
		Files.write(Paths.get(processFolder + File.separator + "/rid_optional.json"), idJson.getBytes());

		return "Success";

	}

	private JSONObject processMVEL(ResidentModel resident, String idJson, String process,
			ContextSchemaDetail contextSchemaDetail, String contextKey) {
		Map<String, Object> allIdentityDetails = new LinkedHashMap<String, Object>();
		Map<String, DocumentDto> documents = getDocuments(resident, idJson, contextSchemaDetail);
		allIdentityDetails.putAll(documents);
		JSONObject json = new JSONObject(idJson);
		json = json.getJSONObject(IDENTITY);
		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			if (!CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId()))
				continue;
			if (s.getType().equalsIgnoreCase(DOCUMENTTYPE) || s.getType().equalsIgnoreCase(BIOMETRICSTYPE)) {
				continue;
			}
			if (json.has(s.getId()))
				allIdentityDetails.put(s.getId(), json.get(s.getId()));
		}
		Map<String, Object> identityObject = getIdentityObject(contextSchemaDetail.getSchema(), process,
				contextSchemaDetail.getSchemaVersion(), resident);
		allIdentityDetails.putAll(identityObject);
		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			if (!CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId()))
				continue;
			List<SchemaRule> rule = s.getRequiredOn();
			if (rule != null) {
				for (SchemaRule sr : rule) {
					RestClient.logInfo(contextKey, "rule:" + sr);
					boolean bval = MosipMasterData.executeMVEL(sr.getExpr(), (Object) allIdentityDetails);
					RestClient.logInfo(contextKey, "rule:result=" + bval);
					if (!bval) {
						json = new JSONObject(idJson);
						json.put(s.getId(), Json.NULL);
					}
				}
			}
		}
		return json;
	}

	private Map<String, DocumentDto> getDocuments(ResidentModel resident, String idJson,
			ContextSchemaDetail contextSchemaDetail) {
		Map<String, DocumentDto> documents = new HashMap<>();
		JSONObject json = new JSONObject(idJson);
		json = json.getJSONObject(IDENTITY);
		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			if (!CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId()))
				continue;
			for (MosipDocument doc : resident.getDocuments()) {
				if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
					DocumentDto documentDto = new DocumentDto();
					if (json.has(s.getId())) {
						JSONObject formateJson = json.getJSONObject(s.getId());
						documentDto.setCategory(doc.getDocCategoryCode());
						documentDto.setFormat(formateJson.get(FORMAT).toString());
						documentDto.setType(formateJson.get("type").toString());
						documentDto.setValue(formateJson.get(VALUE).toString());
						documents.put(s.getId(), documentDto);
					}
				}

			}

		}
		return documents;
	}

	static Map<String, Object> getIdentityObject(List<MosipIDSchema> schemas, String process, double schemaVersion,
			ResidentModel resident) {
		Map<String, Object> allIdentityDetails = new LinkedHashMap<String, Object>();

		allIdentityDetails.put("IDSchemaVersion", schemaVersion);
		allIdentityDetails.put("isNew", false);
		if (process.equals("NEW")) {
			allIdentityDetails.put("isNew", true);
		}
		allIdentityDetails.put("isUpdate", false);
		if (process.equals("UPDATE")) {
			allIdentityDetails.put("isUpdate", true);
		}
		allIdentityDetails.put("isLost", false);
		if (process.equals("LOST")) {
			allIdentityDetails.put("isLost", true);
		}
		allIdentityDetails.put("isCorrection", false);
		if (process.equals("BIOMETRIC_CORRECTION")) {
			allIdentityDetails.put("isCorrection", true);
		}

		allIdentityDetails.put("langCodes", resident.getPrimaryLanguage());
		if (process.equals("NEW")) {
			allIdentityDetails.put("updatableFields", null);
			allIdentityDetails.put("updatableFieldGroups", null);
		}

		return allIdentityDetails;
	}

	String generateEvidenceJson(ResidentModel resident, HashMap<String, String[]> fileInfo, String contextKey,
			Properties prop, ContextSchemaDetail contextSchemaDetail) {

		VariableManager.setVariableValue(contextKey, "INTRODUCER_AVAILABILITY", "false");
		JSONObject identity = new JSONObject();
		List<String> missList = resident.getMissAttributes();

		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			RestClient.logInfo(contextKey, s.toJSONString());
			String primVal = "";
			String secVal = "";
			if (s.getFieldCategory().equals(EVIDENCE) && (s.getInputRequired() || s.getRequired())) {

				if (s.getRequired() && s.getRequiredOn() != null && !s.getRequiredOn().isEmpty()) {

					continue;
				}

				if (s.getType().equals(DOCUMENTTYPE)) {
					int index = 0;
					for (MosipDocument doc : resident.getDocuments()) {
						if (CommonUtil.isExists(missList, doc.getDocCategoryCode()))
							continue;
						index = 0;
						if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
							index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size() - 1, 0)[0];
							String docFile = doc.getDocs().get(index);

							JSONObject o = new JSONObject();
							o.put(FORMAT, "pdf");
							o.put("type", doc.getType().get(index).getDocTypeCode());
							String[] v = fileInfo.get(RID_EVIDENCE);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(RID_EVIDENCE, v);
							o.put(VALUE, s.getId());

							identity.put(s.getId(), o);

							String outFile = fileInfo.get(RID_EVIDENCE)[0] + "/" + fileInfo.get(RID_EVIDENCE)[1];
							try {
								Files.copy(Paths.get(docFile), Paths.get(outFile));
								RestClient.logInfo(contextKey,
										"contextkey" + contextKey + "Index= " + index + " File info= " + fileInfo
												+ " From-docFIle=" + docFile + " To-docFIle=" + outFile + DTYPE
												+ s.getSubType() + "Proof of cat=" + s.getId());

							} catch (Exception e) {
								logger.error(e.getMessage());
							}
							break;
						}

					}
					continue;
				} else if (VariableManager.getVariableValue(contextKey, "uin") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "uin"))) {
					if (resident.getUIN() == null || resident.getUIN().equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), resident.getUIN());
					continue;
				} else if (VariableManager.getVariableValue(contextKey, "introducerUIN") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "introducerUIN"))) {
					if ((resident.isMinor() || resident.isInfant()) && resident.getGuardian() != null) {
						if (resident.getGuardian().getUIN() == null || resident.getGuardian().getUIN().equals("")) {
						} else {
							identity.put(s.getId(), resident.getGuardian().getUIN());
							VariableManager.setVariableValue(contextKey, "INTRODUCER_AVAILABILITY", "true");
						}
					}
					continue;
				} else if (VariableManager.getVariableValue(contextKey, "introducerRID") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "introducerRID"))) {
					if ((resident.isMinor() || resident.isInfant()) && resident.getGuardian() != null) {
						if ((resident.getGuardian().getRID() == null || resident.getGuardian().getRID().equals(""))) {
						} else {
							identity.put(s.getId(), resident.getGuardian().getRID());
							VariableManager.setVariableValue(contextKey, "INTRODUCER_AVAILABILITY", "true");
						}
					}
					continue;
				}

				else if (prop.getProperty("parentOrGuardianuin") != null
						&& s.getId().equals(prop.getProperty("parentOrGuardianuin"))) {
					if ((resident.isMinor() || resident.isInfant()) && resident.getGuardian() != null) {
						if (resident.getGuardian().getUIN() == null || resident.getGuardian().getUIN().equals("")) {
						} else {
							identity.put(s.getId(), resident.getGuardian().getUIN());
							VariableManager.setVariableValue(contextKey, "INTRODUCER_AVAILABILITY", "true");
						}
					}
					continue;
				} else if (prop.getProperty("parentOrGuardianrid") != null
						&& s.getId().equals(prop.getProperty("parentOrGuardianrid"))) {
					if ((resident.isMinor() || resident.isInfant()) && resident.getGuardian() != null) {
						if (resident.getGuardian() != null && (resident.getGuardian().getRID() == null
								|| resident.getGuardian().getRID().equals(""))) {
						} else {
							identity.put(s.getId(), resident.getGuardian().getRID());
							VariableManager.setVariableValue(contextKey, "INTRODUCER_AVAILABILITY", "true");
						}
					}
					continue;
				}

				else if (VariableManager.getVariableValue(contextKey, "introducerName") != null
						&& s.getId().equals(VariableManager.getVariableValue(contextKey, "introducerName"))) {

					if (resident.isMinor() || resident.isInfant()) {
						String primValue = "";
						String secValue = "";
						JSONObject o = new JSONObject();
						o.put(LANGUAGE, resident.getPrimaryLanguage());
						if (resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();

						if (resident.getSecondaryLanguage() != null) {
							if (resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
								secValue = resident.getGuardian().getName_seclang().getFirstName();
						}

						updateSimpleType(s.getId(), identity, primValue, secValue, resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), resident.getThirdLanguage(), contextKey);

					}
					continue;
				} else if (s.getId().toLowerCase().contains("consent")) {
					String consentFlag = VariableManager.getVariableValue(contextKey, "consent").toString();
					if (consentFlag.equalsIgnoreCase("yes"))
						identity.put(s.getId(), "Y");
					else if (consentFlag.equalsIgnoreCase("no"))
						identity.put(s.getId(), "N");
					continue;
				} else if (s.getControlType().equals(CHECKBOX)) {
					primVal = "Y";
					secVal = "Y";
				} else {
					primVal = "Some text value";
					if (resident.getSecondaryLanguage() != null)
						secVal = Translator.translate(resident.getSecondaryLanguage(), primVal, contextKey);

				}
				if (s.getType().equals(SIMPLETYPE)) {

					updateSimpleType(s.getId(), identity, primVal, secVal, resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(), resident.getThirdLanguage(), contextKey);

				} else {
					identity.put(s.getId(), primVal.equals("") ? JSONObject.NULL : primVal);
				}
			}
		}
		JSONObject retObject = new JSONObject();
		retObject.put(IDENTITY, identity);
		return retObject.toString();

	}

	JSONObject constructExceptnNode(BioModality modality) {
		JSONObject node = new JSONObject();
		node.put("type", modality.getType());
		node.put("missingBiometric", DataProviderConstants.getschemaName(modality.getSubType()));
		node.put("reason", modality.getReason());
		node.put("exceptionType", modality.getExceptionType());
		node.put("individualType", "applicant");
		return node;
	}

	JSONObject constructBioMetaNode() {
		JSONObject node = new JSONObject();
		node.put("numRetry", 1);
		node.put("forceCaptured", false);
		node.put("birindex", "4c099c1f-4fb2-4de3-8a2f-928f79430e9b");
		return node;
	}

	JSONObject constructBioMetaData(ResidentModel resident, JSONObject identity) {

		List<String> lstAttr = resident.getFilteredBioAttribtures();
		if (lstAttr != null) {
			JSONObject biometrics = new JSONObject();
			JSONObject applicant = new JSONObject();

			for (String n : DataProviderConstants.schemaNames) {
				if (lstAttr.contains(n)) {
					applicant.put(n, constructBioMetaNode());
				}
			}

			biometrics.put(INTRODUCER, new JSONObject());
			biometrics.put(APPLICANT_AUTH, new JSONObject());

			biometrics.put("individualBiometrics", applicant);
			identity.put("biometrics", biometrics);

		}
		return identity;
	}

	JSONObject constructBioException(ResidentModel resident, JSONObject identity, List<MosipIDSchema> mosipIDSchema) {
		// update biometric exceptions

		List<BioModality> exceptionAttrib = resident.getBioExceptions();
		if (exceptionAttrib != null) {
			JSONObject exceptionBiometrics = new JSONObject();
			JSONObject applicant = new JSONObject();

			for (BioModality bm : exceptionAttrib) {

				applicant.put(DataProviderConstants.getschemaName(bm.getSubType()), constructExceptnNode(bm));

			}

			exceptionBiometrics.put(INTRODUCER, new JSONObject());
			exceptionBiometrics.put(APPLICANT_AUTH, new JSONObject());

			exceptionBiometrics.put("individualBiometrics", applicant);
			identity.put("exceptionBiometrics", exceptionBiometrics);

		}
		return identity;
	}

	JSONObject constructBioException(ResidentModel resident, JSONObject identity) {
		// update biometric exceptions
		List<BioModality> exceptionAttrib = resident.getBioExceptions();
		if (exceptionAttrib != null) {
			JSONObject exceptionBiometrics = new JSONObject();
			JSONObject applicant = new JSONObject();

			for (BioModality bm : exceptionAttrib) {

				applicant.put(bm.getSubType(), constructExceptnNode(bm));

			}

			exceptionBiometrics.put(INTRODUCER, new JSONObject());
			exceptionBiometrics.put(APPLICANT_AUTH, new JSONObject());

			exceptionBiometrics.put("applicant", applicant);
			identity.put("exceptionBiometrics", exceptionBiometrics);

		}
		return identity;
	}

	JSONObject updateSimpleType(String id, JSONObject identity, String primValue, String secValue, String primLang,
			String secLang, String thirdLang, String contextKey) {

		if (primValue == null)
			primValue = "Some Text Value";

		if ((secValue == null || secValue.equals("")) && secLang != null && !secLang.equals(""))
			secValue = Translator.translate(secLang, primValue, contextKey);

		String thirdValue = "";
		if (thirdLang != null && !thirdLang.equals(""))
			thirdValue = Translator.translate(thirdLang, primValue, contextKey);

		// array
		JSONArray ar = new JSONArray();
		JSONObject o = new JSONObject();
		o.put(LANGUAGE, primLang);
		if (primValue != null && primValue.equals(""))
			o.put(VALUE, Json.NULL);
		else
			o.put(VALUE, primValue);
		ar.put(o);

		if (secLang != null) {
			o = new JSONObject();
			o.put(LANGUAGE, secLang);
			if (secValue != null && secValue.equals(""))
				o.put(VALUE, Json.NULL);
			else
				o.put(VALUE, secValue);
			ar.put(o);
		}
		if (thirdLang != null) {
			o = new JSONObject();
			o.put(LANGUAGE, thirdLang);
			if (thirdValue.equals(""))
				o.put(VALUE, Json.NULL);
			else
				o.put(VALUE, thirdValue);
			ar.put(o);
		}

		identity.put(id, ar);
		return identity;
	}

	Boolean generateCBEFF(ResidentModel resident, List<String> bioAttrib, String outFile, String contextKey,
			String purpose, String qualityScore, List<String> missAttribs, boolean genarateValidCbeff)
			throws Exception {

		String strVal = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "usemds").toString();
		boolean bMDS = Boolean.valueOf(strVal);
		String cbeff = null;

		if (resident.getBioExceptions() == null || resident.getBioExceptions().isEmpty())
			cbeff = resident.getBiometric().getCbeff();

		if (bMDS) {
			if (cbeff == null) {
				MDSRCaptureModel capture = BiometricDataProvider.regenBiometricViaMDS(resident, contextKey, purpose,
						qualityScore);

				if (capture == null) {
					logger.error("Failed to generate biometric via mds");
					return false;
				}

				resident.getBiometric().setCapture(capture.getLstBiometrics());
				String strCBeff = BiometricDataProvider.toCBEFFFromCapture(bioAttrib, capture, outFile, missAttribs,
						genarateValidCbeff, resident.getBioExceptions(), contextKey);

				resident.getBiometric().setCbeff(strCBeff);

			} else {
//				PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
				PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outFile)));
				writer.print(cbeff);
				writer.close();
			}

		} else {

			if (cbeff == null) {

				String strCBeff = BiometricDataProvider.toCBEFF(bioAttrib, resident.getBiometric(), outFile,
						genarateValidCbeff, contextKey);
				resident.getBiometric().setCbeff(strCBeff);

			} else {
//				PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
				PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outFile)));
				writer.print(cbeff);
				writer.close();
			}
		}
		resident.save();
		return true;
	}

	// check the dynamic field logic and change here
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

			// context should set Male/Female code values
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
			//

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
				addr = "#%d, %d Street, %d block";// + schemaItem.getId();
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
			JSONObject identity) {
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
							someVal = CommonUtil.genStringAsperRegex(regexpr);
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

	private static Boolean updateFromAdditionalAttribute(JSONObject identity, MosipIDSchema s, ResidentModel resident,
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
						dfmPrim = dfm;
						break;
					}
				}
				secValue = primaryValue;

				if (secLanguage != null)
					for (DynamicFieldModel dfm1 : dynaFields.get(secLanguage)) {
						if (dfm1.getIsActive() && dfm1.getName().equals(s.getId())) {
							secValue = dfm1.getFieldVal().get(0).getCode();
							break;
						}
					}

				CreatePersona.constructNode(identity, s.getId(), resident.getPrimaryLanguage(),
						resident.getSecondaryLanguage(), primaryValue, secValue,
						s.getType().equals(SIMPLETYPE) ? true : false);
				found = true;

			}

		}
		return found;
	}

	String generateIDJson(ResidentModel resident, HashMap<String, String[]> fileInfo, String contextKey,
			Properties prop, JSONObject preregResponse, String purpose, ContextSchemaDetail contextSchemaDetail,
			String qualityScore, boolean genarateValidCbeff) {

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

		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			RestClient.logInfo(contextKey, s.toJSONString());
			// if not reqd field , skip it
			if (!CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId()))
				continue;

			if (lstMissedAttributes != null
					&& lstMissedAttributes.stream().anyMatch(v -> v.equalsIgnoreCase(s.getId()))) {
				continue;
			}

			if (VariableManager.getVariableValue(contextKey, "uin") != null
					&& s.getId().equals(VariableManager.getVariableValue(contextKey, "uin"))) {
				String uin = resident.getUIN();
				if (uin != null && !uin.trim().equals("")) {
					identity.put(s.getId(), uin.trim());
				}
				continue;
			}
			if (!s.getRequired() && !s.getInputRequired()) {
				continue;
			}

			if (!s.getRequired() && !(s.getRequiredOn() != null && s.getRequiredOn().size() > 0)) {
				continue;
			}

			if (VariableManager.getVariableValue(contextKey, "IDSchemaVersion") != null
					&& s.getId().equals(VariableManager.getVariableValue(contextKey, "IDSchemaVersion"))) {
				identity.put(s.getId(), contextSchemaDetail.getSchemaVersion());
				continue;
			}

			if (updateFromAdditionalAttribute(identity, s, resident, contextKey)) {
				continue;
			}
			if (processDynamicFields(s, identity, resident, contextKey))
				continue;

			if (s.getFieldCategory().equals("pvt") || s.getFieldCategory().equals("kyc")) {
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
				} else if (s.getSubType().toLowerCase().contains("residenceStatus")) {
					primaryValue = resident.getResidentStatus().getCode();
					VariableManager.setVariableValue(contextKey, "ID_OBJECT-residenceStatus", primaryValue);
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
					JSONObject o = new JSONObject();
					o.put(FORMAT, CBEFF);
					o.put(VERSION, 1.0f);
					String[] v = fileInfo.get(RID_FOLDER);
					v[1] = s.getId() + BIO_CBEFF_XML;
					fileInfo.put(RID_FOLDER, v);
					o.put(VALUE, s.getId() + BIO_CBEFF);
					identity.put(s.getId(), o);

					String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + fileInfo.get(RID_FOLDER)[1];
					try {
						List<String> missAttribs = resident.getMissAttributes();
						List<BioModality> bioExceptions = resident.getBioExceptions();

						List<String> bioAttrib = s.getBioAttributes();
						if (missAttribs != null && !missAttribs.isEmpty())
							bioAttrib.removeAll(missAttribs);
						if (resident.getFilteredBioAttribtures() == null)
							resident.setFilteredBioAttribtures(bioAttrib);
						if (resident.getSkipFace())
							bioAttrib.removeAll(List.of("face"));
						if (resident.getSkipIris())
							bioAttrib.removeAll(List.of("leftEye", "rightEye"));
						if (resident.getSkipFinger()) {
							bioAttrib.removeAll(List.of(DataProviderConstants.schemaFingerNames));
						}
						RestClient.logInfo(contextKey, "Before Cbeff Generation contextkey=" + contextKey + " fileinfo="
								+ fileInfo + " outFile=" + outFile);

						boolean bret = false;

						bret = generateCBEFF(resident, bioAttrib, outFile, contextKey, purpose, qualityScore,
								missAttribs, genarateValidCbeff);

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
							bret = generateCBEFF(resident, bioAttrib,
									fileInfo.get(RID_FOLDER)[0] + "/"
											+ prop.get("mosip.test.regclient.officerBiometricFileName") + ".xml",
									contextKey, purpose, qualityScore, missAttribs, genarateValidCbeff);
							if (bret == false)
								return "";
						}
						if (prop.containsKey("mosip.test.regclient.supervisorBiometricFileName")) {
							bret = generateCBEFF(resident, bioAttrib,
									fileInfo.get(RID_FOLDER)[0] + "/"
											+ prop.get("mosip.test.regclient.supervisorBiometricFileName") + ".xml",
									contextKey, purpose, qualityScore, missAttribs, genarateValidCbeff);

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
						String[] v = fileInfo.get(RID_FOLDER);
						v[2] = s.getId() + BIO_CBEFF_XML;
						fileInfo.put(RID_FOLDER, v);
						o.put(VALUE, s.getId() + BIO_CBEFF);
						identity.put(s.getId(), o);

						String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + v[2];
						try {
							// Implement excetpions by parsing 'Miss' list
							List<String> missAttribs = resident.getMissAttributes();
							List<String> bioAttrib = s.getBioAttributes();
							if (missAttribs != null && !missAttribs.isEmpty())
								bioAttrib.removeAll(missAttribs);

							boolean bret = generateCBEFF(resident.getGuardian(), bioAttrib, outFile, contextKey,
									purpose, qualityScore, missAttribs, genarateValidCbeff);

							if (bret == false)
								return "";

						} catch (Exception e) {
							logger.error(GENERATEIDJSONV2, e);
						}

					} else if (resident.getGuardian() != null) {
						String primValue = null;
						String secGValue = null;
						if (resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();
						if (resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
							secGValue = resident.getGuardian().getName_seclang().getFirstName();

						updateSimpleType(s.getId(), identity, primValue, secGValue, resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), resident.getThirdLanguage(), contextKey);

					}
					continue;
				} else if (s.getType().equals(BIOMETRICSTYPE)) {
					continue;
				} else if (s.getType().equals(DOCUMENTTYPE)) {

					int index = 0;
					for (MosipDocument doc : resident.getDocuments()) {

						if (CommonUtil.isExists(lstMissedAttributes, doc.getDocCategoryCode()))
							continue;
						index = 0;
						if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {

							index = resident.getDocIndexes().get(doc.getDocCategoryCode());

							String docFile = doc.getDocs().get(0);
							RestClient.logInfo(contextKey,
									DOCFILE + docFile + DTYPE + s.getSubType() + CAT + s.getId());

							JSONObject o = new JSONObject();
							o.put(FORMAT, "pdf");
							o.put("type", doc.getType().get(0).getDocTypeCode());
							String[] v = fileInfo.get(RID_FOLDER);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(RID_FOLDER, v);
							o.put(VALUE, s.getId());

							identity.put(s.getId(), o);

							String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + fileInfo.get(RID_FOLDER)[1];
							try {
								Files.copy(Paths.get(docFile), Paths.get(outFile));

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
						primaryValue = generateDefaultAttributes(s, resident, identity);
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
						primaryValue = locModel.getName();

						break;
					}
				}
				if (locations_seclang != null)
					for (String locKey : locationSet_sec) {
						MosipLocationModel locModel = locations_seclang.get(locKey);

						if (s.getId().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase())) {
							secValue = locModel.getName();
							break;
						}
					}

				if (primaryValue == null || primaryValue.equals("")) {
					primaryValue = generateDefaultAttributes(s, resident, identity);
					if (secLanguage != null) {
						secValue = Translator.translate(secLanguage, primaryValue, contextKey);
					}
				}

				if (s.getType().equals(SIMPLETYPE)) {

					updateSimpleType(s.getId(), identity, primaryValue, secValue, primaryLanguage, secLanguage,
							resident.getThirdLanguage(), contextKey);

				} else {
					if (primaryValue.equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), primaryValue);
				}
			}

		}
		JSONObject retObject = new JSONObject();
		retObject.put(IDENTITY, identity);
		idjson = retObject.toString();
		return idjson;
	}

	private void loadMapperProp(String contextKey, Properties prop) {
		String hostName = null;
		if (contextKey != null && !contextKey.equals(""))
			hostName = contextKey.split("_")[0];
		else
			throw new RuntimeException("ContextKey not found !!");
		Boolean contextMapperFound = false;
		String propPath = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.env.mapperpath")
				.toString();
		RestClient.logInfo(contextKey, propPath);
		File folder = new File(String.valueOf(propPath) + File.separator);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				if (file.getName().contains(hostName + DOMAIN_NAME)) {
					propPath = file.getAbsolutePath();
					contextMapperFound = true;
					break;
				}
			}
		}
		String filePath = propPath + "/default.properties";
		if (contextMapperFound) {
			filePath = propPath;

		}

		try (FileInputStream fis = new FileInputStream(filePath)) {
			prop.load(fis);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	String generateMetaDataJson(ResidentModel resident, String preRegistrationId, String machineId, String centerId,
			HashMap<String, String[]> fileInfo, String contextKey, ContextSchemaDetail contextSchemaDetail) {

		String templateMetaJsonPath = VariableManager.getVariableValue(contextKey, "mountPath").toString()
				+ VariableManager.getVariableValue(contextKey, "templateIDMeta").toString().trim();

		String templateIdentityStr = CommonUtil.readFromJSONFile(templateMetaJsonPath);
		JSONObject templateIdentity = new JSONObject(templateIdentityStr).getJSONObject(IDENTITY);
		JSONObject identity = new JSONObject();
		JSONArray docArray = new JSONArray();

		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			if (s.getType().equals(DOCUMENTTYPE) && s.getRequired()) {
				int index = 0;
				for (MosipDocument doc : resident.getDocuments()) {
					index = 0;
					if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
						index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size() - 1, 0)[0];
						String docFile = doc.getDocs().get(index);
						RestClient.logInfo(contextKey, DOCFILE + docFile + DTYPE + s.getSubType() + CAT + s.getId());

						JSONObject o = new JSONObject();
						o.put("documentCategory", doc.getDocCategoryCode());
						o.put(DOCUMENTTYPE, doc.getType().get(index).getCode());
						o.put("documentName", s.getId());
						o.put("documentOwner", "Applicant");
						o.put("refNumber", JSONObject.NULL);
						docArray.put(o);

						break;
					}

				}
				continue;
			}
		}
		identity.put(DOCUMENTS, docArray);
		identity.put("capturedRegisteredDevices", templateIdentity.getJSONArray("capturedRegisteredDevices"));

		identity.put("creationDate", CommonUtil.getUTCDateTime(null));
		identity = constructBioException(resident, identity, contextSchemaDetail.getSchema());
		identity = constructBioMetaData(resident, identity);
		identity.put("operationsData", templateIdentity.getJSONArray("operationsData"));

		JSONArray metadata = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put(LABEL, "creationDate");
		obj.put(VALUE, CommonUtil.getUTCDateTime(null));
		metadata.put(obj);

		if (preRegistrationId != null && !preRegistrationId.equals("")) {
			obj = new JSONObject();
			obj.put(LABEL, "preRegistrationId");
			obj.put(VALUE, preRegistrationId);
			metadata.put(obj);

		}
		if (centerId != null && !centerId.equals("")) {
			obj = new JSONObject();
			obj.put(LABEL, "centerId");
			obj.put(VALUE, centerId);
			metadata.put(obj);
		}

		if (machineId != null && !machineId.equals("")) {
			obj = new JSONObject();
			obj.put(LABEL, "machineId");
			obj.put(VALUE, machineId);
			metadata.put(obj);
		}

		identity.put("metaData", metadata);

		JSONObject retObject = new JSONObject();
		retObject.put(IDENTITY, identity);
		return retObject.toString();
	}

	String genRID_PacketTypeJson(String src, String process, String packetType,
			ContextSchemaDetail contextSchemaDetail) {

		JSONObject retObject = new JSONObject();
		retObject.put("process", process.toUpperCase());
		retObject.put("source", src.toUpperCase());
		retObject.put("creationdate", CommonUtil.getUTCDateTime(null));
		retObject.put("providerversion", "v1.0");
		retObject.put("schemaversion", contextSchemaDetail.getSchemaVersion());
		retObject.put("encryptedhash", "");
		retObject.put("signature", "");
		retObject.put("id", "");
		retObject.put("packetname", "id_" + packetType);
		retObject.put("signature", "");
		retObject.put("providername", "PacketWriterImpl");

		return retObject.toString();

	}

	public static void main(String[] args) {

		ResidentDataProvider provider = new ResidentDataProvider();
		List<ResidentModel> residents = provider.generate("contextKey");
		try {
			new PacketTemplateProvider().generate("registration_client", "new", residents.get(0), "/temp/newpacket",
					null, null, null, null, null, null, null, null, true);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}

}
