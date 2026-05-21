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
        List<String> methodLabels = LABELS.get(method);
        if (methodLabels == null) {
            methodLabels = LABELS.get(method.toLowerCase(Locale.ROOT));
        }
        if (methodLabels != null && index < methodLabels.size()) {
            return methodLabels.get(index);
        }
        return null;
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
                List<String> labels = new java.util.ArrayList<>();
                for (JsonNode node : entry.getValue()) {
                    labels.add(node.asText());
                }
                map.put(entry.getKey(), labels);
                map.put(entry.getKey().toLowerCase(Locale.ROOT), labels);
            });
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load step-parameter-labels.json", e);
        }
    }
}
