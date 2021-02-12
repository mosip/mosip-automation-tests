package org.mosip.dataprovider.models;

import java.io.Serializable;

import lombok.Data;
@Data
public class LocationHierarchyModel   implements Serializable{
	
	 private static final long serialVersionUID = 1L;
	 int hierarchyLevel;
	 
	String hierarchyLevelName;
	String langCode;
	Boolean isActive;
}
