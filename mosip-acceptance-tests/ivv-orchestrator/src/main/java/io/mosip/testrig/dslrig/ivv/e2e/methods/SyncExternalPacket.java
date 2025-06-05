package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.PacketUtility;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;

public class SyncExternalPacket extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(SyncExternalPacket.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {
		String rid=null;
		
		if (step.getParameters().isEmpty() && !step.getScenario().getGeneratedResidentData().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, "process paramter is  missing in step: " + step.getName());
		} else {			
				 rid = step.getParameters().get(0);
				if (rid.startsWith("$$")) {
					rid = step.getScenario().getVariables().get(rid);
					if (rid == null)
						logger.info("RID is null");
			}
			String responce = packetUtility.syncAndTriggerPacket(rid, step.getScenario().getCurrentStep(), step);
			JSONObject jsonObject = new JSONObject(responce);

	        if (jsonObject.has("workflowInstanceId")) {
	            String workflowInstanceId = jsonObject.getString("workflowInstanceId");
	            logger.info("workflowInstanceId: " + workflowInstanceId);
	        } else {
	            throw new RuntimeException("workflowInstanceId is missing in the response.");
	        }

			
			
			
		}

	}
}
