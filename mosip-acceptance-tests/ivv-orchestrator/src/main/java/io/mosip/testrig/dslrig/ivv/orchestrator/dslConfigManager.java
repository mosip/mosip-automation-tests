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
		// Load scope specific properties
		try {
			String path = TestRunner.getGlobalResourcePath() + "/config/dsl.properties";
			Properties props = getproperties(path);
			// Convert Properties to Map and add to moduleSpecificPropertiesMap
			for (String key : props.stringPropertyNames()) {
                  
				String value = System.getenv(key) == null ? props.getProperty(key) : System.getenv(key);
				moduleSpecificPropertiesMap.put(key, value);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		// Add module specific properties as well.
		init(moduleSpecificPropertiesMap);
	}

	public static String getmountPathForScenario() {
		return ConfigManager.getproperty("mountPathForScenario");
	}

	public static String getThreadCount() {
		return ConfigManager.getproperty("threadCount");
	}
	
	public static String getUinWaitTime() {
		return ConfigManager.getproperty("uinWaitTime");
	}
	
	public static String getNextPacketUploadWaitTime() {
		return ConfigManager.getproperty("nextPacketUploadWaitTime");
	}

	public static Boolean useExternalScenarioSheet() {
		return ConfigManager.getproperty("useExternalScenarioSheet").equalsIgnoreCase("yes");
	}

	public static String getpacketUtilityBaseUrl() {
		return ConfigManager.getproperty("packetUtilityBaseUrl");
	}
	
	public static String getEsignetMockBaseURL() { 
		return getproperty("esignetMockBaseURL");
	}
	
	public static int getLangselect() {	
		return Integer.parseInt(getproperty("langselect")); 
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
	
	public static synchronized boolean isInTobeBugList(String stringToFind) {
		List<String> mergedSkipList = new ArrayList<>();
//	     Add from file
		List<String> fileSkipList = loadTestcaseToBeSkippedList();
		for (String scenario : fileSkipList) {
			if (!mergedSkipList.contains(scenario)) {
				mergedSkipList.add(scenario);
			}
		}

		if (IsDebugEnabled())
			LOGGER.info("Final skip list: " + mergedSkipList + ", stringToFind: " + stringToFind);

		for (String scenario : mergedSkipList) {
			if (scenario.equalsIgnoreCase(stringToFind)) {
				return true;
			}
		}
		return false;
	}

	
	 public static List<String> loadTestcaseToBeSkippedList() {
	        List<String> testcaseToBeSkippedList = new ArrayList<>();
	        try (BufferedReader br = new BufferedReader(
	                new FileReader(BaseTestCase.getGlobalResourcePath() + "/" + "config/TestCaseSkip.txt"))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	                // Ignore lines starting with # as they are comments
	                if (line.startsWith("#")) {
	                    continue;
	                }

	                // Split the line by "==" and store the second part
	                if (line.contains("==")) {
	                    String[] parts = line.split("==");
	                    if (parts.length > 1) {
	                        testcaseToBeSkippedList.add(parts[1].trim());
	                    }
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return testcaseToBeSkippedList;
	    }


	public static synchronized boolean isInTobeExecuteList(String stringToFind) {
		// If there are no specific execution list is provided , execute all scenarios
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
		// If there are no specific execution list is provided , execute all scenarios
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
			String value = entry.getValue().toString(); // Convert the value to a String
			properties.setProperty(key, value);
		}

		return properties;
	}
}
