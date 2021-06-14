package io.mosip.test.packetcreator.mosippacketcreator.dto;

import java.util.List;

import lombok.Data;

@Data
public class MockABISExpectationsDto {

	String operation;			// Insert, Identify - default -> Identify
	String personaPath;
	boolean isDuplicate;		//True - show this hash as duplicate
	List<String> modalities;	//List of fingers, face, iris, whose hash should be registered
	List<String> refHashs;		//List of hash , which should be returned with identify request
	int delaySec;					// Delay in seconds
}
