package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(name = "ExternalPacketRequest", description = "Create and upload CRVS / external process packets via packet manager.")
public class ExternalPacketRequestDTO {

	@NotEmpty
	@Schema(description = "Persona and template paths (same ordering as PreRegisterRequest).", example = "[\"/templates/t.json\",\"/personas/p.json\"]", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<String> personaFilePath;

	@Schema(description = "Packet source.", example = "REGISTRATION_CLIENT")
	private String source;

	@ToString.Exclude
	@Schema(description = "Existing UIN when required by process.", example = "123456789012")
	private String uin;

	@NotBlank
	@ToString.Exclude
	@Schema(description = "Registration ID to create the packet under.", requiredMode = Schema.RequiredMode.REQUIRED)
	private String rid;

}
