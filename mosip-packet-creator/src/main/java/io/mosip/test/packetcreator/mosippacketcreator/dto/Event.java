package io.mosip.test.packetcreator.mosippacketcreator.dto;

import java.util.Map;

import lombok.Data;

@Data
public class Event {
    private String id; //uuid
    private String transactionId; //privided by the publisher.
    Type type;
    private String timestamp; //ISO format
    private String dataShareUri; //URL
   
	private Map<String, Object> data;

}