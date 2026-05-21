package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.function.Consumer;

import org.junit.Test;

/**
 * Tests for {@link ReadableDslStepCodec}.
 * <p>
 * Maintainability: prefer {@link #assertRoundTrip(String)} with DSL inputs only. That way
 * label and phrase changes in {@code step-parameter-labels.json} or encode logic stay in sync
 * automatically. Use hand-written Gherkin only for legacy or alternate readable forms that
 * encode() does not produce (split {@code @@} fields, bare scenario variables like
 * {@code center77}, shorthands like {@code new template}).
 * </p>
 */
public class ReadableDslStepCodecTest {

    // --- encode / round-trip (DSL is the single source of truth) ---

    @Test
    public void encodeUsesReadableLabelsAndVariableNames() {
        String dsl = "$$personaFilePath=e2e_getResidentData(1,adult,false,Male)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("persona type is adult"));
        assertTrue(encoded.contains("guardian flag is false"));
        assertTrue(encoded.contains("gender is Male"));
        assertTrue(encoded.contains("store result in persona file path"));
        assertTrue(!encoded.contains("argument 1"));
    }

    @Test
    public void roundTripGetResidentDataWithBioFlags() {
        assertRoundTrip("$$personaFilePath=e2e_getResidentData(1,adult,false,Male@@false@@false@@true)");
    }

    @Test
    public void decodeGetResidentDataScenario97BioFlags() {
        String gherkin = "I get resident data where persona type is adult, and guardian flag is false, "
                + "and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true "
                + "and store result in persona file path";
        assertDecodeToDsl(gherkin,
                "$$personaFilePath=e2e_getResidentData(adult,false,Male@@false@@false@@true)");
    }

