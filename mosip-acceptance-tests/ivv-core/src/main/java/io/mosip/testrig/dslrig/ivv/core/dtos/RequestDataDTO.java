package io.mosip.testrig.dslrig.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDataDTO {
    private String url;
    private String request;
    private String filePath;

    public RequestDataDTO(String u, String r){
        this.url = u;
        this.request = r;
    }

    public RequestDataDTO(String u, String r, String fp){
        this.url = u;
        this.request = r;
        this.filePath = fp;
    }
}
