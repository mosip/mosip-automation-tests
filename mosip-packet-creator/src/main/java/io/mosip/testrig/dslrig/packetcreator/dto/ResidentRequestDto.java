package io.mosip.testrig.dslrig.packetcreator.dto;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ResidentGenerateRequest", description = "Demographic hints used when generating a new resident persona.")
public class ResidentRequestDto {

	@Schema(description = "Gender for generated persona.", example = "Male")
	private Gender gender;

	@Schema(description = "Age or age group string.", example = "25")
	private String age;

	@Schema(description = "Primary language code.", example = "eng")
	private String primaryLanguage;

	@Schema(description = "Secondary language code.", example = "hin")
	private String secondaryLanguage;
}
