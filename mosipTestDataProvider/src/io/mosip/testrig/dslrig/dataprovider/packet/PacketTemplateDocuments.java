package io.mosip.testrig.dslrig.dataprovider.packet;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import io.mosip.testrig.dslrig.dataprovider.models.ContextSchemaDetail;
import io.mosip.testrig.dslrig.dataprovider.models.DocumentDto;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;

/**
 * Document field mapping for packet ID JSON (extracted from {@code PacketTemplateProvider}).
 */
public final class PacketTemplateDocuments {

	private static final String IDENTITY = "identity";
	private static final String FORMAT = "format";
	private static final String VALUE = "value";

	private PacketTemplateDocuments() {
	}

	public static Map<String, DocumentDto> buildDocumentMap(ResidentModel resident, String idJson,
			ContextSchemaDetail contextSchemaDetail) {
		Map<String, DocumentDto> documents = new HashMap<>();
		if (resident.getDocuments() == null) {
			return documents;
		}
		JSONObject json = new JSONObject(idJson);
		json = json.getJSONObject(IDENTITY);
		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			if (!CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId())) {
				continue;
			}
			for (MosipDocument doc : resident.getDocuments()) {
				if (s.getSubType() != null
						&& doc.getDocCategoryCode().equalsIgnoreCase(s.getSubType())) {
					if (json.has(s.getId())) {
						JSONObject formateJson = json.getJSONObject(s.getId());
						DocumentDto documentDto = new DocumentDto();
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
}
