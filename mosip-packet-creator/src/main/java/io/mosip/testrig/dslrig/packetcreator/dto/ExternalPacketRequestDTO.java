package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;

import lombok.Data;

@Data
public class ExternalPacketRequestDTO {
	
	private List<String> personaFilePath;
	private String source;

}
