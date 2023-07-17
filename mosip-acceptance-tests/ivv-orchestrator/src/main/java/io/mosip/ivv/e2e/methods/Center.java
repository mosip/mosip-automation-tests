package io.mosip.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Logger;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.CenterHelper;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.apirig.testscripts.PatchWithPathParam;
import io.mosip.testrig.apirig.testscripts.PutWithPathParam;
import io.mosip.testrig.apirig.testscripts.SimplePost;
import io.mosip.testrig.apirig.testscripts.SimplePut;

public class Center extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(Center.class);
	
	SimplePost simplepost=new SimplePost() ;
	PatchWithPathParam patchwithpathparam=new PatchWithPathParam();
	SimplePut simpleput=new SimplePut();
	PutWithPathParam putwithpathparam=new PutWithPathParam();
	CenterHelper centerHelper=new CenterHelper();
	
	//GetWithParam
	@Override
	public void run() throws RigInternalError {
		String id = null;
		int centerCount=0;
		Boolean activeFlag=false;
		String calltype = null;
		HashMap<String, String> map=new HashMap<String, String>();
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			{

				this.hasError=true;
				throw new RigInternalError("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
			}
		} else {
			calltype = step.getParameters().get(0); 

		}
		if(step.getParameters().size() >= 2) { 
			 id = step.getParameters().get(1);
			 if (id.startsWith("$$")) {
					map = step.getScenario().getVariables();
				}}

		if(step.getParameters().size() >= 3) { 
			centerCount = Integer.parseInt(step.getParameters().get(2));
		}		
		
		if(step.getParameters().size() >= 4) { 
			String activecheck = step.getParameters().get(3);
			if (activecheck.contains("t") || activecheck.contains("T")) 
				activeFlag=true;	
		}
		switch (calltype) {
		case "CREATE":
		
			String centerId=centerHelper.centerCreate(id);
			centerHelper.centerUpdate(centerId,id);
			centerHelper.centerStatusUpdate(centerId,activeFlag);
			map.put("centerId"+centerCount, centerId);
			map.put("zoneCode", id);
			map.put("langCode", BaseTestCase.languageCode);
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().putAll(map);
			break;
		case "ACTIVE_FLAG":
			centerHelper.centerStatusUpdate(map.get("centerId"+centerCount),activeFlag);
			break;
		case "UPDATE_NONLANG":
			break;

		case "UPDATE_LANG":
		
			break;
		case "DCOM":
			centerHelper.centerDcom(map.get("centerId"+centerCount));
			break;
	
		default:
			break;
		}

	}


}


