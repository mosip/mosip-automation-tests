package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;

public class ThreadCountChanger implements IAlterSuiteListener  {
	static Logger logger = Logger.getLogger(ThreadCountChanger.class);
	
	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	@Override
    public void alter(List<XmlSuite> suites) {
		/* System.err.println("**Alter is invoked**"); */

        ConfigManager.init();
        int count = Integer.parseInt(ConfigManager.getThreadCount());
        
        logger.info("Running suite with thread count : "+count);

        for (XmlSuite suite : suites) {
            suite.setThreadCount(count);
        }
    }
}


