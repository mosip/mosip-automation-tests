package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;

public class GetBioModalityHash extends BaseTestCaseUtil implements StepInterface {
	Map<String, String> modalityHashValueMap = new HashMap<>();
	Logger logger = Logger.getLogger(GetBioModalityHash.class);

	public void run() throws RigInternalError {
		List<String> modalitySubTypeList = new ArrayList<>();
		List<String> inputList=null;
		String personaPath = null;
		String personaId = null;
		if (step.getParameters().size() == 2) { /// id=878787877
			personaId = step.getParameters().get(0);
			if (!personaId.equals("-1")) {
				if (residentPersonaIdPro.get(personaId) == null) {
					logger.error("Persona id : [" + personaId + "] is not present is the system");
					throw new RigInternalError("Persona id : [" + personaId + "] is not present is the system");
				}
				personaPath = residentPersonaIdPro.get(personaId).toString();
				if (StringUtils.isBlank(personaPath))
					throw new RigInternalError(
							"PersonaPath is not present in the system for persona id : [" + personaId + "]");
			} else {
				for (String id : residentPersonaIdPro.stringPropertyNames()) {
					personaPath = residentPersonaIdPro.getProperty(id);
					personaId = id;
					break;
				}
			}
		} else
			throw new RigInternalError("missing input param [personaid,List<String> modalitySubType]");
		  inputList = PacketUtility.getParamsArg(step.getParameters().get(1), "@@"); // List<String> ModalitysubTypeList
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
					modalityKeyTogetHashValue = (modalitysubType.equalsIgnoreCase("left")) ? E2EConstants.LEFT_EYE_HASH: E2EConstants.RIGHT_EYE_HASH;
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
					throw new RigInternalError("Given Bio subType:[" + modalitysubType + "] is not valid");
				}
				String hashResponse = packetUtility.retrieveBiometric(personaPath, modalityHashList);
				if (hashResponse != null && !hashResponse.isEmpty() && modalityKeyTogetHashValue != null) {
					String hashValue = JsonPrecondtion.getValueFromJson(hashResponse, modalityKeyTogetHashValue);
					modalityHashValueMap.put(modalitysubType, hashValue);
				}
			}
			hashtable.put(personaId, modalityHashValueMap);

		}

	}

