package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;

import lombok.Data;

@Data
public class MockABISExpectationsDto {

	String operation;			
	String personaPath;
	boolean isDuplicate;		
	List<String> modalities;	
	List<String> refHashs;		
	int delaySec;	
	String statusCode;
	String failureReason;
}
