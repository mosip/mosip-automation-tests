package io.mosip.testrig.dslrig.packetcreator.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Regression: both "duplicate" and "isDuplicate" JSON properties must deserialize.
 */
public class MockABISExpectationsDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void should_deserializeDuplicateAlias_whenJsonUsesDuplicate() throws Exception {
		MockABISExpectationsDto dto = objectMapper.readValue(
				"{\"operation\":\"INSERT\",\"personaPath\":\"/p.json\",\"duplicate\":true}",
				MockABISExpectationsDto.class);
		assertTrue(dto.isDuplicate());
	}

	@Test
	void should_deserializeIsDuplicate_whenJsonUsesIsDuplicate() throws Exception {
		MockABISExpectationsDto dto = objectMapper.readValue(
				"{\"operation\":\"INSERT\",\"personaPath\":\"/p.json\",\"isDuplicate\":true}",
				MockABISExpectationsDto.class);
		assertTrue(dto.isDuplicate());
	}
}
