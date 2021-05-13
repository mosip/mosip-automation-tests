package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.service.BaseTestCase;

public class SetContext extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SetContext.class);
	
	 

@Override
public void run() throws RigInternalError {
	constantIntializer();
	String contextKeyValue="dev_context";
	String userAndMachineDetailParam=null;
	if (step.getParameters() == null || step.getParameters().isEmpty() ||step.getParameters().size()<1) {
		logger.warn("SetContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
	} else {
		contextKeyValue=step.getParameters().get(0);
		//contextKey.put("contextKey",contextKeyValue );
		contextKey.put(contextKeyValue,"true");
		contextInuse.clear();
		contextInuse.put("contextKey",contextKeyValue);
		if(step.getParameters().size()>1)
			userAndMachineDetailParam=step.getParameters().get(1);
	}
	//packetUtility.createContext(contextKeyValue,BaseTestCase.ApplnURI+"/");
	packetUtility.createContexts(contextKeyValue,userAndMachineDetailParam,BaseTestCase.ApplnURI+"/");
	
}

}
