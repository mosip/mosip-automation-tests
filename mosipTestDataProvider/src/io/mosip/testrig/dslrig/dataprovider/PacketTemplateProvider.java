package io.mosip.testrig.dslrig.dataprovider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.models.ContextSchemaDetail;
import io.mosip.testrig.dslrig.dataprovider.models.DocumentDto;
import io.mosip.testrig.dslrig.dataprovider.models.DynamicFieldModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipGenderModel;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.models.SchemaRule;
import io.mosip.testrig.dslrig.dataprovider.packet.CrvsIdJsonBuilder;
import io.mosip.testrig.dslrig.dataprovider.packet.EvidenceJsonBuilder;
import io.mosip.testrig.dslrig.dataprovider.packet.IdJsonBuilder;
import io.mosip.testrig.dslrig.dataprovider.packet.PacketJsonSupport;
import io.mosip.testrig.dslrig.dataprovider.packet.PacketMetadataBuilder;
import io.mosip.testrig.dslrig.dataprovider.packet.PacketTemplateDocuments;
import io.mosip.testrig.dslrig.dataprovider.persona.PersonaBiometricsAssembler;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;


@SuppressWarnings("unchecked")
public class PacketTemplateProvider {
	private static final Logger logger = LoggerFactory.getLogger(PacketTemplateProvider.class);

	public static String RID_FOLDER = "rid_id";
	public static String RID_EVIDENCE = "rid_evidence";
	public static String RID_OPTIONAL = "rid_optional";

	private static final String ID_JSON = "/ID.json";
	private static final String PACKET_META_INFO_JSON = "/packet_meta_info.json";
	private static final String EVIDENCE = "evidence";
	private static final String IDENTITY = "identity";
	private static final String BIOMETRICSTYPE = "biometricsType";
	private static final String DOCUMENTTYPE = "documentType";

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


	public String generate(String source, String process, ResidentModel resident, String packetFilePath,
			String preregId, String machineId, String centerId, String contextKey, Properties props,
			JSONObject preregResponse, String purpose, String qualityScore, boolean genarateValidCbeff)
			throws Exception {
		final HashMap<String, String[]> fileInfo = new HashMap<String, String[]>();
		String rootFolder = packetFilePath;
		String ridFolder = "";
		Path path = Paths.get(rootFolder);
		RestClient.logInfo(contextKey, "path1:" + path);
		PersonaBiometricsAssembler.ensureAssembled(resident,
				PersonaBiometricsAssembler.hintsFromResident(resident), contextKey);
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
			idJson = IdJsonBuilder.generateIDJson(resident, fileInfo, contextKey, props, preregResponse, purpose,
					contextSchemaDetail, qualityScore, genarateValidCbeff, process);

			if (idJson != null && idJson.equalsIgnoreCase("Failed to generate biometric via mds"))
				return "Failed to generate biometric via mds";

			if (idJson == null || idJson.isBlank())
				return "Failed to generate ID JSON - biometric generation failed";

		} catch (Throwable e) {
			logger.error("generate", e);
			return e.getMessage();
		}
		JSONObject processMVEL = processMVEL(resident, idJson, process, contextSchemaDetail, contextKey);
		idJson = processMVEL.toString();

		CommonUtil.write(Paths.get(ridFolder + ID_JSON), idJson.getBytes());
		String metadataJson = PacketMetadataBuilder.generateMetaDataJson(resident, preregId, machineId, centerId,
				fileInfo, contextKey, contextSchemaDetail);
		CommonUtil.write(Paths.get(ridFolder + PACKET_META_INFO_JSON), metadataJson.getBytes());


		String evidenceJson = EvidenceJsonBuilder.generateEvidenceJson(resident, fileInfo, contextKey, props,
				contextSchemaDetail);
		CommonUtil.write(Paths.get(rid_evidence_folder + ID_JSON), evidenceJson.getBytes());
		CommonUtil.write(Paths.get(rid_evidence_folder + PACKET_META_INFO_JSON), metadataJson.getBytes());


		CommonUtil.write(Paths.get(rid_optional_folder + ID_JSON), evidenceJson.getBytes());
		CommonUtil.write(Paths.get(rid_optional_folder + PACKET_META_INFO_JSON), metadataJson.getBytes());

		idJson = PacketMetadataBuilder.genRID_PacketTypeJson(source, process, "id", contextSchemaDetail);
		CommonUtil.write(Paths.get(processFolder + File.separator + "/rid_id.json"), idJson.getBytes());
		idJson = PacketMetadataBuilder.genRID_PacketTypeJson(source, process, EVIDENCE, contextSchemaDetail);
		CommonUtil.write(Paths.get(processFolder + File.separator + "/rid_evidence.json"), idJson.getBytes());
		idJson = PacketMetadataBuilder.genRID_PacketTypeJson(source, process, "optional", contextSchemaDetail);
		CommonUtil.write(Paths.get(processFolder + File.separator + "/rid_optional.json"), idJson.getBytes());

