package io.mosip.testrig.dslrig.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationUser extends PersonaDef {
    private String id = "";
    private String userId = "";
    private String password = "";
    private String centerId = "";
    private String gender = "";
    private String macAddress = "";
    private String no_Of_User="";
    private String keyIndex="";
}
