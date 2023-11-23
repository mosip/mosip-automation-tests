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

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;

public class ConfigureMockAbis extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(ConfigureMockAbis.class);
	boolean isFound = false;
	
	static {
		if (ConfigManager.IsDebugEnabled())
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
//		Long waitTime = DEFAULT_WAIT_TIME;

		// "e2e_configureMockAbis(-1,Right IndexFinger,true,Right
		// IndexFinger,$$personaFilePath,$$modalityHashValue,-1,@@Duplicate)"

		if (step.getParameters().size() >= 9 && step.getParameters().get(8).contains("true")) {
			waitTimeFromActuator = PacketUtility.getActuatorDelay();
			delaysec = TIME_IN_MILLISEC * waitTimeFromActuator;
		}

		if (step.getParameters().size() == 4) { /// id=878787877
			personaId = step.getParameters().get(0);
			if (!personaId.equals("-1")) {
				if (step.getScenario().getResidentPersonaIdPro().get(personaId) == null) {
					logger.error("Persona id : [" + personaId + "] is not present is the system");
					this.hasError=true;throw new RigInternalError("Persona id : [" + personaId + "] is not present is the system");
				}
				personaPath = step.getScenario().getResidentPersonaIdPro().get(personaId).toString();
				if (StringUtils.isBlank(personaPath)) {
					this.hasError=true;
					throw new RigInternalError(
							"PersonaPath is not present in the system for persona id : [" + personaId + "]");}
			} else {
				for (String personaid : step.getScenario().getResidentPersonaIdPro().stringPropertyNames()) {
					personaId = personaid;
					personaPath = step.getScenario().getResidentPersonaIdPro().getProperty(personaid);
					break;
				}
			}
		} else if (step.getParameters().size() >= 6) {

			personaPath = step.getParameters().get(4);
			if (personaPath.startsWith("$$")) {
				personaPath = step.getScenario().getVariables().get(personaPath);
				isFound = true;
			}
		} else {this.hasError=true;
			throw new RigInternalError(
					"missing input param [personaid,List<String> modalitySubType,duplicate,List<String> hashModality]");
		}
		inputList = PacketUtility.getParamsArg(step.getParameters().get(1), "@@"); // List<String> ModalitysubTypeList
		inputList.stream().forEach(key -> modalitysubTypeList.add(key));
		duplicate = Boolean.parseBoolean(step.getParameters().get(2)); // boolean isDuplicate
		hashProp = PacketUtility.getParamsArg(step.getParameters().get(3), "@@"); // List<String> hashModality
		hashProp.stream().forEach(key -> hashModality.add(key));

		if (step.getParameters().size() >= 7 && step.getParameters().get(6).contains("delay")) {
			// If it is true , read the mockAbis delay time from actuator eg: Packet
			// reprocessing interval
			waitTimeFromActuator = PacketUtility.getActuatorDelay();
			delaysec = TIME_IN_MILLISEC * waitTimeFromActuator;
		} else if (step.getParameters().size() >= 7) {
			// Otherwise , read value which is passed from dsl step
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
		// hashtable.clear();

	}

	private JSONArray buildMockRequest(String personaPath, boolean duplicate, List<String> hashModality,
			List<String> modalitysubTypeList, String personaId, long delaySec, String statusCode, String failureReason) {
		Map<String, String> modalityHashValueMap = new HashMap<>();
		if (isFound) {
			// $$modalityHashValue
			modalityHashValueMap.clear();
			String _hashValue = step.getParameters().get(5);
			if (_hashValue.startsWith("$$"))
				_hashValue = step.getScenario().getVariables().get(_hashValue);
			logger.info(_hashValue);
			String[] keyValue = _hashValue.split(",");
			if (keyValue != null) {
				for (String s : keyValue) {
					String[] arr = s.split("=");
					if (arr.length > 1) {
						String key = (arr[0].trim().startsWith("{")) ? arr[0].trim().substring(1) : arr[0].trim();
						String value = (arr[1].trim().endsWith("}"))
								? arr[1].trim().substring(0, arr[1].trim().length() - 1)
								: arr[1].trim();
						modalityHashValueMap.put(key, value);
					}
				}
			}

		} else {
			modalityHashValueMap.clear();
			modalityHashValueMap = hashtable.get(personaId);
		}
		// Map<String, String> modalityHashValueMap = hashtable.get(personaId);
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
		/*
		 * if (!hashModality.isEmpty() && hashModality.size() > 0)
		 * hashModality.stream().forEach(hashModal ->
		 * refHashs.put(modalityHashValueMap.get(hashModal)));
		 */
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
