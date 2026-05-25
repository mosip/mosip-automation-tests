package io.mosip.testrig.dslrig.packetcreator.config;

import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiConstants;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration
public class OpenApiCustomizerConfig {

	private static final Map<String, ParameterDoc> KNOWN_PARAMETERS = Map.ofEntries(
			Map.entry("contextKey", new ParameterDoc(OpenApiConstants.CONTEXT_KEY_DESC, OpenApiConstants.CONTEXT_KEY_EXAMPLE)),
			Map.entry("rid", new ParameterDoc(OpenApiConstants.RID_DESC, OpenApiConstants.RID_EXAMPLE)),
			Map.entry("preregId", new ParameterDoc(OpenApiConstants.PREREG_ID_DESC, "12345678901234")),
			Map.entry("uin", new ParameterDoc(OpenApiConstants.UIN_DESC, "123456789012")),
			Map.entry("process", new ParameterDoc(OpenApiConstants.PROCESS_DESC, "NEW")),
			Map.entry("machineId", new ParameterDoc(OpenApiConstants.MACHINE_ID_DESC, "10001")),
			Map.entry("offset", new ParameterDoc("Byte offset in the target file where content is written.", "0")),
			Map.entry("getRidFromSync", new ParameterDoc("When true, obtain RID from sync response instead of generating locally.", "true")),
			Map.entry("genarateValidCbeff", new ParameterDoc("When true, generate CBEFF XML that passes validation rules.", "true")),
			Map.entry("introducerInfoToken", new ParameterDoc("When true, validate introducer info token for CRVS packets.", "false")),
			Map.entry("qualityScore", new ParameterDoc("Biometric quality score applied to generated CBEFF (string numeric).", "90")),
			Map.entry("clear", new ParameterDoc("When true, drain and clear collected internal API logs after read.", "true")),
			Map.entry("reportHints", new ParameterDoc("When true, embed HTML report hints in formatted log output.", "false")));

	@Bean
	public OperationCustomizer packetCreatorOperationCustomizer() {
		return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {
			enrichParameters(operation);
			addDefaultErrorResponses(operation);
			return operation;
		};
	}

	private static void enrichParameters(Operation operation) {
		if (operation.getParameters() == null) {
			return;
		}
		for (Parameter parameter : operation.getParameters()) {
			ParameterDoc doc = KNOWN_PARAMETERS.get(parameter.getName());
			if (doc == null) {
				continue;
			}
			if (parameter.getDescription() == null || parameter.getDescription().isBlank()
					|| parameter.getDescription().equals(parameter.getName())) {
				parameter.setDescription(doc.description());
			}
			if (parameter.getExample() == null) {
				parameter.setExample(doc.example());
			}
			if ("path".equals(parameter.getIn())) {
				parameter.setRequired(true);
			}
		}
	}

	private static void addDefaultErrorResponses(Operation operation) {
		ApiResponses responses = operation.getResponses();
		if (responses == null) {
			responses = new ApiResponses();
			operation.setResponses(responses);
		}
		addErrorResponseIfAbsent(responses, "400", OpenApiConstants.RESPONSE_400);
		addErrorResponseIfAbsent(responses, "500", OpenApiConstants.RESPONSE_500);
	}

	private static void addErrorResponseIfAbsent(ApiResponses responses, String code, String description) {
		if (responses.containsKey(code)) {
			return;
		}
		Schema<?> errorSchema = new Schema<>();
		errorSchema.setType("object");
		errorSchema.setDescription("ServiceException error payload (fields vary by endpoint)");
		errorSchema.setExample(Map.of("errorCode", "EXAMPLE_ERROR", "message", "Detailed error message"));

		Content content = new Content().addMediaType("application/json", new MediaType().schema(errorSchema));
		responses.addApiResponse(code, new ApiResponse().description(description).content(content));
	}

	private record ParameterDoc(String description, String example) {
	}

}
