package io.mosip.testrig.dslrig.packetcreator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "PacketReprocessRequest", description = "Trigger packet reprocessing in Registration Processor for an existing RID.")
public class PacketReprocessDto {

	@Schema(description = "Registration ID to reprocess.", example = "10001100790000120240101120000001", requiredMode = Schema.RequiredMode.REQUIRED)
	private String RID;

	@Schema(description = "Workflow instance id from registration status.", example = "wf-12345", requiredMode = Schema.RequiredMode.REQUIRED)
	private String workflowInstanceId;

}
