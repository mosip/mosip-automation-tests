package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReadableDslStepCodecTest {

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
    public void roundTripScenarioStep() {
        String dsl = "$$rid=e2e_generateAndUploadPacket($$prid,$$templatePath)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void reformatPreservesCustomUserLabels() {
        String original = "I user where user action is CREATE_CENTERMAPPING, and user index or master user is "
                + "environment 1 details, and center index is 1";
        String reformatted = ReadableDslStepCodec.reformatGherkinStep(original);
        assertTrue(reformatted.contains("center index is 1"));
        assertTrue(!reformatted.contains("password or zone flag"));
    }

    @Test
    public void decodeReadableVariableReferences() {
        String gherkin = "I get uin by rid where source registration ID is the saved registration ID and store result in UIN";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$uin=e2e_getUINByRid($$rid)", stripParamLabels(dsl));
    }

    @Test
    public void encodeEkycDemoWithNonDollarOutputVar() {
        String dsl = "ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("I ekyc demo where"));
        assertTrue(encoded.contains("store result in ekycData"));
        assertTrue(!encoded.contains("execute action"));
    }

    @Test
    public void decodeNestedExecuteActionWrapper() {
        String gherkin = "execute action \"I execute action \\\"ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)\\\"\"";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)", stripParamLabels(dsl));
    }

    @Test
    public void roundTripScenario111EkycDemo() {
        String dsl = "ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void decodeConfigureMockAbisKeepsDelayAndStatusSeparate() {
        String gherkin = "I configure mock abis where check persona presence is -1, and modality subtypes is "
                + "Right IndexFinger, and duplicate match flag is false, and hash modality keys is Right IndexFinger, "
                + "and persona path is $$personaFilePath, and modality hash map is $$modalityHashValue, "
                + "and delay seconds is -1, and password is Success";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        String stripped = stripParamLabels(dsl);
        assertTrue("delay seconds and status must be comma-separated, not merged with @@",
                stripped.contains(",-1,@@Success)"));
        assertTrue(!stripped.contains("-1@@Success"));
    }

    @Test
    public void decodeScenario77MachineStepResolvesCenterVariable() {
        String gherkin = "I machine where call type is CREATE, and center details is center77, "
                + "and center index is 77 and store result in details77";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$details77=e2e_Machine(CREATE,$$center77,77)", stripParamLabels(dsl));
    }

    @Test
    public void decodeScenario197MachineStepResolvesCenterVariable() {
        String gherkin = "I machine where call type is CREATE, and center details is center5, "
                + "and center index is 5 and store result in details5";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$details5=e2e_Machine(CREATE,$$center5,5)", stripParamLabels(dsl));
    }

    @Test
    public void decodeScenario197CenterStepResolvesUserVariable() {
        String gherkin = "I center where call type is CREATE, and user details is user5, "
                + "and center index is 5, and center active flag is T and store result in center5";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$center5=e2e_Center(CREATE,$$user5,5,T)", stripParamLabels(dsl));
    }

    @Test
    public void roundTripScenario77MachineStep() {
        String dsl = "$$details77=e2e_Machine(CREATE,$$center77,77)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void decodeCheckStatusSecondRegistrationId() {
        String gherkin = "I check status where packet status is REREGISTER, "
                + "and registration id is the saved second registration ID";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("e2e_checkStatus(REREGISTER,$$rid2)", stripParamLabels(dsl));
    }

    @Test
    public void decodeGeneratePacketNewTemplateShorthand() {
        String gherkin = "I generate and upload packet skipping prereg where persona file path is the saved persona file path, "
                + "and packet template path is new template and store result in second registration ID";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$rid2=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$newTemplate)",
                stripParamLabels(dsl));
    }

    @Test
    public void decodeGeneratePacketUsesSavedNewTemplate() {
        String gherkin = "I generate and upload packet skipping prereg where persona file path is the saved persona file path, "
                + "and packet template path is the saved new packet template path and store result in second registration ID";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$rid2=e2e_generateAndUploadPacketSkippingPrereg($$personaFilePath,$$newTemplate)",
                stripParamLabels(dsl));
    }

    @Test
    public void decodeRidsyncLostPacketZipPath() {
        String gherkin = "I ridsync where packet type is LOST, and packet zip path is the saved packet zip path "
                + "and store result in rid lost";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$ridLost=e2e_Ridsync(LOST,$$zipPacketPath)", stripParamLabels(dsl));
    }

    @Test
    public void decodePostMockMvResolvesSecondRegistrationId() {
        String gherkin = "I post mock mv where registration id is the saved second registration ID, "
                + "and manual verification decision is PROCESSED";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("e2e_postMockMv($$rid2,PROCESSED)", stripParamLabels(dsl));
    }

    @Test
    public void decodeResolvesDisplayStyleScenarioVariableReference() {
        String gherkin = "I get packet template where packet type is BIOMETRIC_CORRECTION, "
                + "and persona file path is persona file path1 and store result in template path1";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$templatePath1=e2e_getPacketTemplate(BIOMETRIC_CORRECTION,$$personaFilePath1)",
                stripParamLabels(dsl));
    }

    @Test
    public void roundTripUpdateDemoOrBioDetailsMultiBioType() {
        String dsl = "e2e_updateDemoOrBioDetails(face@@iris@@finger,0,0,$$personaFilePath)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("bio type is face@@iris@@finger"));
        assertTrue(!encoded.contains("password is iris"));
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void decodeUpdateDemoOrBioDetailsRecoversSplitBioType() {
        String gherkin = "I update demo or bio details where bio type is face, and password is iris@@finger, "
                + "and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("e2e_updateDemoOrBioDetails(face@@iris@@finger,0,0,$$personaFilePath)",
                stripParamLabels(dsl));
    }

    @Test
    public void decodeScenario130UpdateDemoOrBioDetailsRecoversSplitUpdateAttributes() {
        String gherkin = "I update demo or bio details where bio type is 0, and miss fields is 0, "
                + "and update attributes is addressLine1=bnglr, and password is phoneNumber=3938333736, "
                + "and parameter 5 is the saved persona file path";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("e2e_updateDemoOrBioDetails(0,0,addressLine1=bnglr@@phoneNumber=3938333736,$$personaFilePath)",
                stripParamLabels(dsl));
    }

    @Test
    public void roundTripScenario130UpdateDemoOrBioDetails() {
        String dsl = "e2e_updateDemoOrBioDetails(0,0,addressLine1=bnglr@@phoneNumber=3938333736,$$personaFilePath)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("addressLine1=bnglr@@phoneNumber=3938333736"));
        assertTrue(!encoded.contains("password is phoneNumber"));
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void roundTripGetBioModalityHashMultiSubtype() {
        String dsl = "$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(!encoded.contains("password is Left"));
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void decodeGetBioModalityHashRecoversSplitModalitySubtypes() {
        String gherkin = "I get bio modality hash where check persona presence is -1, and modality subtypes is "
                + "Right IndexFinger, and password is Left LittleFinger, and parameter 4 is the saved persona file path "
                + "and store result in modality hash value";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals("$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath)",
                stripParamLabels(dsl));
    }

    @Test
    public void decodeScenario86GetBioModalityHashWithPersonaFilePath1() {
        String gherkin = "I get bio modality hash where persona ID is -1, and modality subtypes is "
                + "Right IndexFinger@@Left LittleFinger, and persona path is the saved persona file path1 "
                + "and store result in modality hash value";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals(
                "$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath1)",
                stripParamLabels(dsl));
    }

    @Test
    public void decodeScenario86LegacySplitModalitySubtypesWithPersonaFilePath1() {
        String gherkin = "I get bio modality hash where check persona presence is -1, and modality subtypes is "
                + "Right IndexFinger, and password is Left LittleFinger, and parameter 4 is persona file path1 "
                + "and store result in modality hash value";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals(
                "$$modalityHashValue=e2e_getBioModalityHash(-1,Right IndexFinger@@Left LittleFinger,$$personaFilePath1)",
                stripParamLabels(dsl));
    }

    @Test
    public void roundTripSetContextSupervisorOperatorCbeffCredentials() {
        String dsl = "e2e_setContext(env_context,$$details2,false,null,"
                + "null@@null@@null@@null@@OperatorBiometrics_bio_CBEFF@@SupervisorBiometrics_bio_CBEFF)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        assertTrue(encoded.contains("negative test or signature flag is null@@null@@null@@null"));
        assertTrue(!encoded.contains("password is null"));
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void decodeSetContextRecoversSplitCredentialBundle() {
        String gherkin = "I set context where context key is env_context, and pre-requisite details is the saved "
                + "environment 2 details, and generate private key is false, and put scenario details in context is null, "
                + "and negative test or signature flag is null, and password is null@@null@@null@@null@@"
                + "OperatorBiometrics_bio_CBEFF@@SupervisorBiometrics_bio_CBEFF";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals(
                "e2e_setContext(env_context,$$details2,false,null,"
                        + "null@@null@@null@@null@@OperatorBiometrics_bio_CBEFF@@SupervisorBiometrics_bio_CBEFF)",
                stripParamLabels(dsl));
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
        String decoded = ReadableDslStepCodec.decode(encoded);
        assertEquals(dsl, stripParamLabels(decoded));
    }

    @Test
    public void decodeUpdateBioExceptionInPersonaRecoversSplitModalities() {
        String gherkin = "I update bio exception in persona where persona file path is the saved persona file path, "
                + "and biometric exception modalities is Finger:Left Thumb, and password is Finger:Left IndexFinger@@"
                + "Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@"
                + "Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@"
                + "Finger:Right LittleFinger@@Iris:Left@@Iris:Right";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals(
                "e2e_UpdateBioExceptionInPersona($$personaFilePath,Finger:Left Thumb@@Finger:Left IndexFinger@@"
                        + "Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@"
                        + "Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@"
                        + "Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right)",
                stripParamLabels(dsl));
    }

    @Test
    public void decodeUpdateBioExceptionInPersonaTwoModalities() {
        String gherkin = "I update bio exception in persona where persona file path is the saved persona file path, "
                + "and biometric exception modalities is Finger:Left IndexFinger, and password is Finger:Right IndexFinger";
        String dsl = ReadableDslStepCodec.decode(gherkin);
        assertEquals(
                "e2e_UpdateBioExceptionInPersona($$personaFilePath,Finger:Left IndexFinger@@Finger:Right IndexFinger)",
                stripParamLabels(dsl));
    }

    @Test
    public void roundTripConfigureMockAbisDelayAndStatus() {
        String dsl = "e2e_configureMockAbis(-1,Right IndexFinger,false,Right IndexFinger,$$personaFilePath,"
                + "$$modalityHashValue,-1,@@Success)";
        String encoded = ReadableDslStepCodec.encode(dsl);
        String decoded = ReadableDslStepCodec.decode(encoded);
        String stripped = stripParamLabels(decoded);
        assertTrue(stripped.contains(",-1,@@Success)"));
        assertTrue(!stripped.contains("-1@@Success"));
    }

    private static String stripParamLabels(String dsl) {
        return dsl.replaceAll("/\\*[^*]*\\*/", "");
    }
}
