package io.mosip.ivv.orchestrator;

import java.util.List;

import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

public class ThreadCountChanger implements IAlterSuiteListener  {
	
	@Override
    public void alter(List<XmlSuite> suites) {
        System.err.println("**Alter is invoked**");
        int count = Integer.parseInt(System.getProperty("threadCount", "1"));

        for (XmlSuite suite : suites) {
            suite.setThreadCount(count);
        }
    }
}


