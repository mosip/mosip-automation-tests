package io.mosip.testrig.dslrig.dataprovider.packet;

import java.util.HashMap;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.dataprovider.models.ContextSchemaDetail;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Packet metadata and audit JSON builders (extracted from {@code PacketTemplateProvider}).
 */
public final class PacketMetadataBuilder {

	private static final String IDENTITY = "identity";
	private static final String DOCUMENTTYPE = "documentType";
	private static final String DOCUMENTS = "documents";
	private static final String DOCFILE = "docFIle=";
	private static final String DTYPE = " dType=";
	private static final String CAT = " cat=";
	private static final String LABEL = "label";
	private static final String VALUE = "value";

	private PacketMetadataBuilder() {
	}

	public static String generateMetaDataJson(ResidentModel resident, String preRegistrationId, String machineId,
			String centerId, HashMap<String, String[]> fileInfo, String contextKey,
			ContextSchemaDetail contextSchemaDetail) {

		String templateMetaJsonPath = System.getProperty("java.io.tmpdir")
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
		identity = PacketJsonSupport.constructBioException(resident, identity, contextSchemaDetail.getSchema());
		identity = PacketJsonSupport.constructBioMetaData(resident, identity);
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

	public static JSONObject generateMetaInfoJson(ResidentModel resident, String process, String RegistrationId,
			String machineId, String centerId, String contextKey, ContextSchemaDetail contextSchemaDetail) {
		JSONObject metaInfo = new JSONObject();
		JSONArray metaDataArray = new JSONArray();
		String templateMetaJsonPath = System.getProperty("java.io.tmpdir")
				+ VariableManager.getVariableValue(contextKey, "templateIDMeta").toString().trim();

		String templateIdentityStr = CommonUtil.readFromJSONFile(templateMetaJsonPath);
		JSONObject templateIdentity = new JSONObject(templateIdentityStr).getJSONObject(IDENTITY);

		JSONObject obj = new JSONObject();
		obj.put("label", "registrationType");
		obj.put("value", process);
		metaDataArray.put(obj);

		if (machineId != null && !machineId.isEmpty()) {
			obj = new JSONObject();
			obj.put("label", "machineId");
			obj.put("value", machineId);
			metaDataArray.put(obj);
		}

		if (centerId != null && !centerId.isEmpty()) {
			obj = new JSONObject();
			obj.put("label", "centerId");
			obj.put("value", centerId);
			metaDataArray.put(obj);
		}
		JSONArray operationsData = templateIdentity.getJSONArray("operationsData");
		if (process != null && process.contains("CRVS")) {
			for (int i = 0; i < operationsData.length(); i++) {
				JSONObject object = operationsData.getJSONObject(i);
				if ("officerId".equals(object.getString("label"))) {
					object.put("value", VariableManager.getVariableValue(contextKey, "mosip.test.regclient.userid"));
					break;
				}
			}
		}


		metaInfo.put("metaData", metaDataArray.toString());

		metaInfo.put("registrationId", RegistrationId != null ? RegistrationId : JSONObject.NULL);
		metaInfo.put("operationsData", operationsData.toString());
		metaInfo.put("capturedRegisteredDevices",
				templateIdentity.getJSONArray("capturedRegisteredDevices").toString());
		metaInfo.put("creationDate", CommonUtil.getUTCDateTime(null));

		return metaInfo;
	}

	public static JSONObject generateAuditNode(String RID) {
		JSONObject auditNode = new JSONObject();
		auditNode.put("uuid", UUID.randomUUID().toString());
		auditNode.put("createdAt", CommonUtil.getUTCDateTime(null));
		auditNode.put("eventId", "REG-EVT-066");
		auditNode.put("eventName", "PACKET_CREATION_SUCCESS");
		auditNode.put("eventType", "USER");
		auditNode.put("hostName", "DESKTOP-JL4BAEV");
		auditNode.put("hostIp", "localhost");
		auditNode.put("applicationId", "REG");
		auditNode.put("applicationName", "REGISTRATION");
		auditNode.put("sessionUserId", "crvs");
		auditNode.put("sessionUserName", "crvs");
		auditNode.put("id", RID);
		auditNode.put("idType", "REGISTRATION_ID");
		auditNode.put("createdBy", "crvs");
		auditNode.put("moduleName", "Packet Handler");
		auditNode.put("moduleId", "REG-MOD-117");
		auditNode.put("description", "Packet Successfully Created");
		auditNode.put("actionTimeStamp", CommonUtil.getUTCDateTime(null));

		return auditNode;
	}

	public static String genRID_PacketTypeJson(String src, String process, String packetType,
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
		retObject.put("providername", "PacketWriterImpl");

		return retObject.toString();

	}
}
