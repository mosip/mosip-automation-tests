package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.parser.Utils.StepParser;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

/**
 * Loads DSL scenarios directly from a Cucumber {@code .feature} file (no CSV intermediate).
 */
public class GherkinFeatureParser {

    public static final String MASTER_FEATURE_FILE = "scenarios.feature";

    private final GherkinStepTranslator translator = new GherkinStepTranslator();

    public static File resolveFeatureFile(String featuresDirOrFile) throws IOException {
        File path = new File(featuresDirOrFile);
        if (path.isFile() && path.getName().endsWith(".feature")) {
            return path;
        }
        File master = new File(path, MASTER_FEATURE_FILE);
        if (master.isFile()) {
            return master;
        }
        throw new IOException("Gherkin feature file not found: " + MASTER_FEATURE_FILE + " under "
                + path.getAbsolutePath());
    }

    public ArrayList<Scenario> parseScenarios(File featureFile) throws RigInternalError {
        try {
            List<ParsedScenario> parsed = parseFeatureFile(featureFile);
            parsed.sort(Comparator.comparing(GherkinFeatureParser::scenarioSortKey));
            ArrayList<Scenario> scenarios = new ArrayList<>();
            for (ParsedScenario ps : parsed) {
                scenarios.add(toScenario(ps));
            }
            return scenarios;
        } catch (IOException e) {
            throw new RigInternalError("Failed to parse Gherkin feature file: " + e.getMessage());
        }
    }

    /**
     * Invoked for each DSL step (e.g. for HTML report metadata).
     */
    public void forEachStep(File featureFile, TriConsumer<String, String, String> stepConsumer) throws IOException {
        for (ParsedScenario ps : parseFeatureFile(featureFile)) {
            for (int i = 0; i < ps.steps.size(); i++) {
                stepConsumer.accept(ps.steps.get(i), ps.scenarioId, ps.stepTexts.get(i));
            }
        }
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    private Scenario toScenario(ParsedScenario ps) {
        Scenario scenario = new Scenario();
        scenario.setId(ps.scenarioId);
        scenario.setDescription(ps.description != null ? ps.description : "");
        scenario.setPersonaClass(ps.persona != null ? ps.persona : "ResidentMaleAdult");
        scenario.setGroupName(ps.group != null ? ps.group : "NA");
        scenario.setTags(parseTags(ps.tag != null ? ps.tag : "Positive_Test"));
        ArrayList<Scenario.Step> steps = new ArrayList<>();
        for (String dsl : ps.steps) {
            Scenario.Step step = StepParser.parse(dsl);
            step.setScenario(scenario);
            steps.add(step);
            if (!scenario.getModules().contains(step.getModule())) {
                scenario.getModules().add(step.getModule());
            }
        }
        scenario.setSteps(steps);
        return scenario;
    }

    private static ArrayList<String> parseTags(String tags) {
        ArrayList<String> tag = new ArrayList<>();
        for (String part : tags.split(",")) {
            if (!part.isBlank()) {
                tag.add(part.trim());
            }
        }
        return tag;
    }

    private static String scenarioSortKey(ParsedScenario s) {
        if ("AFTER_SUITE".equalsIgnoreCase(s.scenarioId)) {
            return "zzz_AFTER_SUITE";
        }
        try {
            return String.format("%09d", Integer.parseInt(s.scenarioId));
        } catch (NumberFormatException e) {
            return s.scenarioId;
        }
    }

    List<ParsedScenario> parseFeatureFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<ParsedScenario> scenarios = new ArrayList<>();
        List<String> pendingTagLines = new ArrayList<>();
        ParsedScenario current = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.startsWith("@")) {
                pendingTagLines.add(line);
                continue;
            }

            if (line.startsWith("Feature:")) {
                String desc = line.substring("Feature:".length()).trim();
                if (hasScenarioTag(pendingTagLines)) {
                    if (current == null) {
                        current = newScenario();
                        applyTags(pendingTagLines, current);
                        pendingTagLines.clear();
                    }
                    current.description = desc;
                }
                continue;
            }

            if (line.startsWith("Scenario:")) {
                String scenarioTitle = line.substring("Scenario:".length()).trim();
                if (current != null && !current.steps.isEmpty()) {
                    scenarios.add(current);
                    current = newScenario();
                    applyTags(pendingTagLines, current);
                    pendingTagLines.clear();
                } else if (current == null) {
                    current = newScenario();
                    applyTags(pendingTagLines, current);
                    pendingTagLines.clear();
                }
                if (current != null && (current.description == null || current.description.isBlank())) {
                    current.description = stripPersonaPrefixFromTitle(scenarioTitle, current.persona);
                }
                continue;
            }

            if (isStepLine(line)) {
                if (current == null) {
                    throw new IOException("Step without scenario context in " + file.getName() + ": " + line);
                }
                String gherkinText = translator.extractGherkinStepText(line);
                String dsl = translator.extractDslFromStepLine(line);
                if (dsl == null) {
                    throw new IOException("Could not parse Gherkin step in " + file.getName() + ": " + line);
                }
                current.steps.add(dsl);
                current.stepTexts.add(gherkinText);
            }
        }

        if (current != null) {
            scenarios.add(current);
        }

        if (scenarios.isEmpty()) {
            Matcher m = Pattern.compile("scenario-(\\d+)").matcher(file.getName());
            if (m.find()) {
                throw new IOException("No scenarios parsed from " + file.getName());
            }
        }

        for (ParsedScenario s : scenarios) {
            if (s.scenarioId == null) {
                throw new IOException("Missing @scenario_<id> tag in " + file.getName());
            }
        }
        return scenarios;
    }

    private static boolean hasScenarioTag(List<String> tagLines) {
        for (String tagLine : tagLines) {
            for (String tag : tagLine.split("\\s+")) {
                if (tag.startsWith("@scenario_")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static ParsedScenario newScenario() {
        ParsedScenario s = new ParsedScenario();
        s.steps = new ArrayList<>();
        s.stepTexts = new ArrayList<>();
        return s;
    }

    private static void applyTags(List<String> tagLines, ParsedScenario scenario) {
        for (String tagLine : tagLines) {
            parseTagsLine(tagLine, scenario);
        }
    }

    private static void parseTagsLine(String tagLine, ParsedScenario scenario) {
        for (String tag : tagLine.split("\\s+")) {
            if (tag.startsWith("@scenario_")) {
                scenario.scenarioId = tag.substring("@scenario_".length());
            } else if (tag.startsWith("@persona_")) {
                scenario.persona = tag.substring("@persona_".length());
            } else if (tag.startsWith("@group_")) {
                scenario.group = tag.substring("@group_".length());
            } else if (tag.equals("@Positive_Test") || tag.equals("@Negative_Test")) {
                scenario.tag = tag.substring(1);
            }
        }
    }

    private static boolean isStepLine(String line) {
        return line.startsWith("Given ") || line.startsWith("When ") || line.startsWith("Then ")
                || line.startsWith("And ") || line.startsWith("But ");
    }

    /** Scenario titles are written as {@code <persona> - <description>}; reports use description only. */
    private static String stripPersonaPrefixFromTitle(String title, String persona) {
        if (title == null || title.isBlank() || persona == null || persona.isBlank()) {
            return title;
        }
        String prefix = persona + " - ";
        if (title.startsWith(prefix)) {
            return title.substring(prefix.length()).trim();
        }
        return title;
    }

    static final class ParsedScenario {
        String scenarioId;
        String tag = "Positive_Test";
        String persona;
        String group = "NA";
        String description;
        List<String> steps;
        List<String> stepTexts;
    }
}
