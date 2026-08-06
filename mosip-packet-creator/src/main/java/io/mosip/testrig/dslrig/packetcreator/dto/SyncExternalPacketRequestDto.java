package io.mosip.testrig.dslrig.packetcreator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "SyncExternalPacketRequest", description = "Registration ID to sync and upload the external packet for.")
public class SyncExternalPacketRequestDto {

	@NotBlank
	@Schema(description = "Registration ID to sync.", requiredMode = Schema.RequiredMode.REQUIRED)
	private String rid;

}
