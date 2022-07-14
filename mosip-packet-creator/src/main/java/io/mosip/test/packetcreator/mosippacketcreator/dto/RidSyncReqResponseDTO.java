package io.mosip.test.packetcreator.mosippacketcreator.dto;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RidSyncReqResponseDTO {

	private Map<String,String> headers;

	private String requestBody;

}