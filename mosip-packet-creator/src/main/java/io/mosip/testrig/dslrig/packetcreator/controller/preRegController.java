package io.mosip.testrig.dslrig.packetcreator.controller;



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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.testrig.dslrig.dataprovider.util.DataProviderConstants;
import io.mosip.testrig.dslrig.packetcreator.dto.AppointmentDto;
import io.mosip.testrig.dslrig.packetcreator.dto.PreRegisterRequestDto;
import io.mosip.testrig.dslrig.packetcreator.service.PacketSyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "preRegController", description = "REST APIs for Pre Registration")
@RequestMapping(value = "/prereg")
@RestController
public class preRegController {

	  @Value("${mosip.test.persona.configpath}")
		private String personaConfigPath;
	  
	  @Autowired
	    PacketSyncService packetSyncService;

	   private static final Logger logger = LoggerFactory.getLogger(preRegController.class);
	   
	@ApiOperation(value = "Request for OTP for the given Persona and channel {mail | phone}", response = String.class)
		
	@PostMapping(value = "/otp/request/{to}/{contextKey}") 
    public @ResponseBody String requestOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto, @PathVariable("to") String to,
    		@PathVariable("contextKey") String contextKey    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.requestOtp(preRegisterRequestDto.getPersonaFilePath(), to, contextKey);
    	
    	} catch (Exception ex){
             logger.error("requestOtp", ex);
    	}
    	return "{Failed}";
    }
    
	@ApiOperation(value = "Verify the sent OTP for the given Persona and channel {mail | phone}", response = String.class)
	
    @PostMapping(value = "/otp/verify/{to}/{otp}/{contextKey}")
    public @ResponseBody String verifyOtp(@RequestBody PreRegisterRequestDto preRegisterRequestDto,@PathVariable("to") String to,
    		@PathVariable("contextKey") String contextKey, @PathVariable("otp") String otp    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.verifyOtp(preRegisterRequestDto.getPersonaFilePath().get(0), to, otp, contextKey);
    	
    	} catch (Exception ex){
             logger.error("verifyOtp", ex);
    	}
    	return "{Failed}";
    }

	@ApiOperation(value = "Post Application for a given Persona", response = String.class)

    @PostMapping(value = "/application/{contextKey}")
    public @ResponseBody String preRegisterResident(@RequestBody PreRegisterRequestDto preRegisterRequestDto,
    		@PathVariable("contextKey") String contextKey    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.preRegisterResident(preRegisterRequestDto.getPersonaFilePath(), contextKey);
    	
    	} catch (Exception ex){
             logger.error("registerResident", ex);
    	}
    	return "{Failed}";
    }
	@ApiOperation(value = "Update Application for a given PreRegID with updated Persona", response = String.class)
	@PutMapping(value = "/application/{preregid}/{contextKey}")
    public @ResponseBody String updateResidentApplication(
    		@RequestBody PreRegisterRequestDto preRegisterRequestDto,
    		@PathVariable("preregid") String preregId,
    		@PathVariable("contextKey") String contextKey    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.updateResidentApplication(preRegisterRequestDto.getPersonaFilePath().get(0),preregId, contextKey);
    	
    	} catch (Exception ex){
             logger.error("registerResident", ex);
    	}
    	return "{Failed}";
    }

	
	@ApiOperation(value = "Retrive all Applications for the logged in user or for a given pre-registraion-Id", response = String.class)

	@GetMapping(value = "/application/all//{contextKey}")
    public @ResponseBody String preRegisterGetApplications(
    		@RequestParam(name="preregId",required = false) String preregId,
    		@PathVariable("contextKey") String contextKey    		) {

    	//28602756053278
    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.preRegisterGetApplications(null,preregId, contextKey);
    	
    	} catch (Exception ex){
             logger.error("registerResident", ex);
    	}
    	return "{Failed}";
    }
	@ApiOperation(value = "Retrive all Booked Applications for the logged in user or for a given pre-registraion-Id", response = String.class)

    @GetMapping(value = "/application/booked//{contextKey}")
    public @ResponseBody String preRegisterGetApplicationsBooked(
    		@RequestParam(name="preregId",required = false) String preregId,
    		@PathVariable("contextKey") String contextKey    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.preRegisterGetApplications("Booked",preregId, contextKey);
    	
    	} catch (Exception ex){
             logger.error("registerResident", ex);
    	}
    	return "{Failed}";
    }

    /*
     * Book first nn th available slot
     */
	@ApiOperation(value = "Book n'th Available slot for a given pre-registraion-Id", response = String.class)

    @PostMapping(value = "/appointment/available/{preregid}/{nthSlot}/{contextKey}")
    public @ResponseBody String bookAppointment(@PathVariable("preregid") String preregId,@PathVariable("nthSlot") int  nthSlot,
    		@PathVariable("contextKey") String contextKey
    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.bookAppointment(preregId,nthSlot, contextKey);
    	
    	} catch (Exception ex){
             logger.error("bookAppointment", ex);
    	}
    	return "{\"Failed\"}";
    }

	@ApiOperation(value = "Book Specific slot (by date & time slot) for a given pre-registraion-Id", response = String.class)

    @PostMapping(value = "/appointment/{preregid}/{contextKey}")
    public @ResponseBody String bookSpecificAppointment(@PathVariable("preregid") String preregId,
    		@RequestBody AppointmentDto appointmentDto,
    		@PathVariable("contextKey") String contextKey
    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.bookSpecificAppointment(preregId,appointmentDto, contextKey);
    	
    	} catch (Exception ex){
             logger.error("bookAppointment", ex);
    	}
    	return "{\"Failed\"}";
    }

	@ApiOperation(value = "Get All Available slots", response = String.class)

    @GetMapping(value = "/appointment/available/{contextKey}")
    public @ResponseBody String getAvailableAppointments(
    		@PathVariable("contextKey") String contextKey    		) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.getAvailableAppointments(contextKey);
    	
    	} catch (Exception ex){
             logger.error("getAvailableAppointments", ex);
    	}
    	return "{\"Failed\"}";
    }

    /*
     * Try to book a specified slot 
     */
	@ApiOperation(value = "Book n'th slot for a given pre-registraion-Id. If bookOnHoliday -> true ? then dont skip holidays", response = String.class)

	@PostMapping(value = "/appointment/{preregid}/{nthSlot}/{bookOnHolidays}/{contextKey}")
    public @ResponseBody String bookAppointmentSpecified(@PathVariable("preregid") String preregId,
    		@PathVariable("nthSlot") int  nthSlot,
    		@PathVariable("bookOnHolidays") boolean bookOnHolidays,
    		@PathVariable("contextKey") String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.bookAppointmentSlot(preregId,nthSlot, bookOnHolidays,contextKey);
    	
    	} catch (Exception ex){
             logger.error("bookAppointment", ex);
    	}
    	return "{\"Failed\"}";
    }

	@ApiOperation(value = "Upload documents for a given pre-registration-Id from specified persona", response = String.class)

    @PostMapping(value = "/documents/{preregid}/{contextKey}")
    public @ResponseBody String uploadDocuments(@RequestBody PreRegisterRequestDto preRegisterRequestDto,@PathVariable("preregid") String preregId,
    		@PathVariable("contextKey") String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.uploadDocuments(preRegisterRequestDto.getPersonaFilePath().get(0),preregId, contextKey);
    	
    	} catch (Exception ex){
             logger.error("uploadDocuments", ex);
    	}
    	return "{\"Failed\"}";
    }
    
	@ApiOperation(value = "Cancel appointment for a given pre-registration-Id", response = String.class)

    @PostMapping(value = "/appointment/cancel/{preregid}/{contextKey}")
    public @ResponseBody String cancelAppointment(
    		@RequestBody AppointmentDto appointmentDto,
    		@PathVariable("preregid") String preregId,
    		@PathVariable("contextKey") String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.cancelAppointment(preregId, appointmentDto,contextKey);
    	
    	} catch (Exception ex){
             logger.error("bookAppointment", ex);
    	}
    	return "{\"Failed\"}";
    }
	@ApiOperation(value = "Delete Applications for a given pre-registration-Id", response = String.class)

    @DeleteMapping(value = "/application/{preregid}/{contextKey}")
    public @ResponseBody String deleteApplication(
    		@PathVariable("preregid") String preregId,
    		@PathVariable("contextKey") String contextKey) {
    	
    	return packetSyncService.deleteApplication(preregId, contextKey);
    	
    }
	
	
	
	
	@ApiOperation(value = "Update applications status for a given pre-registration-Id", response = String.class)
	@PutMapping(value = "/application/status/{preregid}/{contextKey}")
	public @ResponseBody String updatePreRegStatus(@PathVariable("preregid") String preregId,
			@RequestParam(name = "statusCode") String statusCode,
			@PathVariable("contextKey") String contextKey) {

    	try{    	
    		if(personaConfigPath !=null && !personaConfigPath.equals("")) {
    			DataProviderConstants.RESOURCE = personaConfigPath;
    		}
    		return packetSyncService.updatePreRegistrationStatus(preregId, statusCode,contextKey);
    	
    	} catch (Exception ex){
             logger.error("updatePreRegStatus", ex);
    	}
    	return "{Failed}";
    }
    
}
