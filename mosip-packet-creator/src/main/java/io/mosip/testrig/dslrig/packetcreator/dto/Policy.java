package io.mosip.testrig.dslrig.packetcreator.dto;

import java.util.List;

import lombok.Data;

@Data
public class Policy {
    List<policyAllowedAuthType> allowedAuthTypes;
    List<Object> allowedKycAttributes;
    String authTokenType;

}
