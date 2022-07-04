package io.mosip.ivv.e2e.methods;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.service.BaseTestCase;

public class SetContext extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(SetContext.class);

	@Override
	public void run() throws RigInternalError {
		constantIntializer();
		String contextKeyValue = "dev_context";
	
		String userAndMachineDetailParam = null;
		String mosipVersion = null;
		boolean generatePrivateKey = Boolean.FALSE;
		String status = null;
		String negative="valid";
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> dummyholder = new HashMap<String, String>();
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.warn("SetContext Arugemnt is  Missing : Please pass the argument from DSL sheet");
		} else {
			contextKeyValue = step.getParameters().get(0);
			// contextKey.put("contextKey",contextKeyValue );
			contextKey.put(contextKeyValue, "true");
			contextInuse.clear();
			contextInuse.put("contextKey", contextKeyValue);
			if (step.getParameters().size() > 1) { // machineid=112121@@.......
				String value = step.getParameters().get(1);
				if (!(value.equalsIgnoreCase("-1")) && value.contains("@@"))
					userAndMachineDetailParam = value;
				else if (value.startsWith("$$")) {
					
					map = step.getScenario().getVariables();
				}
			}
			if (step.getParameters().size() > 2) { // 1@@2(mosip.version)
				List<String> version = PacketUtility.getParamsArg(step.getParameters().get(2), "@@");
				if (!(version.contains("-1")))
					mosipVersion = version.get(0) + "." + version.get(1);
			}
			if (step.getParameters().size() > 3) // true/false (want to generate privatekey)
				generatePrivateKey = Boolean.parseBoolean(step.getParameters().get(3));

			if (step.getParameters().size() > 4) // deactivate
				status = step.getParameters().get(4);
			if (step.getParameters().size() > 5) //  for negative operator and supervisor 
				negative = step.getParameters().get(5);
			
		}
		
		
		if (userAndMachineDetailParam != null)
			packetUtility.createContexts(contextKeyValue, userAndMachineDetailParam, mosipVersion, generatePrivateKey,
					status, BaseTestCase.ApplnURI + "/");
		else if(map != null)
			packetUtility.createContexts(negative,contextKeyValue, map, mosipVersion, generatePrivateKey, status,
					BaseTestCase.ApplnURI + "/");
	}
}
