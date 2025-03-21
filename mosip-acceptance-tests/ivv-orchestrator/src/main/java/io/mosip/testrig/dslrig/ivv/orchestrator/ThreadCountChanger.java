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
	    System.out.println("ThreadCountChanger invoked!");
	    logger.info("ThreadCountChanger invoked!");

	    int count = Integer.parseInt(dslConfigManager.getThreadCount()); // Read thread count from config
	    logger.info("Running suite with thread count: " + count);

	    for (XmlSuite suite : suites) {
	        logger.info("Before setting, thread count for suite: " + suite.getName() + " -> " + suite.getThreadCount());

	        suite.setParallel(XmlSuite.ParallelMode.METHODS);
	        suite.setThreadCount(count);

	        suite.getTests().forEach(test -> {
	            test.setParallel(XmlSuite.ParallelMode.METHODS);
	            test.setThreadCount(count);
	        });

	        logger.info("Thread count set for suite: " + suite.getName() + " -> " + suite.getThreadCount());
	    }
	}
}


