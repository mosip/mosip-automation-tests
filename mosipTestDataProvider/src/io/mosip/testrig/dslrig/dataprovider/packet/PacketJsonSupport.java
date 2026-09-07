package io.mosip.testrig.dslrig.dataprovider.packet;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * JSON node helpers for packet identity and biometric metadata (extracted from
 * {@code PacketTemplateProvider}).
 */
public final class PacketJsonSupport {

	private static final String INTRODUCER = "introducer";
	private static final String APPLICANT_AUTH = "applicant-auth";
	private static final String LANGUAGE = "language";
	private static final String VALUE = "value";

	private PacketJsonSupport() {
	}

	public static boolean isNewRegistrationProcess(String process) {
		if (process == null || process.isBlank()) {
			return false;
		}
		String normalized = process.trim();
		return "NEW".equalsIgnoreCase(normalized) || "CRVS_NEW".equalsIgnoreCase(normalized);
	}

	public static boolean isResidentUinField(String schemaId, String contextKey) {
		if (schemaId == null || schemaId.isBlank()) {
			return false;
		}
		if ("UIN".equalsIgnoreCase(schemaId)) {
			return true;
		}
		Object mapped = VariableManager.getVariableValue(contextKey, "uin");
		return mapped != null && schemaId.equals(mapped.toString());
	}

	public static void omitResidentUin(JSONObject identity, String contextKey) {
		if (identity == null) {
			return;
		}
		identity.remove("UIN");
		Object mapped = VariableManager.getVariableValue(contextKey, "uin");
		if (mapped != null && !mapped.toString().isBlank()) {
			identity.remove(mapped.toString());
		}
	}

	public static JSONObject constructExceptnNode(BioModality modality) {
		JSONObject node = new JSONObject();
		node.put("type", modality.getType());
		node.put("missingBiometric", DataProviderConstants.getschemaName(modality.getSubType()));
		node.put("reason", modality.getReason());
		node.put("exceptionType", modality.getExceptionType());
		node.put("individualType", "applicant");
		return node;
	}

	public static JSONObject constructBioMetaNode() {
		JSONObject node = new JSONObject();
		node.put("numRetry", 1);
		node.put("forceCaptured", false);
		node.put("birindex", "4c099c1f-4fb2-4de3-8a2f-928f79430e9b");
		return node;
	}

	public static JSONObject constructBioMetaData(ResidentModel resident, JSONObject identity) {

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

	public static JSONObject constructBioException(ResidentModel resident, JSONObject identity,
			List<MosipIDSchema> mosipIDSchema) {

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

	public static JSONObject constructBioException(ResidentModel resident, JSONObject identity) {

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

	public static JSONObject updateSimpleType(String id, JSONObject identity, String primValue, String secValue,
			String primLang, String secLang, String thirdLang, String contextKey) {

		if (primValue == null)
			primValue = "Some Text Value";

		if ((secValue == null || secValue.equals("")) && secLang != null && !secLang.equals(""))
			secValue = Translator.translate(secLang, primValue, contextKey);

		String thirdValue = "";
		if (thirdLang != null && !thirdLang.equals(""))
			thirdValue = Translator.translate(thirdLang, primValue, contextKey);


		JSONArray ar = new JSONArray();
		JSONObject o = new JSONObject();
		o.put(LANGUAGE, primLang);
		if (primValue != null && primValue.equals(""))
			o.put(VALUE, JSONObject.NULL);
		else
			o.put(VALUE, primValue);
		ar.put(o);

		if (secLang != null) {
			o = new JSONObject();
			o.put(LANGUAGE, secLang);
			if (secValue != null && secValue.equals(""))
				o.put(VALUE, JSONObject.NULL);
			else
				o.put(VALUE, secValue);
			ar.put(o);
		}
		if (thirdLang != null) {
			o = new JSONObject();
			o.put(LANGUAGE, thirdLang);
			if (thirdValue.equals(""))
				o.put(VALUE, JSONObject.NULL);
			else
				o.put(VALUE, thirdValue);
			ar.put(o);
		}

		identity.put(id, ar);
		return identity;
	}

	public static JSONObject updateSimpleTypeString(String id, JSONObject identity, String primValue, String secValue,
			String primLang, String secLang, String thirdLang, String contextKey) {

		if (primValue == null)
			primValue = "Some Text Value";

		if ((secValue == null || secValue.equals("")) && secLang != null && !secLang.equals(""))
			secValue = Translator.translate(secLang, primValue, contextKey);

		String thirdValue = "";
		if (thirdLang != null && !thirdLang.equals(""))
			thirdValue = Translator.translate(thirdLang, primValue, contextKey);


		JSONArray ar = new JSONArray();
		JSONObject o = new JSONObject();
		o.put(LANGUAGE, primLang);
		if (primValue != null && primValue.equals(""))
			o.put(VALUE, JSONObject.NULL);
		else
			o.put(VALUE, primValue);
		ar.put(o);

		if (secLang != null) {
			o = new JSONObject();
			o.put(LANGUAGE, secLang);
			if (secValue != null && secValue.equals(""))
				o.put(VALUE, JSONObject.NULL);
			else
				o.put(VALUE, secValue);
			ar.put(o);
		}
		if (thirdLang != null) {
			o = new JSONObject();
			o.put(LANGUAGE, thirdLang);
			if (thirdValue.equals(""))
				o.put(VALUE, JSONObject.NULL);
			else
				o.put(VALUE, thirdValue);
			ar.put(o);
		}

		identity.put(id, ar.toString());
		return identity;
	}
}
