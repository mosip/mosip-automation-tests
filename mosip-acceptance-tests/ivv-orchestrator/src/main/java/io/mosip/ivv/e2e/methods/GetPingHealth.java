package io.mosip.ivv.e2e.methods;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.assertj.core.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.restassured.response.Response;

public class GetPingHealth extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(GetPingHealth.class);

	@Override
	public void run() throws RigInternalError {

		String modules = null;
		HashMap<String, String> context=null;
		if (step.getParameters() == null || step.getParameters().isEmpty() ||step.getParameters().size()<1) {
			context = contextInuse;
			modules="";
		} else {
			context = contextInuse;
			if(step.getParameters().size()==1)
				modules = step.getParameters().get(0);
			else
				modules="";
		}
		
		String uri=baseUrl + "/ping?contextKey="+context.get(E2EConstants.CONTEXTKEY)+"&module="+modules;
		
		Response response = getRequest(uri, "");
		
		JSONObject res = new JSONObject(response.asString());
		
		logger.info(res.toString());
		
		  if(res.get("status").equals(true)) {
			  	logger.info("RESPONSE="+res.toString());
		}
		else
		{
			logger.error("RESPONSE="+res.toString());
			throw new RuntimeException("Health check status" + res.toString());
		}
		
	}}