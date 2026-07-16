package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class ConfigureMockAbis extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(ConfigureMockAbis.class);
	boolean isFound = false;

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	public void run() throws RigInternalError {
		String personaPath = null;
		boolean duplicate = false;
		List<String> inputList = null;
		List<String> hashProp = null;
		List<String> hashModality = new ArrayList<>();
		List<String> modalitysubTypeList = new ArrayList<>();
		String personaId = null;
		String vv = "hhhhhnjkj.kjkjk.mkmkmk";
		List<String> errorList = null;
		long delaysec = -1;
		String statusCode = null;
		String failureReason = null;

		long waitTimeFromActuator = 0;
		if (step.getParameters().size() >= 9 && step.getParameters().get(8).contains("true")) {
			waitTimeFromActuator = PacketUtility.getActuatorDelay();
			delaysec = TIME_IN_MILLISEC * waitTimeFromActuator;
		}
		if (step.getParameters().size() == 4) {
			personaId = step.getParameters().get(0);
			if (!personaId.equals("-1")) {
				if (step.getScenario().getResidentPersonaIdPro().get(personaId) == null) {
					logger.error("Persona id : [" + personaId + "] is not present is the system");
					this.hasError = true;
					throw new RigInternalError("Persona id : [" + personaId + "] is not present is the system");
				}
				personaPath = step.getScenario().getResidentPersonaIdPro().get(personaId).toString();
				if (StringUtils.isBlank(personaPath)) {
					this.hasError = true;
					throw new RigInternalError(
							"PersonaPath is not present in the system for persona id : [" + personaId + "]");
				}
			} else {
				for (String personaid : step.getScenario().getResidentPersonaIdPro().stringPropertyNames()) {
					personaId = personaid;
					personaPath = step.getScenario().getResidentPersonaIdPro().getProperty(personaid);
					break;
				}
			}
		} else if (step.getParameters().size() >= 6) {
			personaPath = resolveScenarioVariable(step.getParameters().get(4));
			isFound = referencesScenarioVariable(step.getParameters().get(5));
		} else {
			this.hasError = true;
			throw new RigInternalError(
					"missing input param [personaid,List<String> modalitySubType,duplicate,List<String> hashModality]");
		}
		inputList = PacketUtility.getParamsArg(step.getParameters().get(1), "@@"); 
		inputList.stream().forEach(key -> modalitysubTypeList.add(key));
		duplicate = Boolean.parseBoolean(step.getParameters().get(2)); 
		hashProp = PacketUtility.getParamsArg(step.getParameters().get(3), "@@"); 
		hashProp.stream().forEach(key -> hashModality.add(key));

		if (step.getParameters().size() >= 7 && step.getParameters().get(6).contains("delay")) {


			waitTimeFromActuator = PacketUtility.getActuatorDelay();
			delaysec = TIME_IN_MILLISEC * waitTimeFromActuator;
		} else if (step.getParameters().size() >= 7) {

			delaysec = Long.parseLong(step.getParameters().get(6));
		}

		if (step.getParameters().size() >= 7) {

			if (step.getParameters().get(7).contains("@@")) {
				errorList = PacketUtility.getParamsArg(step.getParameters().get(7), "@@");
				statusCode = errorList.get(0);
				failureReason = errorList.get(1);
			} else {
				statusCode = step.getParameters().get(7);
			}
		}
		JSONArray jsonOutterReq = buildMockRequest(personaPath, duplicate, hashModality, modalitysubTypeList, personaId,
				delaysec, statusCode, failureReason);
		packetUtility.setMockabisExpectaion(jsonOutterReq, step.getScenario().getCurrentStep(), step);

	}

	private JSONArray buildMockRequest(String personaPath, boolean duplicate, List<String> hashModality,
			List<String> modalitysubTypeList, String personaId, long delaySec, String statusCode,
			String failureReason) throws RigInternalError {
		Map<String, String> modalityHashValueMap = new HashMap<>();
		if (isFound) {
			modalityHashValueMap.clear();
			String hashValue = resolveScenarioVariable(step.getParameters().get(5));
			logger.info(hashValue);
			if (!StringUtils.isBlank(hashValue)) {
				for (String entry : hashValue.split(",")) {
					String[] keyAndValue = entry.split("=", 2);
					if (keyAndValue.length < 2) {
						continue;
					}
					String key = keyAndValue[0].trim();
					String value = keyAndValue[1].trim();
					if (key.length() > 0 && key.charAt(0) == '{') {
						key = key.substring(1);
					}
					if (value.length() > 0 && value.charAt(value.length() - 1) == '}') {
						value = value.substring(0, value.length() - 1);
					}
					modalityHashValueMap.put(key, value);
				}
			}
		} else {
			modalityHashValueMap.clear();
			Map<String, String> scoped = hashtable.get(scenarioScopedPersonaKey(step.getScenario().getId(), personaId));
			if (scoped != null) {
				modalityHashValueMap = scoped;
			}
		}

		JSONArray outterReq = new JSONArray();
		JSONObject jsonOutterReq = new JSONObject();
		jsonOutterReq.put("delaySec", delaySec);
		jsonOutterReq.put("duplicate", duplicate);
		JSONArray modalities = new JSONArray();
		if (!modalitysubTypeList.isEmpty() && modalitysubTypeList.size() > 0)
			modalitysubTypeList.stream().forEach(modality -> modalities.put(modality));
		jsonOutterReq.put("modalities", modalities);
		jsonOutterReq.put("operation", "Identify");
		jsonOutterReq.put("personaPath", personaPath);

		jsonOutterReq.put("statusCode", statusCode);
		jsonOutterReq.put("failureReason", failureReason);

		JSONArray refHashs = new JSONArray();
		if (!hashModality.isEmpty() && hashModality.size() > 0) {
			for (String hash : hashModality) {
				refHashs.put(modalityHashValueMap.get(hash));
			}
		}
		jsonOutterReq.put("refHashs", refHashs);
		outterReq.put(jsonOutterReq);
		return outterReq;
	}
}
