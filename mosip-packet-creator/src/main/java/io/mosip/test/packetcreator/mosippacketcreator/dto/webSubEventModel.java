package io.mosip.test.packetcreator.mosippacketcreator.dto;

import lombok.Data;

@Data
public class webSubEventModel {

		private String publisher;
		private String topic;
		private String publishedOn;
		private Event event;
	
}
