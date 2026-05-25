package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Maps DSL {@code e2e_*} actions to readable Gherkin steps.
 */
public class GherkinStepTranslator {

    private static final String DSL_COMMENT_PREFIX = "# @dsl: ";
    private static final Pattern DSL_ACTION = Pattern.compile("^(\\$\\$\\w+=)?e2e_\\w+\\(.*");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<StepPattern> patterns = new ArrayList<>();

    public GherkinStepTranslator() {
        loadMappings();
    }

    private void loadMappings() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("step-gherkin-mappings.json")) {
            if (in == null) {
                return;
            }
            JsonNode root = MAPPER.readTree(in);
            for (JsonNode node : root.path("patterns")) {
                StepPattern p = new StepPattern();
                p.actionPattern = Pattern.compile(node.path("actionPattern").asText());
                if (node.has("targetenvKeyword") && node.has("firstKeyword")) {
                    p.targetenvComponent = "targetenv";
                    p.targetenvGherkin = node.path("targetenvKeyword").asText();
                    p.gherkin = node.path("gherkin").asText();
                    p.firstKeyword = node.path("firstKeyword").asText();
                } else {
                    p.gherkin = node.path("gherkin").asText();
                    p.keyword = node.has("keyword") ? node.path("keyword").asText() : "And";
                }
                patterns.add(p);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load step-gherkin-mappings.json", e);
        }
    }

    /**
     * Converts a DSL action string to a Gherkin step line (without keyword).
     */
    public String toGherkinText(String action, String stepDescription) {
        return ReadableDslStepCodec.encode(action);
    }

    /** Decodes readable English Gherkin step text back to a DSL action string. */
    public String gherkinToDsl(String gherkinBody) {
        if (gherkinBody == null || gherkinBody.isBlank()) {
            throw new IllegalArgumentException("Empty Gherkin step text");
        }
        String trimmed = gherkinBody.trim();
        if (isDslAction(trimmed)) {
            return trimmed;
        }
        return ReadableDslStepCodec.decode(trimmed);
    }

    public String resolveKeyword(String action, int stepIndex, String previousKeyword) {
        String normalized = normalizeAction(action);
        for (StepPattern p : patterns) {
            Matcher m = p.actionPattern.matcher(normalized);
            if (!m.matches()) {
                continue;
            }
            if (p.firstKeyword != null && extractPingComponent(normalized) != null
                    && !"targetenv".equals(extractPingComponent(normalized))) {
                return p.firstKeyword;
            }
            if (p.targetenvGherkin != null && "targetenv".equals(extractPingComponent(normalized))) {
                return "And";
            }
            if ("Then".equals(p.keyword)) {
                return "Then";
            }
            return stepIndex == 0 ? "Given" : (p.keyword != null ? p.keyword : "And");
        }
        if (stepIndex == 0) {
            return "Given";
        }
        if ("Then".equalsIgnoreCase(previousKeyword) || normalized.contains("CheckRIDStage")) {
            return "Then";
        }
        return "And";
    }

    public String dslComment(String action) {
        return DSL_COMMENT_PREFIX + action.trim();
    }

    public String extractDslFromLine(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith(DSL_COMMENT_PREFIX)) {
            return trimmed.substring(DSL_COMMENT_PREFIX.length()).trim();
        }
        return null;
    }

    /** Returns DSL action from a Gherkin step line when the step text is an {@code e2e_*} action. */
    public String extractDslFromStepLine(String line) {
        String fromComment = extractDslFromLine(line);
        if (fromComment != null) {
            return fromComment;
        }
        String stepText = extractGherkinStepText(line);
        if (isDslAction(stepText)) {
            return stepText.trim();
        }
        try {
            return gherkinToDsl(stepText);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isDslAction(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return DSL_ACTION.matcher(text.trim()).matches();
    }

    public String extractGherkinStepText(String line) {
        String trimmed = line.trim();
        for (String kw : new String[] { "Given ", "When ", "Then ", "And ", "But " }) {
            if (trimmed.startsWith(kw)) {
                return trimmed.substring(kw.length()).trim();
            }
        }
        return trimmed;
    }

    private static String normalizeAction(String action) {
        if (action == null) {
            return "";
        }
        return action.replaceAll("/\\*.*?\\*/", "").trim();
    }

    private static String extractPingComponent(String action) {
        Matcher m = Pattern.compile("^e2e_getPingHealth\\((\\w+)\\)$").matcher(action);
        return m.matches() ? m.group(1) : null;
    }

    private static String expandPlaceholders(String template, Matcher m) {
        String result = template;
        for (int i = 1; i <= m.groupCount(); i++) {
            result = result.replace("{" + i + "}", m.group(i) != null ? m.group(i) : "");
        }
        return result;
    }

    private static String descriptionToGherkin(String description, String action) {
        if (description != null && !description.isBlank()) {
            String d = description.trim();
            if (!d.endsWith(".")) {
                return d.substring(0, 1).toLowerCase() + d.substring(1);
            }
            return d.substring(0, 1).toLowerCase() + d.substring(1, d.length() - 1);
        }
        return "I execute DSL action \"" + action + "\"";
    }

    private static class StepPattern {
        Pattern actionPattern;
        String gherkin;
        String keyword;
        String firstKeyword;
        String targetenvGherkin;
        String targetenvComponent;
    }
}
