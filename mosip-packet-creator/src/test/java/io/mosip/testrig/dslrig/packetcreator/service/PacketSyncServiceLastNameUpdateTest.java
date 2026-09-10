package io.mosip.testrig.dslrig.packetcreator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Properties;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import io.mosip.testrig.dslrig.dataprovider.models.Name;
import io.mosip.testrig.dslrig.dataprovider.models.ResidentModel;
import io.mosip.testrig.dslrig.dataprovider.util.Gender;

class PacketSyncServiceLastNameUpdateTest {

	@Test
	void should_updateOnlyLastName_keepingFirstAndMiddleName() throws Exception {
		PacketSyncService service = new PacketSyncService(null, null);
		ResidentModel persona = new ResidentModel();
		Name name = new Name();
		name.setFirstName("John");
		name.setMidName("Quincy");
		name.setSurName("Doe");
		persona.setName(name);
		persona.setPrimaryLanguage("eng");
		persona.setGender(Gender.Male);

		Name secName = new Name();
		secName.setFirstName("Jean");
		secName.setMidName("Q");
		secName.setSurName("Doe");
		persona.setName_seclang(secName);
		persona.setSecondaryLanguage("fra");

		Properties attrs = new Properties();
		attrs.setProperty("lastName", "Smith");
		JSONObject result = service.updatePersona(attrs, persona, "test_context");

		assertEquals("John", persona.getName().getFirstName());
		assertEquals("Quincy", persona.getName().getMidName());
		assertEquals("Smith", persona.getName().getSurName());
		assertEquals("Jean", persona.getName_seclang().getFirstName());
		assertEquals("Smith", persona.getName_seclang().getSurName());
		assertEquals("Doe", result.getJSONObject("oldValues").getString("lastName"));
		assertEquals("Smith", result.getJSONObject("newValues").getString("lastName"));
		assertNotEquals("Doe", persona.getName().getSurName());
	}

	@Test
	void should_acceptSurNameAliasForLastNameUpdate() {
		PacketSyncService service = new PacketSyncService(null, null);
		ResidentModel persona = new ResidentModel();
		Name name = new Name();
		name.setFirstName("Ada");
		name.setMidName("Lovelace");
		name.setSurName("Byron");
		persona.setName(name);
		persona.setPrimaryLanguage("eng");
		persona.setGender(Gender.Female);

		Properties attrs = new Properties();
		attrs.setProperty("surName", "King");
		service.updatePersona(attrs, persona, "test_context");

		assertEquals("Ada", persona.getName().getFirstName());
		assertEquals("Lovelace", persona.getName().getMidName());
		assertEquals("King", persona.getName().getSurName());
	}

	@Test
	void should_regenerateLastNameWhenAttributeHasNoValue() throws Exception {
		PacketSyncService service = new PacketSyncService(null, null);
		ResidentModel persona = new ResidentModel();
		Name name = new Name();
		name.setFirstName("Maya");
		name.setMidName("R");
		name.setSurName("Patel");
		persona.setName(name);
		persona.setPrimaryLanguage("eng");
		persona.setGender(Gender.Female);

		Properties attrs = new Properties();
		attrs.setProperty("lastName", "");
		JSONObject result = service.updatePersona(attrs, persona, "test_context");

		assertEquals("Maya", persona.getName().getFirstName());
		assertEquals("R", persona.getName().getMidName());
		assertNotEquals("Patel", persona.getName().getSurName());
		assertEquals("Patel", result.getJSONObject("oldValues").getString("lastName"));
		assertEquals(persona.getName().getSurName(), result.getJSONObject("newValues").getString("lastName"));
	}
}
