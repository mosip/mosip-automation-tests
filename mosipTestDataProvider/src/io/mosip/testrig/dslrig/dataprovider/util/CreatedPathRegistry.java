package io.mosip.testrig.dslrig.dataprovider.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.testrig.dslrig.dataprovider.biometric.MdsPortRegistry;
import io.mosip.testrig.dslrig.dataprovider.util.internalapi.InternalApiLogCollector;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

/**
 * Tracks created files/dirs without putting them in VariableManager/JCache
 * (that cache-per-scenario was the leftover heap after file delete).
 */
public final class CreatedPathRegistry {

	private static final Logger logger = LoggerFactory.getLogger(CreatedPathRegistry.class);

	public static final String GLOBAL_CONTEXT = "global_created_paths";
	public static final String CREATED_PATHS_KEY = "created_paths_";
	public static final String SHARED_WORK_DIRS_KEY = "shared_work_dirs_";
	public static final String PKTCREATOR_FILES_KEY = "pktcreator_files_";
	public static final String PKM_FILES_KEY = "pkm_";

	public static final String[] SCENARIO_PATH_KEYS = {
			"residents_",
			"packets_",
			"preregIds_",
			"Passport_",
			"DrivingLic_",
			PKTCREATOR_FILES_KEY,
			PKM_FILES_KEY,
			CREATED_PATHS_KEY
	};

	private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Set<String>>> PATHS =
			new ConcurrentHashMap<>();

	private CreatedPathRegistry() {
	}

	public static void register(String contextKey, String variableKey, String path) {
		if (!isSafeToTrack(path)) {
			return;
		}
		addPath(contextKey, variableKey, path);
		addPath(GLOBAL_CONTEXT, CREATED_PATHS_KEY, path);
	}

	/**
	 * Lifetime work directories shared by parallel scenarios (pktcreator / prereg).
	 * Deleted only by {@link #clearAll()}, not by per-scenario packet-data delete.
	 */
	public static void registerShared(String path) {
		if (!isSafeToTrack(path)) {
			return;
		}
		addPath(GLOBAL_CONTEXT, SHARED_WORK_DIRS_KEY, path);
		addPath(GLOBAL_CONTEXT, CREATED_PATHS_KEY, path);
	}

	/**
	 * Never track the persona resource root, Profile/Default, or whole privatekeys/Profile folders.
	 */
	public static boolean isSafeToTrack(String path) {
		if (path == null || path.isBlank()) {
			return false;
		}
		Path normalized = Paths.get(path).toAbsolutePath().normalize();
		Path name = normalized.getFileName();
		if (name == null) {
			return false;
		}
		String fileName = name.toString();
		if ("resource".equalsIgnoreCase(fileName) || "privatekeys".equalsIgnoreCase(fileName)
				|| "Profile".equalsIgnoreCase(fileName) || "config".equalsIgnoreCase(fileName)) {
			logger.warn("Refusing to track broad resource directory: {}", normalized);
			return false;
		}
		if ("Default".equalsIgnoreCase(fileName)) {
			Path parent = normalized.getParent();
			if (parent != null && parent.getFileName() != null
					&& "Profile".equalsIgnoreCase(parent.getFileName().toString())) {
				logger.warn("Refusing to track Default MDS profile: {}", normalized);
				return false;
			}
		}
		return true;
	}

	public static void clearScenario(String contextKey) throws IOException {
		if (contextKey == null || contextKey.isBlank() || GLOBAL_CONTEXT.equals(contextKey)) {
			return;
		}
		Set<String> scenarioPaths = new LinkedHashSet<>();
		for (String key : SCENARIO_PATH_KEYS) {
			scenarioPaths.addAll(snapshot(contextKey, key));
			deleteTracked(contextKey, key);
		}
		PATHS.remove(contextKey);
		untrackGlobalCreated(scenarioPaths);
		releaseContextMemory(contextKey);
	}

	public static String clearAll() throws IOException {
		Set<String> contexts = new LinkedHashSet<>(PATHS.keySet());
		contexts.addAll(VariableManager.getContextKeys());
		for (String ctx : contexts) {
			if (GLOBAL_CONTEXT.equals(ctx) || VariableManager.NS_DEFAULT.equals(ctx)) {
				continue;
			}
			try {
				clearScenario(ctx);
			} catch (Exception e) {
				logger.warn("Failed to clear scenario created paths for context {}: {}", ctx, e.getMessage());
			}
		}
		deleteTracked(GLOBAL_CONTEXT, CREATED_PATHS_KEY);
		deleteTracked(GLOBAL_CONTEXT, SHARED_WORK_DIRS_KEY);
		PATHS.remove(GLOBAL_CONTEXT);
		PATHS.clear();
		InternalApiLogCollector.clear(InternalApiLogCollector.GLOBAL_LOG_KEY);
		VariableManager.deleteNameSpace(GLOBAL_CONTEXT);
		return "Deleted all created data successfully";
	}

	static void releaseContextMemory(String contextKey) {
		try {
			RestClient.clearRunScopedCache(contextKey);
		} catch (Exception e) {
			logger.warn("Failed to clear run-scoped cache for {}: {}", contextKey, e.getMessage());
		}
		InternalApiLogCollector.clear(contextKey);
		MdsPortRegistry.remove(contextKey);
		VariableManager.deleteNameSpace(contextKey);
	}

	public static List<String> snapshot(String contextKey, String key) {
		ConcurrentHashMap<String, Set<String>> byKey = PATHS.get(contextKey);
		if (byKey == null) {
			return List.of();
		}
		Set<String> set = byKey.get(key);
		if (set == null || set.isEmpty()) {
			return List.of();
		}
		return List.copyOf(set);
	}

	private static void addPath(String contextKey, String variableKey, String path) {
		PATHS.computeIfAbsent(contextKey, unused -> new ConcurrentHashMap<>())
				.computeIfAbsent(variableKey, unused -> ConcurrentHashMap.newKeySet())
				.add(path);
	}

	private static void untrackGlobalCreated(Set<String> scenarioPaths) {
		if (scenarioPaths == null || scenarioPaths.isEmpty()) {
			return;
		}
		ConcurrentHashMap<String, Set<String>> global = PATHS.get(GLOBAL_CONTEXT);
		if (global == null) {
			return;
		}
		Set<String> created = global.get(CREATED_PATHS_KEY);
		if (created != null) {
			created.removeAll(scenarioPaths);
			if (created.isEmpty()) {
				global.remove(CREATED_PATHS_KEY, created);
			}
		}
		if (global.isEmpty()) {
			PATHS.remove(GLOBAL_CONTEXT, global);
		}
	}

	static void deleteTracked(String contextKey, String key) throws IOException {
		for (String trimmedPath : snapshot(contextKey, key)) {
			if (isSafeToTrack(trimmedPath)) {
				CommonUtil.deleteOldTempDir(trimmedPath, contextKey);
			}
		}
		ConcurrentHashMap<String, Set<String>> byKey = PATHS.get(contextKey);
		if (byKey != null) {
			byKey.remove(key);
			if (byKey.isEmpty()) {
				PATHS.remove(contextKey, byKey);
			}
		}
	}
}
