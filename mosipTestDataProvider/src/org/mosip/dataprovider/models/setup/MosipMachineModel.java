package org.mosip.dataprovider.models.setup;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties({"createdBy","createdDateTime","updatedBy","updatedDateTime","isDeleted","deletedDateTime","zone","machineTypeName","mapStatus"})
public class MosipMachineModel implements Serializable{

	 private static final long serialVersionUID = 1L;
	 
	String id;
	String	ipAddress;
    Boolean isActive;
    String 	langCode;
    String	macAddress;
    String	machineSpecId;
    String	name;
    String	publicKey;
    String	regCenterId;
    String	serialNum;
    String	signPublicKey;
    String	validityDateTime;
    String	zoneCode;
    public boolean isActive() {
    	return isActive;
    }
}

