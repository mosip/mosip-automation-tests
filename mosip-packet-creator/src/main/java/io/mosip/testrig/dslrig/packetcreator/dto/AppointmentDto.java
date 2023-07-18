package io.mosip.testrig.dslrig.packetcreator.dto;

import lombok.Data;

@Data
public class AppointmentDto {
	String registration_center_id;
	String appointment_date;
	String time_slot_from;
	String time_slot_to;
	String pre_registration_id;
}
