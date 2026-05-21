package io.mosip.testrig.dslrig.ivv.parser.gherkin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites a {@code .feature} file so each DSL step uses readable parameter and variable names.
 * Usage: {@code java ...GherkinFeatureFormatter <input.feature> [output.feature]}
 */
public final class GherkinFeatureFormatter {

    private static final String[] STEP_KEYWORDS = { "Given ", "When ", "Then ", "And ", "But " };

    private GherkinFeatureFormatter() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: GherkinFeatureFormatter <input.feature> [output.feature]");
            System.exit(1);
        }
        Path input = Path.of(args[0]);
        Path output = args.length > 1 ? Path.of(args[1]) : input;
        formatFeatureFile(input, output);
        System.out.println("Formatted: " + output.toAbsolutePath());
    }

    public static void formatFeatureFile(Path input, Path output) throws IOException {
        List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
        GherkinStepTranslator translator = new GherkinStepTranslator();
        List<String> out = new ArrayList<>(lines.size());
        for (String rawLine : lines) {
            String line = rawLine;
            String trimmed = rawLine.trim();
            if (isStepLine(trimmed)) {
                String keyword = stepKeyword(trimmed);
                String body = translator.extractGherkinStepText(trimmed);
                String readable = ReadableDslStepCodec.reformatGherkinStep(body);
                line = keyword + readable;
            }
            out.add(line);
        }
        Files.write(output, out, StandardCharsets.UTF_8);
    }

    private static boolean isStepLine(String line) {
        for (String kw : STEP_KEYWORDS) {
            if (line.startsWith(kw)) {
                return true;
            }
        }
        return false;
    }

    private static String stepKeyword(String line) {
        for (String kw : STEP_KEYWORDS) {
            if (line.startsWith(kw)) {
                return kw;
            }
        }
        return "";
    }
}
