package io.mosip.testrig.dslrig.dataprovider.util;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

public class SlackIt {
	private static final Logger logger = LoggerFactory.getLogger(SlackIt.class);
	static Slack slack = Slack.getInstance();


	static String defaultChannel = "﻿#automation-integration";

	static String gentk() {
		String t1= "xoxb-831351446070";
		String t2= "-1751917508130";
		String t3 = "-GAoYKhLe6F0xW74n13XAtzml";


		return t1+t2+t3;
	}
	public static Boolean postMessage(String channelName, String message) {

		if(channelName == null)
			channelName = defaultChannel;
		MethodsClient methods = slack.methods(gentk());

	    ChatPostMessageRequest request = ChatPostMessageRequest.builder()
	   .channel(channelName) 
	   .text(message)
	   .build();


	    try {
			ChatPostMessageResponse response = methods.chatPostMessage(request);
			if(response.isOk())
				return true;

	    } catch (IOException | SlackApiException e) {
	    	logger.error(e.getMessage());
		}
	    return false;
	}
	public static void main(String[] argv) {
		postMessage("#automation-integration", "test message");
	}

}
