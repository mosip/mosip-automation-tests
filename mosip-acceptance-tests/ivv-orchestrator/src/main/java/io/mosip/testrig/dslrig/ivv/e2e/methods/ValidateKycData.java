package io.mosip.testrig.dslrig.ivv.e2e.methods;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.mosip.testrig.apirig.kernel.util.ConfigManager;
import io.mosip.testrig.apirig.service.BaseTestCase;
import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;

public class ValidateKycData  extends BaseTestCaseUtil implements StepInterface {
	boolean isDataPresent = false;
	String data = "";
	String responce = "";
	static Logger logger = Logger.getLogger(ValidateKycData.class);
	protected static final String AUTH_POLICY_REQUEST1 = "config/AuthPolicy5.json";
	String newResponse= "";
	org.json.JSONObject responseJson;
	@Override
	public void run() throws RigInternalError, FeatureNotSupportedError {

		if (step.getParameters() == null || step.getParameters().isEmpty()) {
			logger.error("Parameter is  missing from DSL step");
			assertTrue(false, " paramter is  missing in step: " + step.getName());
		} else if (step.getParameters().size() == 2) {
			data = step.getParameters().get(0);
			responce = step.getParameters().get(1);
		}
		newResponse= step.getScenario().getVariables() .get(responce);
		responseJson = new org.json.JSONObject(newResponse);
		//validateKycData();
//
//		org.json.simple.JSONObject AUTH_POLICY_REQUEST2 = BaseTestCase.getRequestJson(AUTH_POLICY_REQUEST1);
//		org.json.simple.JSONArray allowedKycAttributes = (JSONArray) AUTH_POLICY_REQUEST2.get("allowedKycAttributes");
//
//		for (Object attribute : allowedKycAttributes) {
//			org.json.simple.JSONObject attributeObject = (JSONObject) attribute;
//			if (data.equals(attributeObject.get("attributeName"))) {
//				isDataPresent = true;
//				break;
//			}
//		}
		if(responseJson.has(data)) {
			System.out.println("data is there");
			logger.info(data +" data is there");
			logger.info(responseJson);
		}else {
			logger.info(data +" data is not present");
		}
	}
//	public void validateKycData() {
//		org.json.simple.JSONObject AUTH_POLICY_REQUEST2 = BaseTestCase.getRequestJson(AUTH_POLICY_REQUEST1);
//		org.json.simple.JSONArray allowedKycAttributes = (JSONArray) AUTH_POLICY_REQUEST2.get("allowedKycAttributes");
//
//		for (Object attribute : allowedKycAttributes) {
//			org.json.simple.JSONObject attributeObject = (JSONObject) attribute;
//			if (data.equals(attributeObject.get("attributeName"))) {
//				isDataPresent = true;
//				break;
//			}
//		}
//		if(responseJson.has(data)) {
//			System.out.println("data is there");
//		}
//
//	}


}






