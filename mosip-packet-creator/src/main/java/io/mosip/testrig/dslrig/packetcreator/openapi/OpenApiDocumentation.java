package io.mosip.testrig.dslrig.packetcreator.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Reusable OpenAPI annotations for common Packet Creator request/response shapes.
 */
public final class OpenApiDocumentation {

	private OpenApiDocumentation() {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@RequestBody(
			description = OpenApiConstants.BASE64_P12_BODY_DESC,
			required = true,
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(
							type = "string",
							format = "byte",
							description = "Base64-encoded PKCS#12 (.p12) file content",
							example = "MIIKpAIBAzCCCl4GCSqGSIb3DQEHAaCCCk0EggpJ"),
					examples = @ExampleObject(
							name = "Base64 PKCS#12 string",
							summary = "Quoted JSON string",
							value = "\"MIIKpAIBAzCCCl4GCSqGSIb3DQEHAaCCCk0EggpJ...\"")))
	public @interface Base64P12RequestBody {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@RequestBody(
			description = OpenApiConstants.PERSONA_PATH_BODY_DESC,
			required = true,
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(type = "string", description = "Persona JSON file path", example = "/personas/default.json"),
					examples = @ExampleObject(name = "Persona path", value = "\"/personas/default_person.json\"")))
	public @interface PersonaPathRequestBody {
	}

	@Target({ ElementType.METHOD, ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponses({
			@ApiResponse(responseCode = "400", description = OpenApiConstants.RESPONSE_400),
			@ApiResponse(responseCode = "500", description = OpenApiConstants.RESPONSE_500) })
	public @interface StandardErrorResponses {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Plain-text success message with saved file path",
					content = @Content(
							mediaType = MediaType.TEXT_PLAIN_VALUE,
							schema = @Schema(
									type = "string",
									example = "File uploaded successfully and saved as /tmp/db-server/device-dsk-partner.p12"))),
			@ApiResponse(
					responseCode = "500",
					description = "Failed to write certificate file",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(type = "string", example = "{\"error\":\"Access denied\"}"))) })
	public @interface UploadDeviceCertResponses {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "JSON string: `{\"Success\"}` when mock MDS key cache was evicted; `{\"Failed\"}` on error",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(type = "string", example = "{\"Success\"}"))) })
	public @interface ClearDeviceCertCacheResponses {
	}

}
