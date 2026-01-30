package io.mosip.testrig.dslrig.dataprovider;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class FaceVariationGeneratorTest {

    @Test
    public void testGenerateFaceVariationsProducesUniquePngOutputs() throws Exception {
        Path tmp = Files.createTempDirectory("faceTest");
        try {
            Path inputRoot = tmp.resolve("input");
            Files.createDirectories(inputRoot);
            Path faceDir = inputRoot.resolve("001");
            Files.createDirectories(faceDir);

            // create a small sample image
            BufferedImage sample = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = sample.createGraphics();
            g.setColor(Color.PINK);
            g.fillRect(0, 0, 64, 64);
            g.setColor(Color.BLUE);
            g.fillOval(8, 8, 48, 48);
            g.dispose();

            Path sampleFile = faceDir.resolve("person.jpg");
            ImageIO.write(sample, "jpg", sampleFile.toFile());

            Path outputRoot = tmp.resolve("output");
            Files.createDirectories(outputRoot);

            // run the generator for the single sample file
            FaceVariationGenerator.generateFaceVariations(inputRoot.toString(), outputRoot.toString());

            Path outDir = outputRoot.resolve("001");
            assertTrue("Output directory should exist", Files.exists(outDir));

            Set<String> names = new HashSet<>();
            int count = 0;
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(outDir)) {
                for (Path p : ds) {
                    String n = p.getFileName().toString();
                    // Ensure files use .png extension
                    assertTrue("Output file must end with .png: " + n, n.toLowerCase().endsWith(".png"));
                    // Ensure original jpg is not saved
                    assertFalse("Original jpg should not be present in outputs", n.equalsIgnoreCase("person.jpg"));
                    names.add(n);
                    count++;
                }
            }

            assertTrue("Should have created at least one output file", count > 0);
            assertEquals("Filenames should be unique", count, names.size());

        } finally {
            // best-effort cleanup
            try {
                Files.walk(tmp)
                        .sorted((a, b) -> b.compareTo(a))
                        .forEach(p -> p.toFile().delete());
            } catch (Exception ignored) {}
        }
    }
}
