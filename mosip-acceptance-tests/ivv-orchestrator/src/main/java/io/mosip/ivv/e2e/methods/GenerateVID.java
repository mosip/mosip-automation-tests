package io.mosip.ivv.e2e.methods;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthPartnerProcessor;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.testscripts.BioAuth;
import io.mosip.testscripts.DemoAuth;
import io.mosip.testscripts.OtpAuth;
import io.mosip.testscripts.PostWithBodyWithOtpGenerate;
import io.restassured.response.Response;

public class GenerateVID extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(GenerateVID.class);
	private static final String GenerateVID = "idaData/GenerateVID/createGenerateVID.yml";
	Properties uinResidentDataPathFinalProps = new Properties();
	PostWithBodyWithOtpGenerate generatevid=new PostWithBodyWithOtpGenerate() ;

	@Override
	public void run() throws RigInternalError {
		//uinPersonaProp.put("6471974360", "C:\\\\Users\\\\Sohan.Dey\\\\AppData\\\\Local\\\\Temp\\\\residents_1250718917110156783\\\\101681016810168.json");
		vidPersonaProp.clear();
		String uins = null;
		String vidtype = null;
		List<String> uinList = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("VID Type[Perpetual/Temporary] parameter is  missing from DSL step");
			throw new RigInternalError("VID Type[Perpetual/Temporary] paramter is  missing in step: " + step.getName());
		} else {
			vidtype = step.getParameters().get(0); 

		}
		if (step.getParameters().size() == 2) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());
		
		
		Object[] testObj=generatevid.getYmlTestData(GenerateVID);

		TestCaseDTO test=(TestCaseDTO)testObj[0];
	
	for (String uin : uinList) {
		String input=test.getInput();
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					uin, "individualId");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
				 vidtype, "vidType");
		 input = JsonPrecondtion.parseAndReturnJsonContent(input,
					uin, "sendOtp.individualId");
		test.setInput(input);
		
		
		
		try {
			generatevid.test(test);
			Response response= generatevid.response;
			if (response!= null)
			{
				JSONObject jsonResp = new JSONObject(response.getBody().asString());
		        String vid = jsonResp.getJSONObject("response").getString("vid"); 
		        vidPersonaProp.put(vid, uin);
		        
		        System.out.println(vidPersonaProp);
			}
			
		} catch (AuthenticationTestException | AdminTestException e) {
			throw new RigInternalError(e.getMessage());

		}
	}

	}
}
		
		