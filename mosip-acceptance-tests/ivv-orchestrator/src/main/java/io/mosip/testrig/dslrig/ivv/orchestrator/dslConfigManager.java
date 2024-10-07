package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

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
				moduleSpecificPropertiesMap.put(key, props.getProperty(key));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		// Add module specific properties as well.
		init(moduleSpecificPropertiesMap);
	}

	
	  public static String getmountPathForScenario() { return
	  ConfigManager.getproperty("mountPathForScenario"); }
	  
	  public static String getThreadCount() { return
	  ConfigManager.getproperty("threadCount"); }
	  
	  public static Boolean useExternalScenarioSheet() { return
	  ConfigManager.getproperty("useExternalScenarioSheet").equalsIgnoreCase("yes");
	  }
	  
	  public static String getpacketUtilityBaseUrl() { return
	  ConfigManager.getproperty("packetUtilityBaseUrl"); }
	 

	public static synchronized boolean isInTobeSkippedList(String stringToFind) {
		String toSkippedList = ConfigManager.getproperty("servicesNotDeployed");
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

	public static synchronized boolean isInTobeExecuteList(String stringToFind) {
		// If there are no specific execution list is provided , execute all scenarios
		String toExecuteList = ConfigManager.getproperty("scenariosToExecute");
		if (toExecuteList != null && toExecuteList.isEmpty())
			return true;

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
