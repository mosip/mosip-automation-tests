package io.mosip.test.packetcreator.mosippacketcreator.dto;

import java.util.List;

import lombok.Data;

@Data
public class Policy {
    List<policyAllowedAuthType> allowedAuthTypes;
    List<Object> allowedKycAttributes;
    String authTokenType;

}
