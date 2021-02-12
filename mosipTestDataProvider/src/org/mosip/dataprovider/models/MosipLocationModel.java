package org.mosip.dataprovider.models;

import java.io.Serializable;

import lombok.Data;
@Data
public class MosipLocationModel  implements Serializable{
	
	 private static final long serialVersionUID = 1L;
	String code;
	String langCode;
	String name;
	int hierarchyLevel;
	String hierarchyName;
	String parentLocCode;
	Boolean isActive;
	

	public void setHierarchyName(String hierarchyName) {
		//if(hierarchyName.equals("Postal Code"))
		//	hierarchyName ="postalCode";
		this.hierarchyName = hierarchyName;
	}

}
