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
 * Validates persona file paths against allowed base directories to prevent path
 * traversal.
 * Guards are structured so CodeQL can recognize path sanitization (normalize,
 * {@code ..} check,
 * {@link Path#startsWith(Path)} against a trusted root) on the same expression
 * used for I/O.
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
	 * Validates and resolves a persona file path. All file existence checks use a
	 * path
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

		Path normalizedRoot = matchedRoot.toAbsolutePath().normalize();
		Path canonicalRoot = toCanonicalTrustedPath(normalizedRoot);

		if (!isPathUnderRoot(resolved, normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}

		Path safePath = buildPathUnderRoot(normalizedRoot, resolved);
		if (!isPathUnderRoot(safePath, normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (safePath.toString().contains("..")) {
			throw new IOException("Invalid persona file path");
		}
		if (!safePath.startsWith(normalizedRoot) && !safePath.startsWith(canonicalRoot)) {
			throw new IOException("Path traversal attempt detected");
		}

		Path realPath;
		try {
			realPath = safePath.toRealPath();
		} catch (IOException e) {
			throw new IOException("Resident data file does not exist");
		}
		if (!realPath.startsWith(canonicalRoot) && !realPath.startsWith(normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (!Files.isRegularFile(realPath)) {
			throw new IOException("Resident data file does not exist");
		}
		return realPath;
	}

	/**
	 * Builds the clone target path from an already validated source file (same
	 * parent directory).
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

		Path normalizedRoot = matchedRoot.toAbsolutePath().normalize();
		Path canonicalRoot = toCanonicalTrustedPath(normalizedRoot);

		Path safeTarget = buildPathUnderRoot(normalizedRoot, tentativeTarget);
		if (!isPathUnderRoot(safeTarget, normalizedRoot)) {
			throw new IOException("Path traversal attempt detected");
		}
		if (safeTarget.toString().contains("..")) {
			throw new IOException("Invalid target path");
		}
		if (!safeTarget.startsWith(normalizedRoot) && !safeTarget.startsWith(canonicalRoot)) {
			throw new IOException("Path traversal attempt detected");
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
	 * Checks that {@code path} lies under {@code root}. Only trusted roots are
	 * resolved via {@link Files}; user paths are compared with {@code normalize()}
	 * and {@link Path#startsWith(Path)} against normalized and canonical roots.
	 */
	private static boolean isPathUnderRoot(Path path, Path root) {
		Path normalizedPath = path.toAbsolutePath().normalize();
		Path normalizedRoot = root.toAbsolutePath().normalize();
		if (normalizedPath.toString().contains("..")) {
			return false;
		}
		if (normalizedPath.startsWith(normalizedRoot)) {
			return true;
		}
		Path canonicalRoot = toCanonicalTrustedPath(root);
		return normalizedPath.startsWith(canonicalRoot);
	}

	/**
	 * Resolves symlinks/junctions for trusted roots only.
	 */
	private static Path toCanonicalTrustedPath(Path trustedRoot) {
		Path normalizedPath = trustedRoot.toAbsolutePath().normalize();
		try {
			if (Files.exists(normalizedPath)) {
				return normalizedPath.toRealPath();
			}
			Path parent = normalizedPath.getParent();
			if (parent != null && Files.exists(parent)) {
				return parent.toRealPath().resolve(normalizedPath.getFileName()).normalize();
			}
		} catch (IOException ignored) {
			// use normalized absolute path
		}
		return normalizedPath;
	}

	/**
	 * Reconstructs {@code candidate} as {@code root.resolve(relative)} using only
	 * validated name components.
	 */
	private static Path buildPathUnderRoot(Path root, Path candidate) throws IOException {
		Path normalizedRoot = root.toAbsolutePath().normalize();
		Path normalizedCandidate = candidate.toAbsolutePath().normalize();
		Path canonicalRoot = toCanonicalTrustedPath(root);

		Path baseRoot;
		Path relative;
		if (normalizedCandidate.startsWith(normalizedRoot)) {
			baseRoot = normalizedRoot;
			relative = normalizedRoot.relativize(normalizedCandidate);
		} else if (normalizedCandidate.startsWith(canonicalRoot)) {
			baseRoot = canonicalRoot;
			relative = canonicalRoot.relativize(normalizedCandidate);
		} else {
			throw new IOException("Invalid persona file path");
		}

		if (relative.isAbsolute() || containsUnsafePathComponent(relative)) {
			throw new IOException("Invalid persona file path");
		}
		Path safePath = baseRoot;
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
