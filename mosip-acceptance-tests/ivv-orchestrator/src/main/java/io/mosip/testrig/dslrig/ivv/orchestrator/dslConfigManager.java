package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.mapping.Set;

import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.ConfigManager;

public class dslConfigManager extends ConfigManager {
	private static final Logger LOGGER = Logger.getLogger(dslConfigManager.class);

	public static void init() {
		Map<String, Object> moduleSpecificPropertiesMap = new HashMap<>();

		try {
			String path = TestRunner.getGlobalResourcePath() + "/config/dsl.properties";
			Properties props = getproperties(path);

			for (String key : props.stringPropertyNames()) {

				String value = System.getenv(key) == null ? props.getProperty(key) : System.getenv(key);
				moduleSpecificPropertiesMap.put(key, value);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}

		init(moduleSpecificPropertiesMap);
	}

	public static String getmountPathForScenario() {
		return ConfigManager.getproperty("mountPathForScenario");
	}

	public static String getThreadCount() {
		return ConfigManager.getproperty("threadCount");
	}

	public static int getMaxSuiteTime() {
		try {
		    return Integer.parseInt(ConfigManager.getproperty("maxSuiteTime"));
		} catch (NumberFormatException e) {
		    return 2; 
		}
	}

	public static String getUinWaitTime() {
		return ConfigManager.getproperty("uinWaitTime");
	}

	/** Used when env actuator is unreachable (e.g. transient DNS failure). Override with {@code -Denv.langcode}. */
	public static String getDefaultLanguageCode() {
		return ConfigManager.getproperty("defaultLanguageCode");
	}

	public static String checkNotification() {
		return ConfigManager.getproperty("checkNotification");
	}

	public static String getNextPacketUploadWaitTime() {
		return ConfigManager.getproperty("nextPacketUploadWaitTime");
	}

	public static Boolean useExternalScenarioSheet() {
		return ConfigManager.getproperty("useExternalScenarioSheet").equalsIgnoreCase("yes");
	}

	public static Boolean useGherkinScenarios() {
		try {
			String value = ConfigManager.getproperty("useGherkinScenarios");
			if (value == null || value.trim().isEmpty()) {
				return true;
			}
			return !value.equalsIgnoreCase("no");
		} catch (Exception e) {
			return true;
		}
	}

	private static final String GHERKIN_FEATURES_DIR = "config";

	public static String getGherkinFeaturesPath() {
		return GHERKIN_FEATURES_DIR;
	}

	/** Scenarios load from {@code config/scenarios.feature} by default; set {@code useGherkinScenarios=no} for external JSON. */
	public static String getpacketUtilityBaseUrl() {
		return ConfigManager.getproperty("packetUtilityBaseUrl");
	}

	public static String getEsignetMockBaseURL() { 
		return getproperty("esignetMockBaseURL");
	}

	public static synchronized boolean isInTobeSkippedList(String stringToFind) {
		String toSkippedList = ConfigManager.getproperty("scenariosToSkip");
		List<String> toBeSkippedLsit = Arrays.asList(toSkippedList.split(","));
		if (IsDebugEnabled())
			LOGGER.info("toSkippedList:  " + toSkippedList + ", toBeSkippedLsit : " + toBeSkippedLsit
					+ ", stringToFind : " + stringToFind);
		for (String string : toBeSkippedLsit) {
			if (string.equalsIgnoreCase(stringToFind))
				return true;
		}
		return false;
	}

	public static synchronized boolean isInTobeBugList(String scenario) {
	    Map<String, String> skipMap = loadTestcaseToBeSkippedMap();
	    return skipMap.containsKey(scenario);
	}

	public static String getBugId(String scenario) {
	    Map<String, String> skipMap = loadTestcaseToBeSkippedMap();
	    return skipMap.getOrDefault(scenario, "");
	}

	public static Map<String, String> loadTestcaseToBeSkippedMap() {
	    Map<String, String> testcaseToBeSkippedMap = new HashMap<>();

	    try (BufferedReader br = new BufferedReader(
	            new FileReader(BaseTestCase.getGlobalResourcePath() + "/" + "config/TestCaseSkip.txt"))) {

	        String line;
	        while ((line = br.readLine()) != null) {

	            if (line.startsWith("#")) {
	                continue;
	            }

	            if (line.contains("==")) {
	                String[] parts = line.split("==");
	                if (parts.length > 1) {
	                    String bugId = parts[0].trim();
	                    String scenario = parts[1].trim();
	                    testcaseToBeSkippedMap.put(scenario, bugId);
	                }
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    return testcaseToBeSkippedMap;
	}


	public static synchronized boolean isInTobeExecuteList(String stringToFind) {

		String toExecuteList = ConfigManager.getproperty("scenariosToExecute");
		if (toExecuteList != null && toExecuteList.isEmpty())
			return true;
		if (toExecuteList.contains("@@")) {
		    String[] pairs = toExecuteList.split(",");
		    List<String> ids = new ArrayList<>();

		    for (String pair : pairs) {
		        String[] parts = pair.split("@@");
		        if (parts.length > 0) {
		            ids.add(parts[0].trim()); 
		        }
		    }
		    toExecuteList = String.join(",", ids);
		}

		List<String> toBeExecuteList = Arrays.asList(toExecuteList.split(","));
		if (IsDebugEnabled())
			LOGGER.info("toExecuteList:  " + toExecuteList + ", toBeExecuteList : " + toBeExecuteList
					+ ", stringToFind : " + stringToFind);
		for (String string : toBeExecuteList) {
			if (string.trim().equalsIgnoreCase(stringToFind))
				return true;
		}
		return false;
	}

	public static synchronized boolean isInTobeGroupExecuteList(String stringToFind) {

		String toExecuteList = ConfigManager.getproperty("scenariosFlowToExecute");
		if (toExecuteList != null && toExecuteList.isEmpty())
			return true;

		List<String> toBeExecuteList = Arrays.asList(toExecuteList.split(","));
		if (IsDebugEnabled())
			LOGGER.info("toExecuteList:  " + toExecuteList + ", toBeExecuteList : " + toBeExecuteList
					+ ", stringToFind : " + stringToFind);
		for (String string : toBeExecuteList) {
			if (stringToFind.toLowerCase().contains(string.toLowerCase()))
				return true;
		}
		return false;
	}

	public static Properties getConfigProperties() {
		Properties properties = new Properties();
		for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().toString(); 
			properties.setProperty(key, value);
		}

		return properties;
	}


	public static boolean isInternalApiLoggingForReport() {
		if (Boolean.parseBoolean(System.getProperty("dslrig.internal.api.logging", "false"))) {
			return true;
		}
		try {
			String v = ConfigManager.getproperty("internalApiLogging");
			return v != null && "yes".equalsIgnoreCase(v.trim());
		} catch (Exception e) {
			return false;
		}
	}


	public static String getInternalApiLoggingForContextProps() {
		return isInternalApiLoggingForReport() ? "yes" : "no";
	}


	/** Max chars kept in each report textarea; 0 disables truncation. */
	public static int getReportPayloadMaxChars() {
		String jvm = System.getProperty("dsl.report.payload.max.chars");
		if (jvm != null && !jvm.isBlank()) {
			try {
				return Math.max(0, Integer.parseInt(jvm.trim()));
			} catch (NumberFormatException ignored) {
				return 8192;
			}
		}
		try {
			String v = ConfigManager.getproperty("reportPayloadMaxChars");
			if (v == null || v.isBlank()) {
				return 8192;
			}
			return Math.max(0, Integer.parseInt(v.trim()));
		} catch (Exception e) {
			return 8192;
		}
	}
}
