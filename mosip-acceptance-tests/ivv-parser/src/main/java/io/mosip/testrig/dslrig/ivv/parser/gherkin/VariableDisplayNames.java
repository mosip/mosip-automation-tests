package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Maps DSL scenario variables ({@code $$name}) to readable names in Gherkin and back.
 */
final class VariableDisplayNames {

    private static final Pattern DSL_VAR = Pattern.compile("\\$\\$\\w+");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, String> dslToDisplay;
    private final List<Map.Entry<String, String>> displayToDslSorted;

    private static final VariableDisplayNames INSTANCE = new VariableDisplayNames();

    private VariableDisplayNames() {
        Map<String, String> forward = loadForward();
        this.dslToDisplay = forward;
        List<Map.Entry<String, String>> reverse = new ArrayList<>();
        for (Map.Entry<String, String> e : forward.entrySet()) {
            reverse.add(Map.entry(e.getValue().toLowerCase(Locale.ROOT), e.getKey()));
        }
        reverse.sort(Comparator.comparingInt((Map.Entry<String, String> e) -> e.getKey().length()).reversed());
        this.displayToDslSorted = reverse;
    }

    static String toDisplay(String dslVariable) {
        if (dslVariable == null) {
            return null;
        }
        String trimmed = dslVariable.trim();
        String mapped = INSTANCE.dslToDisplay.get(trimmed);
        if (mapped != null) {
            return mapped;
        }
        if (trimmed.startsWith("$$")) {
            return humanizeDslVariable(trimmed);
        }
        return trimmed;
    }

    static String toDsl(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("$$")) {
            return trimmed;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry : INSTANCE.displayToDslSorted) {
            if (lower.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return trimmed;
    }

    static String replaceDslVarsInText(String text) {
        if (text == null || !text.contains("$$")) {
            return text;
        }
        Matcher m = DSL_VAR.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(toDisplay(m.group())));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    static String replaceDisplayNamesInText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        if (text.trim().startsWith("$$")) {
            return text.trim();
        }
        String result = text.trim();
        for (Map.Entry<String, String> entry : INSTANCE.displayToDslSorted) {
            if (result.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return result;
    }

    private static String humanizeDslVariable(String dslVar) {
        String name = dslVar.substring(2);
        String spaced = name.replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ');
        return spaced.toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> loadForward() {
        try (InputStream in = VariableDisplayNames.class.getClassLoader()
                .getResourceAsStream("variable-display-names.json")) {
            if (in == null) {
                return Collections.emptyMap();
            }
            JsonNode root = MAPPER.readTree(in);
            Map<String, String> map = new HashMap<>();
            root.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load variable-display-names.json", e);
        }
    }
}
