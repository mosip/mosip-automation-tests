package io.mosip.testrig.dslrig.packetcreator.dto;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "RidSyncReqResponse", description = "Captured outbound RID sync HTTP call (headers and body) for test reporting.")
public class RidSyncReqResponseDTO {

	@Schema(description = "HTTP headers sent to Registration Processor.", example = "{\"Content-Type\":\"application/json\"}")
	private Map<String,String> headers;

	@Schema(description = "Raw JSON request body sent during sync.", example = "{\"request\":{}}")
	private String requestBody;

}
