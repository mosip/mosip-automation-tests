package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encodes DSL {@code e2e_*} actions into readable Cucumber English and decodes them back.
 * Format: {@code I <action phrase> where <label> is <value>, ...}
 */
public final class ReadableDslStepCodec {

    private static final Pattern DSL_ACTION = Pattern.compile("^(?:(\\$\\$\\w+|\\w+)=)?(e2e_\\w+)\\((.*)\\)\\s*$");
    private static final Pattern EXECUTE_ACTION = Pattern.compile("^execute action \"(.+)\"$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMBEDDED_DSL = Pattern.compile("(?:([\\w$]+)=)?(e2e_\\w+\\([^)]*\\))");
    private static final Pattern ENGLISH_STEP = Pattern.compile("^I (.+?) where (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENGLISH_STEP_NO_ARGS = Pattern.compile("^I (.+)$", Pattern.CASE_INSENSITIVE);

    /**
     * Standalone words uppercased in {@code e2e_*} names (e.g. {@code by uin} → {@code ByUIN}).
     * Do not include {@code rid} — phrases like {@code uinby rid} must stay {@code getUinbyRid};
     * {@link io.mosip.testrig.dslrig.ivv.orchestrator.StepClassResolver} maps all case variants.
     */
    private static final Set<String> DSL_ACRONYMS = Set.of(
            "uin", "vid", "prid", "otp", "oidc", "oauth", "abis", "ekyc");

    /** Scenario output variables that are not prefixed with {@code $$} in DSL. */
    private static final Set<String> PLAIN_OUTPUT_VARS = Set.of("ekycData");

    private static final Map<String, String> KNOWN_METHOD_PHRASES = Map.of(
            "get uin by rid", "e2e_getUINByRid",
            "get email by uin", "e2e_getEmailByUIN",
            "check ridstage", "e2e_CheckRIDStage",
            "check tags", "e2e_CheckTags",
            "update bio exception in persona", "e2e_UpdateBioExceptionInPersona",
            "configure mock abis", "e2e_configureMockAbis");

    private ReadableDslStepCodec() {
    }

    public static String encode(String action) {
        if (action == null || action.isBlank()) {
            return "";
        }
        String trimmed = action.trim();
        Matcher m = DSL_ACTION.matcher(trimmed);
        if (!m.matches()) {
            return "I execute action \"" + trimmed + "\"";
        }
        String outVar = m.group(1);
        String method = m.group(2);
        String args = m.group(3);
        List<ParamToken> params = splitParams(args, method);
        if ("e2e_configureMockAbis".equalsIgnoreCase(method)) {
            params = normalizeConfigureMockAbisTokensForGherkin(params);
        }
        if ("e2e_getResidentData".equals(method)) {
            params = expandGetResidentDataBioFlagsForGherkin(params);
        }
        List<String> clauses = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            ParamToken p = params.get(i);
            String label = resolveLabel(method, i, params, p);
            String displayValue = formatValueForGherkin(label, p.rawValue.trim());
            clauses.add(label + " is " + displayValue);
        }
        String phrase = humanizeMethodName(method);
        String body = clauses.isEmpty()
                ? "I " + phrase
                : "I " + phrase + " where " + String.join(", and ", clauses);
        if (outVar != null) {
            body = body + " and store result in " + VariableDisplayNames.toDisplay(outVar);
        }
        return body;
    }

