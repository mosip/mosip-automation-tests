package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.util.regex.Pattern;

/**
 * Converts list segments inside a single DSL argument: canonical {@code @@} in storage,
 * readable {@code and} in Gherkin (comma still separates arguments in {@code e2e_*()}).
 */
public final class DslListSeparator {

    /** Word-boundary {@code and} used inside one parameter value (not between parameters). */
    private static final Pattern ENGLISH_AND = Pattern.compile("\\s+and\\s+", Pattern.CASE_INSENSITIVE);

    private DslListSeparator() {
    }

    /** {@code Male@@false@@false} → {@code Male and false and false} for feature files. */
    public static String toDisplay(String dslValue) {
        if (dslValue == null || dslValue.isEmpty()) {
            return dslValue;
        }
        String clean = stripInlineLabels(dslValue);
        if (!clean.contains("@@")) {
            return clean;
        }
        String[] parts = clean.split("@@", -1);
        StringBuilder sb = new StringBuilder(parts[0].trim());
        for (int i = 1; i < parts.length; i++) {
            sb.append(" and ").append(parts[i].trim());
        }
        return sb.toString();
    }

    /** {@code Male and false and false} → {@code Male@@false@@false} for DSL / orchestrator. */
    public static String toDsl(String displayValue) {
        if (displayValue == null || displayValue.isEmpty()) {
            return displayValue;
        }
        String trimmed = stripInlineLabels(displayValue.trim());
        if (trimmed.contains("@@")) {
            return trimmed;
        }
        return ENGLISH_AND.matcher(trimmed).replaceAll("@@");
    }

    static String stripInlineLabels(String value) {
        return value.replaceAll("/\\*[^*]*\\*/", "").trim();
    }
}
