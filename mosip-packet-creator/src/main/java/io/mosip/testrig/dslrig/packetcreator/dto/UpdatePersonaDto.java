package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;
import java.util.Properties;

import lombok.Data;

@Data
public class UpdatePersonaDto {
	String personaFilePath;
	String testPersonaPath;
	Properties updateAttributeList;
	List<String> regenAttributeList;
	List<String> missAttributeList;	
	List<String> retriveAttributeList;	
}
