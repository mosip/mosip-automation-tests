package io.mosip.testrig.dslrig.packetcreator.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "RidSyncReqRequest", description = "Same fields as SyncRidRequest; returns raw sync HTTP request/response for debugging.")
public class RidSyncReqRequestDto {

    @Schema(description = "Officer or operator name.", example = "officer01")
    private String name;

    @Schema(description = "Contact phone.", example = "9999999999")
    private String phone;

    @Schema(description = "Contact email.", example = "officer@example.com")
    private String email;

    @Schema(description = "Path to packet container zip.", example = "/tmp/packets/container.zip", requiredMode = Schema.RequiredMode.REQUIRED)
    private String containerPath;

    @Schema(description = "Supervisor status.", example = "APPROVED")
    private String supervisorStatus;

    @Schema(description = "Supervisor comment.", example = "OK")
    private String supervisorComment;

    @Schema(description = "MOSIP process.", example = "NEW", requiredMode = Schema.RequiredMode.REQUIRED)
    private String process;

    @Schema(description = "Additional info request id.", example = "")
    private String additionalInfoReqId;
}
