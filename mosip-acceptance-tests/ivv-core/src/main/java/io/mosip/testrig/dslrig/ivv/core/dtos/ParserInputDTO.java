package io.mosip.testrig.dslrig.ivv.core.dtos;

import java.util.Properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParserInputDTO {
    private Properties configProperties;
    private String personaSheet;
    private String rcSheet;
    private String partnerSheet;
    private String scenarioSheet;
    private String configsSheet;
    private String globalsSheet;
    private String documentsSheet;
    private String biometricsSheet;
    private String mappingJSON;
    private String idObjectSchema;
    private String documentsFolder;
    private String biometricsFolder;
}
