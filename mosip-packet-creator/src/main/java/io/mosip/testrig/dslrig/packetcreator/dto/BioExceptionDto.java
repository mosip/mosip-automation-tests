package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;

import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BioExceptionRequest", description = "Apply biometric exception modalities to a persona file.")
public class BioExceptionDto {

	@Schema(description = "Path to persona JSON to update.", example = "/personas/default.json", requiredMode = Schema.RequiredMode.REQUIRED)
	String personaFilePath;

	@Schema(description = "Biometric modalities marked as exception (missing/unavailable).", requiredMode = Schema.RequiredMode.REQUIRED)
	List<BioModality> exceptions;

}
