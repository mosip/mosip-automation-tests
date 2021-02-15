package io.mosip.test.packetcreator.mosippacketcreator.dto;

import java.util.Hashtable;
import java.util.Properties;

import lombok.Data;
@Data
public class PersonaRequestDto {

	Hashtable<PersonaRequestType, Properties> requests;

}
