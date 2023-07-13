package io.mosip.ivv.core.dtos;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDataDTO {
    private int status;
    private String body;
    private Map<String, String> cookies;

    public ResponseDataDTO(int s, String b, Map<String, String> c){
        this.status = s;
        this.body = b;
        this.cookies = c;
    }
}
