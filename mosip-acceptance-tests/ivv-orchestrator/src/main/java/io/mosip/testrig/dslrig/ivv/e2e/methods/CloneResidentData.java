package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class CloneResidentData extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(CloneResidentData.class);

	static {
		if (dslConfigManager.IsDebugEnabled()) {
			logger.setLevel(Level.ALL);
		} else {
			logger.setLevel(Level.ERROR);
		}
	}

	@Override
	public void run() throws RigInternalError {
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			this.hasError = true;
			throw new RigInternalError("Resident data path parameter is missing in step: " + step.getName());
		}

		String personaFilePath = step.getParameters().get(0);
		if (personaFilePath.startsWith("$$")) {
			personaFilePath = step.getScenario().getVariables().get(personaFilePath);
		}

		if (personaFilePath == null || personaFilePath.isBlank()) {
			this.hasError = true;
			throw new RigInternalError("Unable to resolve resident data path in step: " + step.getName());
		}

		Path source = Path.of(personaFilePath);
		if (!Files.exists(source)) {
			this.hasError = true;
			throw new RigInternalError("Resident data file does not exist: " + personaFilePath);
		}

		String fileName = source.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		String base = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
		String ext = dotIndex > 0 ? fileName.substring(dotIndex) : "";
		Path target = source.resolveSibling(base + "_oldbio" + ext);

		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			this.hasError = true;
			throw new RigInternalError("Failed to clone resident data file: " + e.getMessage());
		}

		if (step.getOutVarName() != null) {
			step.getScenario().getVariables().put(step.getOutVarName(), target.toString());
		}
		logger.info("Cloned resident data path: " + target);
	}
}
