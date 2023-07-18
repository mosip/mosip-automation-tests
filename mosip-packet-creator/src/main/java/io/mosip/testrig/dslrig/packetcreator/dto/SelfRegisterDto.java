package io.mosip.testrig.dslrig.packetcreator.dto;

import lombok.Data;

@Data
public class SelfRegisterDto {
    String address;
    String contactNumber;
    String emailId;
    String organizationName;
    String partnerId;
    String partnerType;
    String policyGroup;
}
