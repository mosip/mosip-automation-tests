package io.mosip.testrig.dslrig.packetcreator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "SyncRidRequest", description = "Supervisor and container metadata sent to Registration Processor during RID sync.")
public class SyncRidDto {

    @Schema(description = "Officer or operator name recorded in sync metadata.", example = "officer01")
    private String name;

    @Schema(description = "Contact phone for sync metadata.", example = "9999999999")
    private String phone;

    @Schema(description = "Contact email for sync metadata.", example = "officer@example.com")
    private String email;

    @Schema(description = "Absolute path to the encrypted packet container (zip) on the test runner.", example = "/tmp/packets/10001100790000120240101120000001.zip", requiredMode = Schema.RequiredMode.REQUIRED)
    private String containerPath;

    @Schema(description = "Supervisor approval status.", example = "APPROVED")
    private String supervisorStatus;

    @Schema(description = "Supervisor comment.", example = "OK")
    private String supervisorComment;

    @Schema(description = "MOSIP process name.", example = "NEW", requiredMode = Schema.RequiredMode.REQUIRED)
    private String process;

    @Schema(description = "Additional info request id when used.", example = "")
    private String additionalInfoReqId;
}
