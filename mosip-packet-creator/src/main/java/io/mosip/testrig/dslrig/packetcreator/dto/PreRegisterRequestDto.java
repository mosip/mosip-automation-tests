package io.mosip.testrig.dslrig.packetcreator.dto;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@Schema(name = "PreRegisterRequest", description = "Paths to persona/template files and optional additional-info request id for packet flows.")
public class PreRegisterRequestDto {

	@Schema(description = "Ordered file paths: index 0 = packet/CBEFF template path; index 1 = persona JSON path (optional for some APIs).", example = "[\"/templates/packet_template.json\",\"/personas/default.json\"]", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<String> personaFilePath;

	@Schema(description = "Additional info request id from resident email flow; pass null or omit when not used.", example = "abc-123-def")
	private String additionalInfoReqId;

	@Schema(description = "OTP recipient email or phone; required for the OTP request/verify APIs.", example = "resident@example.com")
	@ToString.Exclude
	private String emailOrPhone;

	@Schema(description = "When true, read RID from sync response instead of generating locally.", example = "false")
	private boolean getRidFromSync;

	@Schema(description = "OTP to verify; required for the OTP-verification API.", example = "111111")
	@NotBlank(message = "otp must not be blank", groups = OtpVerification.class)
	@ToString.Exclude
	private String otp;

	/** Validation group applied only by the OTP-verification controller method. */
	public interface OtpVerification {
	}

}
