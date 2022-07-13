package org.mosip.dataprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mosip.dataprovider.models.BioModality;
import org.mosip.dataprovider.models.DocumentDto;
import org.mosip.dataprovider.models.DynamicFieldModel;
import org.mosip.dataprovider.models.MosipDocument;
import org.mosip.dataprovider.models.MosipGenderModel;
import org.mosip.dataprovider.models.MosipIDSchema;
import org.mosip.dataprovider.models.MosipLocationModel;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.SchemaRule;
import org.mosip.dataprovider.models.SchemaValidator;
import org.mosip.dataprovider.models.mds.MDSRCaptureModel;
import org.mosip.dataprovider.preparation.MosipMasterData;
import org.mosip.dataprovider.test.CreatePersona;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.Gender;
import org.mosip.dataprovider.util.Translator;

import com.google.gson.JsonObject;

import io.cucumber.core.gherkin.messages.internal.gherkin.internal.com.eclipsesource.json.Json;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import variables.VariableManager;

/*
 * Generate Packet structure for a given Resident record
 */
@SuppressWarnings("unchecked")
public class PacketTemplateProvider {

	static HashMap<String, String[]> fileInfo = new HashMap<String, String[]>();

	public static String RID_FOLDER = "rid_id";
	public static String RID_EVIDENCE = "rid_evidence";
	public static String RID_OPTIONAL = "rid_optional";

	Hashtable<Double, Properties> allSchema = MosipMasterData.getIDSchemaLatestVersion();

	Double schemaVersion = allSchema.keys().nextElement();
	
	List<MosipIDSchema> schema = (List<MosipIDSchema>) allSchema.get(schemaVersion).get("schemaList");
	List<String> requiredAttribs = (List<String>) allSchema.get(schemaVersion).get("requiredAttributes");
	
	private static final String DOMAIN_NAME = ".mosip.net";
	Properties prop = new Properties();

	public void getSchema() {
		allSchema = MosipMasterData.getIDSchemaLatestVersion();
		schemaVersion = allSchema.keys().nextElement();
		schema = (List<MosipIDSchema>) allSchema.get(schemaVersion).get("schemaList");
		requiredAttribs = (List<String>) allSchema.get(schemaVersion).get("requiredAttributes");
	}

	// generate un encrypted template
	public void generate(String source, String process, ResidentModel resident, String packetFilePath, String preregId,
			String machineId, String centerId,String contextKey,Properties props,JSONObject preregResponse) throws IOException {

		String rootFolder = packetFilePath;
		String ridFolder = "";
		Path path = Paths.get(rootFolder);

		getSchema();

		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		String sourceFolder = rootFolder + File.separator + source.toUpperCase();
		path = Paths.get(sourceFolder);
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
		String processFolder = sourceFolder + File.separator + process.toUpperCase();
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
		//String idJson = generateIDJson(resident, fileInfo);
		
		String idJson = generateIDJsonV2(resident, fileInfo,contextKey,props,preregResponse);
		JSONObject processMVEL = processMVEL(resident, idJson, schema, process);
		idJson = processMVEL.toString();
		Files.write(Paths.get(ridFolder + "/ID.json"), idJson.getBytes());
		String metadataJson = generateMetaDataJson(resident, preregId, machineId, centerId, fileInfo);
		Files.write(Paths.get(ridFolder + "/packet_meta_info.json"), metadataJson.getBytes());

		// Generate evidence json

		//String evidenceJson = generateEvidenceJson(resident, fileInfo);
		String evidenceJson = generateEvidenceJsonV2(resident, fileInfo);
		Files.write(Paths.get(rid_evidence_folder + "/ID.json"), evidenceJson.getBytes());
		Files.write(Paths.get(rid_evidence_folder + "/packet_meta_info.json"), metadataJson.getBytes());

		// copy the dummy jsons to optional also

		Files.write(Paths.get(rid_optional_folder + "/ID.json"), evidenceJson.getBytes());
		Files.write(Paths.get(rid_optional_folder + "/packet_meta_info.json"), metadataJson.getBytes());

		idJson = genRID_PacketTypeJson(source, process, "id");
		Files.write(Paths.get(processFolder + File.separator + "/rid_id.json"), idJson.getBytes());
		idJson = genRID_PacketTypeJson(source, process, "evidence");
		Files.write(Paths.get(processFolder + File.separator + "/rid_evidence.json"), idJson.getBytes());
		idJson = genRID_PacketTypeJson(source, process, "optional");
		Files.write(Paths.get(processFolder + File.separator + "/rid_optional.json"), idJson.getBytes());

	}
	private JSONObject processMVEL(ResidentModel resident,String idJson,List<MosipIDSchema> schema,String process) {
		Map<String, Object> allIdentityDetails = new LinkedHashMap<String, Object>();
		Map<String, DocumentDto> documents = getDocuments(resident,idJson,schema);
		allIdentityDetails.putAll(documents);
		 JSONObject json = new JSONObject(idJson);
		 json=json.getJSONObject("identity");
		 for (MosipIDSchema s : schema) {
			 if (!CommonUtil.isExists(requiredAttribs, s.getId()))
					continue;
			 if(s.getType().equalsIgnoreCase("documentType") || s.getType().equalsIgnoreCase("biometricsType")) {
				 continue;
			 }
			 if(json.has(s.getId()))
			 allIdentityDetails.put(s.getId(),  json.get(s.getId()));
		 }
		 Map<String, Object> identityObject = getIdentityObject(schema,process,schemaVersion, resident);
		 allIdentityDetails.putAll(identityObject);
		 for(MosipIDSchema s : schema) {
			 if (!CommonUtil.isExists(requiredAttribs, s.getId()))
					continue;
			 List<SchemaRule>  rule = s.getRequiredOn();
				if(rule != null) {
					for(SchemaRule sr: rule) {
						System.out.println("rule:" + sr);
						boolean bval = MosipMasterData.executeMVEL(sr.getExpr(),(Object)allIdentityDetails);
						System.out.println("rule:result=" + bval);
						if(!bval) {
							json= new JSONObject(idJson);
							json.put(s.getId(), Json.NULL);
						}
					}
				}
		 }
		return json;
	}
	
