package io.mosip.testrig.dslrig.packetcreator.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.models.setup.MosipMachineModel;
import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.dto.BioExceptionDto;
import io.mosip.testrig.dslrig.packetcreator.dto.MockABISExpectationsDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PersonaRequestDto;
import io.mosip.testrig.dslrig.packetcreator.dto.UpdatePersonaDto;
import io.mosip.testrig.dslrig.packetcreator.service.PacketSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "PersonaController", description = "REST APIs for Persona management")
@RestController
public class PersonaController {

	@Autowired
	PacketSyncService packetSyncService;

	private static final Logger logger = LoggerFactory.getLogger(PersonaController.class);

	@ApiOperation(value = "Update given persona record with the given list of attribute values", response = String.class)
	@PutMapping(value = "/persona/{id}/{contextKey}")
	public @ResponseBody String updatePersonaData(@RequestBody List<UpdatePersonaDto> personaRequestDto,
			@PathVariable("id") String id, @PathVariable("contextKey") String contextKey) {
		try {
			
			VariableManager.Init(contextKey);

			return packetSyncService.updatePersonaData(personaRequestDto, contextKey);

		} catch (Exception ex) {
			logger.error("updatePersonaData", ex);
		}
		return "{Failed}";

	}

	@ApiOperation(value = "Update given persona record with the given list of biometric exceptions", response = String.class)
	@PutMapping(value = "/persona/bioexceptions/{contextKey}")
	public @ResponseBody String updatePersonaBioExceptions(@RequestBody BioExceptionDto personaBERequestDto,
			// @PathVariable("id") String id,
			@PathVariable("contextKey") String contextKey) {
		try {
			
			return packetSyncService.updatePersonaBioExceptions(personaBERequestDto, contextKey);

		} catch (Exception ex) {
			logger.error("updatePersonaData", ex);
		}
		return "{Failed}";

	}

	@ApiOperation(value = "Create persona record as per the given specification", response = String.class)
	@PostMapping(value = "/persona/{count}/{contextKey}")
	public @ResponseBody String generateResidentData(@RequestBody PersonaRequestDto residentRequestDto,
			@PathVariable("count") int count, @PathVariable("contextKey") String contextKey) {

		try {
			
			DataProviderConstants.ANGULI_PATH = VariableManager.getVariableValue(VariableManager.NS_DEFAULT, "mosip.test.persona.Angulipath").toString();;
			
			logger.info("personaAnguliPath =" + DataProviderConstants.ANGULI_PATH);

			logger.info("Resource Path=" + DataProviderConstants.RESOURCE);
			logger.info(
					"DOC_Template Path=" + DataProviderConstants.RESOURCE + DataProviderConstants.DOC_TEMPLATE_PATH);

			// clear all tokens
			// VariableManager.setVariableValue("urlSwitched", "true");

			return packetSyncService.generateResidentData(count, residentRequestDto, contextKey).toString();

		} catch (Exception ex) {
			logger.error("generateResidentData", ex);
		}
		return "{Failed}";
	}

	@ApiOperation(value = "Return from the given persona record , list of specified attribute values", response = String.class)
	@GetMapping(value = "/persona/{contextKey}")
	public @ResponseBody String getPersonaData(@RequestBody List<UpdatePersonaDto> personaRequestDto,
			@PathVariable("contextKey") String contextKey) {
		try {
		
			return packetSyncService.getPersonaData(personaRequestDto, contextKey);

		} catch (Exception ex) {
			logger.error("getPersonaData", ex);
		}
		return "{Failed}";

	}

	@ApiOperation(value = "Extended API to set Persona specific expectations in mock ABIS ", response = String.class)
	@PostMapping(value = "/persona/mockabis/v2/expectations/{contextKey}")
	public @ResponseBody String setPersonaMockABISExpectationV2(@RequestBody List<MockABISExpectationsDto> expectations,
			@PathVariable("contextKey") String contextKey) {

		try {
			
			return packetSyncService.setPersonaMockABISExpectation(expectations, contextKey);

		} catch (Exception ex) {
			logger.error("setPersonaMockABISExpectation", ex);
		}
		return "{Failed}";

	}

	@ApiOperation(value = "Delete expectation for a given Id", response = String.class)
	@DeleteMapping(value = "/mock-abis-service/config/expectation/{contextKey}")
	public @ResponseBody String deleteExpectations(@PathVariable("contextKey") String contextKey) {

		return packetSyncService.deleteMockAbisExpectations(contextKey);

	}

	@ApiOperation(value = "Update the machine details ", response = String.class)
	@PutMapping(value = "/updateMachine/{contextKey}")
	public @ResponseBody String updateMachine(@RequestBody MosipMachineModel machine,
			@PathVariable("contextKey") String contextKey) {
		try {
		
			return packetSyncService.updateMachine(machine, contextKey);

		} catch (Exception ex) {
			logger.error("updateMachine", ex);
		}
		return "{Failed}";
	}

}
