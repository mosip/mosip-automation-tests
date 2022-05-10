package io.mosip.ivv.e2e.methods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.service.BaseTestCase;

public class WritePreReq extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(WritePreReq.class);

	@Override
	public void run() {

		String value = null;
		String appendedkey = null;
		HashMap<String, String> map = new HashMap<String, String>();
		Reporter.log("==========STEP ====== WritePreReq ", true);
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("PreRequisite Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else if(step.getParameters().size() >= 1) {
			value = step.getParameters().get(0);

			if (value.startsWith("$$")) {
				map = step.getScenario().getVariables();
			}
		}
		if(step.getParameters().size() >= 2) {appendedkey=step.getParameters().get(1);
		map.put("appendedkey", appendedkey);
		}
		// Instantiating the properties file
		Properties props = new Properties();
		// Populating the properties file
		props.putAll(map);
		// Instantiating the FileInputStream for output file
		try {
			String path = (TestRunner.getExternalResourcePath() + "/config/" + BaseTestCase.environment + "_prereqdata_"
					+ appendedkey + ".properties");
			  File file = new File(path);
			 if (file.createNewFile()) {
		            
				 Reporter.log("File has been created at this path : " +path,true);
		        } else {
		        
		        	Reporter.log("File already exists at the path : " + path,true);
		        }
			
			
			FileOutputStream outputStrem = new FileOutputStream(path);
			// Storing the properties file

			props.store(outputStrem, "This is path where file is created" + path);
			Reporter.log(props.toString(), true);
			
			Reporter.log("This is path where file is created" + path, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}