package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestRunner;

public class WritePreReq extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(WritePreReq.class);

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		String value = null;
		String appendedkey = null;
		HashMap<String, String> map = new HashMap<String, String>();
//		Reporter.log("==========STEP ====== WritePreReq ");
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("PreRequisite Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else if (step.getParameters().size() >= 1) {
			value = step.getParameters().get(0);

			if (value.startsWith("$$")) {
				map = step.getScenario().getVariables();
			}
		}
		if (step.getParameters().size() >= 2) {
			appendedkey = step.getParameters().get(1);
			map.put("appendedkey", appendedkey);
		}
		Properties props = new Properties();
		Properties kernelprops = ConfigManager.propsKernel;
		try {
			props.putAll(kernelprops);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (entry.getValue() == null) {
					props.setProperty(entry.getKey(), "");
				} else if (entry.getValue() != null)
					props.setProperty(entry.getKey(), entry.getValue());
			}
			// props.putAll(map);
			String path = (TestRunner.getExternalResourcePath() + "/config/" + BaseTestCase.environment + "_prereqdata_"
					+ appendedkey + ".properties");
			HashMap<String, String> propertiesMap = new HashMap<String, String>();
			for (Entry<Object, Object> entry : props.entrySet()) {
				propertiesMap.put((String) entry.getKey(), (String) entry.getValue());
			}
			prereqDataSet.put(path, propertiesMap);
			/*
			 * if (ConfigManager.IsDebugEnabled()) Reporter.log(props.toString());
			 */
		} catch (Exception e) {
			this.hasError = true;
			logger.error(e.getMessage());
			throw new RigInternalError("WritePreRequisite Data is not returned properly");
		}
	}
}