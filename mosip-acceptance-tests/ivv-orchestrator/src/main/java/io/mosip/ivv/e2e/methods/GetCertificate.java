package io.mosip.ivv.e2e.methods;

import java.util.HashMap;

import org.apache.log4j.Logger;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.RegprocStatusHelper;

public class GetCertificate extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GetCertificate.class);

	RegprocStatusHelper regprocStatusHelper = new RegprocStatusHelper();

	@SuppressWarnings("unchecked")
	@Override
	public void run() throws RigInternalError {

		String personFilePathvalue = null;

		String thumbprint = null;
/*
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			throw new RigInternalError(
					"Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		} 
		
		if (step.getParameters().size() ==1) {
			String id = step.getParameters().get(0);

			if (id.startsWith("$$")) {
				HashMap<String, String> map = step.getScenario().getVariables();
				personFilePathvalue = map.get("$$personaFilePath");
//C:\Users\NEEHAR~1.GAR\AppData\Local\Temp\residents_11970612103081897992\1390477148.json
			}
		}
		if (step.getParameters().size() ==2) {
			String id = step.getParameters().get(1);

			if (id.startsWith("$$")) {
				HashMap<String, String> map = step.getScenario().getVariables();
				personFilePathvalue = map.get("personFilePathvalue");

			}
		}
		*/
		personFilePathvalue="C:\\Users\\NEEHAR~1.GAR\\AppData\\Local\\Temp\\residents_11970612103081897992\\1390477148.json";
		thumbprint = regprocStatusHelper.getCert();
		HashMap<String, String> map = new HashMap<String, String>();
		if (step.getOutVarName() != null) {
			map.put("thumbprint", thumbprint);
			step.getScenario().getVariables().putAll(map);

		}
		//regprocStatusHelper.operatoronboard(thumbprint, personFilePathvalue);
	}
}