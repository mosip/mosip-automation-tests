package io.mosip.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.MachineHelper;
import io.mosip.testscripts.DeleteWithParam;
import io.mosip.testscripts.GetWithParam;
import io.mosip.testscripts.GetWithQueryParam;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePostForAutoGenId;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

public class Machine extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(Machine.class);
	
	MachineHelper machineHelper=new MachineHelper();

	@SuppressWarnings("unchecked")
	@Override
	public void run() throws RigInternalError {
		String id = null;
		String activecheck="T";
		String calltype = null;
		int centerCount=0;
		HashMap<String, String> machineDetailsmap=null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			throw new RigInternalError("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		} else {
			calltype = step.getParameters().get(0); 

		}
		if(step.getParameters().size() >= 2 && step.getParameters().get(1).startsWith("$$")) { 
			id = step.getParameters().get(1);
			if (id.startsWith("$$")) {
				machineDetailsmap = step.getScenario().getVariables();
			}
		} if(step.getParameters().size() >= 3) {
			 centerCount = Integer.parseInt(step.getParameters().get(2));}
		 if(step.getParameters().size() >= 4) {
			 activecheck = step.getParameters().get(3);}
		switch (calltype) {
		case "CREATE":
			logger.info("Usage of this step :   $$machineDetails=e2e_Machine(CREATE,$$centerId1,T)");
			String machinetypecode=machineHelper.createMachineType();
			String machinetypestatus=machineHelper.activateMachineType(machinetypecode,activecheck);
				System.out.println(machinetypecode + " " + machinetypestatus);
				String machinespecId=machineHelper.createMachineSpecification(machinetypecode);
			    machineHelper.activateMachineSpecification(machinespecId,activecheck);
			    
			   machineDetailsmap=machineHelper.createMachine(machinespecId,machineDetailsmap,centerCount);
				machineHelper.activateMachine(machineDetailsmap.get("machineid"),activecheck);
				
				if (step.getOutVarName() != null)
					step.getScenario().getVariables().putAll(machineDetailsmap);
				Reporter.log(machineDetailsmap.toString(), true);
			break;
		case "ACTIVE_FLAG":
			machineHelper.activateMachine(machineDetailsmap.get("machineid"),activecheck);
			break;

		case "UPDATE":
			machineDetailsmap=machineHelper.updateMachine(machineDetailsmap,centerCount);
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().putAll(machineDetailsmap);
			Reporter.log(machineDetailsmap.toString(), true);
			break;
			
		case "DCOM":
			machineHelper.dcomMachine(machineDetailsmap.get("machineid")); 
			break;
		
		default:
			break;
		}

	}


}
