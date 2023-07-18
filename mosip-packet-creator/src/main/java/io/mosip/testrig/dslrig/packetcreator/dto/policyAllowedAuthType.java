package io.mosip.testrig.dslrig.packetcreator.dto;

import lombok.Data;

@Data
public class policyAllowedAuthType {
    String authSubType;
    String authType;
    String mandatory;
}
