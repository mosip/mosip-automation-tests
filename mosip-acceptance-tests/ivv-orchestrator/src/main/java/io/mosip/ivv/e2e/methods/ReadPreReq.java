package io.mosip.ivv.e2e.methods;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.testrig.apirig.service.BaseTestCase;

public class ReadPreReq extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(ReadPreReq.class);

	@Override
	public void run() throws RigInternalError {

		HashMap<String, String> map = new HashMap<String, String>();
		Properties propertylist = new Properties();
		String path = null;
		String appendedkey = null;

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("PreRequisite Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else if (step.getParameters().size() >= 1) {
			appendedkey = step.getParameters().get(0);
		}
		path = (TestRunner.getExternalResourcePath() + "/config/" + BaseTestCase.environment + "_prereqdata_"
				+ appendedkey + ".properties");

		logger.info("ReadPreReq :" + path);
		try(FileReader reader = new FileReader(path);) {
			if (prereqDataSet.get(path) == null) {

				
				propertylist.load(reader);

				for (String propertykey : propertylist.stringPropertyNames()) {
					String val = propertylist.getProperty(propertykey);
					map.put(propertykey, val);
				}

				prereqDataSet.put(path, map);
			}
			if (step.getOutVarName() != null) {

				step.getScenario().getVariables().putAll(prereqDataSet.get(path));
			}

			Reporter.log(prereqDataSet.get(path).toString(), true);
		} catch (Exception e) {
			this.hasError = true;
			logger.error(e.getMessage());
			throw new RigInternalError("PreRequisite Data is not set properly");

		} 
		

	}

}
