package io.mosip.ivv.e2e.methods;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.CenterHelper;
import io.mosip.testscripts.DeleteWithParam;
import io.mosip.testscripts.GetWithParam;
import io.mosip.testscripts.GetWithQueryParam;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

public class Center extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(Center.class);
	private static final String PostCenter = "masterdata/RegistrationCenter/CreateRegistrationCenter.yml";
	private static final String PatchCenter = "masterdata/UpdateRegCentStatus/UpdateRegCentStatus.yml";
	private static final String PutCenterNonLang = "masterdata/UpdateRegistrationCenterNonLanguage/UpdateRegistrationCenterNonLanguage.yml";
	private static final String PutCenterLang = "masterdata/UpdateRegistrationCenterLang/UpdateRegistrationCenterLang.yml";
	private static final String PutCenterDecom = "masterdata/DecommissionRegCenter/DecommissionRegCenter.yml";
	
	SimplePost simplepost=new SimplePost() ;
	PatchWithPathParam patchwithpathparam=new PatchWithPathParam();
	SimplePut simpleput=new SimplePut();
	PutWithPathParam putwithpathparam=new PutWithPathParam();
	CenterHelper centerHelper=new CenterHelper();
	//GetWithParam
	@Override
	public void run() throws RigInternalError {
		String id = null;
		Boolean activeFlag=false;
		String calltype = null;

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			throw new RigInternalError("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		} else {
			calltype = step.getParameters().get(0); 

		}
		if(step.getParameters().size() == 2) { 
			String activecheck = step.getParameters().get(1);
			if (activecheck.contains("t") || activecheck.contains("T")) 
				activeFlag=true;
		}		

		switch (calltype) {
		case "CREATE":
		
			String centerId=centerHelper.centerCreate();
			centerHelper.centerUpdate(centerId);
			centerHelper.centerStatusUpdate(centerId,activeFlag);
			//centerHelper.centerDcom(centerId);
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().put(step.getOutVarName(), centerId);
			break;
		case "ACTIVE_FLAG":
			
			break;
		case "UPDATE_NONLANG":
			
			break;

		case "UPDATE_LANG":
		
			break;
		case "DCOM":
			
			break;
	
		default:
			break;
		}

	}


}


