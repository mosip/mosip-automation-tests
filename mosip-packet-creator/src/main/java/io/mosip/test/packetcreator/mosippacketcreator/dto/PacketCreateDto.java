package io.mosip.test.packetcreator.mosippacketcreator.dto;

import lombok.Data;

@Data
public class PacketCreateDto {

    private String idJsonPath;
    private String templatePath;
    private String source;
    private String process;
}
