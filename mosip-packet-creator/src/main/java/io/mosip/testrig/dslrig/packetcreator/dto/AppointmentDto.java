package io.mosip.testrig.dslrig.packetcreator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "AppointmentRequest", description = "Book or update a pre-registration appointment slot.")
public class AppointmentDto {

	@Schema(description = "Registration center id.", example = "10001", requiredMode = Schema.RequiredMode.REQUIRED)
	String registration_center_id;

	@Schema(description = "Appointment date (yyyy-MM-dd).", example = "2024-06-15", requiredMode = Schema.RequiredMode.REQUIRED)
	String appointment_date;

	@Schema(description = "Slot start time.", example = "09:00:00")
	String time_slot_from;

	@Schema(description = "Slot end time.", example = "09:15:00")
	String time_slot_to;

	@Schema(description = "Pre-registration application id.", example = "12345678901234", requiredMode = Schema.RequiredMode.REQUIRED)
	String pre_registration_id;
}
