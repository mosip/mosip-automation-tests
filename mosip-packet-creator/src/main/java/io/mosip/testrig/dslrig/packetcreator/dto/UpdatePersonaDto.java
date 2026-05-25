package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;
import java.util.Properties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UpdatePersonaRequest", description = "Update, regenerate, or retrieve attributes on an existing persona JSON file.")
public class UpdatePersonaDto {

	@Schema(description = "Primary persona file path.", example = "/personas/default.json", requiredMode = Schema.RequiredMode.REQUIRED)
	String personaFilePath;

	@Schema(description = "Secondary test persona path when applicable.", example = "/personas/variant.json")
	String testPersonaPath;

	@Schema(description = "Key-value demographic fields to merge into persona.", example = "{\"dateOfBirth\":\"1990/01/01\"}")
	Properties updateAttributeList;

	@Schema(description = "Attribute names to regenerate (e.g. fresh biometrics).", example = "[\"FINGER\"]")
	List<String> regenAttributeList;

	@Schema(description = "Attributes to mark missing.", example = "[]")
	List<String> missAttributeList;

	@Schema(description = "Attributes to read back in the response.", example = "[\"UIN\"]")
	List<String> retriveAttributeList;
}