    public static String decode(String gherkinBody) {
        if (gherkinBody == null || gherkinBody.isBlank()) {
            throw new IllegalArgumentException("Empty Gherkin step");
        }
        String text = gherkinBody.trim();
        while (true) {
            Matcher executeAction = EXECUTE_ACTION.matcher(text);
            if (!executeAction.matches()) {
                break;
            }
            text = executeAction.group(1).trim();
        }
        String embeddedDsl = extractEmbeddedDslAction(text);
        if (embeddedDsl != null) {
            return embeddedDsl;
        }
        String outVar = null;
        Matcher storeMatcher = Pattern.compile(" and store result in (.+?)\\s*$", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (storeMatcher.find()) {
            String stored = storeMatcher.group(1).trim();
            if (!stored.startsWith("$$")) {
                stored = VariableDisplayNames.toDsl(stored);
            }
            if (!stored.startsWith("$$") && !isPlainOutputVar(stored)) {
                stored = "$$" + toDslVariableName(stored);
            }
            outVar = stored + "=";
            text = text.substring(0, storeMatcher.start()).trim();
        }
        text = text.replaceFirst("(?i)\\s+where\\s*$", "");

        String phrase;
        String wherePart = "";
        Matcher withArgs = ENGLISH_STEP.matcher(text);
        if (withArgs.matches()) {
            phrase = withArgs.group(1).trim();
            wherePart = withArgs.group(2).trim();
        } else {
            Matcher noArgs = ENGLISH_STEP_NO_ARGS.matcher(text);
            if (!noArgs.matches()) {
                throw new IllegalArgumentException("Unrecognized Gherkin step (expected 'I ...' or 'I ... where ...'): "
                        + gherkinBody);
            }
            phrase = noArgs.group(1).trim();
        }
        String method = toMethodName(phrase);
        List<ParamToken> params = wherePart.isEmpty() ? new ArrayList<>() : parseWhereClauses(wherePart);
        if ("e2e_getResidentData".equalsIgnoreCase(method)) {
            params = normalizeGetResidentDataGherkinLabels(params);
        }
        params = collapseAtAtPasswordParams(params, method);
        if ("e2e_getResidentData".equalsIgnoreCase(method)) {
            params = mergeGetResidentDataGenderAndBioFlags(params);
        }
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                args.append(',');
            }
            args.append(formatParamForDsl(method, i, params.get(i)));
        }
        String dsl = method + "(" + args + ")";
        return outVar != null ? outVar + dsl : dsl;
    }

    private static String formatParamForDsl(String method, int index, ParamToken p) {
        String value = p.rawValue.trim();
        if ("e2e_configureMockAbis".equalsIgnoreCase(method) && index == 7 && !value.contains("@@")
                && isConfigureMockAbisStatusOnlyValue(value)) {
            value = "@@" + value;
        }
        if ("e2e_getResidentData".equalsIgnoreCase(method) && value.contains("@@")) {
            String labeled = formatGetResidentDataBioParamForDsl(value);
            if (labeled != null) {
                return labeled;
            }
        }
        if (p.label != null && !p.label.isBlank()) {
            return value + "/*" + p.label + "*/";
        }
        return value;
    }

    /**
     * {@code generate private key is false} in readable Gherkin is a mistaken label for guardian flag
     * on {@code e2e_getResidentData} (not {@code e2e_setContext}).
     */
    private static List<ParamToken> normalizeGetResidentDataGherkinLabels(List<ParamToken> params) {
        List<ParamToken> normalized = new ArrayList<>();
        for (ParamToken p : params) {
            if (p.label != null && p.label.equalsIgnoreCase("GENERATE_PRIVATE_KEY")) {
                normalized.add(new ParamToken(p.rawValue, "GUARDIAN_FLAG"));
            } else {
                normalized.add(p);
            }
        }
        return normalized;
    }

    /**
     * Ensures {@code gender is Male} and {@code password is false@@false@@true} (or split bio fields)
     * become one {@code Male@@false@@false@@true} argument even if {@link #collapseAtAtPasswordParams}
     * did not merge them.
     */
    private static List<ParamToken> mergeGetResidentDataGenderAndBioFlags(List<ParamToken> params) {
        List<ParamToken> result = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            ParamToken current = params.get(i);
            if (i + 1 < params.size() && current.label != null
                    && current.label.equalsIgnoreCase("GENDER")) {
                ParamToken next = params.get(i + 1);
                String nextClean = next.rawValue.replaceAll("/\\*.*?\\*/", "").trim();
                if (next.label != null && isGetResidentDataBioLabel(next.label)
                        && isLikelyPartialBioFlagsOnly(nextClean)) {
                    String merged = current.rawValue.replaceAll("/\\*.*?\\*/", "").trim()
                            + "@@" + atAtContinuationValue(new ParamToken(nextClean, next.label));
                    result.add(new ParamToken(merged, "GENDER"));
                    i++;
                    continue;
                }
            }
            result.add(current);
        }
        return result;
    }

    /** Bio tail without leading gender (e.g. {@code false@@false@@true}). */
    private static boolean isLikelyPartialBioFlagsOnly(String value) {
        String clean = value.replaceAll("/\\*.*?\\*/", "").trim();
        if (!clean.contains("@@")) {
            return clean.matches("(?i)true|false");
        }
        String[] parts = clean.split("@@", -1);
        if (parts.length < 1 || parts.length > 3) {
            return false;
        }
        for (String part : parts) {
            if (!part.trim().matches("(?i)true|false")) {
                return false;
            }
        }
        return true;
    }

    /** Formats {@code Male@@false@@false@@false} with per-segment GENDER/FINGER/IRIS/FACE labels. */
    private static String formatGetResidentDataBioParamForDsl(String value) {
        String clean = value.replaceAll("/\\*.*?\\*/", "").trim();
        String[] parts = clean.split("@@", -1);
        if (parts.length >= 4 && isLikelyGenderBioFlags(parts)) {
            return parts[0] + "/*GENDER*/@@" + parts[1] + "/*FINGER_BIOMETRIC_FLAG*/@@" + parts[2]
                    + "/*IRIS_BIOMETRIC_FLAG*/@@" + parts[3] + "/*FACE_BIOMETRIC_FLAG*/";
        }
        return null;
    }

    /** Reformats a Gherkin step line preserving parameter labels from the original text. */
    public static String reformatGherkinStep(String gherkinBody) {
        return encode(decode(gherkinBody));
    }

    private static List<ParamToken> parseWhereClauses(String wherePart) {
        List<ParamToken> result = new ArrayList<>();
        String[] parts = wherePart.split(", and ");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("and ")) {
                part = part.substring(4).trim();
            }
            int isIdx = part.toLowerCase().lastIndexOf(" is ");
            if (isIdx < 0) {
                throw new IllegalArgumentException("Invalid parameter clause: " + part);
            }
            String labelEnglish = part.substring(0, isIdx).trim();
            String value = resolveParamValue(part.substring(isIdx + 4).trim(), labelEnglish);
            result.add(new ParamToken(value, toLabelConstant(labelEnglish)));
        }
        return result;
    }

    private static String toLabelConstant(String englishLabel) {
        return englishLabel.trim().replace(' ', '_').toUpperCase();
    }

    private static String humanizeLabel(String label) {
        return label.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    private static String resolveLabel(String method, int index, List<ParamToken> params, ParamToken p) {
        if (p.label != null && !isGenericLabel(p.label)) {
            return humanizeLabel(p.label);
        }
        return fallbackLabel(method, index, params);
    }

    private static boolean isGenericLabel(String label) {
        String normalized = label.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return normalized.matches("ARGUMENT_\\d+")
                || normalized.matches("PARAMETER_\\d+")
                || normalized.equals("ARGUMENT")
                || normalized.equals("PARAMETER");
    }

    private static String formatValueForGherkin(String label, String rawValue) {
        String trimmed = rawValue.trim();
        if (trimmed.startsWith("$$")) {
            return "the saved " + VariableDisplayNames.toDisplay(trimmed);
        }
        String displayValue = VariableDisplayNames.replaceDslVarsInText(trimmed);
        if (displayValue.equalsIgnoreCase(label)) {
            return "the saved " + label;
        }
        return displayValue;
    }

    private static String resolveParamValue(String value) {
        return resolveParamValue(value, null);
    }

    private static String resolveParamValue(String value, String labelEnglish) {
        String trimmed = value.trim();
        if (trimmed.startsWith("the saved ")) {
            trimmed = trimmed.substring("the saved ".length()).trim();
        }
        if (trimmed.startsWith("$$")) {
            return trimmed;
        }
        String dsl = VariableDisplayNames.toDsl(trimmed);
        if (dsl.startsWith("$$")) {
            return dsl;
        }
        // Round-tripped scenario scratch vars (e.g. center77, user76, details77) are not in
        // variable-display-names.json but must stay $$-prefixed in DSL.
        if (looksLikeBareScenarioVariable(trimmed)) {
            return "$$" + trimmed;
        }
        // Display-style scratch vars from Gherkin (e.g. persona file path1, template path1).
        if (looksLikeDisplayScenarioVariable(trimmed)) {
            return "$$" + toDslVariableName(trimmed);
        }
        // Single-word path shorthands (e.g. "template" for packet template path) from store-result names.
        if (labelEnglish != null && isPathParameterLabel(labelEnglish) && trimmed.matches("^[a-z][a-z0-9]*$")) {
            return "$$" + toDslVariableName(trimmed);
        }
        return dsl;
    }

    private static boolean isPathParameterLabel(String labelEnglish) {
        String label = labelEnglish.toLowerCase(Locale.ROOT);
        return label.contains("file path") || label.contains("template path") || label.contains("persona path")
                || label.contains("zip path");
    }

    /** Matches unmapped scenario variables such as {@code center77}, {@code rid1}, {@code uin1}. */
    private static boolean looksLikeBareScenarioVariable(String value) {
        return value != null && value.matches("^[a-z][a-zA-Z0-9]*\\d+$");
    }

    /**
     * Matches readable multi-word scenario variables (e.g. {@code persona file path1}) that are
     * stored as {@code $$personaFilePath1} but referenced without the {@code $$} prefix in Gherkin.
     */
    private static boolean looksLikeDisplayScenarioVariable(String value) {
        return value != null && value.contains(" ") && value.matches("^[a-z][a-z0-9 ]+\\d*$");
    }

    private static String toDslVariableName(String displayName) {
        String[] words = displayName.trim().split("\\s+");
        if (words.length == 0) {
            return displayName;
        }
        StringBuilder sb = new StringBuilder(words[0].toLowerCase(Locale.ROOT));
        for (int i = 1; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1).toLowerCase(Locale.ROOT));
                }
            }
        }
        return sb.toString();
    }

    private static String fallbackLabel(String method, int index, List<ParamToken> params) {
        int labelIndex = index;
        if ("e2e_getResidentData".equals(method) && !params.isEmpty()
                && !params.get(0).rawValue.trim().matches("\\d+")) {
            labelIndex = index + 1;
        }
        String configured = StepParameterLabels.labelFor(method, labelIndex);
        if (configured != null) {
            return configured;
        }
        return "parameter " + (index + 1);
    }

    private static boolean isPlainOutputVar(String name) {
        if (name == null) {
            return false;
        }
        for (String plain : PLAIN_OUTPUT_VARS) {
            if (plain.equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    /** Pulls {@code [var=]e2e_*()} from nested {@code execute action "..."} wrappers. */
    private static String extractEmbeddedDslAction(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        if (text.startsWith("e2e_")) {
            return text;
        }
        Matcher m = EMBEDDED_DSL.matcher(text);
        String last = null;
        while (m.find()) {
            String var = m.group(1);
            String action = m.group(2);
            last = var != null && !var.isBlank() ? var.trim() + "=" + action : action;
        }
        return last;
    }

    private static String humanizeMethodName(String method) {
        String name = method.startsWith("e2e_") ? method.substring(4) : method;
        String spaced = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        spaced = spaced.replaceAll("(?i)(UIN|VID|OTP)([Bb]y)", "$1 $2");
        spaced = spaced.replaceAll("(?i)([Bb]y)(Rid|UIN|VID)", "$1 $2");
        return spaced.toLowerCase(Locale.ROOT);
    }

    private static String toMethodName(String phrase) {
        String normalizedPhrase = phrase.trim().toLowerCase(Locale.ROOT);
        String known = KNOWN_METHOD_PHRASES.get(normalizedPhrase);
        if (known != null) {
            return known;
        }
        String[] words = phrase.trim().split("\\s+");
        if (words.length == 0) {
            throw new IllegalArgumentException("Empty action phrase");
        }
        StringBuilder sb = new StringBuilder("e2e_");
        if (words.length == 1) {
            String w = words[0];
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) {
                sb.append(w.substring(1));
            }
            return sb.toString();
        }
        sb.append(words[0]);
        for (int i = 1; i < words.length; i++) {
            String w = words[i];
            if (w.isEmpty()) {
                continue;
            }
            sb.append(capitalizeWordForMethod(w));
        }
        return sb.toString();
    }

    private static String capitalizeWordForMethod(String word) {
        String lower = word.toLowerCase(Locale.ROOT);
        if (DSL_ACRONYMS.contains(lower)) {
            return lower.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    static List<ParamToken> splitParams(String args) {
        return splitParams(args, null);
    }

    static List<ParamToken> splitParams(String args, String method) {
        List<ParamToken> tokens = new ArrayList<>();
        if (args == null || args.isBlank()) {
            return tokens;
        }
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if (c == '(') {
                depth++;
                current.append(c);
            } else if (c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                addParamToken(tokens, current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            addParamToken(tokens, current.toString());
        }
        return expandAtAtPasswordTokens(tokens, method);
    }

    /**
     * Inverse of {@link #expandAtAtPasswordTokens}: rejoins values split for readable Gherkin
     * (e.g. {@code user index is 1} + {@code password is Techno@123} → {@code 1@@Techno@123}).
     */
    private static List<ParamToken> collapseAtAtPasswordParams(List<ParamToken> params, String method) {
        List<ParamToken> collapsed = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            ParamToken current = params.get(i);
            while (i + 1 < params.size() && isAtAtSplitContinuation(current, params.get(i + 1), method)) {
                current = mergeAtAtContinuation(current, params.get(i + 1), method);
                i++;
            }
            collapsed.add(current);
        }
        return collapsed;
    }

    private static boolean isAtAtSplitContinuation(ParamToken current, ParamToken next, String method) {
        if ("e2e_setContext".equalsIgnoreCase(method) && isSetContextCredentialContinuation(current, next)) {
            return true;
        }
        if ("e2e_getResidentData".equalsIgnoreCase(method) && isGetResidentDataGenderBioContinuation(current, next)) {
            return true;
        }
        if (next.rawValue.startsWith("@@")
                && (next.label == null || next.label.equalsIgnoreCase("PASSWORD"))) {
            if ("e2e_configureMockAbis".equalsIgnoreCase(method)) {
                return isMockAbisStatusLeadParam(current)
                        || isConfigureMockAbisErrorCodeContinuation(current, next);
            }
            return true;
        }
        if (next.label != null && next.label.equalsIgnoreCase("PASSWORD")) {
            if ("e2e_updateDemoOrBioDetails".equalsIgnoreCase(method)
                    && (isBioTypeLeadParam(current) || isUpdateAttributesLeadParam(current))) {
                return true;
            }
            if ("e2e_getBioModalityHash".equalsIgnoreCase(method) && isModalitySubtypesLeadParam(current)) {
                return true;
            }
            if ("e2e_configureMockAbis".equalsIgnoreCase(method)
                    && (isConfigureMockAbisFingerprintContinuation(current, next)
                            || isConfigureMockAbisErrorCodeContinuation(current, next))) {
                return true;
            }
            if ("e2e_UpdateBioExceptionInPersona".equalsIgnoreCase(method)
                    && isBiometricExceptionModalitiesLeadParam(current)) {
                return true;
            }
            return isUserCredentialLeadParam(current) || isMockAbisStatusLeadParam(current);
        }
        return false;
    }

    /** {@code Right IndexFinger@@Left LittleFinger} is one modality-subtypes argument in readable Gherkin. */
    private static boolean isModalitySubtypesLeadParam(ParamToken current) {
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        return label.contains("MODALITY") && label.contains("SUBTYPE");
    }

    /** {@code Right IndexFinger@@Left LittleFinger} is one hash-modality-keys argument in readable Gherkin. */
    private static boolean isHashModalityKeysLeadParam(ParamToken current) {
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        return label.contains("HASH") && label.contains("MODALITY");
    }

    /** Merges split fingerprint subtypes (including mislabeled {@code persona path} fields). */
    private static boolean isConfigureMockAbisFingerprintContinuation(ParamToken current, ParamToken next) {
        if (next.label == null || !next.label.equalsIgnoreCase("PASSWORD")) {
            return false;
        }
        if (!next.rawValue.contains("Finger")) {
            return false;
        }
        if (isModalitySubtypesLeadParam(current) || isHashModalityKeysLeadParam(current)) {
            return true;
        }
        if (current.label != null) {
            String label = current.label.toUpperCase(Locale.ROOT);
            return label.contains("PERSONA") && label.contains("PATH");
        }
        return false;
    }

    /** {@code @@Success} parsed in DSL must not become a generic {@code password} field in Gherkin. */
    private static List<ParamToken> normalizeConfigureMockAbisTokensForGherkin(List<ParamToken> params) {
        List<ParamToken> normalized = new ArrayList<>();
        for (ParamToken p : params) {
            if (p.label != null && p.label.equalsIgnoreCase("PASSWORD") && p.rawValue.startsWith("@@")) {
                String status = p.rawValue.substring(2).replaceAll("/\\*.*?\\*/", "").trim();
                if (isConfigureMockAbisStatusOnlyValue(status)) {
                    normalized.add(new ParamToken(p.rawValue, "MOCK_ABIS_STATUS"));
                    continue;
                }
            }
            normalized.add(p);
        }
        return normalized;
    }

    private static boolean isConfigureMockAbisStatusOnlyValue(String value) {
        String v = value.trim();
        return v.equalsIgnoreCase("Success") || v.equalsIgnoreCase("Error")
                || v.equalsIgnoreCase("Duplicate");
    }

    /** {@code 10@@Error} is one mock-ABIS status argument; do not merge {@code -1} with {@code Success}. */
    private static boolean isConfigureMockAbisErrorCodeContinuation(ParamToken current, ParamToken next) {
        if (next.label == null || !next.label.equalsIgnoreCase("PASSWORD")) {
            return false;
        }
        String status = next.rawValue.trim();
        if (!status.equalsIgnoreCase("Error") && !status.equalsIgnoreCase("Success")
                && !status.equalsIgnoreCase("Duplicate")) {
            return false;
        }
        return current.rawValue.trim().matches("[1-9]\\d*");
    }

    private static boolean isBioTypeLeadParam(ParamToken current) {
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        return label.contains("BIO");
    }

    /** {@code addressLine1=bnglr@@phoneNumber=3938333736} is one update-attributes argument in readable Gherkin. */
    private static boolean isUpdateAttributesLeadParam(ParamToken current) {
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        return label.contains("UPDATE") && label.contains("ATTRIBUTE");
    }

    /** {@code Finger:Left Thumb@@Finger:Left IndexFinger} is one biometric-exception-modality argument in readable Gherkin. */
    private static boolean isBiometricExceptionModalitiesLeadParam(ParamToken current) {
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        return label.contains("BIOMETRIC") && label.contains("EXCEPTION");
    }

    /**
     * {@code e2e_setContext} 5th argument is {@code supervisor@@password@@regclient@@regclientPw@@operatorCbeff@@supervisorCbeff};
     * do not split the leading {@code null@@} into separate Gherkin fields.
     */
    private static boolean isSetContextCredentialContinuation(ParamToken current, ParamToken next) {
        if (next.label == null || !next.label.equalsIgnoreCase("PASSWORD")) {
            return false;
        }
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        return label.contains("NEGATIVE") || label.contains("SIGNATURE")
                || label.contains("REGISTRATION_STATUS") || label.contains("PUT_SCENARIO");
    }

    /**
     * {@code Male@@false@@false@@true} is one gender/biometric-flags argument in readable Gherkin
     * (written as {@code gender is Male} plus {@code password is false@@false@@true}, or as separate
     * finger/iris/face flag fields).
     */
    private static boolean isGetResidentDataGenderBioContinuation(ParamToken current, ParamToken next) {
        if (current.label == null || next.label == null) {
            return false;
        }
        if (!isGetResidentDataBioLabel(current.label) || !isGetResidentDataBioLabel(next.label)) {
            return false;
        }
        String cur = current.label.toUpperCase(Locale.ROOT);
        return !cur.contains("PERSONA") && !cur.contains("GUARDIAN") && !cur.contains("RESIDENT");
    }

    private static boolean isGetResidentDataBioLabel(String label) {
        String u = label.toUpperCase(Locale.ROOT);
        return u.equals("GENDER")
                || u.equals("PASSWORD")
                || u.contains("FINGER")
                || u.contains("IRIS")
                || u.contains("FACE")
                || (u.contains("BIOMETRIC") && u.contains("FLAG"));
    }

    /** {@code userIndex@@password} is a single DSL argument split for readable Gherkin. */
    private static boolean isUserCredentialLeadParam(ParamToken current) {
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        return label.contains("USER");
    }

    /** {@code statusCode@@failureReason} is a single mock ABIS status argument split for Gherkin. */
    private static boolean isMockAbisStatusLeadParam(ParamToken current) {
        if (current.label == null) {
            return false;
        }
        String label = current.label.toUpperCase(Locale.ROOT);
        if (label.contains("DELAY")) {
            return false;
        }
        return label.contains("MOCK_ABIS")
                || label.contains("STATUS_CODE")
                || label.endsWith("_STATUS");
    }

    private static String atAtContinuationValue(ParamToken next) {
        return next.rawValue.startsWith("@@") ? next.rawValue.substring(2) : next.rawValue;
    }

    private static ParamToken mergeAtAtContinuation(ParamToken current, ParamToken next, String method) {
        if ("e2e_setContext".equalsIgnoreCase(method) && isSetContextCredentialContinuation(current, next)
                && "null".equalsIgnoreCase(current.rawValue.trim()) && next.rawValue.startsWith("null@@")) {
            // When the password field already holds the full credential bundle (6 {@code @@} segments),
            // a separate leading {@code null} negative-flag field is spurious. Otherwise rejoin the
            // segment that {@link #expandAtAtPasswordTokens} peeled into the negative-flag field.
            if (next.rawValue.split("@@", -1).length >= 6) {
                return new ParamToken(next.rawValue, current.label);
            }
            return new ParamToken(current.rawValue + "@@" + next.rawValue, current.label);
        }
        String tail = atAtContinuationValue(next);
        return new ParamToken(current.rawValue + "@@" + tail, current.label);
    }

    /**
     * Readable Gherkin for {@code Male@@false@@false@@true}: {@code gender is Male} and
     * {@code password is false@@false@@true} (decoded back into one DSL argument).
     */
    private static List<ParamToken> expandGetResidentDataBioFlagsForGherkin(List<ParamToken> tokens) {
        List<ParamToken> result = new ArrayList<>();
        for (ParamToken token : tokens) {
            String value = token.rawValue.replaceAll("/\\*.*?\\*/", "").trim();
            if (value.contains("@@")) {
                String[] parts = value.split("@@", -1);
                if (parts.length >= 4 && isLikelyGenderBioFlags(parts)) {
                    String genderLabel = token.label != null && !isGenericLabel(token.label)
                            ? token.label : "GENDER";
                    result.add(new ParamToken(parts[0], genderLabel));
                    result.add(new ParamToken(parts[1] + "@@" + parts[2] + "@@" + parts[3], "PASSWORD"));
                    continue;
                }
            }
            result.add(token);
        }
        return result;
    }

    private static boolean isLikelyGenderBioFlags(String[] parts) {
        return parts[0].matches("(?i)Male|Female|Any")
                && parts[1].matches("(?i)true|false")
                && parts[2].matches("(?i)true|false")
                && parts[3].matches("(?i)true|false");
    }

    /** Splits segments joined with {@code @@} in DSL into separate readable Gherkin parameters. */
    private static List<ParamToken> expandAtAtPasswordTokens(List<ParamToken> tokens, String method) {
        if ("e2e_updateDemoOrBioDetails".equalsIgnoreCase(method)
                || "e2e_getBioModalityHash".equalsIgnoreCase(method)
                || "e2e_configureMockAbis".equalsIgnoreCase(method)
                || "e2e_setContext".equalsIgnoreCase(method)
                || "e2e_UpdateBioExceptionInPersona".equalsIgnoreCase(method)
                || "e2e_getResidentData".equalsIgnoreCase(method)) {
            return tokens;
        }
        List<ParamToken> expanded = new ArrayList<>();
        for (ParamToken token : tokens) {
            if (token.rawValue.contains("@@")) {
                String[] parts = token.rawValue.split("@@", 2);
                addParamToken(expanded, parts[0]);
                if (parts.length > 1) {
                    expanded.add(new ParamToken(parts[1].trim(), "PASSWORD"));
                }
            } else {
                expanded.add(token);
            }
        }
        return expanded;
    }

    private static void addParamToken(List<ParamToken> tokens, String piece) {
        piece = piece.trim();
        if (piece.isEmpty()) {
            return;
        }
        if (piece.startsWith("@@")) {
            Matcher pm = Pattern.compile("^@@(.+?)(?:/\\*([^*]+)\\*/)?$").matcher(piece);
            if (pm.matches()) {
                String label = pm.group(2) != null ? pm.group(2).trim() : "PASSWORD";
                tokens.add(new ParamToken("@@" + pm.group(1).trim(), label));
                return;
            }
        }
        Matcher cm = Pattern.compile("^(.*)/\\*([^*]+)\\*/$").matcher(piece);
        if (cm.matches()) {
            tokens.add(new ParamToken(cm.group(1).trim(), cm.group(2).trim()));
        } else {
            tokens.add(new ParamToken(piece, null));
        }
    }

    static final class ParamToken {
        final String rawValue;
        final String label;

        ParamToken(String rawValue, String label) {
            this.rawValue = rawValue;
            this.label = label;
        }
    }
}
