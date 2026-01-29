package io.mosip.testrig.dslrig.dataprovider;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class FaceVariationGenerator {

    // ================= VARIATION FLAGS =================
    public static final int HIGH_CONTRAST_FACE = 1 << 0;
    private static int faceVariations = 0;

    // ================= ENTRY POINT =================
    public static String faceVariationGenerator(
            String contextKey,
            int currentScenarioNumber,
            int impressionToPick) throws IOException {

        // 🔥 RUN_ID GENERATED PER METHOD CALL
        long RUN_ID = System.currentTimeMillis();

        String basePath = System.getProperty("java.io.tmpdir")
                + VariableManager.getVariableValue(
                        contextKey,
                        "mosip.test.persona.facedatapath");

        String inputImagePath =
                basePath + "/" + String.format("face%04d.jpg", impressionToPick);

        String outputPath =
                basePath + "/output/" + currentScenarioNumber;

        generateFaceVariations(
                inputImagePath,
                outputPath,
                currentScenarioNumber,
                RUN_ID);

        return outputPath;
    }

    // ================= MAIN GENERATOR =================
    private static void generateFaceVariations(
            String inputFaceTemplatePath,
            String outputUniqueFaceDataPath,
            int scenarioNumber,
            long runId) {

        resetFaceVariations();
        setFaceVariations(HIGH_CONTRAST_FACE);

        try {
            List<String> files = listFiles(inputFaceTemplatePath);

            for (String file : files) {
                Path path = Paths.get(file);
                String fileName = path.getFileName().toString();
                String faceFolder = path.getParent().getFileName().toString();

                generateForSingleImage(
                        file,
                        outputUniqueFaceDataPath,
                        faceFolder,
                        fileName,
                        scenarioNumber,
                        runId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SINGLE IMAGE =================
    private static void generateForSingleImage(
            String imagePath,
            String outputPath,
            String faceFolder,
            String fileName,
            int scenarioNumber,
            long runId) throws IOException {

        BufferedImage original = ImageIO.read(new File(imagePath));
        if (original == null) {
            throw new RuntimeException("Failed to read image: " + imagePath);
        }

        if (isVariationSet(HIGH_CONTRAST_FACE)) {

            VariationResult result =
                    applyHighContrast(original, scenarioNumber, runId);

            Path dir = Paths.get(outputPath, faceFolder);
            Files.createDirectories(dir);

            File outFile = new File(
                    dir.toFile(),
                    result.name + "_" + fileName);

            ImageIO.write(result.image, "png", outFile);
        }
    }

    // ================= VARIATION RESULT =================
    private static class VariationResult {
        String name;
        BufferedImage image;

        VariationResult(String name, BufferedImage image) {
            this.name = name;
            this.image = image;
        }
    }

    // ================= HIGH CONTRAST VARIATION =================
    private static VariationResult applyHighContrast(
            BufferedImage original,
            int scenarioNumber,
            long runId) {

        BufferedImage copy = cloneImage(original);

        // 🎯 UNIQUE SEED PER CALL
        long seed = runId
                + (scenarioNumber * 10_000L)
                + System.nanoTime();

        Random random = new Random(seed);

        // Strong contrast (never near-original)
        float factor = 1.7f + random.nextFloat() * 1.6f; // 1.7 → 3.3

        BufferedImage contrasted = adjustContrast(copy, factor);

        return new VariationResult(
                "HighContrast_" + seed,
                contrasted);
    }

    // ================= IMAGE HELPERS =================
    private static BufferedImage cloneImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(
                src.getWidth(),
                src.getHeight(),
                src.getType());

        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    private static BufferedImage adjustContrast(
            BufferedImage image,
            float factor) {

        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage out = new BufferedImage(w, h, image.getType());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(image.getRGB(x, y));

                int r = clamp(((c.getRed() - 128) * factor) + 128);
                int g = clamp(((c.getGreen() - 128) * factor) + 128);
                int b = clamp(((c.getBlue() - 128) * factor) + 128);

                out.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return out;
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }

    // ================= VARIATION FLAGS =================
    private static void setFaceVariations(int v) {
        faceVariations |= v;
    }

    private static void resetFaceVariations() {
        faceVariations = 0;
    }

    private static boolean isVariationSet(int v) {
        return (faceVariations & v) != 0;
    }

    // ================= FILE WALK =================
    private static List<String> listFiles(String path) throws IOException {
        List<String> files = new ArrayList<>();

        Files.walkFileTree(Paths.get(path),
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(
                            Path file,
                            BasicFileAttributes attrs) {
                        files.add(file.toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
        return files;
    }
}
