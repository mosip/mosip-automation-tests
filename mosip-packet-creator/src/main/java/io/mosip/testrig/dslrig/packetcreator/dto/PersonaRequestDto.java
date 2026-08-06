package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.Hashtable;
import java.util.Properties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
@Data
public class PersonaRequestDto {

	@NotNull
	Hashtable<PersonaRequestType, Properties> requests;

	@ToString.Exclude
	String uin;

	@ToString.Exclude
	String rid;

}
