package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.DslPacketTemplateCache;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class UpdateBioExceptionInPersona extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(UpdateBioExceptionInPersona.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		List<String> exceptionArray = new ArrayList<String>();
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("bioType paramter is  missing in step: " + step.getName());
		}
		if (step.getParameters().size() < 2) {
			logger.error("Biometric exception modalities parameter is missing from DSL step");
			this.hasError = true;
			throw new RigInternalError("biometric exception modalities paramter is  missing in step: " + step.getName());
		}

		String personaFilePath = step.getParameters().get(0);
		String modalitiesArg = joinBioExceptionModalities(step.getParameters());
		logger.info("UpdateBioExceptionInPersona personaFilePath=" + personaFilePath
				+ ", rawParameters=" + step.getParameters()
				+ ", mergedModalities=" + modalitiesArg);

		if (modalitiesArg != null && !modalitiesArg.isBlank()) {
			String[] str = modalitiesArg.split("@@");
			for (String s : str) {
				String trimmed = s.trim();
				if (!trimmed.isEmpty()) {
					exceptionArray.add(trimmed);
				}
			}
		}
		logger.info("UpdateBioExceptionInPersona parsed exception modalities (" + exceptionArray.size() + "): "
				+ exceptionArray);

		if (personaFilePath.startsWith("$$")) {
			personaFilePath = step.getScenario().getVariables().get(personaFilePath);
			packetUtility.updateBioException(personaFilePath, exceptionArray, step);
			DslPacketTemplateCache.incrementUpdateCounter(personaFilePath);
		}
	}

	/**
	 * Merges biometric exception modalities from the second DSL argument onward. Legacy steps
	 * incorrectly split modalities on commas (e.g. {@code Finger:Left Thumb,Finger:Left IndexFinger@@...}).
	 */
	static String joinBioExceptionModalities(List<String> parameters) {
		if (parameters == null || parameters.size() < 2) {
			return "";
		}
		return parameters.subList(1, parameters.size()).stream()
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.joining("@@"));
	}
}
