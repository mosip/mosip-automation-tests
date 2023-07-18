package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;

import io.mosip.testrig.dslrig.dataprovider.models.BioModality;
import lombok.Data;

@Data
public class BioExceptionDto {

	String personaFilePath;
	List<BioModality> exceptions;	
	

}
