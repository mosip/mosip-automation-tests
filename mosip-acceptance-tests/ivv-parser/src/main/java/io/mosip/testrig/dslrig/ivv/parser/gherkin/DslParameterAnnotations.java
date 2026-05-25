package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Applies inline parameter comments and DSL value rules when converting Gherkin to canonical DSL.
 * Rules are driven by {@code step-parameter-annotations.json} (not per-scenario).
 */
final class DslParameterAnnotations {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, Map<Integer, List<Rule>>> RULES = load();
    private static final Set<String> MOCK_ABIS_STATUS_WORDS = Set.of(
            "SUCCESS", "DUPLICATE", "ERROR");

    private DslParameterAnnotations() {
    }

    /** Whether this argument index is a step literal (e.g. JSON field name), not a {@code $$} variable. */
    static boolean isLiteralArgument(String method, int index) {
        List<Rule> rules = rulesFor(method, index);
        if (rules == null) {
            return false;
        }
        for (Rule rule : rules) {
            if ("literal".equals(rule.valueRule)) {
                return true;
            }
        }
        return false;
    }

    static String formatArgument(String method, int index, String rawValue) {
        String dslValue = DslListSeparator.toDsl(DslListSeparator.stripInlineLabels(rawValue));
        List<Rule> rules = rulesFor(method, index);
        if (rules == null || rules.isEmpty()) {
            return dslValue;
        }
        String annotation = null;
        for (Rule rule : rules) {
            if (rule.valueRule != null) {
                dslValue = applyValueRule(rule.valueRule, dslValue);
            }
            if (rule.annotation != null && (rule.match == null || rule.match.equals(dslValue))) {
                annotation = rule.annotation;
            }
        }
        return annotate(dslValue, annotation);
    }

    /** Strips leading {@code @@} from mock ABIS status tokens for readable Gherkin. */
    static String toGherkinDisplay(String englishLabel, String dslValue) {
        if (englishLabel == null) {
            return dslValue;
        }
        String label = englishLabel.toLowerCase(Locale.ROOT);
        if (!label.contains("mock abis status")) {
            return dslValue;
        }
        String trimmed = DslListSeparator.stripInlineLabels(dslValue.trim());
        if (trimmed.startsWith("@@")) {
            return trimmed.substring(2);
        }
        return trimmed;
    }

    private static String applyValueRule(String valueRule, String dslValue) {
        if ("mockAbisStatus".equals(valueRule)) {
            return toMockAbisStatusDsl(dslValue);
        }
        return dslValue;
    }

    private static String toMockAbisStatusDsl(String value) {
        String trimmed = DslListSeparator.stripInlineLabels(value.trim());
        if (trimmed.contains("@@")) {
            return trimmed;
        }
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (MOCK_ABIS_STATUS_WORDS.contains(upper)) {
            return "@@" + trimmed;
        }
        return trimmed;
    }

    private static String annotate(String value, String annotation) {
        if (annotation == null || annotation.isBlank()) {
            return value;
        }
        return value + "/*" + annotation + "*/";
    }

    private static List<Rule> rulesFor(String method, int index) {
        Map<Integer, List<Rule>> methodRules = RULES.get(method);
        if (methodRules == null) {
            methodRules = RULES.get(method.toLowerCase(Locale.ROOT));
        }
        return methodRules == null ? null : methodRules.get(index);
    }

    private static Map<String, Map<Integer, List<Rule>>> load() {
        try (InputStream in = DslParameterAnnotations.class.getClassLoader()
                .getResourceAsStream("step-parameter-annotations.json")) {
            if (in == null) {
                return Collections.emptyMap();
            }
            JsonNode root = MAPPER.readTree(in);
            Map<String, Map<Integer, List<Rule>>> map = new HashMap<>();
            root.fields().forEachRemaining(methodEntry -> {
                Map<Integer, List<Rule>> indexRules = new HashMap<>();
                methodEntry.getValue().fields().forEachRemaining(indexEntry -> {
                    int index = Integer.parseInt(indexEntry.getKey());
                    List<Rule> rules = new java.util.ArrayList<>();
                    for (JsonNode node : indexEntry.getValue()) {
                        rules.add(Rule.from(node));
                    }
                    indexRules.put(index, rules);
                });
                map.put(methodEntry.getKey(), indexRules);
                map.put(methodEntry.getKey().toLowerCase(Locale.ROOT), indexRules);
            });
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load step-parameter-annotations.json", e);
        }
    }

    private static final class Rule {
        final String match;
        final String annotation;
        final String valueRule;

        Rule(String match, String annotation, String valueRule) {
            this.match = match;
            this.annotation = annotation;
            this.valueRule = valueRule;
        }

        static Rule from(JsonNode node) {
            return new Rule(
                    node.has("match") ? node.get("match").asText() : null,
                    node.has("annotation") ? node.get("annotation").asText() : null,
                    node.has("valueRule") ? node.get("valueRule").asText() : null);
        }
    }
}
