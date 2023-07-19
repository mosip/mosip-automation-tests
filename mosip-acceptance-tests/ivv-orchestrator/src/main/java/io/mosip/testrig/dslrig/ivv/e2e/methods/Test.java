package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;


	public class Test extends BaseTestCaseUtil implements StepInterface {
		Logger logger = Logger.getLogger(Test.class);

		@Override
		public void run() throws RigInternalError {
			int loopCount =0;
			
			/*
			 * StepInterface st = getInstanceOf(step); st.setExtentInstance(extentTest);
			 * st.setSystemProperties(properties); st.setState(store); st.setStep(step);
			 * st.setup(); st.validateStep();
			 */
			
//			get Number of iterations from actuator
//			int loopCount = valuefrom actuator
//			while(loopCount){  

//			"$$personaFilePath3=e2e_getResidentData(1,true,false,Any)",
//			   getResidentData.run();
			
		    if (step.getParameters().size()==1) 
		    loopCount = Integer.parseInt(step.getParameters().get(0));
		    
		    while(loopCount>0) {
	
		 	GetResidentData getResidentData = new GetResidentData();
		 	ArrayList<String> parameters = new ArrayList<String>();
		 	
		
		 	parameters.add("1");
		 	parameters.add("true");
		 	parameters.add("false");
   		    parameters.add("Any");
   		    

   		    
   		    step.setParameters(parameters);
		 	
   		// nofResident=Integer.parseInt(step.getParameters().get(0));
   		    
			getResidentData.run(); 
			//  step.getScenario().getVariables().put(step.getOutVarName(), resFilePath);
			
			
			
			GetPacketTemplate packetTemplate = new GetPacketTemplate();
			packetTemplate.run();			
}
}}