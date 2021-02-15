package io.mosip.test.packetcreator.mosippacketcreator.dto;
import org.mosip.dataprovider.util.Gender;
import lombok.Data;

@Data
public class ResidentRequestDto {
	private Gender gender;	// Male/Female/Any
	private String age;		//Minor/Adult
	private String primaryLanguage;	//default to 'eng'
	private String secondaryLanguage;	    
}


