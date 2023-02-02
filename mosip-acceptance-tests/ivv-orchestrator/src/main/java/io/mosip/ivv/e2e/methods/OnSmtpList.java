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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.pojo.Root;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.kernel.util.ConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

import io.mosip.service.BaseTestCase;

public class OnSmtpList extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(OnSmtpList.class);
	static HashMap map=new HashMap<Object, Object>();

	static Boolean flag=false;

	public void run() {
		try {
			  Properties kernelprops=ConfigManager.propsKernel;
				String a1="wss://smtp.";
				String externalurl=kernelprops.getProperty("keycloak-external-url");
			    String a2=	externalurl.substring(externalurl.indexOf(".")+1);
			    String a3="/mocksmtp/websocket"; 
				  
			WebSocket ws = HttpClient
					.newHttpClient()
					.newWebSocketBuilder()
//					.buildAsync(URI.create("wss://smtp.qa-1201-b2.mosip.net/mocksmtp/websocket"), new WebSocketClient())
					.buildAsync(URI.create(a1+a2+a3), new WebSocketClient())
					.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static class WebSocketClient implements WebSocket.Listener {
		Long count=(long) 00;
		Root root =new Root();
		public WebSocketClient() {  

		}

		@Override
		public void onOpen(WebSocket webSocket) {
			System.out.println("onOpen using subprotocol " + webSocket.getSubprotocol());
			WebSocket.Listener.super.onOpen(webSocket);
		}


		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			// TODO Auto-generated method stub
			return Listener.super.onClose(webSocket, statusCode, reason);
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			if(flag) {
				System.out.println(map);
				System.out.println("End Closure of listner " );
				onClose(webSocket, 0, "After suite invoked closing");
			}
			try {       
				ObjectMapper om = new ObjectMapper();

				root = om.readValue(data.toString(), Root.class);
				map.put(root.to.value.get(0).address,root.html);
				System.out.println(" After adding onText received " + count + "data" + data + root);
			} catch (JsonMappingException e) {

				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} 	catch(JSONException e)
			{
				e.printStackTrace();
			}

			return WebSocket.Listener.super.onText(webSocket, data, last);
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {

			System.out.println("Bad day! " + webSocket.toString());
			error.printStackTrace();
			WebSocket.Listener.super.onError(webSocket, error);
		}
	}
}





/*

{
"attachments": [],
"headers": {},
"headerLines": [
	{
		"key": "date",
		"line": "Date: Fri, 6 Jan 2023 12:36:32 +0000 (UTC)"
	},
	{
		"key": "from",
		"line": "From: do-not-reply@mosip.io"
	},
	{
		"key": "to",
		"line": "To: alok.test.mosip@gmail.com"
	},
	{
		"key": "message-id",
		"line": "Message-ID: <1375994350.47282.1673008592273@notifier-85b56cf8b6-d7tqq>"
	},
	{
		"key": "subject",
		"line": "Subject: UIN Generated"
	},
	{
		"key": "mime-version",
		"line": "MIME-Version: 1.0"
	},
	{
		"key": "content-type",
		"line": "Content-Type: multipart/mixed; \r\n\tboundary=\"----=_Part_47280_955397360.1673008592158\""
	}
],
"html": "Dear $name_eng, Your UIN for Registration ID: 10636106261007320230106123531 has been successfully generated and will reach soon at your postal address. Thank You",
"subject": "UIN Generated",
"date": "2023-01-06T12:36:32.000Z",
"to": {
	"value": [
		{
			"address": "alok.test.mosip@gmail.com",
			"name": ""a
		}
	],
	"html": "<span class=\"mp_address_group\"><a href=\"mailto:alok.test.mosip@gmail.com\" class=\"mp_address_email\">alok.test.mosip@gmail.com</a></span>",
	"text": "alok.test.mosip@gmail.com"
},
"from": {
	"value": [
		{
			"address": "do-not-reply@mosip.io",
			"name": ""
		}
	],
	"html": "<span class=\"mp_address_group\"><a href=\"mailto:do-not-reply@mosip.io\" class=\"mp_address_email\">do-not-reply@mosip.io</a></span>",
	"text": "do-not-reply@mosip.io"
},
"messageId": "<1375994350.47282.1673008592273@notifier-85b56cf8b6-d7tqq>"
}

 */




//import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
//import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); 
public class From{
 public ArrayList<Value> value;
 public String html;
 public String text;
}

public class HeaderLine{
 public String key;
 public String line;
}

public class Headers{
}

public class Root{
 public ArrayList<Object> attachments;
 public Headers headers;
 public ArrayList<HeaderLine> headerLines;
 public String html;
 public String subject;
 public Date date;
 @JsonProperty("to") 
 public To myto;
 public From from;
 public String messageId;
}

public class To{
 public ArrayList<Value> value;
 public String html;
 public String text;
}

public class Value{
 public String address;
 public String name;
}

 */