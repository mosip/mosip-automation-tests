package io.mosip.ivv.e2e.methods;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Reporter;

import com.google.gson.JsonObject;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.ivv.orchestrator.TestRunner;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
        
import io.mosip.service.BaseTestCase;

public class AfterSuite extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(AfterSuite.class);
  
	public void run() {
		  try {
			  OnSmtpList.flag=true;
			  
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	}}