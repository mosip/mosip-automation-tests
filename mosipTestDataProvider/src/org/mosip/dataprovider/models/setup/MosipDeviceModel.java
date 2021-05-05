package org.mosip.dataprovider.models.setup;

import java.io.Serializable;

import lombok.Data;

@Data
public class MosipDeviceModel implements Serializable{

	 private static final long serialVersionUID = 1L;
	String id;
    
	String createdBy;
    String deviceSpecId;
     
    String 	ipAddress;
    String  isActive;
    String  isDeleted;
    String  langCode;
    String  macAddress;
    String  name;
    String  regCentId;
    String  serialNum;
    String  updatedBy;
    String createdDateTime;
    String deletedDateTime;
    String updatedDateTime;
}
