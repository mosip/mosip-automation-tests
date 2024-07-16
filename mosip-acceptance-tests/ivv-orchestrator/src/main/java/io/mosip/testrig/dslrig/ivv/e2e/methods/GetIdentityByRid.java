package io.mosip.testrig.dslrig.ivv.e2e.methods;

import java.io.File;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.mosip.testrig.apirig.utils.FileUtil;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testrig.dslrig.ivv.orchestrator.TestResources;
import io.restassured.response.Response;

@Scope("prototype")
@Component
public class GetIdentityByRid extends BaseTestCaseUtil implements StepInterface {

	private String getIdentityUrl = "/idrepository/v1/identity/idvid/";
	private String identitypath = "preReg/identity/";
	static Logger logger = Logger.getLogger(GetIdentityByRid.class);
	KernelAuthentication kauth = new KernelAuthentication();

	static {
		if (ConfigManager.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() throws RigInternalError {
		getIdentity(this.step.getScenario().getPridsAndRids());
	}

	public void getIdentity(HashMap<String, String> rids) throws RigInternalError {
		step.getScenario().getUinReqIds().clear();
		for (String rid : rids.values()) {
			if (rid != null) {
				long startTime = System.currentTimeMillis();
				logger.info(this.getClass().getSimpleName() + " starts at..." + startTime + " MilliSec");
				Response response = RestClient.getRequestWithCookie(BaseTestCase.ApplnURI + getIdentityUrl + rid,
						MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, "Authorization",
						kauth.getTokenByRole("regproc"));
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				logger.info(
						"Time taken to execute " + this.getClass().getSimpleName() + ": " + elapsedTime + " MilliSec");
				logger.info("Response from get Identity for RID: " + rid + " " + response.asString());
				GlobalMethods.ReportRequestAndResponse("", "", BaseTestCase.ApplnURI + getIdentityUrl + rid, "",
						response.getBody().asString());
				String url = BaseTestCase.ApplnURI + getIdentityUrl + rid;
				JSONObject res = new JSONObject(response.asString());
				if (!res.get("response").toString().equals("null")) {
					JSONObject respJson = new JSONObject(res.get("response").toString());
					JSONObject identityJson = new JSONObject(respJson.get("identity").toString());
					String uin = identityJson.get("UIN").toString();
					step.getScenario().getUinReqIds().put(uin, null);
					FileUtil.createFile(new File(TestResources.getResourcePath() + identitypath + uin + ".json"),
							response.asString());
				} else {
					logger.error("Issue while fetching identity for RID: " + rid + " Response: " + res.toString());
					this.hasError = true;
					throw new RigInternalError("Not able to Fetch identity for RID: " + rid);
				}

			}
		}
	}

}
