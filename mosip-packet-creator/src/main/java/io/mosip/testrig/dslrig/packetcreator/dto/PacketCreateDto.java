package io.mosip.testrig.dslrig.packetcreator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "PacketCreateRequest", description = "Input for creating a registration packet container from ID JSON and CBEFF template.")
public class PacketCreateDto {

    @Schema(description = "Path to identity JSON (demographic data) file.", example = "/output/id.json", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idJsonPath;

    @Schema(description = "Path to CBEFF / packet template file.", example = "/output/template.xml", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templatePath;

    @Schema(description = "Packet source label, e.g. REGISTRATION_CLIENT.", example = "REGISTRATION_CLIENT")
    private String source;

    @Schema(description = "MOSIP process name.", example = "NEW", requiredMode = Schema.RequiredMode.REQUIRED)
    private String process;

    @Schema(description = "Additional info request id when applicable.", example = "null")
    private String additionalInfoReqId;
}
