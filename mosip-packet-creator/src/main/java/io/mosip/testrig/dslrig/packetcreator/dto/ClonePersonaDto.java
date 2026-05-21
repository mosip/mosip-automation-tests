package io.mosip.testrig.dslrig.packetcreator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ClonePersonaRequest", description = "Copy an existing persona JSON file to a sibling backup path.")
public class ClonePersonaDto {

	@Schema(description = "Source persona file path.", example = "/tmp/residents_123/9514345242.json", requiredMode = Schema.RequiredMode.REQUIRED)
	String personaFilePath;
}
