package io.mosip.testrig.dslrig.dataprovider.packet;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.PacketTemplateProvider;
import io.mosip.testrig.dslrig.dataprovider.models.ContextSchemaDetail;
import io.mosip.testrig.dslrig.dataprovider.models.MosipDocument;
import io.mosip.testrig.dslrig.dataprovider.models.MosipIDSchema;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.CommonUtil;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.util.Translator;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Evidence packet ID JSON builder (extracted from {@code PacketTemplateProvider}).
 */
public final class EvidenceJsonBuilder {

	private static final Logger logger = LoggerFactory.getLogger(EvidenceJsonBuilder.class);

	private static final String IDENTITY = "identity";
	private static final String EVIDENCE = "evidence";
	private static final String DOCUMENTTYPE = "documentType";
	private static final String FORMAT = "format";
	private static final String VALUE = "value";
	private static final String DTYPE = " dType=";
	private static final String CAT = " cat=";
	private static final String CHECKBOX = "checkbox";
	private static final String SIMPLETYPE = "simpleType";
	private static final String LANGUAGE = "language";

	private EvidenceJsonBuilder() {
	}

	public static String generateEvidenceJson(ResidentModel resident, HashMap<String, String[]> fileInfo,
			String contextKey, Properties prop, ContextSchemaDetail contextSchemaDetail) {

		VariableManager.setVariableValue(contextKey, "INTRODUCER_AVAILABILITY", "false");
		JSONObject identity = new JSONObject();
		List<String> missList = resident.getMissAttributes();

		for (MosipIDSchema s : contextSchemaDetail.getSchema()) {
			RestClient.logInfo(contextKey, s.toJSONString());
			String primVal = "";
			String secVal = "";

			if (s.getFieldCategory() != null && ((s.getInputRequired() != null && s.getInputRequired()) ||
					(s.getRequired() != null && s.getRequired())) && s.getFieldCategory().equals(EVIDENCE)) {

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
							String[] v = fileInfo.get(PacketTemplateProvider.RID_EVIDENCE);
							v[1] = s.getId() + ".pdf";
							fileInfo.put(PacketTemplateProvider.RID_EVIDENCE, v);
							o.put(VALUE, s.getId());

							identity.put(s.getId(), o);

							String outFile = fileInfo.get(PacketTemplateProvider.RID_EVIDENCE)[0] + "/"
									+ fileInfo.get(PacketTemplateProvider.RID_EVIDENCE)[1];
							try {

								CommonUtil.copyFileWithBuffer(Paths.get(docFile), Paths.get(outFile));
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
				} else if (PacketJsonSupport.isResidentUinField(s.getId(), contextKey)) {
					Object processValue = VariableManager.getVariableValue(contextKey, "process");
					String process = processValue == null ? "" : processValue.toString();
					if (!PacketJsonSupport.isNewRegistrationProcess(process)) {
						if (resident.getUIN() == null || resident.getUIN().equals(""))
							identity.put(s.getId(), JSONObject.NULL);
						else
							identity.put(s.getId(), resident.getUIN());
					}
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

						PacketJsonSupport.updateSimpleType(s.getId(), identity, primValue, secValue,
								resident.getPrimaryLanguage(), resident.getSecondaryLanguage(),
								resident.getThirdLanguage(), contextKey);

					}
					continue;
				} else if (s.getId().toLowerCase().contains("consent")) {
					Object consentValue = VariableManager.getVariableValue(contextKey, "consent");
					String consentFlag = consentValue != null ? consentValue.toString() : "";
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

					PacketJsonSupport.updateSimpleType(s.getId(), identity, primVal, secVal,
							resident.getPrimaryLanguage(), resident.getSecondaryLanguage(), resident.getThirdLanguage(),
							contextKey);

				} else {
					identity.put(s.getId(), primVal.equals("") ? JSONObject.NULL : primVal);
				}
			}
		}
		Object processValue = VariableManager.getVariableValue(contextKey, "process");
		String process = processValue == null ? "" : processValue.toString();
		if (PacketJsonSupport.isNewRegistrationProcess(process)) {
			PacketJsonSupport.omitResidentUin(identity, contextKey);
		}
		JSONObject retObject = new JSONObject();
		retObject.put(IDENTITY, identity);
		return retObject.toString();

	}
}
