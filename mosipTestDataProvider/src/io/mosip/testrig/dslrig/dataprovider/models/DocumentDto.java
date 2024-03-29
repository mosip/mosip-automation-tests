package io.mosip.testrig.dslrig.dataprovider.models;

import lombok.Data;

@Data
public class DocumentDto {
	
	private byte[] document;
	private String value;
	private String type;
	private String category;
	private String owner;
	private String format;
	private String refNumber;
}