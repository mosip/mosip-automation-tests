package io.mosip.testrig.dslrig.ivv.core.dsl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

/**
 * Single variable store with {@link VariableScope#STEP step}, {@link VariableScope#SCENARIO scenario},
 * and {@link VariableScope#GLOBAL global} layers. Lookup order: step → scenario → global.
 */
public final class VariableStore {

    private final Map<String, String> global;
    private final Map<String, String> scenario;
    private final Map<String, String> step = new HashMap<>();

    public VariableStore(Map<String, String> global, Map<String, String> scenario) {
        this.global = global != null ? global : Collections.emptyMap();
        this.scenario = scenario != null ? scenario : Collections.emptyMap();
    }

    public static VariableStore forStep(Scenario.Step step, Store store) {
        Map<String, String> globals = store != null && store.getGlobals() != null
                ? store.getGlobals()
                : Collections.emptyMap();
        Map<String, String> scenarioVars = step != null && step.getScenario() != null
                && step.getScenario().getVariables() != null
                ? step.getScenario().getVariables()
                : Collections.emptyMap();
        return new VariableStore(globals, scenarioVars);
    }

    public void clearStep() {
        step.clear();
    }

    public void put(VariableScope scope, String key, String value) {
        if (key == null || value == null) {
            return;
        }
        String normalized = normalizeDslKey(key);
        switch (scope) {
            case STEP:
                step.put(normalized, value);
                break;
            case SCENARIO:
                if (scenario instanceof HashMap) {
                    ((HashMap<String, String>) scenario).put(normalized, value);
                }
                break;
            case GLOBAL:
                if (global instanceof HashMap) {
                    ((HashMap<String, String>) global).put(normalized, value);
                    String bare = bareKey(normalized);
                    if (!bare.equals(normalized)) {
                        ((HashMap<String, String>) global).put(bare, value);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void putAll(VariableScope scope, Map<String, String> values) {
        if (values == null) {
            return;
        }
        for (Map.Entry<String, String> e : values.entrySet()) {
            put(scope, e.getKey(), e.getValue());
        }
    }

    public boolean contains(String dslKey) {
        return lookupRaw(normalizeDslKey(dslKey)) != null;
    }

    public String resolveReference(String raw) throws RigInternalError {
        if (isBlank(raw)) {
            return raw;
        }
        String normalized = stripInlineAnnotations(raw.trim());
        if (normalized.startsWith("$$")) {
            return requireNonEmpty(lookupScoped(normalized), normalized);
        }
        String variableKey = "$$" + toDslVariableName(normalized);
        if (containsInAnyScope(variableKey)) {
            return requireNonEmpty(lookupScoped(variableKey), variableKey);
        }
        return normalized;
    }

    public boolean referencesVariable(String raw) {
        if (isBlank(raw)) {
            return false;
        }
        String normalized = stripInlineAnnotations(raw.trim());
        if (normalized.startsWith("$$")) {
            return true;
        }
        return containsInAnyScope("$$" + toDslVariableName(normalized));
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

    Map<String, String> stepView() {
        return Collections.unmodifiableMap(step);
    }

    private String lookupScoped(String dslKey) {
        String key = normalizeDslKey(dslKey);
        String value = lookupRaw(key);
        if (value != null) {
            return value;
        }
        String bare = bareKey(key);
        if (!bare.equals(key)) {
            value = lookupRaw("$$" + bare);
            if (value != null) {
                return value;
            }
            value = lookupRaw(bare);
        }
        return value;
    }

    private String lookupRaw(String dslKey) {
        if (step.containsKey(dslKey)) {
            return step.get(dslKey);
        }
        if (scenario.containsKey(dslKey)) {
            return scenario.get(dslKey);
        }
        if (global.containsKey(dslKey)) {
            return global.get(dslKey);
        }
        String bare = bareKey(dslKey);
        if (global.containsKey(bare)) {
            return global.get(bare);
        }
        return null;
    }

    private boolean containsInAnyScope(String dslKey) {
        String key = normalizeDslKey(dslKey);
        return step.containsKey(key) || scenario.containsKey(key) || global.containsKey(key)
                || global.containsKey(bareKey(key));
    }

    private static String requireNonEmpty(String value, String key) throws RigInternalError {
        if (value != null && !value.isBlank()) {
            return value;
        }
        throw new RigInternalError("Variable is not set or empty: " + key
                + " (checked scopes: step, scenario, global)");
    }

    private static String normalizeDslKey(String key) {
        if (key == null) {
            return "";
        }
        String k = key.trim();
        return k.startsWith("$$") ? k : "$$" + k;
    }

    private static String bareKey(String dslKey) {
        return dslKey.startsWith("$$") ? dslKey.substring(2) : dslKey;
    }

    private static String stripInlineAnnotations(String value) {
        return value.replaceAll("/\\*[^*]*\\*/", "").trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
