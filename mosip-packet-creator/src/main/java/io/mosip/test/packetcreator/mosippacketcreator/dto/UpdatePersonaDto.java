package io.mosip.test.packetcreator.mosippacketcreator.dto;

import java.util.List;
import java.util.Properties;

import lombok.Data;

@Data
public class UpdatePersonaDto {
	String personaFilePath;
	Properties updateAttributeList;
	List<String> regenAttributeList;
	List<String> missAttributeList;	//ID Scheme elements or biometric sub modalities to be added to exception list
	List<String> retriveAttributeList;	// Persona attribute values you want to return
}
