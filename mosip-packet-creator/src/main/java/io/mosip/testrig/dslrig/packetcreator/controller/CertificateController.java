package io.mosip.testrig.dslrig.packetcreator.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.sbi.devicehelper.SBIDeviceHelper;
import io.mosip.testrig.dslrig.dataprovider.BiometricDataProvider;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;
import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiConstants;
import io.mosip.testrig.dslrig.packetcreator.openapi.OpenApiDocumentation;
import io.mosip.testrig.dslrig.packetcreator.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "CertificateController", description = """
		REST APIs for mock-SBI device certificates used during biometric capture in DSL tests.

		Upload partner device PKCS#12 files and refresh in-memory certificate caches on the mock MDS side.
		""")
public class CertificateController {

	@Value("${mosip.test.persona.configpath}")
	private String personaConfigPath;

	@Autowired
	CertificateService certificateService;

	private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

	@Operation(
			summary = "Upload a device PKCS#12 certificate",
			description = """
					Decodes a **Base64-encoded PKCS#12** (`.p12`) payload and writes it for the given context.

					**What happens**
					1. Resolves `{db-server}` from context variables for `contextKey`.
					2. Creates `{java.io.tmpdir}/{db-server}/` if missing.
					3. Saves the file as **device-dsk-partner.p12**.
					4. Returns a plain-text message with the absolute path on success.

					**When to use:** Before mock SBI / device auth flows that require a partner device key on the test runner.
					""")
	@OpenApiDocumentation.Base64P12RequestBody
	@OpenApiDocumentation.UploadDeviceCertResponses
	@OpenApiDocumentation.StandardErrorResponses
	@PostMapping(value = "/uploadDeviceCert/{contextKey}")
	public @ResponseBody String uploadDeviceCert(
			@RequestBody String encodedDeviceCert,
			@Parameter(description = OpenApiConstants.CONTEXT_KEY_DESC, required = true, example = OpenApiConstants.CONTEXT_KEY_EXAMPLE)
			@PathVariable("contextKey") String contextKey) {
		try {
			byte[] fileBytes = Base64.getDecoder().decode(encodedDeviceCert);
			String tempDir = System.getProperty("java.io.tmpdir") + File.separator
					+ VariableManager.getVariableValue(contextKey, "db-server");
			File file = new File(tempDir, "device-dsk-partner.p12");
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(fileBytes);
			}
			return "File uploaded successfully and saved as " + file.getAbsolutePath();
		} catch (IOException e) {
			logger.error("Error uploading device certificate", e);
			return "{\"error\":\"" + e.getMessage() + "\"}";
		}
	}

	@Operation(
			summary = "Clear device certificate cache (mock MDS)",
			description = """
					Evicts cached device keys from **SBIDeviceHelper** for the AUTHCERTS path of this context.

					**What happens**
					1. Resolves `AUTHCERTS` from environment variable or context variable.
					2. Falls back to `{java.io.tmpdir}/AUTHCERTS` if unset.
					3. Builds path `DSL-IDA-{db-server}` under the certs directory and calls `evictKeys`.

					**Response body:** JSON string `{"Success"}` or `{"Failed"}` (HTTP 200 in both cases).
					""")
	@OpenApiDocumentation.ClearDeviceCertCacheResponses
	@OpenApiDocumentation.StandardErrorResponses
	@GetMapping(value = "/clearDeviceCertCache/{contextKey}")
	public @ResponseBody String clearDeviceCertCache(
			@Parameter(description = OpenApiConstants.CONTEXT_KEY_DESC, required = true, example = OpenApiConstants.CONTEXT_KEY_EXAMPLE)
			@PathVariable("contextKey") String contextKey) {
		try {
			Path p12path = null;
			String certsDir = System.getenv(BiometricDataProvider.AUTHCERTSPATH) == null
					? VariableManager.getVariableValue(contextKey, BiometricDataProvider.AUTHCERTSPATH).toString()
					: System.getenv(BiometricDataProvider.AUTHCERTSPATH);

			if (certsDir == null || certsDir.length() == 0) {
				certsDir = System.getProperty("java.io.tmpdir") + File.separator + "AUTHCERTS";
			}

			p12path = Paths.get(certsDir, "DSL-IDA-" + VariableManager.getVariableValue(contextKey, "db-server"));

			SBIDeviceHelper.evictKeys(p12path.toString());
			return "{\"Success\"}";
		} catch (Exception ex) {
			logger.error("Clear device certificate cache ", ex);
		}
		return "{\"Failed\"}";
	}

}
