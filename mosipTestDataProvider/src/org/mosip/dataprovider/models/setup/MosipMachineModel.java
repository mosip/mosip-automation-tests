package org.mosip.dataprovider.models.setup;

import java.io.Serializable;

import lombok.Data;

@Data
public class MosipMachineModel implements Serializable{

	 private static final long serialVersionUID = 1L;
	 
	String id;
	String	ipAddress;
    boolean isActive;
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
}
