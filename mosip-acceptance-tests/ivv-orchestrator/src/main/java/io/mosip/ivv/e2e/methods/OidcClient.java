package io.mosip.ivv.e2e.methods;

import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePostForAutoGenId;
import io.restassured.response.Response;

public class OidcClient extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(OidcClient.class);
	private static final String OidcClient = "idaData/CreateOIDCClient/CreateOIDCClient.yml";
	SimplePostForAutoGenId oidcClient=new SimplePostForAutoGenId() ;

	@Override
	public void run() throws RigInternalError {
		
		oidcClientProp.clear();
		
		
		Object[] testObj=oidcClient.getYmlTestData(OidcClient);

		TestCaseDTO test=(TestCaseDTO)testObj[0];
	
		try {
			try {
				oidcClient.test(test);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Response response= oidcClient.response;
			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
		        String clientId = jsonResp.getJSONObject("response").getString("clientId"); 
		        
		        oidcClientProp.put("clientId", clientId); //"$$clientId=e2e_OidcClient()"
		        
		        System.out.println(clientId);
			}
			
		} catch (AuthenticationTestException | AdminTestException e) {
			throw new RigInternalError(e.getMessage());

		}

	}
}
		
		