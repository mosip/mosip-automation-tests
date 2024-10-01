package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;


public class ThreadCountChanger implements IAlterSuiteListener  {
	static Logger logger = Logger.getLogger(ThreadCountChanger.class);
	
	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	@Override
    public void alter(List<XmlSuite> suites) {
		/* System.err.println("**Alter is invoked**"); */

       // ConfigManager.init();
        int count = Integer.parseInt(dslConfigManager.getThreadCount());
        
        logger.info("Running suite with thread count : "+count);

        for (XmlSuite suite : suites) {
            suite.setThreadCount(count);
        }
    }
}


