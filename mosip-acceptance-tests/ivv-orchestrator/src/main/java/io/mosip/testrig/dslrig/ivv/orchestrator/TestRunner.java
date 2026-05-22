package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.TestNG;

import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.KeyCloakUserAndAPIKeyGeneration;
import io.mosip.testrig.apirig.utils.KeycloakUserManager;
import io.mosip.testrig.apirig.utils.MispPartnerAndLicenseKeyGeneration;
import io.mosip.testrig.apirig.utils.PartnerRegistration;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.OTPListener;

public class TestRunner {
	private static final Logger LOGGER = Logger.getLogger(TestRunner.class);
	public static String jarUrl = resolveJarUrl();

	/** Resolved once on the main thread; safe for parallel TestNG workers (ReadPreReq, etc.). */
	private static volatile String cachedRunType;
	private static volatile String cachedExternalResourcePath;
	private static volatile String cachedGlobalResourcePath;
	private static volatile String cachedLocalResourcePath;

	public static void main(String[] args) {
		initializeResourcePaths();
		removeOldMosipTestTestResource();
		if (checkRunType().equalsIgnoreCase("JAR")) {
			extractResourceFromJar();
		}
		BaseTestCase.setRunContext(checkRunType(), jarUrl);
		copyTestResources();
		BaseTestCase.environment = System.getProperty("env.user");
		BaseTestCase.ApplnURI = System.getProperty("env.endpoint");
		BaseTestCase.testLevel = System.getProperty("env.testLevel");


		String timeStamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
		dslConfigManager.init();
		AdminTestUtil.init();
		BaseTestCase.currentModule = BaseTestCase.runContext + GlobalConstants.DSL;
		KeycloakUserManager.removeUser();
		KeycloakUserManager.createUsers();
		KeycloakUserManager.closeKeycloakInstance();

		if (dslConfigManager.IsDebugEnabled())
			LOGGER.setLevel(Level.ALL);
		else
			LOGGER.setLevel(Level.ERROR);

		setLogLevels();
		initializeLanguagesWithRetry();

		OTPListener mockSMTPListener = new OTPListener();
		mockSMTPListener.run();
		startTestRunner();
	}

	/**
	 * Loads languages from the env actuator. Retries on transient DNS/network failures
	 * (e.g. {@code UnknownHostException} for {@code api-internal.*.mosip.net}) instead of
	 * failing with {@link IndexOutOfBoundsException} on an empty list.
	 */
	private static void initializeLanguagesWithRetry() {
		final int maxAttempts = Integer.parseInt(
				System.getProperty("env.languageLoadAttempts", "3"));
		final long baseDelayMs = Long.parseLong(
				System.getProperty("env.languageLoadRetryMs", "3000"));

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			if (attempt > 1) {
				LOGGER.warn("Retrying env actuator / language load (attempt " + attempt + "/" + maxAttempts
						+ "), endpoint=" + BaseTestCase.ApplnURI);
				sleepQuietly(baseDelayMs * attempt);
				BaseTestCase.initialize();
			} else {
				BaseTestCase.initialize();
			}
			List<String> languages = BaseTestCase.getLanguageList();
			if (languages != null && !languages.isEmpty()) {
				BaseTestCase.languageList = languages;
				BaseTestCase.languageCode = languages.get(0);
				LOGGER.info("Current running language: " + BaseTestCase.languageCode);
				return;
			}
		}

		String fallback = resolveFallbackLanguageCode();
		if (fallback != null && !fallback.isBlank()) {
			BaseTestCase.languageList = new ArrayList<>(Collections.singletonList(fallback));
			BaseTestCase.languageCode = fallback;
			LOGGER.warn("Env actuator returned no languages; using fallback language code '" + fallback
					+ "'. Check VPN/DNS for " + BaseTestCase.ApplnURI
					+ " or set -Denv.langcode=eng in your run command.");
			return;
		}

