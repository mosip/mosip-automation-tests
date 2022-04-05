package io.mosip.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.CenterHelper;
import io.mosip.ivv.orchestrator.UserHelper;
import io.mosip.testscripts.DeleteWithParam;
import io.mosip.testscripts.GetWithParam;
import io.mosip.testscripts.GetWithQueryParam;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

public class User extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(User.class);
	private static final String PostCenter = "masterdata/RegistrationCenter/CreateRegistrationCenter.yml";
	
	UserHelper userHelper=new UserHelper();
	
	//GetWithParam
	@Override
	public void run() throws RigInternalError {
		String id = null;
		String user=null;
		String pwd=null;
		String calltype = null;
		String userZone=null;
		HashMap<String, String> map=new HashMap<String, String>();
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			throw new RigInternalError("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		} else {
			calltype = step.getParameters().get(0); 

		}
		if(step.getParameters().size() >= 2) { 
			 user = step.getParameters().get(1);
			 if(user.contains("@@")){
				 	String userDetails[]=user.split("@@");
				 	user=userDetails[0];
				 	pwd=userDetails[1];
				 	}
			 if (user.startsWith("$$")) {
				 map = step.getScenario().getVariables();
				 user = map.get("userid");

				}
		}		
		if(step.getParameters().size() >= 3) { 
			 id = step.getParameters().get(2);
			 if (id.startsWith("$$")) {
					map = step.getScenario().getVariables();

				}
		}	
		if(step.getParameters().size() >= 4) { 
			userZone = step.getParameters().get(3);
		}
		switch (calltype) {
		case "DELETE_CENTERMAPPING":
			map.put("userid",user);
			map.put("userpassword", pwd);
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().putAll(map);
			userHelper.deleteCenterMapping(user);
			
			break;
		case "DELETE_ZONEMAPPING":
			userHelper.deleteZoneMapping(user,map,userZone);
			break;
		case "CREATE_CENTERMAPPING":
			userHelper.createCenterMapping(user,map);
			break;

		case "CREATE_ZONEMAPPING":
			userHelper.createZoneMapping(map,user);
			break;
		case "ACTIVATE_CENTERMAPPING":
			userHelper.activateCenterMapping(user,id);
			break;
		case "ACTIVATE_ZONEMAPPING":
			userHelper.activateZoneMapping(user,id);
			break;
		default:
			break;
		}

	}


}

