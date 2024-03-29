package io.mosip.testrig.dslrig.packetcreator.dto;


import lombok.Data;

@Data
public class PolicyCreateDto {
    String desc;
    String name;
    Policy policies;
    String policyGroupName;
    String policyId;
    String policyType;
    String version;
}
