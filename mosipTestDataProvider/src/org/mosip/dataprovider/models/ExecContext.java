package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.Properties;

import lombok.Data;

@Data
public class ExecContext implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	private String key;
	Properties properties;
}
