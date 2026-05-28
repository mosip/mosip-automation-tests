package io.mosip.testrig.dslrig.dataprovider.packet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.dataprovider.models.ContextSchemaDetail;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.MosipLocationModel;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.DemographicMissFieldUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * CRVS ID JSON builder (extracted from {@code PacketTemplateProvider}).
 */
public final class CrvsIdJsonBuilder {

	private static final String CHECKBOX = "checkbox";
	private static final String SIMPLETYPE = "simpleType";
	private static final String FIRSTNAME = "firstname";
	private static final double INVALID_SCHEMA = 2.5;
	private static final String LASTNAME = "lastname";
	private static final String MIDDLENAME = "middlename";

	private CrvsIdJsonBuilder() {
	}

	public static JSONObject generateCRVSIDJson(ResidentModel resident, String contextKey, Properties prop,
			ContextSchemaDetail contextSchemaDetail, boolean validateToken, String uin)
			throws JSONException, Exception {

		JSONObject identity = new JSONObject();

		String primaryLanguage = resident.getPrimaryLanguage();
		String secLanguage = resident.getSecondaryLanguage();
		Hashtable<String, MosipLocationModel> locations = resident.getLocation();
		Hashtable<String, MosipLocationModel> locations_seclang = resident.getLocation_seclang();

		Set<String> locationSet = locations.keySet();

		Set<String> locationSet_sec = null;
		if (locations_seclang != null)
			locationSet_sec = locations_seclang.keySet();

		List<String> lstMissedAttributesCRVS = resident.getMissAttributes();
		if (lstMissedAttributesCRVS != null && !lstMissedAttributesCRVS.isEmpty()) {
			lstMissedAttributesCRVS = DemographicMissFieldUtil
					.expandMissAttributeIds(new ArrayList<>(lstMissedAttributesCRVS), contextSchemaDetail.getSchema(),
							contextKey);
		}

		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			RestClient.logInfo(contextKey, s.toJSONString());

			if (!CommonUtil.isExists(contextSchemaDetail.getRequiredAttribs(), s.getId()))
				continue;

			if (lstMissedAttributesCRVS != null
					&& lstMissedAttributesCRVS.stream().anyMatch(v -> v.equalsIgnoreCase(s.getId()))) {
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

			if (VariableManager.getVariableValue(contextKey, "IDSchemaVersion") != null
					&& s.getId().equals(VariableManager.getVariableValue(contextKey, "IDSchemaVersion"))) {
				identity.put(s.getId(), contextSchemaDetail.getSchemaVersion());
				continue;
			}

			if (s.getId().contains("residenceStatus")) {
				VariableManager.setVariableValue(contextKey, "ID_OBJECT-residenceStatus",
						resident.getResidentStatus().getCode());
			}

			if (IdJsonBuilder.updateFromAdditionalAttribute(identity, s, resident, contextKey)) {
				continue;
			}
			if (IdJsonBuilder.processDynamicFields(s, identity, resident, contextKey)) {
				if (s.getId().contains("gender")) {
					Object rawValue = identity.get("gender");
					if (rawValue instanceof JSONArray) {
						identity.put("gender", rawValue.toString());
					}
				}
				continue;
			}

			if (s.getFieldCategory().equals("evidence") && s.getId().equals("nrcId")) {
				identity.put(s.getId(), resident.getNrcId().getNrcId());
				continue;
			}
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
						Pair<String, String> addrLines = IdJsonBuilder.processAddresslines(s, resident, identity,
								contextKey);
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
				for (String locKey : locationSet) {
					MosipLocationModel locModel = locations.get(locKey);

					if (s.getId().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase())
							|| (s.getSubType() != null
									&& s.getSubType().toLowerCase().endsWith(locModel.getHierarchyName().toLowerCase()))) {
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

				if (primaryValue == null || primaryValue.equals("")) {
					primaryValue = IdJsonBuilder.generateDefaultAttributes(s, resident, identity, contextKey);
					if (secLanguage != null) {
						secValue = Translator.translate(secLanguage, primaryValue, contextKey);
					}
				}

				if (s.getType().equals(SIMPLETYPE)) {

					PacketJsonSupport.updateSimpleTypeString(s.getId(), identity, primaryValue, secValue,
							primaryLanguage, secLanguage, resident.getThirdLanguage(), contextKey);

				} else {
					if (primaryValue.equals(""))
						identity.put(s.getId(), JSONObject.NULL);
					else
						identity.put(s.getId(), primaryValue);
				}
			}

		}
		if (validateToken == true
				&& VariableManager.getVariableValue(contextKey, "process").toString().contains("NEW")) {
			identity.put("introducerInfoToken", RestClient.getToken("crvs", contextKey));
		} else if (validateToken == true
				&& VariableManager.getVariableValue(contextKey, "process").toString().contains("DEATH")) {
			identity.put("deceasedInformer", RestClient.getToken("crvs", contextKey));
			identity.put("declaredAsDeceased", "Y");
			identity.put("UIN", uin);
			LocalDate today = LocalDate.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			String formattedDate = today.format(formatter);
			identity.put("deceasedDeclarationDate", formattedDate);

		}
		return identity;
	}
}
