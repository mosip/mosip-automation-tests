package io.mosip.test.packetcreator.mosippacketcreator.dto;

import lombok.Data;
import java.util.List;

@Data
public class Policy {
    List<policyAllowedAuthType> allowedAuthTypes;
    List<Object> allowedKycAttributes;
    String authTokenType;

}
