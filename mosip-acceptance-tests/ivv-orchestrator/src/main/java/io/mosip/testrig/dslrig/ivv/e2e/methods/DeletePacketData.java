package io.mosip.testrig.dslrig.ivv.e2e.methods;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.dslConfigManager;
import io.restassured.response.Response;

public class DeletePacketData extends BaseTestCaseUtil implements StepInterface {
	public static Logger logger = Logger.getLogger(CheckRIDStage.class);

	static {
		if (dslConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@Override
	public void run() throws RigInternalError {

		String url = baseUrl + props.getProperty("deletePacketData");
		Response response = deleteRequest(url, "deletePacketData", step);

		String responseStr = response.getBody().asString();

		if (responseStr.contains("successfully")) {
			GlobalMethods.reportRequest("Delete Packet Data", responseStr);
			Reporter.log("<b style=\"background-color: #0A0;\">Marking test case as passed. As " + responseStr
					+ "</b><br>\n");
		} else {
			logger.info(response.toString() + " data is unable to delete");
		}
	}

}
