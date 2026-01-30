package io.mosip.testrig.dslrig.dataprovider;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import javax.imageio.ImageIO;

import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class FaceVariationGenerator {

    /* =========================================================
       ENTRY POINT
       ========================================================= */

    public static String faceVariationGenerator(String contextKey,
                                                int scenario,
                                                int impressionToPick) throws IOException {

        String basePath = System.getProperty("java.io.tmpdir")
                + VariableManager.getVariableValue(contextKey,
                "mosip.test.persona.facedatapath");

        String inputPath = basePath + "/" + String.format("face%04d.jpg", impressionToPick);
        String outputPath = basePath + "/output/" + scenario;

        generateFaceVariations(inputPath, outputPath);
        return outputPath;
    }

    /* =========================================================
       CORE GENERATION (THREAD SAFE)
       ========================================================= */

    public static void generateFaceVariations(String inputPath,
                                               String outputDir) throws IOException {

        List<String> files = listFiles(inputPath);

        for (String file : files) {

            BufferedImage original = ImageIO.read(new File(file));
            if (original == null) continue;

            FaceVariation variation = FaceVariation.random();

            long seed = System.nanoTime() ^ ThreadLocalRandom.current().nextLong();

            VariationResult result = variation.apply(original, seed);

            Path src = Paths.get(file);
            Path outDir = Paths.get(outputDir, src.getParent().getFileName().toString());
            Files.createDirectories(outDir);

            // Ensure output filename uses .png extension and is unique
            String originalFileName = src.getFileName().toString();
            String outputFileName = changeExtensionToPng(originalFileName);
            String outName = result.name + "_" + seed + "_" + outputFileName;
            ImageIO.write(result.image, "png", outDir.resolve(outName).toFile());
        }
    }

    // helper to convert file name extension to .png
    private static String changeExtensionToPng(String fileName) {
        if (fileName == null) return "image.png";
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        return base + ".png";
    }

    /* =========================================================
       VARIATION ENUM
       ========================================================= */

    private enum FaceVariation {

        LOW_CONTRAST("LowContrast", FaceVariationGenerator::applyLowContrastFaceVariation),
        LOW_BRIGHT("LowBright", FaceVariationGenerator::applyLowBrightFaceVariation),
        HIGH_BRIGHT("HighBright", FaceVariationGenerator::applyHighBrightFaceVariation),
        UNNATURAL_SKIN("UnnaturalSkinTone", FaceVariationGenerator::applyUnnaturalSkinToneFaceVariation),
        HIGH_RES("HighResolution", FaceVariationGenerator::applyHighResolutionFaceVariation),
        LOW_RES("LowResolution", FaceVariationGenerator::applyLowResolutionFaceVariation),
        SHADOW("Shadow", FaceVariationGenerator::applyShadowOnFaceVariation),
        OVER_EXPOSURE("OverExposure", FaceVariationGenerator::applyOverExposureFaceVariation),
        UNDER_EXPOSURE("UnderExposure", FaceVariationGenerator::applyUnderExposureFaceVariation),
        BLUR("Blurred", FaceVariationGenerator::applyBlurredFaceVariation),
        NOISE("Noisy", FaceVariationGenerator::applyNosiyFaceVariation),
        SKEW("Skewed", FaceVariationGenerator::applySkewedFaceVariation),
        AGED("Aged", FaceVariationGenerator::applyAgedFaceVariation);

        private final String name;
        private final BiFunction<BufferedImage, Long, BufferedImage> fn;

        FaceVariation(String name,
                      BiFunction<BufferedImage, Long, BufferedImage> fn) {
            this.name = name;
            this.fn = fn;
        }

        VariationResult apply(BufferedImage img, long seed) {
            return new VariationResult(name, fn.apply(img, seed));
        }

        static FaceVariation random() {
            FaceVariation[] v = values();
            return v[ThreadLocalRandom.current().nextInt(v.length)];
        }
    }

    /* =========================================================
       VARIATION RESULT
       ========================================================= */

    private static class VariationResult {
        final String name;
        final BufferedImage image;

        VariationResult(String name, BufferedImage image) {
            this.name = name;
            this.image = image;
        }
    }

    /* =========================================================
       EXACT ORIGINAL METHODS (PRESERVED)
       ========================================================= */

    private static BufferedImage applyAgedFaceVariation(BufferedImage img, long seed) {
        BufferedImage out = copy(img);
        applyWrinkles(out);
        sharpenImage(out);
        return out;
    }

    private static void applyWrinkles(BufferedImage img) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int y = img.getHeight() / 4; y < img.getHeight() * 3 / 4; y++)
            for (int x = img.getWidth() / 4; x < img.getWidth() * 3 / 4; x++)
                if (r.nextDouble() > 0.99) {
                    Color c = new Color(img.getRGB(x, y));
                    int g = clamp(c.getRed() - 30);
                    img.setRGB(x, y, new Color(g, g, g).getRGB());
                }
    }

    private static void sharpenImage(BufferedImage img) {
        float[] kernel = {0, -1, 0, -1, 5, -1, 0, -1, 0};
        applyConvolution(img, kernel);
    }

    private static void applyConvolution(BufferedImage img, float[] matrix) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage temp = copy(img);
        int size = (int) Math.sqrt(matrix.length);
        int off = size / 2;

        for (int y = off; y < h - off; y++)
            for (int x = off; x < w - off; x++) {
                float r = 0, g = 0, b = 0;
                for (int j = 0; j < size; j++)
                    for (int i = 0; i < size; i++) {
                        Color c = new Color(
                                temp.getRGB(x + i - off, y + j - off));
                        float k = matrix[j * size + i];
                        r += c.getRed() * k;
                        g += c.getGreen() * k;
                        b += c.getBlue() * k;
                    }
                img.setRGB(x, y, new Color(
                        clamp((int) r),
                        clamp((int) g),
                        clamp((int) b)).getRGB());
            }
    }

    private static BufferedImage applySkewedFaceVariation(BufferedImage img, long seed) {
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g = out.createGraphics();
        g.setColor(getBackgroundColor(img));
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.setTransform(AffineTransform.getShearInstance(0.3, 0.2));
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return out;
    }

    private static Color getBackgroundColor(BufferedImage img) {
        return new Color(img.getRGB(0, 0));
    }

    private static BufferedImage applyNosiyFaceVariation(BufferedImage img, long seed) {
        BufferedImage out = copy(img);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int y = 0; y < out.getHeight(); y++)
            for (int x = 0; x < out.getWidth(); x++) {
                Color c = new Color(out.getRGB(x, y));
                int n = (int) (r.nextGaussian() * 20);
                out.setRGB(x, y,
                        new Color(clamp(c.getRed() + n),
                                  clamp(c.getGreen() + n),
                                  clamp(c.getBlue() + n)).getRGB());
            }
        return out;
    }

    private static BufferedImage applyBlurredFaceVariation(BufferedImage img, long seed) {
        BufferedImage out = copy(img);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                out.setRGB(x, y,
                        calculateAverageColor(img, x, y, 5).getRGB());
        return out;
    }

    private static Color calculateAverageColor(BufferedImage img, int x, int y, int r) {
        int rs = 0, gs = 0, bs = 0, c = 0;
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++) {
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < img.getWidth() && ny < img.getHeight()) {
                    Color col = new Color(img.getRGB(nx, ny));
                    rs += col.getRed();
                    gs += col.getGreen();
                    bs += col.getBlue();
                    c++;
                }
            }
        return new Color(rs / c, gs / c, bs / c);
    }

    private static BufferedImage applyUnderExposureFaceVariation(BufferedImage img, long seed) {
        return addUnderexposureEffect(img, 0.5f);
    }

    private static BufferedImage applyOverExposureFaceVariation(BufferedImage img, long seed) {
        return addUnderexposureEffect(img, 2.0f);
    }

    private static BufferedImage addUnderexposureEffect(BufferedImage img, float f) {
        BufferedImage out = copy(img);
        for (int y = 0; y < out.getHeight(); y++)
            for (int x = 0; x < out.getWidth(); x++) {
                Color c = new Color(out.getRGB(x, y));
                out.setRGB(x, y,
                        new Color(clamp((int) (c.getRed() * f)),
                                  clamp((int) (c.getGreen() * f)),
                                  clamp((int) (c.getBlue() * f))).getRGB());
            }
        return out;
    }

    private static BufferedImage applyShadowOnFaceVariation(BufferedImage img, long seed) {
        BufferedImage out = copy(img);
        Graphics2D g = out.createGraphics();
        LinearGradientPaint p = new LinearGradientPaint(
                new Point2D.Float(out.getWidth() / 2f, out.getHeight() / 2f),
                new Point2D.Float(out.getWidth(), out.getHeight()),
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 120), new Color(0, 0, 0, 0)});
        g.setPaint(p);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.dispose();
        return out;
    }

    private static BufferedImage applyHighResolutionFaceVariation(BufferedImage img, long seed) {
        return adjustBrightness(img, 1.4f);
    }

    private static BufferedImage applyLowResolutionFaceVariation(BufferedImage img, long seed) {
        return adjustBrightness(createLowResolutionEffect(img, 5), 1.2f);
    }

    private static BufferedImage createLowResolutionEffect(BufferedImage img, int f) {
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage down = new BufferedImage(w / f, h / f, img.getType());
        Graphics2D g = down.createGraphics();
        g.drawImage(img, 0, 0, down.getWidth(), down.getHeight(), null);
        g.dispose();

        BufferedImage up = new BufferedImage(w, h, img.getType());
        g = up.createGraphics();
        g.drawImage(down, 0, 0, w, h, null);
        g.dispose();
        return up;
    }

    private static BufferedImage applyUnnaturalSkinToneFaceVariation(BufferedImage img, long seed) {
        return adjustColor(img, 1.1f, 1.0f, 0.9f);
    }

    private static BufferedImage applyLowBrightFaceVariation(BufferedImage img, long seed) {
        return adjustBrightness(img, 0.5f);
    }

    private static BufferedImage applyHighBrightFaceVariation(BufferedImage img, long seed) {
        return adjustBrightness(img, 1.2f);
    }

    private static BufferedImage applyHighContrastFaceVariation(BufferedImage img, long seed) {
        float f = 1.7f + ThreadLocalRandom.current().nextFloat() * 1.6f;
        return adjustContrast(img, f);
    }

    private static BufferedImage applyLowContrastFaceVariation(BufferedImage img, long seed) {
        return adjustContrast(img, 0.1f);
    }

    /* =========================================================
       SHARED IMAGE HELPERS
       ========================================================= */

    private static BufferedImage adjustBrightness(BufferedImage img, float f) {
        BufferedImage out = copy(img);
        for (int y = 0; y < out.getHeight(); y++)
            for (int x = 0; x < out.getWidth(); x++) {
                Color c = new Color(out.getRGB(x, y));
                out.setRGB(x, y,
                        new Color(clamp((int) (c.getRed() * f)),
                                  clamp((int) (c.getGreen() * f)),
                                  clamp((int) (c.getBlue() * f))).getRGB());
            }
        return out;
    }

    private static BufferedImage adjustContrast(BufferedImage img, float f) {
        BufferedImage out = copy(img);
        for (int y = 0; y < out.getHeight(); y++)
            for (int x = 0; x < out.getWidth(); x++) {
                Color c = new Color(out.getRGB(x, y));
                out.setRGB(x, y,
                        new Color(
                                clamp((int) ((c.getRed() - 128) * f + 128)),
                                clamp((int) ((c.getGreen() - 128) * f + 128)),
                                clamp((int) ((c.getBlue() - 128) * f + 128)))
                                .getRGB());
            }
        return out;
    }

    private static BufferedImage adjustColor(BufferedImage img, float r, float g, float b) {
        BufferedImage out = copy(img);
        for (int y = 0; y < out.getHeight(); y++)
            for (int x = 0; x < out.getWidth(); x++) {
                Color c = new Color(out.getRGB(x, y));
                out.setRGB(x, y,
                        new Color(clamp((int) (c.getRed() * r)),
                                  clamp((int) (c.getGreen() * g)),
                                  clamp((int) (c.getBlue() * b))).getRGB());
            }
        return out;
    }

    private static BufferedImage copy(BufferedImage img) {
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g = out.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return out;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static List<String> listFiles(String path) throws IOException {
        List<String> files = new ArrayList<>();
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                files.add(file.toString());
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }
}