		throw new IllegalStateException(
				"Could not load mandatory languages from " + BaseTestCase.ApplnURI
						+ " (masterdata actuator/env). Often caused by intermittent UnknownHostException. "
						+ "Retry the run, verify network/VPN, or pass -Denv.langcode=eng (or set "
						+ "defaultLanguageCode in config/dsl.properties).");
	}

	private static String resolveFallbackLanguageCode() {
		String fromJvm = System.getProperty("env.langcode");
		if (fromJvm != null && !fromJvm.isBlank()) {
			return fromJvm.trim();
		}
		String fromConfig = dslConfigManager.getDefaultLanguageCode();
		if (fromConfig != null && !fromConfig.isBlank()) {
			return fromConfig.trim();
		}
		return null;
	}

	private static void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public static void startTestRunner() {
		File homeDir = null;
		TestNG runner = new TestNG();
		List<String> suitefiles = new ArrayList<String>();
		String os = System.getProperty("os.name");
		LOGGER.info(os);
		if (checkRunType().contains("IDE") || os.toLowerCase().contains("windows") == true) {
			homeDir = new File(TestResources.getResourcePath().replace("/MosipTestResource/MosipTemporaryTestResource", "") + "testngFile");
			LOGGER.info("IDE Home Dir=" + homeDir);
		} else {
			homeDir = new File(getGlobalResourcePath() + "/testngFile");
			LOGGER.info("Jar Home Dir=" + homeDir);
		}

		File[] suiteFiles = homeDir.listFiles();
		if (suiteFiles == null || suiteFiles.length == 0) {
			throw new IllegalStateException(
					"No TestNG suite files under " + homeDir.getAbsolutePath()
							+ ". Ensure the image JAR contains testngFile/ and startup completed extractResourceFromJar.");
		}
		for (File file : suiteFiles) {
			if (file.getName().toLowerCase() != null) {
				suitefiles.add(file.getAbsolutePath());
			}
		}

		runner.setTestSuites(suitefiles);
		System.getProperties().setProperty("testng.outpur.dir", "testng-report");
		runner.setOutputDirectory("testng-report");
		System.getProperties().setProperty("emailable.report2.name", "DSL-" + BaseTestCase.environment + "-"
				+ BaseTestCase.testLevel + "-run-" + System.currentTimeMillis() + "-report.html");


		System.getProperties().setProperty("emailable.report3.name", "DSL-" + BaseTestCase.environment + "-"
				+ "EXTENT" + "-run-" + System.currentTimeMillis() + "-report.html");
		runner.run();

		OTPListener mockSMTPListener = new OTPListener();
		mockSMTPListener.bTerminate = true;
		System.exit(0);

	}

	/**
	 * Resolves IDE/JAR resource paths once. Call from {@code main} and {@code @BeforeSuite}
	 * before parallel scenarios start.
	 */
	public static synchronized void initializeResourcePaths() {
		if (cachedExternalResourcePath != null) {
			return;
		}
		cachedRunType = resolveRunType();
		cachedExternalResourcePath = resolveExternalResourcePath(cachedRunType);
		cachedGlobalResourcePath = resolveGlobalResourcePath(cachedRunType, cachedExternalResourcePath);
		cachedLocalResourcePath = resolveLocalResourcePath(cachedRunType, cachedExternalResourcePath);
		LOGGER.info("Resource paths initialized: runType=" + cachedRunType + ", external="
				+ cachedExternalResourcePath + ", global=" + cachedGlobalResourcePath);
	}

	private static void ensureResourcePathsInitialized() {
		if (cachedExternalResourcePath == null) {
			initializeResourcePaths();
		}
	}

	private static String resolveJarUrl() {
		try {
			CodeSource src = TestRunner.class.getProtectionDomain().getCodeSource();
			if (src != null && src.getLocation() != null) {
				return src.getLocation().getPath();
			}
		} catch (Exception e) {
			LOGGER.warn("Could not resolve jar URL from code source: " + e.getMessage());
		}
		return "";
	}

	private static String resolveRunType() {
		if (jarUrl != null && jarUrl.toLowerCase().endsWith(".jar")) {
			return "JAR";
		}
		URL selfResource = TestRunner.class.getResource("TestRunner.class");
		if (selfResource != null && selfResource.getPath().contains(".jar")) {
			return "JAR";
		}
		return "IDE";
	}

	private static String resolveExternalResourcePath(String runType) {
		if ("JAR".equalsIgnoreCase(runType)) {
			return new File(jarUrl).getParentFile().getAbsolutePath();
		}
		return resolveClasspathRoot();
	}

	private static String resolveGlobalResourcePath(String runType, String externalPath) {
		if ("JAR".equalsIgnoreCase(runType)) {
			return externalPath + "/" + TestResources.resourceTestFolderName + "/"
					+ TestResources.resourceFolderName;
		}
		return externalPath + "/" + TestResources.resourceTestFolderName + "/"
				+ TestResources.resourceFolderName;
	}

	private static String resolveLocalResourcePath(String runType, String externalPath) {
		if ("JAR".equalsIgnoreCase(runType)) {
			return externalPath + "/" + TestResources.resourceTestFolderName + "/"
					+ TestResources.resourceFolderName;
		}
		return resolveClasspathRoot();
	}

	/**
	 * Classpath root for IDE runs. {@code ClassLoader.getResource("")} and
	 * {@code Class.getResource("*.class")} are often null under JDK 21 @argfile /
	 * parallel TestNG, which caused NPEs in ReadPreReq during full-suite runs.
	 */
	private static String resolveClasspathRoot() {
		ClassLoader loader = TestRunner.class.getClassLoader();
		if (loader != null) {
			URL root = loader.getResource("");
			if (root != null) {
				return normalizeClasspathRoot(root);
			}
		}
		URL config = TestRunner.class.getResource("/config/config.properties");
		if (config != null) {
			try {
				Path configPath = Paths.get(config.toURI());
				Path classesRoot = configPath.getParent().getParent();
				return classesRoot.toAbsolutePath().toString();
			} catch (URISyntaxException e) {
				LOGGER.warn("Could not derive classpath root from config.properties: " + e.getMessage());
			}
		}
		String userDir = System.getProperty("user.dir");
		String[] relativeCandidates = { "target/classes",
				"mosip-acceptance-tests/ivv-orchestrator/target/classes",
				"ivv-orchestrator/target/classes" };
		for (String relative : relativeCandidates) {
			File candidate = new File(userDir, relative);
			if (new File(candidate, "config/config.properties").isFile()) {
				return candidate.getAbsolutePath();
			}
		}
		if (new File(userDir, "config/config.properties").isFile()) {
			return userDir;
		}
		throw new IllegalStateException(
				"Cannot resolve classpath root for IDE run (user.dir=" + userDir
						+ "). Run from ivv-orchestrator after 'mvn compile', or set user.dir to the module directory.");
	}

	private static String normalizeClasspathRoot(URL url) {
		try {
			if ("file".equals(url.getProtocol())) {
				String path = Paths.get(url.toURI()).toAbsolutePath().toString();
				if (path.contains("test-classes")) {
					path = path.replace("test-classes", "classes");
				}
				return path;
			}
		} catch (URISyntaxException e) {
			LOGGER.warn("Could not parse classpath root URL " + url + ": " + e.getMessage());
		}
		String path = new File(url.getPath()).getAbsolutePath();
		if (path.contains("test-classes")) {
			path = path.replace("test-classes", "classes");
		}
		return path;
	}

	public static String checkRunType() {
		ensureResourcePathsInitialized();
		return cachedRunType;
	}

	public static void removeOldMosipTestTestResource() {
		File mosipTestFile = new File(TestRunner.getGlobalResourcePath());
		if (mosipTestFile.exists())
			if (deleteDirectory(mosipTestFile))
				LOGGER.info("Old MosipTestResource folder successfully deleted!!");
			else
				LOGGER.error("Old MosipTestResource folder not deleted.");
	}

	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(children[i]);
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	public static void extractResourceFromJar() {
		getListOfFilesFromJarAndCopyToExternalResource("testngFile/");
		getListOfFilesFromJarAndCopyToExternalResource("config/");
		getListOfFilesFromJarAndCopyToExternalResource("local/");
		getListOfFilesFromJarAndCopyToExternalResource("preReg/");
		getListOfFilesFromJarAndCopyToExternalResource("kernel/");
		getListOfFilesFromJarAndCopyToExternalResource("idaData/");
		getListOfFilesFromJarAndCopyToExternalResource("ivv_masterdata/");
		getListOfFilesFromJarAndCopyToExternalResource("syncdata/");
		getListOfFilesFromJarAndCopyToExternalResource("regproc/");
	}

	public static void getListOfFilesFromJarAndCopyToExternalResource(String key) {
		ZipInputStream zip = null;
		try {
			CodeSource src = TestRunner.class.getProtectionDomain().getCodeSource();
			if (src != null) {
				URL jar = src.getLocation();
				 zip = new ZipInputStream(jar.openStream());
				while (true) {
					ZipEntry e = zip.getNextEntry();
					if (e == null)
						break;
					String name = e.getName();
					if (name.startsWith(key) && name.contains(".")) {
						if (copyFilesFromJarToOutsideResource(name))
							LOGGER.info("Copied the file: " + name + " to external resource successfully..!");
						else
							LOGGER.error("Fail to copy file: " + name + " to external resource");
					}
				}
			} else {
				LOGGER.error("Something went wrong with jar location");
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured in extracting resource: " + e.getMessage());
		}

		finally {
			closeZipInputStream(zip);
		}
	}

	public static void closeZipInputStream(ZipInputStream zipInputStream) {
	    if (zipInputStream != null) {
	        try {
	            zipInputStream.close();
	        } catch (IOException e) {
	        }
	    }
	}


	public static String getLocalResourcePath() {
		ensureResourcePathsInitialized();
		return cachedLocalResourcePath;
	}


	public static String getGlobalResourcePath() {
		ensureResourcePathsInitialized();
		return cachedGlobalResourcePath;
	}

	private static boolean copyFilesFromJarToOutsideResource(String path) {
		try {
			File resourceFile = new File(TestRunner.jarUrl).getParentFile();
			File destinationFile = new File(resourceFile.getAbsolutePath() + "/"+TestResources.resourceTestFolderName+  "/"+TestResources.resourceFolderName + "/"+ path);
			org.apache.commons.io.FileUtils.copyInputStreamToFile(TestRunner.class.getResourceAsStream("/" + path),
					destinationFile);
			return true;
		} catch (Exception e) {
			LOGGER.error(
					"Exception Occured in copying the resource from jar. Kindly build new jar to perform smooth test execution: "
							+ e.getMessage());
			return false;
		}
	}

	private static void copyTestResources() {
		TestResources.copyTestResource("/testngFile");
		TestResources.copyTestResource("/preReg");
		TestResources.copyTestResource("/config");
		TestResources.copyTestResource("/kernel");
		TestResources.copyTestResource("/idaData");
		TestResources.copyTestResource("/idRepo");
		TestResources.copyTestResource("/ivv_masterdata");
		TestResources.copyTestResource("/syncdata");
		TestResources.copyTestResource("/regproc");
		TestResources.copyTestResource("/dbFiles");
	}

	public static String getExternalResourcePath() {
		ensureResourcePathsInitialized();
		return cachedExternalResourcePath;
	}

	private static void setLogLevels(){
		AdminTestUtil.setLogLevel();
		OutputValidationUtil.setLogLevel();
		PartnerRegistration.setLogLevel();
		KeyCloakUserAndAPIKeyGeneration.setLogLevel();
		MispPartnerAndLicenseKeyGeneration.setLogLevel();
	}


}
