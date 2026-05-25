package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic encode/decode between DSL {@code e2e_*(arg1,arg2)} and readable Gherkin.
 * <ul>
 *   <li>One {@code , and }-separated clause per comma-separated DSL argument.</li>
 *   <li>Lists inside one argument use {@code and} in English and {@code @@} in DSL ({@link DslListSeparator}).</li>
 *   <li>Parameter labels come from {@code step-parameter-labels.json} only.</li>
 * </ul>
 */
public final class ReadableDslStepCodec {

    private static final Pattern DSL_ACTION = Pattern.compile("^(?:(\\$\\$\\w+|\\w+)=)?(e2e_\\w+)\\((.*)\\)\\s*$");
    private static final Pattern EXECUTE_ACTION = Pattern.compile("^execute action \"(.+)\"$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMBEDDED_DSL = Pattern.compile("(?:([\\w$]+)=)?(e2e_\\w+\\([^)]*\\))");
    private static final Pattern ENGLISH_STEP = Pattern.compile("^I (.+?) where (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENGLISH_STEP_NO_ARGS = Pattern.compile("^I (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INLINE_LABEL = Pattern.compile("/\\*([^*]*)\\*/");

    private static final Set<String> DSL_ACRONYMS = Set.of(
            "uin", "vid", "prid", "otp", "oidc", "oauth", "abis", "ekyc");

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
        List<ParamToken> params = splitParams(args);
        List<String> clauses = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            ParamToken p = params.get(i);
            String label = resolveLabel(method, i, params.size(), p);
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
        List<ParamToken> params = wherePart.isEmpty() ? new ArrayList<>() : parseWhereClauses(wherePart, method);
        params = mergeLegacySpilloverClauses(params, method);
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                args.append(',');
            }
            args.append(DslParameterAnnotations.formatArgument(method, i, params.get(i).rawValue));
        }
        String dsl = method + "(" + args + ")";
        return outVar != null ? outVar + dsl : dsl;
    }

    /** Reformats a Gherkin step: decode → canonical DSL → encode (fixes legacy split clauses). */
    public static String reformatGherkinStep(String gherkinBody) {
        return encode(decode(gherkinBody));
    }

    private static List<ParamToken> parseWhereClauses(String wherePart, String method) {
        List<ParamToken> result = new ArrayList<>();
        String[] parts = wherePart.split(", and ");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("and ")) {
                part = part.substring(4).trim();
            }
            int isIdx = part.toLowerCase(Locale.ROOT).lastIndexOf(" is ");
            if (isIdx < 0) {
                throw new IllegalArgumentException("Invalid parameter clause: " + part);
            }
            String labelEnglish = part.substring(0, isIdx).trim();
            String rawValue = part.substring(isIdx + 4).trim();
            int index = result.size();
            String value = resolveParamValue(rawValue, labelEnglish,
                    rawValue.toLowerCase(Locale.ROOT).startsWith("the saved "), method, index);
            result.add(new ParamToken(value, toLabelConstant(labelEnglish)));
        }
        return result;
    }

    /**
     * Older feature lines split one {@code @@} argument into an extra {@code password is ...} clause.
     * Rejoin for any method except {@code e2e_User} (which legitimately uses user + password args).
     */
    private static List<ParamToken> mergeLegacySpilloverClauses(List<ParamToken> params, String method) {
        if ("e2e_User".equalsIgnoreCase(method) || params.size() < 2) {
            return params;
        }
        List<ParamToken> merged = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            ParamToken current = params.get(i);
            while (i + 1 < params.size() && isLegacySpilloverContinuation(current, params.get(i + 1))) {
                String tail = params.get(i + 1).rawValue;
                if (tail.startsWith("@@")) {
                    tail = tail.substring(2);
                }
                current = new ParamToken(
                        DslListSeparator.stripInlineLabels(current.rawValue) + "@@"
                                + DslListSeparator.stripInlineLabels(tail),
                        current.label);
                i++;
            }
            merged.add(current);
        }
        return merged;
    }

    private static boolean isLegacySpilloverContinuation(ParamToken current, ParamToken next) {
        if (next.label == null) {
            return next.rawValue.startsWith("@@");
        }
        String nextLabel = next.label.toUpperCase(Locale.ROOT);
        if (!"PASSWORD".equals(nextLabel)) {
            return false;
        }
        if (current.label != null && current.label.toUpperCase(Locale.ROOT).contains("PASSWORD")) {
            return false;
        }
        return true;
    }

    private static String toLabelConstant(String englishLabel) {
        return englishLabel.trim().replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private static String humanizeLabel(String label) {
        return label.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    private static String resolveLabel(String method, int index, int paramCount, ParamToken p) {
        if (p.label != null && !isGenericLabel(p.label)) {
            return humanizeLabel(p.label);
        }
        String configured = StepParameterLabels.labelFor(method, index, paramCount);
        if (configured != null) {
            return configured;
        }
        return "parameter " + (index + 1);
    }

    private static boolean isGenericLabel(String label) {
        String normalized = label.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return normalized.matches("ARGUMENT_\\d+")
                || normalized.matches("PARAMETER_\\d+")
                || normalized.equals("ARGUMENT")
                || normalized.equals("PARAMETER");
    }

    private static String formatValueForGherkin(String label, String rawValue) {
        String trimmed = DslListSeparator.stripInlineLabels(rawValue.trim());
        if (trimmed.startsWith("$$")) {
            return "the saved " + VariableDisplayNames.toDisplay(trimmed);
        }
        String displayValue = VariableDisplayNames.replaceDslVarsInText(DslListSeparator.toDisplay(trimmed));
        displayValue = DslParameterAnnotations.toGherkinDisplay(label, displayValue);
        if (displayValue.equalsIgnoreCase(label)) {
            return "the saved " + label;
        }
        return displayValue;
    }

    private static String resolveParamValue(String value, String labelEnglish, boolean fromSavedReference,
            String method, int index) {
        String trimmed = value.trim();
        if (trimmed.startsWith("the saved ")) {
            trimmed = trimmed.substring("the saved ".length()).trim();
            fromSavedReference = true;
        }
        if (method != null && DslParameterAnnotations.isLiteralArgument(method, index)) {
            if (trimmed.startsWith("$$")) {
                return trimmed.substring(2);
            }
            if (fromSavedReference) {
                return toScenarioVariable(trimmed);
            }
            return trimmed;
        }
        if (trimmed.startsWith("$$")) {
            return trimmed;
        }
        String mapped = VariableDisplayNames.toDsl(trimmed);
        if (mapped.startsWith("$$")) {
            return mapped;
        }
        if (fromSavedReference) {
            return toScenarioVariable(trimmed);
        }
        if (looksLikeBareScenarioVariable(trimmed)) {
            return "$$" + trimmed;
        }
        if (isLiteralParameterValue(trimmed)) {
            return trimmed;
        }
        if (isScenarioVariableParameterLabel(labelEnglish)) {
            if (looksLikeDisplayNameReference(trimmed) || trimmed.matches("^[a-z][a-z0-9]*$")) {
                return toScenarioVariable(trimmed);
            }
        }
        return trimmed;
    }

    /** Maps a Gherkin display name or known alias to a {@code $$} scenario variable (camelCase). */
    private static String toScenarioVariable(String displayOrDslName) {
        String mapped = VariableDisplayNames.toDsl(displayOrDslName);
        if (mapped.startsWith("$$")) {
            return mapped;
        }
        return "$$" + toDslVariableName(displayOrDslName);
    }

    private static boolean isLiteralParameterValue(String trimmed) {
        if (trimmed.matches("^-?\\d+$")) {
            return true;
        }
        if (trimmed.contains("@@") || trimmed.contains("@")) {
            return true;
        }
        return trimmed.matches("^[A-Z][A-Z0-9_]*$");
    }

    /**
     * Parameter labels that carry scenario variables (paths, IDs, stored outputs), not step literals
     * such as packet type or status codes.
     */
    private static boolean isScenarioVariableParameterLabel(String labelEnglish) {
        if (labelEnglish == null) {
            return false;
        }
        String label = labelEnglish.toLowerCase(Locale.ROOT);
        if (label.contains("type") || label.contains("status") || label.contains("flag")
                || label.contains("component") || label.contains("action") || label.contains("mode")) {
            return false;
        }
        return label.contains("path") || label.endsWith(" id") || label.contains(" id")
                || label.contains("uin") || label.contains("email") || label.contains("details")
                || label.contains("template") || label.contains("zip") || label.contains("persona")
                || label.contains("hash") || label.contains("req id");
    }

    /** Multi-word lowercase phrase used as a shorthand variable reference in Gherkin. */
    private static boolean looksLikeDisplayNameReference(String trimmed) {
        return trimmed.matches("^[a-z][a-z0-9 ]+$") && trimmed.contains(" ");
    }

    private static boolean looksLikeBareScenarioVariable(String value) {
        return value != null && value.matches("^[a-z][a-zA-Z0-9]*\\d+$");
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
            if (!w.isEmpty()) {
                sb.append(capitalizeWordForMethod(w));
            }
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
        return tokens;
    }

    private static void addParamToken(List<ParamToken> tokens, String piece) {
        piece = piece.trim();
        if (piece.isEmpty()) {
            return;
        }
        Matcher cm = INLINE_LABEL.matcher(piece);
        if (cm.find()) {
            String value = piece.replaceAll("/\\*[^*]*\\*/", "").trim();
            String label = null;
            Matcher single = Pattern.compile("^(.*)/\\*([^*]+)\\*/$").matcher(piece.trim());
            if (single.matches()) {
                value = single.group(1).trim();
                label = single.group(2).trim();
            }
            tokens.add(new ParamToken(value, label));
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
