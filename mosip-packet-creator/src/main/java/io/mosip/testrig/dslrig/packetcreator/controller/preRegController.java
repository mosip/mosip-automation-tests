package io.mosip.testrig.dslrig.packetcreator.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.packetcreator.dto.AppointmentDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PreRegisterRequestDto;
import io.mosip.testrig.dslrig.packetcreator.service.PacketSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping(value = "/prereg")
@RestController
@Tag(name = "preRegController", description = "REST APIs for Pre Registration")
public class preRegController {

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	private PacketSyncService packetSyncService;

	private static final Logger logger = LoggerFactory.getLogger(preRegController.class);

	public preRegController(@Lazy PacketSyncService packetSyncService) {
		this.packetSyncService = packetSyncService;
	}

	@Operation(summary = "Request for OTP for the given Persona and channel {mail | phone}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully requested OTP for given persona and channel") })
	@PostMapping(value = "/otp/request/{to}/{contextKey}")
	public @ResponseBody String requestOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("to") String to, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.requestOtp(preRegisterRequestDto.getPersonaFilePath(), to, contextKey);

		} catch (Exception ex) {
			logger.error("requestOtp", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "REQUEST_OTP_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Verify the sent OTP for the given Persona and channel {mail | phone}")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully verified the OTP sent for given persona and channel") })
	@PostMapping(value = "/otp/verify/{to}/{otp}/{contextKey}")
	public @ResponseBody String verifyOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("to") String to, @PathVariable("contextKey") String contextKey,
			@PathVariable("otp") String otp) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.verifyOtp(preRegisterRequestDto.getPersonaFilePath().get(0), to, otp, contextKey);

		} catch (Exception ex) {
			logger.error("verifyOtp", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "VERIFY_OTP_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Post Application for a given Persona")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully created application") })
	@PostMapping(value = "/application/{contextKey}")
	public @ResponseBody String preRegisterResident(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.preRegisterResident(preRegisterRequestDto.getPersonaFilePath(), contextKey);

		} catch (Exception ex) {
			logger.error("preRegisterResident", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "PRE_REGISTER_RESIDENT_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Update Application for a given PreRegID with updated Persona")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully updated the application") })
	@PutMapping(value = "/application/{preregid}/{contextKey}")
	public @ResponseBody String updateResidentApplication(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("preregid") String preregId, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.updateResidentApplication(preRegisterRequestDto.getPersonaFilePath().get(0),
					preregId, contextKey);

		} catch (Exception ex) {
			logger.error("updateResidentApplication", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "UPDATE_RESIDENT_APPLICATION_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Retrive all Applications for the logged in user or for a given pre-registraion-Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrived all the applications") })
	@GetMapping(value = "/application/all//{contextKey}")
	public @ResponseBody String preRegisterGetApplications(
			@RequestParam(name = "preregId", required = false) String preregId,
			@PathVariable("contextKey") String contextKey) {

		// 28602756053278
		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.preRegisterGetApplications(null, preregId, contextKey);

		} catch (Exception ex) {
			logger.error("preRegisterGetApplications", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "PRE_REGISTER_GET_APPLICATIONS_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Retrive all Booked Applications for the logged in user or for a given pre-registraion-Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved all booked applications") })
	@GetMapping(value = "/application/booked//{contextKey}")
	public @ResponseBody String preRegisterGetApplicationsBooked(
			@RequestParam(name = "preregId", required = false) String preregId,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.preRegisterGetApplications("Booked", preregId, contextKey);

		} catch (Exception ex) {
			logger.error("preRegisterGetApplicationsBooked", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "PRE_REGISTER_GET_APPLICATIONS_BOOKED_FAIL", null, ex, ex.getMessage());
		}
 	}
	
	/*
	 * @Operation(summary = "Requesting the OTP")
	 * 
	 * @ApiResponses(value = { @ApiResponse(responseCode = "200", description =
	 * "OTP requested successfully") })
	 * 
	 * @PostMapping(value = "/requestotp/{to}/{contextKey}") public @ResponseBody
	 * String requestOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
	 * 
	 * @PathVariable("to") String to, @PathVariable("contextKey") String contextKey)
	 * {
	 * 
	 * try { if (personaConfigPath != null && !personaConfigPath.equals("")) {
	 * DataProviderConstants.RESOURCE = personaConfigPath; } return
	 * packetSyncService.requestOtp(preRegisterRequestDto.getPersonaFilePath(), to,
	 * contextKey);
	 * 
	 * } catch (Exception ex) { logger.error("requestOtp", ex); } return "{Failed}";
	 * }
	 */

	@Operation(summary = "Verifying the OTP")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OTP verified successfully") })
	@PostMapping(value = "/verifyotp/{to}/{contextKey}")
	public @ResponseBody String verifyOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("to") String to, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.verifyOtp(preRegisterRequestDto.getPersonaFilePath().get(0), to, null, contextKey);

		} catch (Exception ex) {
			logger.error("verifyOtp", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "VERIFY_OTP_FAIL", null, ex, ex.getMessage());
		}
 	}

	/*
	 * Book first nn th available slot
	 */
	@Operation(summary = "Booking the Appointment for a given pre-registration-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Appointment booked successfully") })
	@PostMapping(value = "/bookappointment/{preregid}/{nthSlot}/{contextKey}")
	public @ResponseBody String bookAppointment1(@PathVariable("preregid") String preregId,
			@PathVariable("nthSlot") int nthSlot, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.bookAppointment(preregId, nthSlot, contextKey);

		} catch (Exception ex) {
			logger.error("bookAppointment", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "BOOK_APPOINTMENT_FAIL", null, ex, ex.getMessage());
		}
 	}

	/*
	 * Book first nn th available slot
	 */

	@Operation(summary = "Book n'th Available slot for a given pre-registraion-Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully booked n'th available slot") })
	@PostMapping(value = "/appointment/available/{preregid}/{nthSlot}/{contextKey}")
	public @ResponseBody String bookAppointment(@PathVariable("preregid") String preregId,
			@PathVariable("nthSlot") int nthSlot, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.bookAppointment(preregId, nthSlot, contextKey);

		} catch (Exception ex) {
			logger.error("bookAppointment", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "BOOK_APPOINTMENT_NTH_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Book Specific slot (by date & time slot) for a given pre-registraion-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully booked specific slot") })
	@PostMapping(value = "/appointment/{preregid}/{contextKey}")
	public @ResponseBody String bookSpecificAppointment(@PathVariable("preregid") String preregId,
			@RequestBody AppointmentDto appointmentDto, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.bookSpecificAppointment(preregId, appointmentDto, contextKey);

		} catch (Exception ex) {
			logger.error("bookSpecificAppointment", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "BOOK_SPECIFIC_APPOINTMENT_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Get All Available slots")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved all available slots") })
	@GetMapping(value = "/appointment/available/{contextKey}")
	public @ResponseBody String getAvailableAppointments(@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.getAvailableAppointments(contextKey);

		} catch (Exception ex) {
			logger.error("getAvailableAppointments", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "GET_AVAILABLE_APPOINTMENTS_FAIL", null, ex, ex.getMessage());
		}
 	}

	/*
	 * Try to book a specified slot
	 */
	@Operation(summary = "Book n'th slot for a given pre-registraion-Id. If bookOnHoliday -> true ? then dont skip holidays")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully booked n'th slot for a given pre-registration id") })
	@PostMapping(value = "/appointment/{preregid}/{nthSlot}/{bookOnHolidays}/{contextKey}")
	public @ResponseBody String bookAppointmentSpecified(@PathVariable("preregid") String preregId,
			@PathVariable("nthSlot") int nthSlot, @PathVariable("bookOnHolidays") boolean bookOnHolidays,
			@PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.bookAppointmentSlot(preregId, nthSlot, bookOnHolidays, contextKey);

		} catch (Exception ex) {
			logger.error("bookAppointmentSpecified", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "BOOK_APPOINTMENT_SPECIFIED_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Cancel appointment for a given pre-registration-Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully cancelled the appointment") })
	@PostMapping(value = "/appointment/cancel/{preregid}/{contextKey}")
	public @ResponseBody String cancelAppointment(@RequestBody AppointmentDto appointmentDto,
			@PathVariable("preregId") String preregId, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.cancelAppointment(preregId, appointmentDto, contextKey);

		} catch (Exception ex) {
			logger.error("cancelAppointment", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "CANCEL_APPOINTMENT_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Delete Applications for a given pre-registration-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully deleted the application") })
	@DeleteMapping(value = "/application/{preregid}/{contextKey}")
	public @ResponseBody String deleteApplication(@PathVariable("preregId") String preregId,
			@PathVariable("contextKey") String contextKey) {

		return packetSyncService.deleteApplication(preregId, contextKey);

	}

	@Operation(summary = "Update applications status for a given pre-registration-Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully updated the application status") })
	@PutMapping(value = "/application/status/{preregid}/{contextKey}")
	public @ResponseBody String updatePreRegStatus(@PathVariable("preregId") String preregId,
			@RequestParam(name = "statusCode") String statusCode, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.updatePreRegistrationStatus(preregId, statusCode, contextKey);

		} catch (Exception ex) {
			logger.error("updatePreregStatus", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "UPDATE_PREREG_STATUS_FAIL", null, ex, ex.getMessage());
		}
 	}
	
	@Operation(summary = "Uploading the document for a given pre-registration-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Document uploaded successfully") })
	@PostMapping(value = "/documents/{preregid}/{contextKey}")
	public @ResponseBody String uploadDocuments(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
			@PathVariable("preregId") String preregId, @PathVariable("contextKey") String contextKey) {

		try {
			if (personaConfigPath != null && !personaConfigPath.equals("")) {
				DataProviderConstants.RESOURCE = personaConfigPath;
			}
			return packetSyncService.uploadDocuments(preRegisterRequestDto.getPersonaFilePath().get(0), preregId,
					contextKey);

		} catch (Exception ex) {
			logger.error("uploadDocuments", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_DOCUMENTS_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Delete Booking appointment for a given pre-registration-Id")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully deleted the appointment") })
	@DeleteMapping(value = "/preregistration/v1/applications/appointment/{contextKey}")
	public @ResponseBody String deleteAppointment(@RequestParam(name = "preRegistrationId") String preregId,
			@PathVariable("contextKey") String contextKey) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("preRegistrationId", preregId);
		return packetSyncService.discardBooking(map, contextKey);

	}

	@Operation(summary = "Update appointment for a given PreRegID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully updated the appointment") })
	@PutMapping(value = "/preregistration/v1/applications/appointment/{preregid}/{contextKey}")
	public @ResponseBody String updateAppointment(@PathVariable("preregid") String preregid,
			@PathVariable("contextKey") String contextKey) {
		try {

			return packetSyncService.updatePreRegAppointment(preregid, contextKey);

		} catch (Exception ex) {
			logger.error("updateAppointment", ex);
			throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "UPDATE_APPOINTMENT_FAIL", null, ex, ex.getMessage());
		}
 	}

	@Operation(summary = "Discard Applications for a given pre-registration-Id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully discarded the application") })
	@DeleteMapping(value = "/preregistration/v1/applications/{preregid}/{contextKey}")
	public @ResponseBody String discardApplication(@PathVariable("preregid") String preregId,
			@PathVariable("contextKey") String contextKey) {

		return packetSyncService.deleteApplication(preregId, contextKey);

	}

}