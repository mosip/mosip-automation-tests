package io.mosip.testrig.dslrig.dataprovider.packet;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(PacketTemplateDocuments.class);

	private static final String IDENTITY = "identity";
	private static final String FORMAT = "format";
	private static final String VALUE = "value";

	private PacketTemplateDocuments() {
	}

	public static Map<String, DocumentDto> buildDocumentMap(ResidentModel resident, String idJson,
			ContextSchemaDetail contextSchemaDetail) {
		Map<String, DocumentDto> documents = new HashMap<>();
		if (resident == null || contextSchemaDetail == null || contextSchemaDetail.getSchema() == null
				|| resident.getDocuments() == null || idJson == null || idJson.isBlank()) {
			return documents;
		}
		JSONObject json;
		try {
			JSONObject rawJson = new JSONObject(idJson);
			json = rawJson.optJSONObject(IDENTITY);
			if (json == null) {
				return documents;
			}
		} catch (JSONException e) {
			logger.warn("Skipping document map build due to invalid idJson. residentId={}", resident.getId(), e);
			return documents;
		}
		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			if (s == null || s.getId() == null || !CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId())) {
				continue;
			}
			for (MosipDocument doc : resident.getDocuments()) {
				if (doc == null || doc.getDocCategoryCode() == null) {
					continue;
				}
				if (s.getSubType() != null
						&& doc.getDocCategoryCode().equalsIgnoreCase(s.getSubType())) {
					if (json.has(s.getId())) {
						JSONObject formateJson = json.optJSONObject(s.getId());
						if (formateJson == null) {
							continue;
						}
						DocumentDto documentDto = new DocumentDto();
						documentDto.setCategory(doc.getDocCategoryCode());
						documentDto.setFormat(formateJson.optString(FORMAT));
						documentDto.setType(formateJson.optString("type"));
						documentDto.setValue(formateJson.optString(VALUE));
						documents.put(s.getId(), documentDto);
					}
				}
			}
		}
		return documents;
	}
}
