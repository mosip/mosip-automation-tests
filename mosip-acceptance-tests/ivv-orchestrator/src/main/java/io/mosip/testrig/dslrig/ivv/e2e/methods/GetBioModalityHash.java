package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.e2e.constant.E2EConstants;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;

@Scope("prototype")
@Component
public class GetBioModalityHash extends BaseTestCaseUtil implements StepInterface {
	Map<String, String> modalityHashValueMap = new HashMap<>();
	static Logger logger = Logger.getLogger(GetBioModalityHash.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	public void run() throws RigInternalError {
		List<String> modalitySubTypeList = new ArrayList<>();
		List<String> inputList = null;
		String personaPath = null;
		String personaId = null;
		if (step.getParameters().size() == 2) {
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
				for (String id : step.getScenario().getResidentPersonaIdPro().stringPropertyNames()) {
					personaPath = step.getScenario().getResidentPersonaIdPro().getProperty(id);
					personaId = id;
					break;
				}
			}
		} else if (!step.getParameters().isEmpty() && step.getParameters().size() == 3) {
			personaPath = step.getParameters().get(2);
			if (personaPath.startsWith("$$")) {
				personaPath = step.getScenario().getVariables().get(personaPath);
			}
		} else {
			this.hasError = true;
			throw new RigInternalError("missing input param [personaid,List<String> modalitySubType]");
		}
		inputList = PacketUtility.getParamsArg(step.getParameters().get(1), "@@");
		inputList.stream().forEach(key -> modalitySubTypeList.add(key));

		String modalityKeyTogetHashValue = null;
		for (String modalitysubType : modalitySubTypeList) {
			List<String> modalityHashList = new ArrayList<>();
			switch (modalitysubType) {
			case E2EConstants.FACEBIOTYPE:
				modalityHashList.add(E2EConstants.FACEHASHFETCH);
				modalityKeyTogetHashValue = E2EConstants.FACEHASHFETCH;
				break;
			case E2EConstants.IRISBIOTYPE_LEFT:
			case E2EConstants.IRISBIOTYPE_RIGHT:
				modalityHashList.add(E2EConstants.IRISHASHFETCH);
				modalityKeyTogetHashValue = (modalitysubType.equalsIgnoreCase("left")) ? E2EConstants.LEFT_EYE_HASH
						: E2EConstants.RIGHT_EYE_HASH;
				break;
			case E2EConstants.RIGHT_INDEX:
			case E2EConstants.RIGHT_LITTLE:
			case E2EConstants.RIGHT_RING:
			case E2EConstants.RIGHT_MIDDLE:
			case E2EConstants.LEFT_INDEX:
			case E2EConstants.LEFT_LITTLE:
			case E2EConstants.LEFT_RING:
			case E2EConstants.LEFT_MIDDLE:
			case E2EConstants.LEFT_THUMB:
			case E2EConstants.RIGHT_THUMB:
				modalityHashList.add(E2EConstants.FINGERHASHFETCH);
				modalityKeyTogetHashValue = modalitysubType;
				break;
			default:
				this.hasError = true;
				throw new RigInternalError("Given Bio subType:[" + modalitysubType + "] is not valid");
			}
			String hashResponse = packetUtility.retrieveBiometric(personaPath, modalityHashList, step);
			if (hashResponse != null && !hashResponse.isEmpty() && modalityKeyTogetHashValue != null) {
				String hashValue = JsonPrecondtion.getValueFromJson(hashResponse, modalityKeyTogetHashValue);
				modalityHashValueMap.put(modalitysubType, hashValue);
				logger.info("modalityHashValueMap" + modalityHashValueMap);
			}
		}
		if (step.getOutVarName() != null) {
			step.getScenario().getVariables().put(step.getOutVarName(), modalityHashValueMap.toString());
			logger.info(step.getScenario().getVariables().get(modalityHashValueMap.toString()));
		} else {
			hashtable.put(personaId, modalityHashValueMap);
		}

	}

}
