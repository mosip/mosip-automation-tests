package io.mosip.testrig.dslrig.packetcreator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration(value = "packetcreator_swagger_config")
public class SwaggerConfig {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

	@Autowired
	private OpenApiProperties openApiProperties;

	@Bean
	public OpenAPI openApi() {
		OpenAPI api = new OpenAPI().components(new Components())
				.info(new Info().title(openApiProperties.getInfo().getTitle())
						.version(openApiProperties.getInfo().getVersion())
						.description(OpenApiConstants.API_DESCRIPTION)
						.license(new License().name(openApiProperties.getInfo().getLicense().getName())
								.url(openApiProperties.getInfo().getLicense().getUrl())))
				.addTagsItem(new Tag().name("ContextController").description(
						"Initialize and manage MOSIP test environment context (variables, verification, internal API logs)."))
				.addTagsItem(new Tag().name("PacketController").description(
						"Create registration packets, templates, sync RIDs, bulk upload, and reprocess flows."))
				.addTagsItem(new Tag().name("PersonaController").description(
						"Generate and update persona/resident test data and mock ABIS expectations."))
				.addTagsItem(new Tag().name("preRegController").description(
						"Pre-registration OTP, applications, appointments, and document upload helpers."))
				.addTagsItem(new Tag().name("ResidentController").description(
						"Resident service helpers: RID status, UIN lookup, card download, processing stages."))
				.addTagsItem(new Tag().name("CertificateController").description(
						"Upload mock-SBI device PKCS#12 certificates and clear device cert caches."))
				.addTagsItem(new Tag().name("CommandsController").description(
						"File write utilities and registration-machine private key generation."))
				.addTagsItem(new Tag().name("TestDataController").description(
						"Internal wiring for scheduled jobs and test data services (no public REST methods)."));

		openApiProperties.getService().getServers().forEach(server -> {
			api.addServersItem(new Server().description(server.getDescription()).url(server.getUrl()));
		});
		logger.info("swagger open api bean is ready");
		return api;
	}

	@Bean
	public GroupedOpenApi groupedOpenApi() {
		return GroupedOpenApi.builder().group(openApiProperties.getGroup().getName())
				.pathsToMatch(openApiProperties.getGroup().getPaths().stream().toArray(String[]::new)).build();
	}

}