    @Test
    public void decodeGetResidentDataScenario18BioFlags() {
        String gherkin = "I get resident data where persona type is adult, and guardian flag is false, "
                + "and gender is Male, and password is false@@false@@false "
                + "and store result in persona file path";
        String decoded = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$personaFilePath=e2e_getResidentData(adult,false,Male@@false@@false@@false)",
                stripParamLabels(decoded));
        assertTrue(decoded.contains("Male/*GENDER*/@@false/*FINGER_BIOMETRIC_FLAG*/"));
        assertTrue(!decoded.contains("/*PASSWORD*/"));
        assertTrue(!decoded.contains(",false@@"));
    }

    @Test
    public void decodeGetResidentDataScenario98BioFlags() {
        String gherkin = "I get resident data where persona type is adult, and generate private key is false, "
                + "and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@false "
                + "and store result in persona file path";
        String decoded = stripParamLabels(ReadableDslStepCodec.decode(gherkin));
        assertEquals("$$personaFilePath=e2e_getResidentData(adult,false,Male@@false@@false@@false)", decoded);
        assertTrue(!decoded.contains("GENERATE_PRIVATE_KEY"));
        assertTrue(!decoded.contains("/*PASSWORD*/"));
        String labeled = ReadableDslStepCodec.decode(gherkin);
        assertTrue("labeled=" + labeled, !labeled.contains("/*GENERATE_PRIVATE_KEY*/"));
        assertTrue(labeled.contains("/*GUARDIAN_FLAG*/"));
        assertTrue(labeled.contains("Male/*GENDER*/@@false/*FINGER_BIOMETRIC_FLAG*/"));
        assertTrue(!labeled.contains("/*PASSWORD*/"));
    }

    @Test
    public void decodeGetResidentDataScenario100BioFlags() {
        String gherkin = "I get resident data where persona type is minor, and guardian flag is true, "
                + "and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@false "
                + "and store result in child persona file path";
        String decoded = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$childPersona=e2e_getResidentData(minor,true,Male@@false@@false@@false)",
                stripParamLabels(decoded));
        assertTrue(decoded.contains("Male/*GENDER*/@@false/*FINGER_BIOMETRIC_FLAG*/"));
        assertTrue(decoded.contains("/*FACE_BIOMETRIC_FLAG*/"));
        assertTrue(!decoded.contains("/*PASSWORD*/"));
    }

    @Test
    public void decodeGetResidentDataScenario101BioFlags() {
        String gherkin = "I get resident data where persona type is minor, and guardian flag is true, "
                + "and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true "
                + "and store result in child persona file path";
        String decoded = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$childPersona=e2e_getResidentData(minor,true,Male@@false@@false@@true)",
                stripParamLabels(decoded));
        assertEquals(
                "$$childPersona=e2e_getResidentData(minor/*PERSONA_TYPE*/,true/*GUARDIAN_FLAG*/,"
                        + "Male/*GENDER*/@@false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true/*FACE_BIOMETRIC_FLAG*/)",
                decoded);
        assertTrue(!decoded.contains("/*PASSWORD*/"));
        assertTrue(!decoded.contains(",Male/*GENDER*/,"));
    }

    @Test
    public void roundTripGenerateAndUploadPacket() {
        assertRoundTrip("$$rid=e2e_generateAndUploadPacket($$prid,$$templatePath)");
    }

    @Test
    public void encodeEkycDemoWithNonDollarOutputVar() {
        String dsl = "ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("I ekyc demo where"));
        assertTrue(encoded.contains("store result in ekycData"));
        assertTrue(!encoded.contains("execute action"));
        assertRoundTrip(dsl);
    }

    @Test
    public void roundTripMachineWithScenarioIndexedVariables() {
        assertRoundTrip("$$details77=e2e_Machine(CREATE,$$center77,77)");
    }

    @Test
    public void roundTripUpdateDemoOrBioDetailsMultiBioType() {
        String dsl = "e2e_updateDemoOrBioDetails(face@@iris@@finger,0,0,$$personaFilePath)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("bio type is face@@iris@@finger"));
        assertTrue(!encoded.contains("password is iris"));
        assertRoundTrip(dsl);
    }

    @Test
    public void roundTripUpdateDemoOrBioDetailsWithJoinedUpdateAttributes() {
        assertRoundTrip("e2e_updateDemoOrBioDetails(0,0,addressLine1=bnglr@@phoneNumber=3938333736,$$personaFilePath)");
    }

    @Test
    public void roundTripGetBioModalityHashMultiSubtype() {
        String dsl = "$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(!encoded.contains("password is Left"));
        assertRoundTrip(dsl);
    }

    @Test
    public void roundTripSetContextSupervisorOperatorCbeffCredentials() {
        String dsl = "e2e_setContext(env_context,$$details2,false,null,"
                + "null@@null@@null@@null@@OperatorBiometrics_bio_CBEFF@@SupervisorBiometrics_bio_CBEFF)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("negative test or signature flag is null@@null@@null@@null"));
        assertTrue(!encoded.contains("password is null"));
        assertRoundTrip(dsl);
    }

    @Test
    public void roundTripUpdateBioExceptionInPersonaAllModalities() {
        String dsl = "e2e_UpdateBioExceptionInPersona($$personaFilePath,Finger:Left Thumb@@Finger:Left IndexFinger@@"
                + "Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@"
                + "Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@"
                + "Finger:Right LittleFinger@@Iris:Left@@Iris:Right)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("biometric exception modalities is Finger:Left Thumb@@"));
        assertTrue(!encoded.contains("password is Finger"));
        assertRoundTrip(dsl);
    }

    @Test
    public void roundTripConfigureMockAbisDelayAndStatus() {
        String dsl = "e2e_configureMockAbis(-1,Right IndexFinger,false,Right IndexFinger,$$personaFilePath,"
                + "$$modalityHashValue,-1,@@Success)";
        String decoded = stripParamLabels(ReadableDslStepCodec.decode(ReadableDslStepCodec.encode(dsl)));
        assertTrue(decoded.contains(",-1,@@Success)"));
        assertTrue(!decoded.contains("-1@@Success"));
    }

    @Test
    public void roundTripConfigureMockAbisScenario81() {
        String dsl = "e2e_configureMockAbis(-1,Right IndexFinger,false,Right IndexFinger,"
                + "$$personaFilePath,$$modalityHashValue,-1,@@Success)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.toLowerCase(Locale.ROOT).contains("mock abis status"));
        assertTrue(!encoded.contains("password is"));
        assertRoundTrip(dsl);
    }

    @Test
    public void decodeConfigureMockAbisScenario222SplitStatus() {
        String gherkin = "I configure mock abis where check persona presence is -1, and modality subtypes is "
                + "Right IndexFinger, and duplicate match flag is false, and hash modality keys is Right IndexFinger, "
                + "and persona path is the saved persona file path, and modality hash map is modality hash value, "
                + "and delay from actuator is delay, and mock ABIS status is 10, and password is Error";
        assertDecodeToDsl(gherkin,
                "e2e_configureMockAbis(-1,Right IndexFinger,false,Right IndexFinger,$$personaFilePath,"
                        + "$$modalityHashValue,delay,10@@Error)");
    }

    @Test
    public void roundTripConfigureMockAbisScenario246() {
        String dsl = "e2e_configureMockAbis(-1,Right IndexFinger@@Left LittleFinger,true,"
                + "Right IndexFinger@@Left LittleFinger,$$personaFilePath,$$modalityHashValue,delay,10@@Error)";
        assertRoundTrip(dsl);
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("Right IndexFinger@@Left LittleFinger"));
        assertTrue(!encoded.contains("password is Left LittleFinger"));
    }

    @Test
    public void reformatPreservesCustomUserLabels() {
        String original = "I user where user action is CREATE_CENTERMAPPING, and user index or master user is "
                + "environment 1 details, and center index is 1";
        String reformatted = ReadableDslStepCodec.reformatGherkinStep(original);
        assertTrue(reformatted.contains("center index is 1"));
        assertTrue(!reformatted.contains("password or zone flag"));
    }

    // --- decode from canonical encode output (no duplicated Gherkin text) ---

    @Test
    public void decodeCanonicalGetUinByRid() {
        String dsl = "$$uin=e2e_getUINByRid($$rid)";
        assertDecodeToDsl(ReadableDslStepCodec.encode(dsl), dsl);
    }

    // --- decode: legacy / alternate Gherkin not produced by encode() ---

    @Test
    public void decodeNestedExecuteActionWrapper() {
        String gherkin = "execute action \"I execute action \\\"ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)\\\"\"";
        assertDecodeToDsl(gherkin, "ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)");
    }

    @Test
    public void decodeReadableVariableReferences() {
        String gherkin = "I get uin by rid where source registration ID is the saved registration ID and store result in UIN";
        assertDecodeToDsl(gherkin, "$$uin=e2e_getUINByRid($$rid)");
    }

    @Test
    public void decodeConfigureMockAbisKeepsDelayAndStatusSeparate() {
        String gherkin = "I configure mock abis where check persona presence is -1, and modality subtypes is "
                + "Right IndexFinger, and duplicate match flag is false, and hash modality keys is Right IndexFinger, "
                + "and persona path is $$personaFilePath, and modality hash map is $$modalityHashValue, "
                + "and delay seconds is -1, and password is Success";
        String stripped = stripParamLabels(ReadableDslStepCodec.decode(gherkin));
        assertTrue("delay seconds and status must be comma-separated, not merged with @@",
                stripped.contains(",-1,@@Success)"));
        assertTrue(!stripped.contains("-1@@Success"));
    }

    @Test
    public void decodeMachineResolvesBareScenarioCenterVariable() {
        String gherkin = "I machine where call type is CREATE, and center details is center77, "
                + "and center index is 77 and store result in details77";
        assertDecodeToDsl(gherkin, "$$details77=e2e_Machine(CREATE,$$center77,77)");
    }

    @Test
    public void decodeMachineResolvesBareScenarioCenterVariableDifferentIndex() {
        String gherkin = "I machine where call type is CREATE, and center details is center5, "
                + "and center index is 5 and store result in details5";
        assertDecodeToDsl(gherkin, "$$details5=e2e_Machine(CREATE,$$center5,5)");
    }

    @Test
    public void decodeCenterResolvesBareScenarioUserVariable() {
        String gherkin = "I center where call type is CREATE, and user details is user5, "
                + "and center index is 5, and center active flag is T and store result in center5";
        assertDecodeToDsl(gherkin, "$$center5=e2e_Center(CREATE,$$user5,5,T)");
    }

    @Test
    public void decodeCheckStatusSecondRegistrationId() {
        String gherkin = "I check status where packet status is REREGISTER, "
                + "and registration id is the saved second registration ID";
        assertDecodeToDsl(gherkin, "e2e_checkStatus(REREGISTER,$$rid2)");
    }

    @Test
    public void decodeGeneratePacketNewTemplateShorthand() {
        String gherkin = "I generate and upload packet skipping prereg where persona file path is the saved persona file path, "
                + "and packet template path is new template and store result in second registration ID";
        assertDecodeToDsl(gherkin,
                "$$rid2=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$newTemplate)");
    }

    @Test
    public void decodeGeneratePacketUsesSavedNewTemplate() {
        String gherkin = "I generate and upload packet skipping prereg where persona file path is the saved persona file path, "
                + "and packet template path is the saved new packet template path and store result in second registration ID";
        assertDecodeToDsl(gherkin,
                "$$rid2=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$newTemplate)");
    }

    @Test
    public void decodeScenario241LegacyTemplateShorthandResolvesDslVariable() {
        String getTemplate = "I get packet template where packet type is NEW, and persona file path is resident data "
                + "and store result in template";
        assertDecodeToDsl(getTemplate,
                "$$template=e2e_getPacketTemplate(NEW,$$residentData)");
        String upload = "I generate and upload packet skipping prereg where persona file path is resident data, "
                + "and packet template path is template and store result in registration ID";
        assertDecodeToDsl(upload,
                "$$rid=e2e_generateAndUploadPacketSkippingPrereg($$residentData,$$template)");
    }

    @Test
    public void decodeScenario242PacketFlowResolvesSavedTemplatePath() {
        String getTemplate = "I get packet template where packet type is NEW, and persona file path is the saved persona file path "
                + "and store result in packet template path";
        assertDecodeToDsl(getTemplate,
                "$$templatePath=e2e_getPacketTemplate(NEW,$$personaFilePath)");
        String upload = "I generate and upload packet skipping prereg where persona file path is the saved persona file path, "
                + "and packet template path is the saved packet template path and store result in registration ID";
        assertDecodeToDsl(upload,
                "$$rid=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$templatePath)");
    }

    @Test
    public void decodeRidsyncLostPacketZipPath() {
        String gherkin = "I ridsync where packet type is LOST, and packet zip path is the saved packet zip path "
                + "and store result in rid lost";
        assertDecodeToDsl(gherkin, "$$ridLost=e2e_Ridsync(LOST,$$zipPacketPath)");
    }

    @Test
    public void decodePostMockMvResolvesSecondRegistrationId() {
        String gherkin = "I post mock mv where registration id is the saved second registration ID, "
                + "and manual verification decision is PROCESSED";
        assertDecodeToDsl(gherkin, "e2e_postMockMv($$rid2,PROCESSED)");
    }

    @Test
    public void decodeResolvesDisplayStyleScenarioVariableReference() {
        String gherkin = "I get packet template where packet type is BIOMETRIC_CORRECTION, "
                + "and persona file path is persona file path1 and store result in template path1";
        assertDecodeToDsl(gherkin,
                "$$templatePath1=e2e_getPacketTemplate(BIOMETRIC_CORRECTION,$$personaFilePath1)");
    }

    @Test
    public void decodeUpdateDemoOrBioDetailsRecoversSplitBioType() {
        String gherkin = "I update demo or bio details where bio type is face, and password is iris@@finger, "
                + "and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path";
        assertDecodeToDsl(gherkin, "e2e_updateDemoOrBioDetails(face@@iris@@finger,0,0,$$personaFilePath)");
    }

    @Test
    public void decodeUpdateDemoOrBioDetailsRecoversSplitUpdateAttributes() {
        String gherkin = "I update demo or bio details where bio type is 0, and miss fields is 0, "
                + "and update attributes is addressLine1=bnglr, and password is phoneNumber=3938333736, "
                + "and parameter 5 is the saved persona file path";
        assertDecodeToDsl(gherkin,
                "e2e_updateDemoOrBioDetails(0,0,addressLine1=bnglr@@phoneNumber=3938333736,$$personaFilePath)");
    }

    @Test
    public void decodeGetBioModalityHashRecoversSplitModalitySubtypes() {
        String gherkin = "I get bio modality hash where check persona presence is -1, and modality subtypes is "
                + "Right IndexFinger, and password is Left LittleFinger, and parameter 4 is the saved persona file path "
                + "and store result in modality hash value";
        assertDecodeToDsl(gherkin,
                "$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath)");
    }

    @Test
    public void decodeGetBioModalityHashWithPersonaFilePathIndexedVariable() {
        String gherkin = "I get bio modality hash where persona ID is -1, and modality subtypes is "
                + "Right IndexFinger@@Left LittleFinger, and persona path is the saved persona file path1 "
                + "and store result in modality hash value";
        assertDecodeToDsl(gherkin,
                "$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath1)");
    }

    @Test
    public void decodeGetBioModalityHashLegacySplitModalitySubtypesWithIndexedPersonaPath() {
        String gherkin = "I get bio modality hash where check persona presence is -1, and modality subtypes is "
                + "Right IndexFinger, and password is Left LittleFinger, and parameter 4 is persona file path1 "
                + "and store result in modality hash value";
        assertDecodeToDsl(gherkin,
                "$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath1)");
    }

    @Test
    public void decodeSetContextRecoversSplitCredentialBundle() {
        String gherkin = "I set context where context key is env_context, and pre-requisite details is the saved "
                + "environment 2 details, and generate private key is false, and put scenario details in context is null, "
                + "and negative test or signature flag is null, and password is null@@null@@null@@null@@"
                + "OperatorBiometrics_bio_CBEFF@@SupervisorBiometrics_bio_CBEFF";
        assertDecodeToDsl(gherkin,
                "e2e_setContext(env_context,$$details2,false,null,"
                        + "null@@null@@null@@null@@OperatorBiometrics_bio_CBEFF@@SupervisorBiometrics_bio_CBEFF)");
    }

    @Test
    public void decodeUpdateBioExceptionInPersonaRecoversSplitModalities() {
        String gherkin = "I update bio exception in persona where persona file path is the saved persona file path, "
                + "and biometric exception modalities is Finger:Left Thumb, and password is Finger:Left IndexFinger@@"
                + "Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@"
                + "Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@"
                + "Finger:Right LittleFinger@@Iris:Left@@Iris:Right";
        assertDecodeToDsl(gherkin,
                "e2e_UpdateBioExceptionInPersona($$personaFilePath,Finger:Left Thumb@@Finger:Left IndexFinger@@"
                        + "Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@"
                        + "Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@"
                        + "Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right)");
    }

    @Test
    public void decodeUpdateBioExceptionInPersonaTwoModalities() {
        String gherkin = "I update bio exception in persona where persona file path is the saved persona file path, "
                + "and biometric exception modalities is Finger:Left IndexFinger, and password is Finger:Right IndexFinger";
        assertDecodeToDsl(gherkin,
                "e2e_UpdateBioExceptionInPersona($$personaFilePath,Finger:Left IndexFinger@@Finger:Right IndexFinger)");
    }

    // --- helpers ---

    private static void assertRoundTrip(String dsl) {
        assertRoundTrip(dsl, null);
    }

    private static void assertRoundTrip(String dsl, Consumer<String> extraDecodedChecks) {
        String encoded = ReadableDslStepCodec.encode(dsl);
        String decoded = stripParamLabels(ReadableDslStepCodec.decode(encoded));
        assertEquals(dsl, decoded);
        if (extraDecodedChecks != null) {
            extraDecodedChecks.accept(decoded);
        }
    }

    private static void assertDecodeToDsl(String gherkin, String expectedDsl) {
        assertEquals(expectedDsl, stripParamLabels(ReadableDslStepCodec.decode(gherkin)));
    }

    private static String stripParamLabels(String dsl) {
        return dsl.replaceAll("/\\*[^*]*\\*/", "");
    }
}
