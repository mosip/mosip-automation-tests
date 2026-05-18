package io.mosip.testrig.dslrig.packetcreator.dto;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import lombok.Data;

@Data
public class ResidentRequestDto {
	private Gender gender;	
	private String age;		
	private String primaryLanguage;	
	private String secondaryLanguage;	    
}
