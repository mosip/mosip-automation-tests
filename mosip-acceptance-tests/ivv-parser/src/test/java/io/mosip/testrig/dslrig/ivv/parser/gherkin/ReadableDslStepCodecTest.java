package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Generic round-trip tests for {@link ReadableDslStepCodec} (no scenario-specific cases).
 */
public class ReadableDslStepCodecTest {

    @Test
    public void roundTripGetResidentDataWithBioFlags() {
        assertRoundTrip("$$personaFilePath=e2e_getResidentData(adult,false,Male@@false@@false@@true)");
    }

    @Test
    public void encodeShowsAndBetweenListSegments() {
        String encoded = ReadableDslStepCodec.encode(
                "$$personaFilePath=e2e_getResidentData(adult,false,Male@@false@@false@@true)");
        assertTrue(encoded.contains("gender and biometric flags is Male and false and false and true"));
        assertFalse(encoded.contains("password is"));
        assertFalse(encoded.contains("/*"));
    }

    @Test
    public void decodeEnglishAndListInsideOneArgument() {
        String gherkin = "I get resident data where persona type is adult, and guardian flag is false, "
                + "and gender and biometric flags is Male and false and false and true "
                + "and store result in persona file path";
        assertDecodeToDsl(gherkin,
                "$$personaFilePath=e2e_getResidentData(adult,false,Male@@false@@false@@true)");
    }

    @Test
    public void decodeLegacySpilloverPasswordClauseIntoOneArgument() {
        String gherkin = "I get resident data where persona type is adult, and guardian flag is false, "
                + "and gender is Male, and password is false@@false@@false and store result in persona file path";
        assertDecodeToDsl(gherkin,
                "$$personaFilePath=e2e_getResidentData(adult,false,Male@@false@@false@@false)");
    }

    @Test
    public void roundTripMissingBiometricFields() {
        assertRoundTrip("$$personaFilePath=e2e_getResidentData(adult,false,Male,leftiris@@rightIris)");
    }

    @Test
    public void roundTripGenerateAndUploadPacket() {
        assertRoundTrip("$$rid=e2e_generateAndUploadPacket($$prid,$$templatePath)");
    }

