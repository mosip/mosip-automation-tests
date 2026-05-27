package io.mosip.testrig.dslrig.ivv.core.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

/**
 * Resolves {@code $$} scenario references in step parameters (bind phase).
 * Parse happens in ivv-parser; orchestrator invokes bind immediately before prepare/execute.
 */
public final class StepBinder {

    private StepBinder() {
    }

    public static StepContext bind(Scenario.Step step) throws RigInternalError {
        List<String> raw = step.getParameters();
        List<String> bound = new ArrayList<>();
        if (raw != null) {
            for (String param : raw) {
                bound.add(resolveScenarioVariable(step, param));
            }
        }
        return new StepContext(step, bound);
    }

    public static boolean referencesScenarioVariable(Scenario.Step step, String value) {
        if (isBlank(value)) {
            return false;
        }
        String normalized = value.trim().replaceAll("/\\*[^*]*\\*/", "").trim();
        if (normalized.startsWith("$$")) {
            return true;
        }
        return step.getScenario().getVariables().containsKey("$$" + toDslVariableName(normalized));
    }

    public static String resolveScenarioVariable(Scenario.Step step, String value) throws RigInternalError {
        if (isBlank(value)) {
            return value;
        }
        String normalized = value.trim().replaceAll("/\\*[^*]*\\*/", "").trim();
        if (normalized.startsWith("$$")) {
            String resolved = step.getScenario().getVariables().get(normalized);
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
            throw new RigInternalError("Scenario variable is not set or empty: " + normalized);
        }
        String variableKey = "$$" + toDslVariableName(normalized);
        if (step.getScenario().getVariables().containsKey(variableKey)) {
            String resolved = step.getScenario().getVariables().get(variableKey);
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
            throw new RigInternalError("Scenario variable is not set or empty: " + variableKey);
        }
        return normalized;
    }

    public static String toDslVariableName(String displayName) {
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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
