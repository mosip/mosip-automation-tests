package io.mosip.testrig.dslrig.packetcreator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Validates persona file paths against allowed base directories to prevent path traversal.
 */
@Component
public class PersonaPathValidator {

	private static final String RESIDENTS_PREFIX = "residents_";
	private static final String MOUNTPATH = "mountPath";
	private static final String MOSIP_TEST_TEMP = "mosip.test.temp";
	private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[a-zA-Z0-9._-]+(\\.json)?$");

	@Value("${mosip.test.persona.configpath:}")
	private String personaConfigPath;

	public Path validatePersonaFile(String personaFilePath, String contextKey) throws IOException {
		if (personaFilePath == null || personaFilePath.isBlank()) {
			throw new IOException("personaFilePath is required");
		}
		Path resolved = Paths.get(personaFilePath).toAbsolutePath().normalize();
		String fileName = resolved.getFileName().toString();
		if (!SAFE_FILE_NAME.matcher(fileName).matches()) {
			throw new IOException("Invalid persona file name");
		}
		if (!isUnderAllowedRoot(resolved, contextKey)) {
			throw new IOException("Persona file path is outside allowed directories");
		}
		if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
			throw new IOException("Resident data file does not exist");
		}
		return resolved;
	}

	public Path validateSiblingPath(Path source, Path target, String contextKey) throws IOException {
		Path normalizedTarget = target.toAbsolutePath().normalize();
		Path parent = source.getParent();
		if (parent == null || !normalizedTarget.startsWith(parent.normalize())) {
			throw new IOException("Invalid target path");
		}
		if (!isUnderAllowedRoot(normalizedTarget, contextKey)) {
			throw new IOException("Target path is outside allowed directories");
		}
		return normalizedTarget;
	}

	private boolean isUnderAllowedRoot(Path candidate, String contextKey) {
		for (Path root : getAllowedRoots(contextKey)) {
			if (root != null && candidate.startsWith(root)) {
				return true;
			}
		}
		return false;
	}

	private List<Path> getAllowedRoots(String contextKey) {
		List<Path> roots = new ArrayList<>();
		Path osTemp = getOsTempRoot();
		if (osTemp != null) {
			roots.add(osTemp);
		}
		Path ctxTemp = getContextTempRoot(contextKey);
		if (ctxTemp != null) {
			roots.add(ctxTemp);
		}
		if (personaConfigPath != null && !personaConfigPath.isBlank()) {
			roots.add(Paths.get(personaConfigPath).toAbsolutePath().normalize());
		}
		addCommaSeparatedPaths(roots, contextKey, RESIDENTS_PREFIX);
		return roots;
	}

	private static void addCommaSeparatedPaths(List<Path> roots, String contextKey, String variableKey) {
		if (contextKey == null || contextKey.isBlank()) {
			return;
		}
		Object valueObj = VariableManager.getVariableValue(contextKey, variableKey);
		if (valueObj == null) {
			return;
		}
		for (String segment : valueObj.toString().split(",")) {
			String trimmed = segment.trim();
			if (!trimmed.isEmpty()) {
				roots.add(Paths.get(trimmed).toAbsolutePath().normalize());
			}
		}
	}

	private static Path normalizeAbsolute(Path path) {
		return path.toAbsolutePath().normalize();
	}

	private static Path getOsTempRoot() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if (tmpDir == null || tmpDir.isBlank()) {
			return null;
		}
		return normalizeAbsolute(Paths.get(tmpDir));
	}

	private static Path getContextTempRoot(String contextKey) {
		if (contextKey == null || contextKey.isBlank()) {
			return null;
		}
		Object mountObj = VariableManager.getVariableValue(contextKey, MOUNTPATH);
		Object tempObj = VariableManager.getVariableValue(contextKey, MOSIP_TEST_TEMP);
		if (mountObj == null || tempObj == null) {
			return null;
		}
		String mountPath = mountObj.toString();
		String tempPath = tempObj.toString();
		if (mountPath.isBlank() && tempPath.isBlank()) {
			return null;
		}
		return normalizeAbsolute(Paths.get(mountPath + tempPath));
	}
}