    @Test
    public void roundTripUserCredentialsInOneArgument() {
        String dsl = "e2e_User(DELETE_CENTERMAPPING,dsl1@@Techno@123,$$details1)";
        assertRoundTrip(dsl);
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("dsl1 and Techno@123"));
    }

    @Test
    public void roundTripConfigureMockAbisStatusWithAnd() {
        String dsl = "e2e_configureMockAbis(-1,Right IndexFinger,false,Right IndexFinger,"
                + "$$personaFilePath,$$modalityHashValue,delay,10@@Error)";
        assertRoundTrip(dsl);
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("10 and Error"));
        assertFalse(encoded.toLowerCase().contains("password is"));
    }

    @Test
    public void roundTripUpdateDemoOrBioDetailsMultiBioType() {
        assertRoundTrip("e2e_updateDemoOrBioDetails(face@@iris@@finger,0,0,$$personaFilePath)");
    }

    @Test
    public void roundTripMachineWithVariables() {
        assertRoundTrip("$$details77=e2e_Machine(CREATE,$$center77,77)");
    }

    @Test
    public void decodeReadableVariableReferences() {
        String gherkin = "I get uin by rid where source registration ID is the saved registration ID "
                + "and store result in UIN";
        assertDecodeToDsl(gherkin, "$$uin=e2e_getUINByRid($$rid)");
    }

    @Test
    public void decodeMultiWordDisplayNameAsScenarioVariable() {
        String packetcreator = "I packetcreator where packet type is LOST, and template path is lost template "
                + "and store result in packet zip path";
        assertDecodeToDsl(packetcreator,
                "$$zipPacketPath=e2e_Packetcreator(LOST,$$lostTemplate)");

        String ridsync = "I ridsync where packet type is LOST, and packet zip path is the saved packet zip path "
                + "and store result in rid lost";
        assertDecodeToDsl(ridsync, "$$ridLost=e2e_Ridsync(LOST,$$zipPacketPath)");

        String statusCheck = "I check status where packet status is PROCESSED, and registration ID is rid lost";
        assertDecodeToDsl(statusCheck, "e2e_checkStatus(PROCESSED,$$ridLost)");
    }

    @Test
    public void decodeMultiWordZipPathReferenceWithoutSavedPrefix() {
        String ridsync = "I ridsync where packet type is NEW, and packet zip path is parent zip packet path "
                + "and store result in parent registration ID";
        assertDecodeToDsl(ridsync,
                "$$parentRid=e2e_Ridsync(NEW,$$parentZipPacketPath)");
    }

    @Test
    public void decodeDoesNotTreatPacketTypeAsScenarioVariable() {
        String gherkin = "I packetcreator where packet type is LOST, and template path is the saved packet template path "
                + "and store result in packet zip path";
        assertDecodeToDsl(gherkin,
                "$$zipPacketPath=e2e_Packetcreator(LOST,$$templatePath)");
    }

    @Test
    public void decodeVariableCatalogDisplayNameResolvesToDslVariable() {
        String gherkin = "I ridsync where packet type is NEW, and packet zip path is the saved packet zip path, "
                + "and additional info request ID is additional req id and store result in registration ID";
        assertDecodeToDsl(gherkin, "$$rid=e2e_Ridsync(NEW,$$zipPacketPath,$$additionalReqId)");
    }

    @Test
    public void roundTripVariableFromCatalog() {
        assertRoundTrip("$$rid=e2e_Ridsync(NEW,$$zipPacketPath,$$additionalReqId)");
    }

    @Test
    public void reformatFixesLegacySplitMissFields() {
        String legacy = "I get resident data where persona type is adult, and guardian flag is false, "
                + "and gender is Male, and missing biometric fields is leftiris, and password is rightIris "
                + "and store result in persona file path";
        String reformatted = ReadableDslStepCodec.reformatGherkinStep(legacy);
        assertTrue(reformatted.contains("leftiris and rightIris"));
        assertFalse(reformatted.contains("password is"));
        assertRoundTrip(ReadableDslStepCodec.decode(legacy));
    }

    @Test
    public void decodeValidateKycDataKeepsFieldNameLiteral() {
        String gherkin = "I validate kyc data where KYC field is photo, and response variable is ekycData";
        assertDecodeToDsl(gherkin, "e2e_validateKycData(photo,ekycData)");
    }

    @Test
    public void decodeLiteralArgumentRuleKeepsFieldNameWithoutDollarPrefix() {
        String legacy = "I validate kyc data where UIN is photo, and persona file path is ekycData";
        assertDecodeToDsl(legacy, "e2e_validateKycData(photo,ekycData)");
    }

    @Test
    public void encodeValidateKycDataUsesKycFieldLabelNotUin() {
        String encoded = ReadableDslStepCodec.encode("e2e_validateKycData(photo,ekycData)");
        assertTrue(encoded.contains("KYC field is photo"));
        assertFalse(encoded.contains("UIN is photo"));
    }

    @Test
    public void reformatLegacyValidateKycKeepsPhotoNotDemoFieldName() {
        String legacy = "I validate kyc data where uin is photo, and persona file path is ekycData";
        String reformatted = ReadableDslStepCodec.reformatGherkinStep(legacy);
        assertTrue(reformatted.contains("KYC field is photo"));
        assertFalse(reformatted.contains("KYC field is name"));
        assertEquals("e2e_validateKycData(photo,ekycData)", ReadableDslStepCodec.decode(reformatted));
    }

    @Test
    public void roundTripGetBioModalityHashWithAnnotatedPersonaId() {
        assertRoundTrip("e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath)");
    }

    @Test
    public void roundTripBulkUploadPacketTwoPaths() {
        assertRoundTrip("e2e_bulkUploadPacket($$firstZipPacketPath,$$secondZipPacketPath)");
    }

    @Test
    public void decodeBulkUploadPacketTwoPaths() {
        String gherkin = "I bulk upload packet where first packet zip path is the saved first zip packet path, "
                + "and second packet zip path is the saved second zip packet path";
        assertDecodeToDsl(gherkin,
                "e2e_bulkUploadPacket($$firstZipPacketPath,$$secondZipPacketPath)");
    }

    private static void assertRoundTrip(String dsl) {
        String encoded = ReadableDslStepCodec.encode(dsl);
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(stripInlineAnnotations(dsl), stripInlineAnnotations(decoded));
    }

    private static String stripInlineAnnotations(String dsl) {
        return dsl.replaceAll("/\\*[^*]*\\*/", "");
    }

    private static void assertDecodeToDsl(String gherkin, String expectedDsl) {
        assertEquals(expectedDsl, ReadableDslStepCodec.decode(gherkin));
    }
}
