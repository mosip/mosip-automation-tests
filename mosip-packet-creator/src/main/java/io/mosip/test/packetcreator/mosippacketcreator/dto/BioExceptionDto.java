package io.mosip.test.packetcreator.mosippacketcreator.dto;

import java.util.List;

import org.mosip.dataprovider.models.BioModality;

import lombok.Data;

@Data
public class BioExceptionDto {

	String personaFilePath;
	List<BioModality> exceptions;	
	

}
