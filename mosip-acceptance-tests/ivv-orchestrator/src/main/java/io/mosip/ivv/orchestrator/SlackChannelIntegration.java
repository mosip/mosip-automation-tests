package io.mosip.ivv.orchestrator;
import java.io.IOException;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

/* webhook
 * curl -X POST -H 'Content-type: application/json' --data '{"text":"Hello, World!"}' https://hooks.slack.com/services/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
 * https://hooks.slack.com/services/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
 * oauth token: xoxb-831351446070-1751917508130-IAFVzzZlKb5dEMwQKmfdcWAP
 */
public class SlackChannelIntegration {
	//static String reportUrl="http://ec2-13-233-116-131.ap-south-1.compute.amazonaws.com/MosipTestResource/mosip_e2e_emailable_report.html";
	static String reportUrl="http://automation.mosip.net/MosipTestResource/mosip_e2e_emailable_report.html";
	static Slack slack = Slack.getInstance();
	static String token  = "xoxb-831351446070-1751917508130-IAFVzzZlKb5dEMwQKmfdcWAP";
	//static String token  = "xoxp-831351446070-1724753972099-1775798923792-20fd629a791c9ab53235343d84e9f77a";
	static String defaultChannel = "#automation-integration";
	
	public static Boolean postMessage(String channelName, String message) {
	
		MethodsClient methods = slack.methods(token);
	    // Build a request object
	    ChatPostMessageRequest request = ChatPostMessageRequest.builder()
	   .channel(channelName) // Use a channel ID `C1234567` is preferrable
	   .text(message)
	   .build();

	    // Get a response as a Java object
	    try {
			ChatPostMessageResponse response = methods.chatPostMessage(request);
			if(response.isOk())
				return true;
			
	    } catch (IOException | SlackApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return false;
	}
	
	 public static void main(String[] args) throws Exception {
		 
		 postMessage(defaultChannel, "Test message from Automation");
	 }
}
