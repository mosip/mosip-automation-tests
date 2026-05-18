package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.Map;

import lombok.Data;

@Data
public class Event {
    private String id; 
    private String transactionId; 
    Type type;
    private String timestamp; 
    private String dataShareUri; 

	private Map<String, Object> data;

}
