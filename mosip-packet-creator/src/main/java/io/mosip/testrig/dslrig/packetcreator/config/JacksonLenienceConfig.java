package io.mosip.testrig.dslrig.packetcreator.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.DeserializationFeature;

/**
 * Spring Boot 4 uses Jackson 3, which by default rejects null for primitive fields and trailing tokens.
 * The DSL orchestrator's request bodies relied on the Jackson 2 lenience, so keep it.
 */
@Configuration
public class JacksonLenienceConfig {

	@Bean
	public JsonMapperBuilderCustomizer jackson2CompatibleDeserialization() {
		return builder -> builder.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
				DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
	}
}
