package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encodes DSL {@code e2e_*} actions into readable Cucumber English and decodes them back.
 * Format: {@code I <action phrase> where <label> is <value>, ...}
 */
public final class ReadableDslStepCodec {

    private static final Pattern DSL_ACTION = Pattern.compile("^(?:(\\$\\$\\w+)=)?(e2e_\\w+)\\((.*)\\)\\s*$");
    private static final Pattern ENGLISH_STEP = Pattern.compile("^I (.+?) where (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENGLISH_STEP_NO_ARGS = Pattern.compile("^I (.+)$", Pattern.CASE_INSENSITIVE);

    /**
     * Standalone words uppercased in {@code e2e_*} names (e.g. {@code by uin} → {@code ByUIN}).
     * Do not include {@code rid} — phrases like {@code uinby rid} must stay {@code getUinbyRid};
     * {@link io.mosip.testrig.dslrig.ivv.orchestrator.StepClassResolver} maps all case variants.
     */
    private static final Set<String> DSL_ACRONYMS = Set.of(
            "uin", "vid", "prid", "otp", "oidc", "oauth", "abis", "ekyc");

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
            String label = p.label != null ? humanizeLabel(p.label) : fallbackLabel(method, i);
            clauses.add(label + " is " + p.rawValue.trim());
        }
        String phrase = humanizeMethodName(method);
        String body = clauses.isEmpty()
                ? "I " + phrase
                : "I " + phrase + " where " + String.join(", and ", clauses);
        if (outVar != null) {
            body = body + " and store result in " + outVar.replace("=", "").trim();
        }
        return body;
    }

    public static String decode(String gherkinBody) {
        if (gherkinBody == null || gherkinBody.isBlank()) {
            throw new IllegalArgumentException("Empty Gherkin step");
        }
        String text = gherkinBody.trim();
        if (text.startsWith("e2e_") || text.contains("=e2e_")) {
            return text;
        }
        String outVar = null;
        Matcher storeMatcher = Pattern.compile(" and store result in (\\$\\$\\w+)\\s*$", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (storeMatcher.find()) {
            outVar = storeMatcher.group(1) + "=";
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
        params = collapseAtAtPasswordParams(params);
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                args.append(',');
            }
            args.append(formatParamForDsl(params.get(i)));
        }
        String dsl = method + "(" + args + ")";
        return outVar != null ? outVar + dsl : dsl;
    }

    private static String formatParamForDsl(ParamToken p) {
        if (p.label != null && !p.label.isBlank()) {
            return p.rawValue.trim() + "/*" + p.label + "*/";
        }
        return p.rawValue.trim();
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
            String value = part.substring(isIdx + 4).trim();
            result.add(new ParamToken(value, toLabelConstant(labelEnglish)));
        }
        return result;
    }

    private static String toLabelConstant(String englishLabel) {
        return englishLabel.trim().replace(' ', '_').toUpperCase();
    }

    private static String humanizeLabel(String label) {
        return label.replace('_', ' ').toLowerCase();
    }

    private static String fallbackLabel(String method, int index) {
        if ("e2e_updateDemoOrBioDetails".equals(method)) {
            if (index == 0) {
                return "bio type";
            }
            if (index == 1) {
                return "miss fields";
            }
            if (index == 2) {
                return "update attributes";
            }
            if (index == 3) {
                return "persona file";
            }
        }
        if ("e2e_User".equals(method)) {
            if (index == 0) {
                return "user action";
            }
            if (index == 1) {
                return "user index or master user";
            }
            if (index == 2) {
                return "password";
            }
            if (index == 3) {
                return "password or details";
            }
        }
        return "argument " + (index + 1);
    }

    private static String humanizeMethodName(String method) {
        String name = method.startsWith("e2e_") ? method.substring(4) : method;
        String spaced = name.replaceAll("([a-z])([A-Z])", "$1 $2");
        return spaced.toLowerCase();
    }

    private static String toMethodName(String phrase) {
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
        return expandAtAtPasswordTokens(tokens);
    }

    /**
     * Inverse of {@link #expandAtAtPasswordTokens}: rejoins values split for readable Gherkin
     * (e.g. {@code user index is 1} + {@code password is Techno@123} → {@code 1@@Techno@123}).
     */
    private static List<ParamToken> collapseAtAtPasswordParams(List<ParamToken> params) {
        List<ParamToken> collapsed = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            ParamToken current = params.get(i);
            while (i + 1 < params.size() && isAtAtSplitContinuation(current, params.get(i + 1))) {
                String tail = atAtContinuationValue(params.get(i + 1));
                current = new ParamToken(current.rawValue + "@@" + tail, current.label);
                i++;
            }
            collapsed.add(current);
        }
        return collapsed;
    }

    private static boolean isAtAtSplitContinuation(ParamToken current, ParamToken next) {
        if (next.rawValue.startsWith("@@")) {
            return true;
        }
        if (next.label != null && next.label.equalsIgnoreCase("PASSWORD")) {
            return true;
        }
        return false;
    }

    private static String atAtContinuationValue(ParamToken next) {
        return next.rawValue.startsWith("@@") ? next.rawValue.substring(2) : next.rawValue;
    }

    /** Splits segments joined with {@code @@} in DSL into separate readable Gherkin parameters. */
    private static List<ParamToken> expandAtAtPasswordTokens(List<ParamToken> tokens) {
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
