package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "MockABISExpectationsRequest", description = "Configure mock ABIS duplicate/quality expectations for a persona.")
public class MockABISExpectationsDto {

	@Schema(description = "ABIS operation, e.g. INSERT or IDENTIFY.", example = "INSERT", requiredMode = Schema.RequiredMode.REQUIRED)
	String operation;

	@Schema(description = "Persona file path this expectation applies to.", example = "/personas/default.json", requiredMode = Schema.RequiredMode.REQUIRED)
	String personaPath;

	@Schema(description = "When true, ABIS returns duplicate match.", example = "false")
	@JsonAlias("duplicate")
	boolean isDuplicate;

	@Schema(description = "Biometric modalities included in the expectation.", example = "[\"FINGER\",\"IRIS\"]")
	List<String> modalities;

	@Schema(description = "Reference hash values for gallery matching.", example = "[]")
	List<String> refHashs;

	@Schema(description = "Artificial delay in seconds before ABIS responds.", example = "0")
	int delaySec;

	@Schema(description = "HTTP-style status code returned by mock ABIS.", example = "200")
	String statusCode;

	@Schema(description = "Failure reason when status is non-success.", example = "")
	String failureReason;
}
