package io.mosip.testrig.dslrig.packetcreator.openapi;

/**
 * Shared OpenAPI description text for Packet Creator.
 */
public final class OpenApiConstants {

	private OpenApiConstants() {
	}

	public static final String API_DESCRIPTION = """
			Packet Creator is a DSL Rig companion service used by MOSIP automation tests. \
			It builds registration packets from persona templates, syncs RIDs with Registration Processor, \
			manages test context variables, and integrates with mock SBI, pre-registration, and resident services.

			**Typical flow**
			1. `POST /context/server/{contextKey}` — initialize environment variables for a target MOSIP stack.
			2. Create persona / packet templates via Persona and Packet APIs.
			3. `POST /ridsync/{contextKey}` or `/packetsync/{contextKey}` — sync and upload packets.
			4. Use Resident APIs to poll RID status and retrieve UIN.

			**contextKey** must match the key used when the context was created. Most endpoints require a prior successful context initialization.
			""";

	public static final String CONTEXT_KEY_DESC = """
			Logical environment key for this MOSIP target. Selects variables loaded via \
			`POST /context/server/{contextKey}`. Example: `default`, `dev`, or the name used in your DSL scenario.
			""";

	public static final String CONTEXT_KEY_EXAMPLE = "default";

	public static final String RID_DESC = "Registration ID (RID) assigned by Registration Processor after successful packet sync.";

	public static final String RID_EXAMPLE = "10001100790000120240101120000001";

	public static final String PREREG_ID_DESC = "Pre-registration application ID from the MOSIP pre-registration module.";

	public static final String UIN_DESC = "12-digit Unique Identification Number (UIN) of the resident.";

	public static final String PROCESS_DESC = "MOSIP process name for the packet, e.g. `NEW`, `UPDATE`, `LOST`, `ACTIVATED`.";

	public static final String MACHINE_ID_DESC = "Registration client machine identifier used for key generation.";

	public static final String BASE64_P12_BODY_DESC = """
			**Request body (required):** JSON string containing the **Base64-encoded** bytes of a PKCS#12 (`.p12`) device certificate file.

			Send as `application/json` with a quoted string value, for example:
			`"MIIKpAIBAzCCCl4GCSqGSIb3DQEHAaCCCk0EggpJ..."`

			The service decodes the string, creates `{java.io.tmpdir}/{db-server}/` if needed, and writes **device-dsk-partner.p12**.
			""";

	public static final String PERSONA_PATH_BODY_DESC = """
			**Request body (required):** JSON string — absolute or DSL-relative path to the persona JSON file used for card download.

			Example: `"/path/to/persona/default_person.json"`
			""";

	public static final String RESPONSE_400 = "Invalid request — missing required fields, unknown context, or validation failure.";

	public static final String RESPONSE_500 = "Unexpected server error. See response body for error code and message (ServiceException).";

}
