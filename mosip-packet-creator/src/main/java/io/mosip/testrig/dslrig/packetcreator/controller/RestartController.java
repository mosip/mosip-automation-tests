package io.mosip.testrig.dslrig.packetcreator.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.packetcreator.MosipPacketCreatorApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "preRegController", description = "REST APIs for Pre Registration")
public class RestartController {

	@Operation(summary = "Restart the packet creator application")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully restarted the packet creator application") })
	@PostMapping("/restart")
	public void restart() {
		MosipPacketCreatorApplication.restart();
	}
}
