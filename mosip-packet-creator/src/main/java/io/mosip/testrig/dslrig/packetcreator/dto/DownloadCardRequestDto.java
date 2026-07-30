package io.mosip.testrig.dslrig.packetcreator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "DownloadCardRequest", description = "Persona file path and UIN for downloading a resident's card.")
public class DownloadCardRequestDto {

	@Schema(description = "Persona file path.", example = "/tmp/residents_123/9514345242.json", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "personaFilePath must not be blank")
	String personaFilePath;

	@Schema(description = "UIN to download the card for.", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "uin must not be blank")
	String uin;
}
