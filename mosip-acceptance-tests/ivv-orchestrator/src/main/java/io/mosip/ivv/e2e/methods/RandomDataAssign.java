package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;


import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;

import io.mosip.ivv.orchestrator.BaseTestCaseUtil;


public class RandomDataAssign extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(RandomDataAssign.class);
	public static String _additionalInfo=null;
	@Override
	public void run() throws RigInternalError {
		String data=null;
		
		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false,"process paramter is  missing in step: "+step.getName());
		} else if(step.getParameters().size() == 1){
			data =step.getParameters().get(0);}
		if(step.getOutVarName()!=null)
							step.getScenario().getVariables().put(step.getOutVarName(), data);
					}
				}

	