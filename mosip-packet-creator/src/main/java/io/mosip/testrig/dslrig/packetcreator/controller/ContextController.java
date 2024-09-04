package io.mosip.testrig.dslrig.packetcreator.controller;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.testrig.dslrig.dataprovider.BiometricDataProvider;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.util.RestClient;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.service.ContextUtils;
import com.sun.management.OperatingSystemMXBean;
@RestController
public class ContextController {

	@Autowired
	ContextUtils contextUtils;
	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	private static final Logger logger = LoggerFactory.getLogger(ContextController.class);

	@PostMapping(value = "/context/server/{contextKey}")
	public @ResponseBody String createServerContext(@RequestBody Properties contextProperties,
			@PathVariable("contextKey") String contextKey) {

		logger.info("--------------------Scenario : " + contextProperties.getProperty("scenario")
				+ "---------------------------------------");
		try {
			if (personaConfigPath != null && !personaConfigPath.equals(""))
				DataProviderConstants.RESOURCE = personaConfigPath;
			VariableManager.Init(contextKey);
			/**
			 * String generatePrivateKey =
			 * contextProperties.getProperty("generatePrivateKey"); boolean isRequired =
			 * Boolean.parseBoolean(generatePrivateKey); if (isRequired)
			 * contextUtils.generateKeyAndUpdateMachineDetail(contextProperties,
			 * contextKey);
			 **/
			
			/*
			 * if (RestClient.isDebugEnabled(contextKey)) { OperatingSystemMXBean osBean =
			 * ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class); logger.
			 * info("getProcessCpuLoad What % CPU load this current JVM is taking, from 0.0-1.0"
			 * + osBean.getProcessCpuLoad()); logger.info(
			 * "getSystemCpuLoad What % load the overall system is at, from 0.0-1.0" +
			 * osBean.getSystemCpuLoad()); logger.info(
			 * "Returns the amount of virtual memory that is guaranteed to be available to the running process in bytes, or -1 if this operation is not supported:"
			 * + Long.toString(osBean.getCommittedVirtualMemorySize()));
			 * logger.info("Returns the amount of free physical memory in bytes:" +
			 * Long.toString(osBean.getFreePhysicalMemorySize())); logger.info(
			 * "Returns the amount of free swap space in bytes:" +
			 * Long.toString(osBean.getFreeSwapSpaceSize())); logger.
			 * info("Returns the recent cpu usage for the Java Virtual Machine process:" +
			 * Double.toString(osBean.getProcessCpuLoad())); logger.info(
			 * "Returns the CPU time used by the process on which the Java virtual machine is running in nanoseconds:"
			 * + Long.toString(osBean.getProcessCpuTime())); logger.info(
			 * "Returns the recent cpu usage for the whole system:" +
			 * Double.toString(osBean.getSystemCpuLoad()));
			 * logger.info("Returns the total amount of physical memory in bytes:" +
			 * Long.toString(osBean.getTotalPhysicalMemorySize())); logger.info(
			 * "Returns the total amount of swap space in bytes:" +
			 * Long.toString(osBean.getTotalSwapSpaceSize())); }
			 */
			 
			return contextUtils.createUpdateServerContext(contextProperties, contextKey);
		} catch (Exception ex) {
			logger.error("createServerContext", ex);
			return "{\"" + ex.getMessage() + "\"}";
		}
	}

	@GetMapping(value = "/context/server/{contextKey}")
	public @ResponseBody Properties getServerContext(@PathVariable("contextKey") String contextKey) {
		Properties bRet = null;
		try {
			bRet = contextUtils.loadServerContext(contextKey);
		} catch (Exception ex) {
			logger.error("createServerContext", ex);
		}
		return bRet;
	}

	@GetMapping(value = "/resetContextData/{contextKey}")
	public @ResponseBody String resetContextData(@PathVariable("contextKey") String contextKey) {
		try {
			return VariableManager.deleteNameSpace(
					VariableManager.getVariableValue(contextKey, "urlBase").toString() + "run_context");
		} catch (Exception ex) {
			logger.error("resetNameSpaceData", ex);
			return "{\"" + ex.getMessage() + "\"}";
		}
	}
}