	private Map<String, DocumentDto> getDocuments(ResidentModel resident,String idJson,List<MosipIDSchema> schema){
		 Map<String, DocumentDto> documents = new HashMap<>();	
		JSONObject json = new JSONObject(idJson);
		json=json.getJSONObject("identity");
		for (MosipIDSchema s : schema) {
			if (!CommonUtil.isExists(requiredAttribs, s.getId()))
				continue;
			for (MosipDocument doc : resident.getDocuments()) {
				if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
					DocumentDto documentDto = new DocumentDto();
					if (json.has(s.getId())) {
						JSONObject formateJson = json.getJSONObject(s.getId());
						documentDto.setCategory(doc.getDocCategoryCode());
						documentDto.setFormat(formateJson.get("format").toString());
						documentDto.setType(formateJson.get("type").toString());
						documentDto.setValue(formateJson.get("value").toString());
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

	String generateEvidenceJson(ResidentModel resident, HashMap<String, String[]> fileInfo) {

		JSONObject identity = new JSONObject();

		List<String> missList = resident.getMissAttributes();

		for (MosipIDSchema s : schema) {
			String primVal = "";
			String secVal = "";
			if (s.getFieldCategory().equals("evidence") && (s.getInputRequired() || s.getRequired())) {

				if (! (!s.getRequired()) && (s.getRequiredOn() != null && s.getRequiredOn().size() > 0)) {
					continue;
				}

				if (s.getType().equals("documentType")) {

					// String docType = s.getSubType();
					int index = 0;
					for (MosipDocument doc : resident.getDocuments()) {
						if (CommonUtil.isExists(missList, doc.getDocCategoryCode()))
							continue;
						index = 0;
						if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
							index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size() - 1, 0)[0];
							String docFile = doc.getDocs().get(index);
							System.out.println("docFIle=" + docFile + " dType=" + s.getSubType() + " cat=" + s.getId());

							JSONObject o = new JSONObject();
							o.put("format", "pdf");
							o.put("type", doc.getType().get(index).getCode());
							String[] v = fileInfo.get(RID_EVIDENCE);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(RID_EVIDENCE, v);
							o.put("value", s.getId());

							identity.put(s.getId(), o);

							String outFile = fileInfo.get(RID_EVIDENCE)[0] + "/" + fileInfo.get(RID_EVIDENCE)[1];
							try {
								Files.copy(Paths.get(docFile), Paths.get(outFile));

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}

					}
					continue;
				} else if (s.getId().toLowerCase().equals("uin")) {
					if (resident.getUIN() == null || resident.getUIN().equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), resident.getUIN());
					continue;
				} else if ((s.getGroup().toLowerCase().equals("guardiandetails") || s.getGroup().toLowerCase().equals("introducerdetails")) && 
						(s.getSubType().toLowerCase().matches(".*guard.*uin") || s.getSubType().toLowerCase().equals("uin"))) {
					if (resident.isMinor()) {
						if (resident.getGuardian().getUIN() == null || resident.getGuardian().getUIN().equals("")) {
							// skip if null
							// identity.put(s.getId(), JSONObject.NULL);
						} else
							identity.put(s.getId(), resident.getGuardian().getUIN());
					}
					continue;
				} else if ((s.getGroup().toLowerCase().equals("guardiandetails") || s.getGroup().toLowerCase().equals("introducerdetails")) 
						&& (s.getId().toLowerCase().matches(".*guard.*rid") || s.getSubType().toLowerCase().equals("rid"))) {
					if (resident.isMinor()) {
						if (resident.getGuardian() != null && (resident.getGuardian().getRID() == null
								|| resident.getGuardian().getRID().equals(""))) {
							// As per MONO, skip
							// identity.put(s.getId(), JSONObject.NULL);
						} else
							identity.put(s.getId(), resident.getGuardian().getRID());
					}
					continue;
				} else if ((s.getGroup().toLowerCase().equals("guardiandetails") || s.getGroup().toLowerCase().equals("introducerdetails")) 
						&& (s.getId().toLowerCase().matches(".*guard.*name") || s.getSubType().toLowerCase().equals("introducername"))) {
					if (resident.isMinor()) {
						String primValue = "";
						String secValue = "";
						// JSONArray arr = new JSONArray();
						JSONObject o = new JSONObject();
						o.put("language", resident.getPrimaryLanguage());
						// String value = null;
						if (resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();

						if (resident.getSecondaryLanguage() != null) {
							if (resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
								secValue = resident.getGuardian().getName_seclang().getFirstName();
						}

						updateSimpleType(s.getId(), identity, primValue, secValue, resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), resident.getThirdLanguage());

					}
					continue;
				} else if (s.getId().toLowerCase().contains("consent")) {
					continue;
				} else if (s.getControlType().equals("checkbox")) {
					primVal = "Y";
					secVal = "Y";
				} else {
					primVal = "Some text value";
					if (resident.getSecondaryLanguage() != null)
						secVal = Translator.translate(resident.getSecondaryLanguage(), primVal);

				}
				if (s.getType().equals("simpleType")) {

					updateSimpleType(s.getId(), identity, primVal, secVal, resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(), resident.getThirdLanguage());

				} else {
					identity.put(s.getId(), primVal.equals("") ? JSONObject.NULL : primVal);
				}
			}
		}
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		return retObject.toString();

	}
	
	String generateEvidenceJsonV2(ResidentModel resident, HashMap<String, String[]> fileInfo) {

		JSONObject identity = new JSONObject();
		List<String> missList = resident.getMissAttributes();

		for (MosipIDSchema s : schema) {
			System.out.println(s.toJSONString());
			String primVal = "";
			String secVal = "";
			if (s.getFieldCategory().equals("evidence") && (s.getInputRequired() || s.getRequired())) {

				if (!(!s.getRequired()) && (s.getRequiredOn() != null && s.getRequiredOn().size() > 0)) {
					
					continue;
				}

				if (s.getType().equals("documentType")) {
					int index = 0;
					for (MosipDocument doc : resident.getDocuments()) {
						if (CommonUtil.isExists(missList, doc.getDocCategoryCode()))
							continue;
						index = 0;
						if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
							index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size() - 1, 0)[0];
							String docFile = doc.getDocs().get(index);
							System.out.println("docFIle=" + docFile + " dType=" + s.getSubType() + " cat=" + s.getId());

							JSONObject o = new JSONObject();
							o.put("format", "pdf");
							//o.put("type", doc.getType().get(index).getCode());
							o.put("type", doc.getType().get(index).getDocTypeCode());
							String[] v = fileInfo.get(RID_EVIDENCE);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(RID_EVIDENCE, v);
							o.put("value", s.getId());

							identity.put(s.getId(), o);

							String outFile = fileInfo.get(RID_EVIDENCE)[0] + "/" + fileInfo.get(RID_EVIDENCE)[1];
							try {
								Files.copy(Paths.get(docFile), Paths.get(outFile));

							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						}

					}
					continue;
				} else if (prop.getProperty("uin") != null && s.getId().equals(prop.getProperty("uin"))) {
					if (resident.getUIN() == null || resident.getUIN().equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), resident.getUIN());
					continue;
				} else if (prop.getProperty("introduceruin") != null
						&& s.getId().equals(prop.getProperty("introduceruin"))) {
					if (resident.isMinor()) {
						if (resident.getGuardian().getUIN() == null || resident.getGuardian().getUIN().equals("")) {
							// skip if null
							// identity.put(s.getId(), JSONObject.NULL);
						} else
							identity.put(s.getId(), resident.getGuardian().getUIN());
					}
					continue;
				} else if (prop.getProperty("introducerrid") != null
						&& s.getId().equals(prop.getProperty("introducerrid"))) {
					if (resident.isMinor()) {
						if (resident.getGuardian() != null && (resident.getGuardian().getRID() == null
								|| resident.getGuardian().getRID().equals(""))) {
							// As per MONO, skip
							// identity.put(s.getId(), JSONObject.NULL);
						} else
							identity.put(s.getId(), resident.getGuardian().getRID());
					}
					continue;
				} else if (prop.getProperty("introducername") != null
						&& s.getId().equals(prop.getProperty("introducername"))) {
					if (resident.isMinor()) {
						String primValue = "";
						String secValue = "";
						JSONObject o = new JSONObject();
						o.put("language", resident.getPrimaryLanguage());
						if (resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();

						if (resident.getSecondaryLanguage() != null) {
							if (resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
								secValue = resident.getGuardian().getName_seclang().getFirstName();
						}

						updateSimpleType(s.getId(), identity, primValue, secValue, resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), resident.getThirdLanguage());

					}
					continue;
				} else if (s.getId().toLowerCase().contains("consent")) {
					continue;
				} else if (s.getControlType().equals("checkbox")) {
					primVal = "Y";
					secVal = "Y";
				} else {
					primVal = "Some text value";
					if (resident.getSecondaryLanguage() != null)
						secVal = Translator.translate(resident.getSecondaryLanguage(), primVal);

				}
				if (s.getType().equals("simpleType")) {

					updateSimpleType(s.getId(), identity, primVal, secVal, resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(), resident.getThirdLanguage());

				} else {
					identity.put(s.getId(), primVal.equals("") ? JSONObject.NULL : primVal);
				}
			}
		}
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		return retObject.toString();

	}

	/*
	 * JSONObject constructExceptnNode(String type, String missingBio) { JSONObject
	 * node = new JSONObject(); node.put("type", type); node.put("missingBiometric",
	 * missingBio); node.put("reason", "Temporary"); node.put("exceptionType",
	 * "Temporary"); node.put("individualType", "applicant"); return node; }
	 */
	JSONObject constructExceptnNode(BioModality modality) {
		JSONObject node = new JSONObject();
		node.put("type", modality.getType());
		node.put("missingBiometric", modality.getSubType());
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

			biometrics.put("introducer", new JSONObject());
			biometrics.put("applicant-auth", new JSONObject());

			biometrics.put("applicant", applicant);
			identity.put("biometrics", biometrics);

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

			/*
			 * if(exceptionAttrib.contains("leftEye")) { applicant.put("leftEye",
			 * constructExceptnNode("Iris","leftEye")); }
			 * if(exceptionAttrib.contains("rightEye")) { applicant.put("rightEye",
			 * constructExceptnNode("Iris","rightEye")); } for(String n:
			 * DataProviderConstants.schemaNames) { if(n.contains("Eye") ) continue; if(
			 * n.equals("face")) applicant.put(n, constructExceptnNode("Face",n));
			 * 
			 * if(exceptionAttrib.contains(n)) { applicant.put(n,
			 * constructExceptnNode("Finger",n)); } }
			 */

			exceptionBiometrics.put("introducer", new JSONObject());
			exceptionBiometrics.put("applicant-auth", new JSONObject());

			exceptionBiometrics.put("applicant", applicant);
			identity.put("exceptionBiometrics", exceptionBiometrics);

		}
		return identity;
	}

	JSONObject updateSimpleType(String id, JSONObject identity, String primValue, String secValue, String primLang,
			String secLang, String thirdLang) {

		if (primValue == null)
			primValue = "Some Text Value";

		if ((secValue == null || secValue.equals("")) && secLang != null && !secLang.equals(""))
			secValue = Translator.translate(secLang, primValue);

		String thirdValue = "";
		if (thirdLang != null && !thirdLang.equals(""))
			thirdValue = Translator.translate(thirdLang, primValue);

		// array
		JSONArray ar = new JSONArray();
		JSONObject o = new JSONObject();
		o.put("language", primLang);
		if (primValue != null && primValue.equals(""))
			o.put("value", Json.NULL);
		else
			o.put("value", primValue);
		ar.put(o);
		if (secLang != null) {
			o = new JSONObject();
			o.put("language", secLang);
			if (secValue.equals(""))
				o.put("value", Json.NULL);
			else
				o.put("value", secValue);
			ar.put(o);
		}
		if (thirdLang != null) {
			o = new JSONObject();
			o.put("language", thirdLang);
			if (thirdValue.equals(""))
				o.put("value", Json.NULL);
			else
				o.put("value", thirdValue);
			ar.put(o);
		}

		identity.put(id, ar);
		return identity;
	}

	Boolean generateCBEFF(ResidentModel resident, List<String> bioAttrib, String outFile) throws Exception {

		String strVal = VariableManager.getVariableValue("usemds").toString();
		boolean bMDS = Boolean.valueOf(strVal);
		String cbeff = resident.getBiometric().getCbeff();
		if (bMDS) {
			if (cbeff == null) {
				MDSRCaptureModel capture = BiometricDataProvider.regenBiometricViaMDS(resident);
				resident.getBiometric().setCapture(capture.getLstBiometrics());
				String strCBeff = BiometricDataProvider.toCBEFFFromCapture(bioAttrib, capture, outFile);
				resident.getBiometric().setCbeff(strCBeff);

			} else {
				PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
				writer.print(cbeff);
				writer.close();
			}
		} else {

			if (cbeff == null) {

				String strCBeff = BiometricDataProvider.toCBEFF(bioAttrib, resident.getBiometric(), outFile);
				resident.getBiometric().setCbeff(strCBeff);

			} else {
				PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
				writer.print(cbeff);
				writer.close();
			}
		}
		resident.save();
		return true;
	}

	/*
	 * HashMap<FolderType, [(in)folderPath][(out)biofilename]> fileInfo
	 */
	// check the dynamic field logic and change here
	public static boolean processGender(MosipIDSchema s, ResidentModel resident,JSONObject identity, 
			 Hashtable<String,List<MosipGenderModel>> genderTypesLang,
			 Hashtable<String,List<DynamicFieldModel>> dynaFields) {
			
			boolean processed = false;
			
			if((s.getSubType()!=null && s.getSubType().toLowerCase().equals("gender")) ||
					s.getId().toLowerCase().equals("gender")  ) {
				
				String primLang = resident.getPrimaryLanguage();
				String secLan = resident.getSecondaryLanguage();
				//String thirdLan = resident.getThirdLanguage();
				
				Gender resGen = resident.getGender();
				String genderCode = null;
				String primVal = "";
				String secVal = "";
				
				//context should set Male/Female code values 
				Object obj = VariableManager.getVariableValue(resGen.name());
				if(obj != null) {
					genderCode = obj.toString();
					primVal = secVal = genderCode;
				}
				/*
				 * if(genderCode != null) { if(genderTypesLang != null) { List<MosipGenderModel>
				 * genderTypes = genderTypesLang.get(primLang); if(genderTypes != null)
				 * for(MosipGenderModel g: genderTypes) { if(!g.getIsActive()) continue;
				 * if(g.getCode().equals(genderCode)) { primVal = g.getValue(); break; }
				 * 
				 * } if(secLan != null) { genderTypes = genderTypesLang.get(secLan);
				 * if(genderTypes != null) for(MosipGenderModel g: genderTypes) {
				 * if(!g.getIsActive()) continue; if(g.getCode().equals(genderCode)) { secVal =
				 * g.getValue(); break; }
				 * 
				 * } } } } if(secVal == null || secVal.equals("")) secVal = primVal;
				 * CreatePersona.constructNode(identity, s.getId(),
				 * resident.getPrimaryLanguage(), resident.getSecondaryLanguage(), primVal,
				 * secVal, s.getType().equals("simpleType") ? true: false ); processed = true;
				 */
				
				//
				
				if (dynaFields != null) {
					DynamicFieldModel dfmPrim = null;
					String primaryValue = "";
					String secValue = "";

					for (DynamicFieldModel dfm : dynaFields.get(primLang)) {
						if (dfm.getIsActive() && dfm.getName().equals(s.getId())) {
							//primaryValue = dfm.getFieldVal().get(0).getValue();
							primaryValue = dfm.getFieldVal().get(0).getCode();
							dfmPrim = dfm;
							break;
						}
					}
					secValue = primaryValue;

					if (secLan != null)
						for (DynamicFieldModel dfm1 : dynaFields.get(secLan)) {
							if (dfm1.getIsActive() && dfm1.getName().equals(s.getId())) {
								//secValue = dfm1.getFieldVal().get(0).getValue();
								secValue = dfm1.getFieldVal().get(0).getCode();
								break;
							}
						}

					CreatePersona.constructNode(identity, s.getId(), resident.getPrimaryLanguage(),
							resident.getSecondaryLanguage(), primaryValue, secValue,
							s.getType().equals("simpleType") ? true : false);
					processed = true;

				}
				//
				
			}
			return processed;

	}

	public static Pair<String, String> processAddresslines(MosipIDSchema s, ResidentModel resident,
			JSONObject identity) {
		String addr = null;
		String addr_sec="";
			
		if(s.getControlType().equals("checkbox")) {
			addr = "Y";
			addr_sec="Y";
		}
		else
		{
			String [] addressLines = resident.getAddress();
			int index = 0;
			if(s.getId().toLowerCase().contains("line1"))
				index = 0;
			else
			if(s.getId().toLowerCase().contains("line2"))
				index = 1;
			else
			if(s.getId().toLowerCase().contains("line3"))
				index = 2;
			else
			if(s.getId().toLowerCase().contains("line4"))
					index = 3;
		
	
			if(index > -1)
				addr = addressLines[index];
			if(addr == null  ) {
				Random rand = new Random();
				addr = "#%d, %d Street, %d block" ;//+ schemaItem.getId();
				addr = String.format(addr, (100+ rand.nextInt(999)),
					(1 + rand.nextInt(99)),
					(1 + rand.nextInt(10))
					);
			
				if(resident.getSecondaryLanguage() != null)
					addr_sec =Translator.translate(resident.getSecondaryLanguage(),addr);
			}
			else
			{
				if(resident.getSecondaryLanguage() != null) {
					addr_sec = resident.getAddress_seclang()[index];
					if(addr_sec == null)
						addr_sec =Translator.translate(resident.getSecondaryLanguage(),addr);
				
				}
				
			}
			if(s.getMaximum() > 0 && addr.length() >= s.getMaximum() )
				addr = addr.substring(0,s.getMaximum() -1);
		}
		Pair<String,String> retVal = new Pair<String,String>(addr,addr_sec);
		return retVal;

	}

	public static String generateDefaultAttributes(MosipIDSchema schemaItem, ResidentModel resident,
			JSONObject identity) {
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}
		}
		if (someVal == null)
			someVal = CommonUtil.generateRandomString(schemaItem.getMaximum());
		return someVal;

	}

	private static Boolean updateFromAdditionalAttribute(JSONObject identity, MosipIDSchema s, ResidentModel resident) {
		Boolean bRet = false;
		Hashtable<String,String> addtnAttr = resident.getAddtionalAttributes();
		if(addtnAttr == null) 
			return bRet;
		Enumeration<String> keys =  addtnAttr.keys();
		
		while( keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = addtnAttr.get(key);
			
			if(s.getId().equalsIgnoreCase(key)) {
				if(s.getType().equals("simpleType")) {
					
					JSONArray jsonO= null;
					try {
						jsonO = new JSONArray(value);
						identity.put(s.getId(), jsonO);
					}catch(Exception e) {
					//	e.printStackTrace();
					}
					if(jsonO == null) {
						String secValue = Translator.translate(resident.getSecondaryLanguage(),value);
						CreatePersona.constructNode(identity, s.getId(), resident.getPrimaryLanguage(), resident.getSecondaryLanguage(),
								value,secValue,true);
					}
				}
				else
					identity.put(s.getId(), value);
				bRet = true;
				break;
			}
		}
		return bRet;
	}

	public static Boolean processDynamicFields(MosipIDSchema s, JSONObject identity, ResidentModel resident) {

		String primaryLanguage = resident.getPrimaryLanguage();
		String secLanguage = resident.getSecondaryLanguage();
		Hashtable<String, List<DynamicFieldModel>> dynaFields = resident.getDynaFields();
		Hashtable<String, List<MosipGenderModel>> genderTypes = resident.getGenderTypes();
		boolean found = false;

		if (s.getFieldType().equals("dynamic")) {

			found = processGender(s, resident, identity, genderTypes, dynaFields);
			if (found)
				return found;

			if (dynaFields != null) {
				DynamicFieldModel dfmPrim = null;
				String primaryValue = "";
				String secValue = "";

				for (DynamicFieldModel dfm : dynaFields.get(primaryLanguage)) {
					if (dfm.getIsActive() && dfm.getName().equals(s.getId())) {
						//primaryValue = dfm.getFieldVal().get(0).getValue();
						primaryValue = dfm.getFieldVal().get(0).getCode();
						dfmPrim = dfm;
						break;
					}
				}
				secValue = primaryValue;

				if (secLanguage != null)
					for (DynamicFieldModel dfm1 : dynaFields.get(secLanguage)) {
						if (dfm1.getIsActive() && dfm1.getName().equals(s.getId())) {
							//secValue = dfm1.getFieldVal().get(0).getValue();
							secValue = dfm1.getFieldVal().get(0).getCode();
							break;
						}
					}

				CreatePersona.constructNode(identity, s.getId(), resident.getPrimaryLanguage(),
						resident.getSecondaryLanguage(), primaryValue, secValue,
						s.getType().equals("simpleType") ? true : false);
				found = true;

			}

		}
		return found;
	}

	String generateIDJson(ResidentModel resident, HashMap<String, String[]> fileInfo) {

		String idjson = "";

		JSONObject identity = new JSONObject();

		String primaryLanguage = resident.getPrimaryLanguage();
		String secLanguage = resident.getSecondaryLanguage();
		Hashtable<String, MosipLocationModel> locations = resident.getLocation();
		Hashtable<String, MosipLocationModel> locations_seclang = resident.getLocation_seclang();
		// Hashtable<String,List<DynamicFieldModel>> dynaFields =
		// resident.getDynaFields();
		// Hashtable<String,List<MosipGenderModel>> genderTypes =
		// resident.getGenderTypes();

		Set<String> locationSet = locations.keySet();

		Set<String> locationSet_sec = null;
		if (locations_seclang != null)
			locationSet_sec = locations_seclang.keySet();
		List<String> lstMissedAttributes = resident.getMissAttributes();
		//identity.put("IDSchemaVersion",schemaVersion );
		for (MosipIDSchema s : schema) {
			System.out.println(s.toJSONString());
			// if not reqd field , skip it
			if (!CommonUtil.isExists(requiredAttribs, s.getId()))
				continue;

			if (lstMissedAttributes != null
					&& lstMissedAttributes.stream().anyMatch(v -> v.equalsIgnoreCase(s.getId()))) {
				continue;
			}

			if (s.getId().equalsIgnoreCase("idschemaVersion")) {
				identity.put(s.getId(), schemaVersion);
				continue;
			}

			/*
			 * if (s.getIdVersion() != null) { identity.put("idVersion", schemaVersion);
			 * //continue; }
			 */
			if (s.getId().equalsIgnoreCase("uin")) {
				String uin = resident.getUIN();
				if (uin != null && !uin.trim().equals("")) {
					identity.put(s.getId(), uin.trim());
				}
				continue;
			}
			if (!s.getRequired() && !s.getInputRequired()) {
				continue;
			}
			if ( !s.getRequired() && !(s.getRequiredOn() != null && s.getRequiredOn().size() > 0)) {
				continue;
			}
			if (updateFromAdditionalAttribute(identity, s, resident)) {
				continue;
			}
			if (processDynamicFields(s, identity, resident))
				continue;

			/*
			 * if(s.getFieldType().equals("dynamic")) {
			 * 
			 * boolean found=false; found = processGender(s, resident,identity, genderTypes,
			 * dynaFields); if(found) continue;
			 * 
			 * if(dynaFields != null) { DynamicFieldModel dfmPrim = null; String
			 * primaryValue = ""; String secValue = "";
			 * 
			 * for(DynamicFieldModel dfm: dynaFields.get(primaryLanguage)) {
			 * if(dfm.getIsActive() && dfm.getName().equals(s.getId()) ) { primaryValue =
			 * dfm.getFieldVal().get(0).getValue(); dfmPrim = dfm; break; } if(secLanguage
			 * != null) for(DynamicFieldModel dfm1: dynaFields.get(secLanguage)) {
			 * if(dfm1.getIsActive() && dfm1.getId().equals(dfmPrim.getId())){ secValue =
			 * dfm1.getFieldVal().get(0).getValue(); break; } }
			 * 
			 * CreatePersona.constructNode(identity, s.getId(),
			 * resident.getPrimaryLanguage(), resident.getSecondaryLanguage(), primaryValue,
			 * secValue, s.getType().equals("simpleType") ? true: false ); found=true;
			 * break; }
			 * 
			 * if(found) continue; }
			 */

			if (s.getFieldCategory().equals("pvt") || s.getFieldCategory().equals("kyc")) {
				String primaryValue = "";
				String secValue = "";
				if (s.getId().toLowerCase().equals("fullname")) {
					primaryValue = resident.getName().getFirstName() + " " + resident.getName().getMidName() + " "
							+ resident.getName().getSurName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getFirstName() + " "
								+ resident.getName_seclang().getMidName() + " "
								+ resident.getName_seclang().getSurName();
				} else if (s.getId().toLowerCase().equals("firstname")) {
					primaryValue = resident.getName().getFirstName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getFirstName();
				} else if (s.getId().toLowerCase().equals("lastname") || s.getId().toLowerCase().equals("surname")) {
					primaryValue = resident.getName().getSurName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getSurName();
				} else if (s.getId().toLowerCase().equals("middlename") || s.getId().toLowerCase().equals("midname")) {
					primaryValue = resident.getName().getMidName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getMidName();
				} else if (s.getId().toLowerCase().equals("dateofbirth") || s.getId().toLowerCase().equals("dob")
						|| s.getId().toLowerCase().equals("birthdate")) {
					primaryValue = resident.getDob();
					secValue = primaryValue;
				} else if (s.getId().toLowerCase().equals("gender")) {
					primaryValue = "Female";
					if (resident.getGender().equals("Male"))
						primaryValue = "Male";
					secValue = primaryValue;
				} else if (s.getId().toLowerCase().contains("address")
						&& (s.getGroup() != null && !s.getGroup().toLowerCase().equals("documents"))) {
					if (s.getControlType().equals("checkbox")) {
						primaryValue = "Y";
						if (secLanguage != null)
							secValue = "Y";
					} else {
						Pair<String, String> addrLines = processAddresslines(s, resident, identity);
						primaryValue = addrLines.getValue0();
						secValue = addrLines.getValue1();
					}
				} else if (s.getSubType().toLowerCase().contains("residenceStatus")) {
					primaryValue = resident.getResidentStatus().getCode();
					secValue = primaryValue;
				}
				/*
				 * else if(s.getId().toLowerCase().contains("phone") ||
				 * s.getId().toLowerCase().contains("mobile") ) { primaryValue =
				 * resident.getContact().getMobileNumber(); }
				 */
				else if (s.getId().toLowerCase().contains("email") || s.getId().toLowerCase().contains("mail")) {
					primaryValue = resident.getContact().getEmailId();
				}

				/*
				 * else if(s.getId().toLowerCase().contains("referenceIdentity") ) {
				 * primaryValue = resident.getId(); }
				 */
				else if (s.getId().toLowerCase().contains("blood")) {
					primaryValue = resident.getBloodgroup().getCode();
					secValue = primaryValue;
				} else if (s.getType().equals("biometricsType")
						&& (s.getGroup() != null && s.getGroup().equals("Biometrics"))
						&& s.getId().toLowerCase().contains("individual")) {
					JSONObject o = new JSONObject();
					o.put("format", "cbeff");
					o.put("version", 1.0f);
					String[] v = fileInfo.get(RID_FOLDER);
					v[1] = s.getId() + "_bio_CBEFF.xml";
					fileInfo.put(RID_FOLDER, v);
					o.put("value", s.getId() + "_bio_CBEFF");
					// o.put("value","individualBiometrics_bio_CBEFF");
					identity.put(s.getId(), o);

					String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + fileInfo.get(RID_FOLDER)[1];
					try {
						List<String> missAttribs = resident.getMissAttributes();
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
						generateCBEFF(resident, bioAttrib, outFile);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				} else if (s.getType().equals("biometricsType")
						&& (s.getGroup() != null && s.getGroup().equals("Biometrics"))
						&& s.getSubType().equals("introducer")) {

					if (resident.isMinor() && resident.getGuardian() != null) {
						JSONObject o = new JSONObject();
						o.put("format", "cbeff");
						o.put("version", 1.0f);
						String[] v = fileInfo.get(RID_FOLDER);
						v[2] = s.getId() + "_bio_CBEFF.xml";
						fileInfo.put(RID_FOLDER, v);
						o.put("value", s.getId() + "_bio_CBEFF");
						// o.put("value","individualBiometrics_bio_CBEFF");
						identity.put(s.getId(), o);

						String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + v[2];
						try {
							// Implement excetpions by parsing 'Miss' list
							List<String> missAttribs = resident.getMissAttributes();
							List<String> bioAttrib = s.getBioAttributes();
							if (missAttribs != null && !missAttribs.isEmpty())
								bioAttrib.removeAll(missAttribs);

							generateCBEFF(resident.getGuardian(), bioAttrib, outFile);
							// BiometricDataProvider.toCBEFF(bioAttrib,
							// resident.getGuardian().getBiometric(), outFile);

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					continue;
				} else if (s.getType().equals("biometricsType")
				// && ( s.getGroup() !=null && s.getGroup().equals("Biometrics"))
				// && s.getId().toLowerCase().contains("parent")
				) {
					// identity.put(s.getId(), JSONObject.NULL);
					continue;
				} else if (s.getType().equals("documentType")) {

					// String docType = s.getSubType();
					int index = 0;
					for (MosipDocument doc : resident.getDocuments()) {

						if (CommonUtil.isExists(lstMissedAttributes, doc.getDocCategoryCode()))
							continue;
						index = 0;
						if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {

							index = resident.getDocIndexes().get(doc.getDocCategoryCode());

							// index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size()-1, 0)[0];
							// String docFile = doc.getDocs().get(index); by alok
							String docFile = doc.getDocs().get(0);
							System.out.println("docFIle=" + docFile + " dType=" + s.getSubType() + " cat=" + s.getId());

							JSONObject o = new JSONObject();
							o.put("format", "pdf");
							// o.put("type", doc.getType().get(index).getCode()); by alok
							o.put("type", doc.getType().get(0).getCode());
							String[] v = fileInfo.get(RID_FOLDER);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(RID_FOLDER, v);
							o.put("value", s.getId());

							identity.put(s.getId(), o);

							String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + fileInfo.get(RID_FOLDER)[1];
							try {
								Files.copy(Paths.get(docFile), Paths.get(outFile));

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}

					}
					continue;
				} else if (s.getSubType() != null
						&& (s.getSubType().equals("parentOrGuardianName") || s.getSubType().equals("introducerName")
								|| (s.getGroup() != null && ( s.getGroup().toLowerCase().equals("guardiandetails") 
										|| s.getGroup().toLowerCase().contains("introducer"))))) {  //  this line added by alok
					if (resident.getGuardian() != null) {

						// (JSONObject identity, String primValue, String secValue, String primLang,
						// String secLang, String thirdLang) {
						String primValue = null;
						String secGValue = null;
						if (resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();
						if (resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
							secGValue = resident.getGuardian().getName_seclang().getFirstName();

						updateSimpleType(s.getId(), identity, primValue, secGValue, resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), resident.getThirdLanguage());

					}
				} else if (s.getInputRequired() && s.getId().contains("IdentityNumber")) {
					List<SchemaValidator> validators = s.getValidators();
					if (validators != null) {
						primaryValue = generateDefaultAttributes(s, resident, identity);
					} else {
						primaryValue = resident.getId();
					}

					// int []r = CommonUtil.generateRandomNumbers(2, 99999, 11111);

					// primaryValue = String.format("%d%d", r[0],r[1]);
					identity.put(s.getId(), primaryValue);
					continue;
				}
				/*
				 * else { //System.out.println("SchemaID:"+ s.getId());
				 * if(resident.getDynaFields() != null) { DynamicFieldModel dfmVal = null;
				 * for(DynamicFieldModel dfm: resident.getDynaFields().get(primaryLanguage)) {
				 * //System.out.println("dfm:"+ dfm.getName());
				 * if(s.getId().toLowerCase().endsWith(dfm.getName().toLowerCase())){
				 * primaryValue = dfm.getFieldVal().get(0).getValue(); dfmVal = dfm; break; } }
				 * if(secLanguage != null) for(DynamicFieldModel dfm:
				 * resident.getDynaFields().get(secLanguage)) {
				 * 
				 * if(dfm.getId().equals(dfmVal.getId())){ secValue =
				 * dfm.getFieldVal().get(0).getValue(); break; } } } }
				 */
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
						secValue = Translator.translate(secLanguage, primaryValue);
					}
				}

				if (s.getType().equals("simpleType")
				// && !s.getId().toLowerCase().equals("postalcode")
				) {

					updateSimpleType(s.getId(), identity, primaryValue, secValue, primaryLanguage, secLanguage,
							resident.getThirdLanguage());

				} else {
					// if(s.getRequired() && (primaryValue == null || primaryValue.equals(""))) {
					// primaryValue = new Random().nextLong() +"";
					// }
					if (primaryValue.equals("")) // && !s.getType().equals("string"))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), primaryValue);
				}
			}

		}
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		idjson = retObject.toString();
		return idjson;
	}
	
	
	String generateIDJsonV2(ResidentModel resident, HashMap<String, String[]> fileInfo,String contextKey,Properties props,JSONObject preregResponse) {

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

		loadMapperProp(contextKey);  // load mapped property 

		for (MosipIDSchema s : schema) {
			System.out.println(s.toJSONString());
			// if not reqd field , skip it
			if (!CommonUtil.isExists(requiredAttribs, s.getId()))
				continue;

			if (lstMissedAttributes != null
					&& lstMissedAttributes.stream().anyMatch(v -> v.equalsIgnoreCase(s.getId()))) {
				continue;
			}

			if (prop.getProperty("uin") != null && s.getId().equals(prop.getProperty("uin"))) {
				String uin = resident.getUIN();
				if (uin != null && !uin.trim().equals("")) {
					identity.put(s.getId(), uin.trim());
				}
				continue;
			}
			if (!s.getRequired() && !s.getInputRequired()) { 
				continue;
			}
			
			
			// DEV2
			if (!s.getRequired() && !(s.getRequiredOn() != null && s.getRequiredOn().size() > 0)) {
				continue;
			}
			
			/*QA-Double
			if (s.getRequired() && (s.getRequiredOn() != null && s.getRequiredOn().size() > 0)) {
				continue;
			}*/
	

			if (prop.getProperty("idschemaversion") != null && s.getId().equals(prop.getProperty("idschemaversion"))) {
				identity.put(s.getId(), schemaVersion);
				continue;
			}


			if (updateFromAdditionalAttribute(identity, s, resident)) {
				continue;
			}
			if (processDynamicFields(s, identity, resident))
				continue;

			if (s.getFieldCategory().equals("pvt") || s.getFieldCategory().equals("kyc")) {
				String primaryValue = "";
				String secValue = "";
				if (prop.getProperty("fullname") != null && s.getId().equals(prop.getProperty("fullname"))) {
					primaryValue = resident.getName().getFirstName() + " " + resident.getName().getMidName() + " "
							+ resident.getName().getSurName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getFirstName() + " "
								+ resident.getName_seclang().getMidName() + " "
								+ resident.getName_seclang().getSurName();
				} else if (prop.getProperty("firstname") != null && s.getId().equals(prop.getProperty("firstname"))) {
					primaryValue = resident.getName().getFirstName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getFirstName();
				} else if (prop.getProperty("lastname") != null && s.getId().equals(prop.getProperty("lastname"))) {
					primaryValue = resident.getName().getSurName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getSurName();
				} else if (prop.getProperty("middlename") != null && s.getId().equals(prop.getProperty("middlename"))) {
					primaryValue = resident.getName().getMidName();
					if (secLanguage != null)
						secValue = resident.getName_seclang().getMidName();
				} else if (prop.getProperty("dateofbirth") != null
						&& s.getId().equals(prop.getProperty("dateofbirth"))) {
					primaryValue = resident.getDob();
					secValue = primaryValue;
				} 
				else if (prop.getProperty("addressgroup") != null
						&& s.getId().equals(prop.getProperty("addressgroup"))) {
					if (s.getControlType().equals("checkbox")) {
						primaryValue = "Y";
						if (secLanguage != null)
							secValue = "Y";
					} else {
						Pair<String, String> addrLines = processAddresslines(s, resident, identity);
						primaryValue = addrLines.getValue0();
						secValue = addrLines.getValue1();
					}
				} else if (s.getSubType().toLowerCase().contains("residenceStatus")) {
					primaryValue = resident.getResidentStatus().getCode();
					secValue = primaryValue;
				} else if (prop.getProperty("email") != null && s.getId().equals(prop.getProperty("email"))) {
					primaryValue = resident.getContact().getEmailId();
				}

				else if (s.getId().toLowerCase().contains("blood")) {
					primaryValue = resident.getBloodgroup().getCode();
					secValue = primaryValue;
				} else if (prop.getProperty("individualbiometric") != null
						&& s.getId().equals(prop.getProperty("individualbiometric"))) {
					JSONObject o = new JSONObject();
					o.put("format", "cbeff");
					o.put("version", 1.0f);
					String[] v = fileInfo.get(RID_FOLDER);
					v[1] = s.getId() + "_bio_CBEFF.xml";
					fileInfo.put(RID_FOLDER, v);
					o.put("value", s.getId() + "_bio_CBEFF");
					identity.put(s.getId(), o);

					String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + fileInfo.get(RID_FOLDER)[1];
					try {
						List<String> missAttribs = resident.getMissAttributes();
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
						generateCBEFF(resident, bioAttrib, outFile);
						/*
						 * Adding to set cbeff filefor officer and supervisor
						 */
						if(props.containsKey("mosip.test.regclient.officerBiometricFileName")) {
//							Response response = RestAssured.given()
//					                .contentType(ContentType.JSON)
//					                .when()
//					                .get("https://api-internal.qa4.mosip.net/idrepository/v1/identity/idvid/8130420350?type=bio")
//					                .then()
//					                .extract().response();
//							
					        System.out.println(preregResponse.toString());
					        System.out.println();
					        JSONArray getArray=preregResponse.getJSONObject("response").getJSONArray("documents");
					        JSONObject objects = getArray.getJSONObject(0);
					        String value = (String) objects.get("value");
					      //  byte[] decoded = Base64.getDecoder().decode(value);
					        byte[] decoded =Base64.getUrlDecoder().decode(value);
					        String decodedcbeff = new String(decoded, StandardCharsets.UTF_8);
					        resident.getBiometric().setCbeff(decodedcbeff);
							generateCBEFF(resident, bioAttrib, fileInfo.get(RID_FOLDER)[0] + "/"+props.get("mosip.test.regclient.officerBiometricFileName")+".xml");
						}
							if(props.containsKey("mosip.test.regclient.supervisorBiometricFileName")) {
						generateCBEFF(resident, bioAttrib, fileInfo.get(RID_FOLDER)[0] +"/"+props.get("mosip.test.regclient.supervisorBiometricFileName")+".xml");
							}
						
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue;
				} else if (prop.getProperty("introducerbiometric") != null
						&& s.getId().equals(prop.getProperty("introducerbiometric"))) {

					if (resident.isMinor() && resident.getGuardian() != null) {
						JSONObject o = new JSONObject();
						o.put("format", "cbeff");
						o.put("version", 1.0f);
						String[] v = fileInfo.get(RID_FOLDER);
						v[2] = s.getId() + "_bio_CBEFF.xml";
						fileInfo.put(RID_FOLDER, v);
						o.put("value", s.getId() + "_bio_CBEFF");
						identity.put(s.getId(), o);

						String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + v[2];
						try {
							// Implement excetpions by parsing 'Miss' list
							List<String> missAttribs = resident.getMissAttributes();
							List<String> bioAttrib = s.getBioAttributes();
							if (missAttribs != null && !missAttribs.isEmpty())
								bioAttrib.removeAll(missAttribs);

							generateCBEFF(resident.getGuardian(), bioAttrib, outFile);

						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (resident.getGuardian() != null) {
						String primValue = null;
						String secGValue = null;
						if (resident.getGuardian() != null)
							primValue = resident.getGuardian().getName().getFirstName();
						if (resident.getGuardian() != null && resident.getGuardian().getName_seclang() != null)
							secGValue = resident.getGuardian().getName_seclang().getFirstName();

						updateSimpleType(s.getId(), identity, primValue, secGValue, resident.getPrimaryLanguage(),
								resident.getSecondaryLanguage(), resident.getThirdLanguage());

					}
					continue;
				} else if (s.getType().equals("biometricsType")) {
					continue;
				} else if (s.getType().equals("documentType")) {

					int index = 0;
					for (MosipDocument doc : resident.getDocuments()) {

						if (CommonUtil.isExists(lstMissedAttributes, doc.getDocCategoryCode()))
							continue;
						index = 0;
						if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {

 							index = resident.getDocIndexes().get(doc.getDocCategoryCode());

							String docFile = doc.getDocs().get(0);
							System.out.println("docFIle=" + docFile + " dType=" + s.getSubType() + " cat=" + s.getId());

							JSONObject o = new JSONObject();
							o.put("format", "pdf");
							//o.put("type", doc.getType().get(0).getCode());
							o.put("type", doc.getType().get(0).getDocTypeCode());
							String[] v = fileInfo.get(RID_FOLDER);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(RID_FOLDER, v);
							o.put("value", s.getId());

							identity.put(s.getId(), o);

							String outFile = fileInfo.get(RID_FOLDER)[0] + "/" + fileInfo.get(RID_FOLDER)[1];
							try {
								Files.copy(Paths.get(docFile), Paths.get(outFile));

							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						}

					}
					continue;
				} /*
					 * else if (prop.getProperty("introducerbiometric")!=null &&
					 * s.getId().equals(prop.getProperty("introducerbiometric"))) {
					 * 
					 * if (resident.getGuardian() != null) { String primValue = null; String
					 * secGValue = null; if (resident.getGuardian() != null) primValue =
					 * resident.getGuardian().getName().getFirstName(); if (resident.getGuardian()
					 * != null && resident.getGuardian().getName_seclang() != null) secGValue =
					 * resident.getGuardian().getName_seclang().getFirstName();
					 * 
					 * updateSimpleType(s.getId(), identity, primValue, secGValue,
					 * resident.getPrimaryLanguage(), resident.getSecondaryLanguage(),
					 * resident.getThirdLanguage());
					 * 
					 * } }
					 */
				else if (prop.getProperty("identitynumber") != null
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
						secValue = Translator.translate(secLanguage, primaryValue);
					}
				}

				if (s.getType().equals("simpleType")) {

					updateSimpleType(s.getId(), identity, primaryValue, secValue, primaryLanguage, secLanguage,
							resident.getThirdLanguage());

				} else {
					if (primaryValue.equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), primaryValue);
				}
			}

		}
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		idjson = retObject.toString();
		return idjson;
	}

	private void loadMapperProp(String contextKey) {
		String hostName = null;
		if (contextKey != null && !contextKey.equals(""))
			hostName = contextKey.split("_")[0];
		else
			throw new RuntimeException("ContextKey not found !!");
		
		String propPath = VariableManager.getVariableValue("mosip.test.env.mapperpath").toString();
		System.out.println(propPath);
		try {
			File folder = new File(String.valueOf(propPath) + File.separator);
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) {
				if (file.isFile()) {
					if (file.getName().contains(hostName + DOMAIN_NAME)) {
						propPath = file.getAbsolutePath();
						break;
					}
				}
			}
			prop.load(new FileInputStream(propPath));
		} catch (FileNotFoundException fnf) {
			fnf.printStackTrace();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
	
	
	

	String generateMetaDataJson(ResidentModel resident, String preRegistrationId, String machineId, String centerId,
			HashMap<String, String[]> fileInfo) {

		String templateMetaJsonPath = VariableManager.getVariableValue("templateIDMeta").toString().trim();

		String templateIdentityStr = CommonUtil.readFromJSONFile(templateMetaJsonPath);
		JSONObject templateIdentity = new JSONObject(templateIdentityStr).getJSONObject("identity");
		JSONObject identity = new JSONObject();
		JSONArray docArray = new JSONArray();

		for (MosipIDSchema s : schema) {
			if (s.getType().equals("documentType") && s.getRequired()) {
				int index = 0;
				for (MosipDocument doc : resident.getDocuments()) {
					index = 0;
					if (doc.getDocCategoryCode().toLowerCase().equals(s.getSubType().toLowerCase())) {
						index = CommonUtil.generateRandomNumbers(1, doc.getDocs().size() - 1, 0)[0];
						String docFile = doc.getDocs().get(index);
						System.out.println("docFIle=" + docFile + " dType=" + s.getSubType() + " cat=" + s.getId());

						JSONObject o = new JSONObject();
						o.put("documentCategory", doc.getDocCategoryCode());
						o.put("documentType", doc.getType().get(index).getCode());
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
		identity.put("documents", docArray);
		identity.put("capturedRegisteredDevices", new JSONArray());
		identity.put("exceptionBiometrics", new JSONObject());
		identity.put("creationDate", CommonUtil.getUTCDateTime(null));
		// identity.put("capturedRegisteredDevices",templateIdentity.getJSONArray("capturedRegisteredDevices")
		// );

		constructBioException(resident, identity);
		constructBioMetaData(resident, identity);
		identity.put("operationsData", templateIdentity.getJSONArray("operationsData"));

		JSONArray metadata = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("label", "creationDate");
		obj.put("value", CommonUtil.getUTCDateTime(null));
		metadata.put(obj);

		if (preRegistrationId != null && !preRegistrationId.equals("")) {
			obj = new JSONObject();
			obj.put("label", "preRegistrationId");
			obj.put("value", preRegistrationId);
			metadata.put(obj);

		}
		if (centerId != null && !centerId.equals("")) {
			obj = new JSONObject();
			obj.put("label", "centerId");
			obj.put("value", centerId);
			metadata.put(obj);
		}

		if (machineId != null && !machineId.equals("")) {
			obj = new JSONObject();
			obj.put("label", "machineId");
			obj.put("value", machineId);
			metadata.put(obj);
		}

		identity.put("metaData", metadata);

		/*
		 * JSONArray opData = new JSONArray(); JSONObject o = new JSONObject();
		 * o.put("label", "officerId"); o.put("value", ""); opData.put(o); o = new
		 * JSONObject(); o.put("label", "officerBiometricFileName"); o.put("value", "");
		 * opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "supervisorId"); o.put("value", "");
		 * opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "supervisorBiometricFileName");
		 * o.put("value", ""); opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "supervisorBiometricFileName");
		 * o.put("value", ""); opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "supervisorPassword"); o.put("value",
		 * true); opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "officerPassword"); o.put("value",
		 * true); opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "supervisorPIN"); o.put("value", "");
		 * opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "officerPIN"); o.put("value", "");
		 * opData.put(o); o = new JSONObject(); o.put("label",
		 * "supervisorOTPAuthentication"); o.put("value", ""); opData.put(o);
		 * 
		 * o = new JSONObject(); o.put("label", "officerOTPAuthentication");
		 * o.put("value", ""); opData.put(o);
		 * 
		 * identity.put("operationsData", opData);
		 */
		JSONObject retObject = new JSONObject();
		retObject.put("identity", identity);
		return retObject.toString();
	}

	String genRID_PacketTypeJson(String src, String process, String packetType) {

		JSONObject retObject = new JSONObject();
		retObject.put("process", process.toUpperCase());
		retObject.put("source", src.toUpperCase());
		retObject.put("creationdate", CommonUtil.getUTCDateTime(null));
		retObject.put("providerversion", "v1.0");
		retObject.put("schemaversion", schemaVersion);
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
		List<ResidentModel> residents = provider.generate();
		try {
			new PacketTemplateProvider().generate("registration_client", "new", residents.get(0), "/temp//newpacket",
					null, null, null,null,null,null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(generateIDJson(residents.get(0)));

	}

}