		return "Success";

	}

	public JSONObject generateCRVSField(String source, ResidentModel resident, String process, String machineId,
			String centerId, String contextKey, Properties props,
			String RID, boolean validateToken, String uin)
			throws JSONException, Exception {
		ContextSchemaDetail contextSchemaDetail = getSchema(contextKey);
		JSONObject requestNode = new JSONObject();
		requestNode.put("id", RID);
		requestNode.put("refId", centerId + "_" + machineId);
		requestNode.put("offlineMode", false);
		requestNode.put("process", process);
		requestNode.put("source", source);
		requestNode.put("schemaVersion", contextSchemaDetail.getSchemaVersion());


		JSONObject fieldsString = CrvsIdJsonBuilder.generateCRVSIDJson(resident, contextKey, props, contextSchemaDetail,
				validateToken, uin);
		requestNode.put("fields", fieldsString);


		requestNode.put("metaInfo", PacketMetadataBuilder.generateMetaInfoJson(resident, process, RID, machineId,
				centerId, contextKey, contextSchemaDetail));


		JSONArray auditsArray = new JSONArray();
		auditsArray.put(PacketMetadataBuilder.generateAuditNode(RID));
		requestNode.put("audits", auditsArray);

		requestNode.put("schemaJson", MosipMasterData.getIDSchemaALL(contextKey));

		return requestNode;

	}

	private JSONObject processMVEL(ResidentModel resident, String idJson, String process,
			ContextSchemaDetail contextSchemaDetail, String contextKey) {
		Map<String, Object> allIdentityDetails = new LinkedHashMap<String, Object>();
		Map<String, DocumentDto> documents = PacketTemplateDocuments.buildDocumentMap(resident, idJson,
				contextSchemaDetail);
		allIdentityDetails.putAll(documents);
		JSONObject json = extractIdentityJson(idJson, contextKey, resident);
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
			if (s.getType().equalsIgnoreCase(DOCUMENTTYPE) || s.getType().equalsIgnoreCase(BIOMETRICSTYPE)) {
				continue;
			}
			if (PacketJsonSupport.isNewRegistrationProcess(process)
					&& PacketJsonSupport.isResidentUinField(s.getId(), contextKey)) {
				json.remove(s.getId());
				continue;
			}
			List<SchemaRule> rule = s.getRequiredOn();
			if (rule != null) {
				for (SchemaRule sr : rule) {
					RestClient.logInfo(contextKey, "rule:" + sr);
					boolean bval = MosipMasterData.executeMVEL(sr.getExpr(), (Object) allIdentityDetails);
					RestClient.logInfo(contextKey, "rule:result=" + bval);
					if (!bval) {
						json.put(s.getId(), JSONObject.NULL);
					}
				}
			}
		}

		if (PacketJsonSupport.isNewRegistrationProcess(process)) {
			PacketJsonSupport.omitResidentUin(json, contextKey);
		}

		if (!json.has("identity")) {
			JSONObject identityWrapper = new JSONObject();
			identityWrapper.put("identity", json.toMap());
			json = identityWrapper; 
		}
		return json;
	}

	private JSONObject extractIdentityJson(String idJson, String contextKey, ResidentModel resident) {
		if (idJson == null || idJson.isBlank()) {
			RestClient.logInfo(contextKey, "ID JSON is blank; continuing with empty identity object");
			return new JSONObject();
		}
		try {
			JSONObject root = new JSONObject(idJson);
			JSONObject identity = root.optJSONObject(IDENTITY);
			if (identity == null) {
				RestClient.logInfo(contextKey, "ID JSON does not contain identity; continuing with empty identity object");
				return new JSONObject();
			}
			return identity;
		} catch (JSONException e) {
			logger.warn("Invalid ID JSON while processing MVEL. residentId={}", resident != null ? resident.getId() : null, e);
			RestClient.logInfo(contextKey, "Invalid ID JSON detected; continuing with empty identity object");
			return new JSONObject();
		}
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
		allIdentityDetails.put("updatableFields", null);
		allIdentityDetails.put("updatableFieldGroups", null);

		return allIdentityDetails;
	}

	public static boolean processGender(MosipIDSchema s, ResidentModel resident, JSONObject identity,
			java.util.Hashtable<String, List<MosipGenderModel>> genderTypesLang,
			java.util.Hashtable<String, List<DynamicFieldModel>> dynaFields, String contextKey) {
		return IdJsonBuilder.processGender(s, resident, identity, genderTypesLang, dynaFields, contextKey);
	}

	public static Pair<String, String> processAddresslines(MosipIDSchema s, ResidentModel resident, JSONObject identity,
			String contextKey) {
		return IdJsonBuilder.processAddresslines(s, resident, identity, contextKey);
	}

	public static String generateDefaultAttributes(MosipIDSchema schemaItem, ResidentModel resident,
			JSONObject identity, String contextKey) {
		return IdJsonBuilder.generateDefaultAttributes(schemaItem, resident, identity, contextKey);
	}

	public static Boolean processDynamicFields(MosipIDSchema s, JSONObject identity, ResidentModel resident,
			String contextKey) {
		return IdJsonBuilder.processDynamicFields(s, identity, resident, contextKey);
	}

	public static void main(String[] args) throws Exception {
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
