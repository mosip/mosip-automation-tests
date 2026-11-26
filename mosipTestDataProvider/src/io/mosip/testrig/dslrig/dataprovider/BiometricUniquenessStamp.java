package io.mosip.testrig.dslrig.dataprovider;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public final class BiometricUniquenessStamp {

	private BiometricUniquenessStamp() {
	}

	public static String uniqueToken() {
		return System.currentTimeMillis() + "_" + System.nanoTime() + "_" + UUID.randomUUID();
	}

	public static void apply(BufferedImage img, String token) {
		if (img == null || token == null || token.isEmpty()) {
			return;
		}
		int width = img.getWidth();
		int height = img.getHeight();
		if (width <= 0 || height <= 0) {
			return;
		}

		byte[] hash;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			hash = token.getBytes(StandardCharsets.UTF_8);
		}

		int barY = height - 1;
		for (int x = 0; x < width; x++) {
			int b = hash[x % hash.length] & 0xFF;
			int g = hash[(x + 7) % hash.length] & 0xFF;
			int r = hash[(x + 13) % hash.length] & 0xFF;
			img.setRGB(x, barY, 0xFF000000 | (r << 16) | (g << 8) | b);
		}

		if (height > 1) {
			int y2 = height - 2;
			for (int x = 0; x < width; x++) {
				int v = (hash[x % hash.length] ^ hash[(x * 3) % hash.length]) & 0xFF;
				img.setRGB(x, y2, 0xFF000000 | (v << 16) | (v << 8) | v);
			}
		}

		for (int i = 0; i < hash.length; i++) {
			int x = Math.floorMod((hash[i] & 0xFF) * 31 + i * 17, width);
			int y = Math.floorMod((hash[(i + 1) % hash.length] & 0xFF) * 13 + i * 7, Math.max(1, height - 2));
			int rgb = 0xFF000000 | ((hash[i] & 0xFF) << 16) | ((hash[(i + 2) % hash.length] & 0xFF) << 8)
					| (hash[(i + 4) % hash.length] & 0xFF);
			img.setRGB(x, y, rgb);
		}
	}

	public static void apply(BufferedImage img) {
		apply(img, uniqueToken());
	}
}
