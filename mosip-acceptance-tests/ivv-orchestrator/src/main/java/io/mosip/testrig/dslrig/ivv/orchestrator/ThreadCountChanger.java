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
	    logger.info("ThreadCountChanger invoked!");

	    int workerCount = Integer.parseInt(dslConfigManager.getThreadCount());
	    // When park/resume is on, TestNG may submit many scenarios while only workerCount
	    // run steps concurrently; parked scenarios free workers for others.
	    int count = dslConfigManager.isParkResumeEnabled()
	    		? dslConfigManager.getMaxInFlightScenarios()
	    		: workerCount;
	    logger.info("Running suite with TestNG thread count: " + count
	    		+ " (active park workers=" + workerCount + ", parkResume="
	    		+ dslConfigManager.isParkResumeEnabled() + ")");

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
