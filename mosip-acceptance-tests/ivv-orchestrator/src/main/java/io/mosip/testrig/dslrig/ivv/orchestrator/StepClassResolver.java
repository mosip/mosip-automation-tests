package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;

/**
 * Resolves DSL step names (e.g. {@code getUinbyRID}, {@code getUINByRid}, {@code getEmailByUin})
 * to the actual {@link StepInterface} implementation class on the classpath, ignoring
 * acronym/camelCase differences between Gherkin decode, JSON DSL, and Java source names.
 */
public final class StepClassResolver {

	private static final Logger logger = Logger.getLogger(StepClassResolver.class);

	private static final Map<String, Map<String, Class<?>>> INDEX_BY_PACKAGE = new HashMap<>();

	private StepClassResolver() {
	}

	public static Class<?> resolve(String packageName, String stepName) throws ClassNotFoundException {
		Map<String, Class<?>> index = indexForPackage(packageName);
		Class<?> clazz = index.get(normalizeStepName(stepName));
		if (clazz != null) {
			return clazz;
		}
		String conventional = packageName + "." + toClassSimpleName(stepName);
		try {
			return Class.forName(conventional);
		} catch (ClassNotFoundException | LinkageError e) {
			throw new ClassNotFoundException(
					"No step class for '" + stepName + "' in package " + packageName
							+ " (indexed " + index.size() + " step classes)",
					e);
		}
	}

	private static synchronized Map<String, Class<?>> indexForPackage(String packageName) {
		Map<String, Class<?>> cached = INDEX_BY_PACKAGE.get(packageName);
		if (cached != null) {
			return cached;
		}
		Map<String, Class<?>> built = Collections.unmodifiableMap(scanPackage(packageName));
		INDEX_BY_PACKAGE.put(packageName, built);
		logger.info("StepClassResolver indexed " + built.size() + " classes under " + packageName);
		return built;
	}

	private static Map<String, Class<?>> scanPackage(String packageName) {
		Map<String, Class<?>> index = new HashMap<>();
		String resourcePath = packageName.replace('.', '/');
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Enumeration<URL> resources = loader.getResources(resourcePath);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				if ("file".equals(url.getProtocol())) {
					try {
						scanDirectory(index, loader, packageName, Path.of(url.toURI()));
					} catch (URISyntaxException e) {
						logger.warn("Invalid classpath URL for " + packageName + ": " + url);
					}
				} else if ("jar".equals(url.getProtocol())) {
					scanJar(index, loader, packageName, url);
				}
			}
		} catch (IOException e) {
			logger.warn("Failed to scan step package " + packageName + ": " + e.getMessage());
		}
		return index;
	}

	private static void scanDirectory(Map<String, Class<?>> index, ClassLoader loader, String packageName,
			Path root) throws IOException {
		if (!Files.isDirectory(root)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			paths.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
				String relative = root.relativize(p).toString().replace(File.separatorChar, '.');
				if (relative.contains("$")) {
					return;
				}
				String className = packageName + "." + relative.substring(0, relative.length() - ".class".length());
				registerClass(index, loader, className);
			});
		}
	}

	private static void scanJar(Map<String, Class<?>> index, ClassLoader loader, String packageName, URL jarUrl)
			throws IOException {
		JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
		try (JarFile jar = connection.getJarFile()) {
			String prefix = packageName.replace('.', '/') + "/";
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (!name.startsWith(prefix) || !name.endsWith(".class") || name.contains("$")) {
					continue;
				}
				String className = name.substring(0, name.length() - 6).replace('/', '.');
				registerClass(index, loader, className);
			}
		}
	}

	private static void registerClass(Map<String, Class<?>> index, ClassLoader loader, String className) {
		try {
			Class<?> clazz = Class.forName(className, false, loader);
			if (!StepInterface.class.isAssignableFrom(clazz) || clazz.isInterface()) {
				return;
			}
			String stepKey = normalizeStepName(toStepName(clazz.getSimpleName()));
			Class<?> previous = index.put(stepKey, clazz);
			if (previous != null && previous != clazz) {
				logger.warn("Duplicate step name '" + stepKey + "': keeping " + clazz.getName() + ", ignoring "
						+ previous.getName());
			}
		} catch (ClassNotFoundException | LinkageError e) {
			logger.debug("Skipping " + className + ": " + e.getMessage());
		}
	}

	static String normalizeStepName(String stepName) {
		return stepName.toLowerCase(Locale.ROOT);
	}

	private static String toStepName(String classSimpleName) {
		if (classSimpleName == null || classSimpleName.isEmpty()) {
			return classSimpleName;
		}
		return Character.toLowerCase(classSimpleName.charAt(0)) + classSimpleName.substring(1);
	}

	private static String toClassSimpleName(String stepName) {
		if (stepName == null || stepName.isEmpty()) {
			return stepName;
		}
		return Character.toUpperCase(stepName.charAt(0)) + stepName.substring(1);
	}
}
