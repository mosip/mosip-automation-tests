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
 * Guards are structured so CodeQL can recognize path sanitization (normalize, {@code ..} check,
 * {@link Path#startsWith(Path)} against a trusted root) on the same expression used for I/O.
 */
@Component
public class PersonaPathValidator {

	private static final String RESIDENTS_PREFIX = "residents_";
	private static final String MOUNTPATH = "mountPath";
	private static final String MOSIP_TEST_TEMP = "mosip.test.temp";
	private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[a-zA-Z0-9._-]+(\\.json)?$");

	@Value("${mosip.test.persona.configpath:}")
	private String personaConfigPath;

	/**
	 * Validates and resolves a persona file path. All file existence checks use a path
	 * reconstructed from a trusted root plus validated relative components.
	 */
	public Path validatePersonaFile(String personaFilePath, String contextKey) throws IOException {
		if (personaFilePath == null || personaFilePath.isBlank()) {
			throw new IOException("personaFilePath is required");
		}
		if (personaFilePath.contains("..")) {
			throw new IOException("Invalid persona file path");
		}

		Path resolved = Paths.get(personaFilePath).toAbsolutePath().normalize();
		if (resolved.toString().contains("..")) {
			throw new IOException("Invalid persona file path");
		}

		String fileName = resolved.getFileName().toString();
		if (!SAFE_FILE_NAME.matcher(fileName).matches()) {
			throw new IOException("Invalid persona file name");
		}

		Path matchedRoot = findMatchingAllowedRoot(resolved, contextKey);
		if (matchedRoot == null) {
			throw new IOException("Persona file path is outside allowed directories");
		}

		if (!isPathUnderRoot(resolved, matchedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}

		Path safePath = buildPathUnderRoot(matchedRoot, resolved);
		if (!isPathUnderRoot(safePath, matchedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (safePath.toString().contains("..")) {
			throw new IOException("Invalid persona file path");
		}

		Path realPath;
		try {
			realPath = safePath.toRealPath();
		} catch (IOException e) {
			throw new IOException("Resident data file does not exist");
		}
		if (!isPathUnderRoot(realPath, matchedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (!Files.isRegularFile(realPath)) {
			throw new IOException("Resident data file does not exist");
		}
		return realPath;
	}

	/**
	 * Builds the clone target path from an already validated source file (same parent directory).
	 */
	public Path buildCloneTargetPath(Path validatedSource, String contextKey) throws IOException {
		Path parent = validatedSource.getParent();
		if (parent == null) {
			throw new IOException("Invalid source path");
		}

		String fileName = validatedSource.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		String base = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
		String ext = dotIndex > 0 ? fileName.substring(dotIndex) : "";
		String targetFileName = base + "_oldbio" + ext;

		if (!SAFE_FILE_NAME.matcher(targetFileName).matches()) {
			throw new IOException("Invalid target file name");
		}

		Path tentativeTarget = parent.resolve(targetFileName).normalize().toAbsolutePath();
		if (tentativeTarget.toString().contains("..")) {
			throw new IOException("Invalid target path");
		}

		Path matchedRoot = findMatchingAllowedRoot(tentativeTarget, contextKey);
		if (matchedRoot == null) {
			throw new IOException("Target path is outside allowed directories");
		}

		Path safeTarget = buildPathUnderRoot(matchedRoot, tentativeTarget);
		if (!isPathUnderRoot(safeTarget, matchedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (safeTarget.toString().contains("..")) {
			throw new IOException("Invalid target path");
		}
		if (!isPathUnderRoot(safeTarget, parent)) {
			throw new IOException("Invalid target path");
		}
		return safeTarget;
	}

	private Path findMatchingAllowedRoot(Path candidate, String contextKey) {
		for (Path root : getAllowedRoots(contextKey)) {
			if (root != null && isPathUnderRoot(candidate, root)) {
				return root;
			}
		}
		return null;
	}

	/**
	 * Checks that {@code path} lies under {@code root}, resolving both to real paths when
	 * possible so Windows short names, junctions, and symlinks do not break {@link Path#startsWith}.
	 */
	private static boolean isPathUnderRoot(Path path, Path root) {
		Path canonicalRoot = toCanonicalPath(root);
		Path canonicalPath = toCanonicalPath(path);
		return canonicalPath.startsWith(canonicalRoot);
	}

	private static Path toCanonicalPath(Path path) {
		try {
			if (Files.exists(path)) {
				return path.toRealPath();
			}
			Path parent = path.getParent();
			if (parent != null && Files.exists(parent)) {
				return parent.toRealPath().resolve(path.getFileName()).normalize();
			}
		} catch (IOException ignored) {
			// use normalized absolute path
		}
		return path.toAbsolutePath().normalize();
	}

	/**
	 * Reconstructs {@code candidate} as {@code root.resolve(relative)} using only validated name components.
	 * Root and candidate are canonicalized the same way as {@link #isPathUnderRoot} so symlink/junction
	 * roots (e.g. temp mount) match real paths returned from {@link #validatePersonaFile}.
	 */
	private static Path buildPathUnderRoot(Path root, Path candidate) throws IOException {
		Path canonicalRoot = toCanonicalPath(root);
		Path canonicalCandidate = toCanonicalPath(candidate);
		Path relative = canonicalRoot.relativize(canonicalCandidate.normalize());
		if (containsUnsafePathComponent(relative)) {
			throw new IOException("Invalid persona file path");
		}
		Path safePath = canonicalRoot;
		for (int i = 0; i < relative.getNameCount(); i++) {
			safePath = safePath.resolve(relative.getName(i));
		}
		return safePath.normalize().toAbsolutePath();
	}

	private static boolean containsUnsafePathComponent(Path relative) {
		for (Path component : relative) {
			String name = component.toString();
			if (name.equals("..") || name.equals(".") || name.isEmpty()) {
				return true;
			}
		}
		return relative.toString().contains("..");
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
