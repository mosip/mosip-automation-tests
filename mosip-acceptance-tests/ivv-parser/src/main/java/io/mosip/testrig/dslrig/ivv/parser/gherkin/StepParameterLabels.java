package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Human-readable Gherkin parameter labels per {@code e2e_*} method and argument index.
 */
final class StepParameterLabels {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, List<String>> LABELS = load();

    private StepParameterLabels() {
    }

    static String labelFor(String method, int index) {
        return labelFor(method, index, -1);
    }

    /**
     * @param paramCount number of arguments in the DSL call ({@code -1} = unknown / use default list)
     */
    static String labelFor(String method, int index, int paramCount) {
        List<String> methodLabels = resolveLabelList(method, paramCount);
        if (methodLabels != null && index < methodLabels.size()) {
            return methodLabels.get(index);
        }
        return null;
    }

    private static List<String> resolveLabelList(String method, int paramCount) {
        if (paramCount > 0) {
            String arityKey = method + "#" + paramCount;
            List<String> arityLabels = LABELS.get(arityKey);
            if (arityLabels == null) {
                arityLabels = LABELS.get(arityKey.toLowerCase(Locale.ROOT));
            }
            if (arityLabels != null) {
                return arityLabels;
            }
        }
        List<String> methodLabels = LABELS.get(method);
        if (methodLabels == null) {
            methodLabels = LABELS.get(method.toLowerCase(Locale.ROOT));
        }
        if (methodLabels != null && paramCount > 0 && methodLabels.size() > paramCount
                && "e2e_getResidentData".equalsIgnoreCase(method)
                && paramCount >= 3) {
            int offset = methodLabels.size() - paramCount;
            if (offset > 0 && offset < methodLabels.size()) {
                return methodLabels.subList(offset, methodLabels.size());
            }
        }
        return methodLabels;
    }

    private static Map<String, List<String>> load() {
        try (InputStream in = StepParameterLabels.class.getClassLoader()
                .getResourceAsStream("step-parameter-labels.json")) {
            if (in == null) {
                return Collections.emptyMap();
            }
            JsonNode root = MAPPER.readTree(in);
            Map<String, List<String>> map = new HashMap<>();
            root.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isArray()) {
                    putLabelList(map, entry.getKey(), value);
                } else if (value.isObject()) {
                    value.fields().forEachRemaining(arity -> {
                        String key = entry.getKey() + "#" + arity.getKey();
                        putLabelList(map, key, arity.getValue());
                    });
                }
            });
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load step-parameter-labels.json", e);
        }
    }

    private static void putLabelList(Map<String, List<String>> map, String key, JsonNode arrayNode) {
        List<String> labels = new java.util.ArrayList<>();
        for (JsonNode node : arrayNode) {
            labels.add(node.asText());
        }
        map.put(key, labels);
        map.put(key.toLowerCase(Locale.ROOT), labels);
    }
}
