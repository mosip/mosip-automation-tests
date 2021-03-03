package org.mosip.dataprovider.models;

import lombok.Data;

@Data
public class MosipIdentity {

	Boolean isNew;
	Boolean isUpdate;
	Boolean isChild;
	Boolean isLost;
	String parentOrGuardianUIN;
	String parentOrGuardianRID;
	String updatableFieldGroups;
	String updatableFields;		
}
