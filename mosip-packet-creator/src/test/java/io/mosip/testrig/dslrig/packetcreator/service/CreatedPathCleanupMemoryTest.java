package io.mosip.testrig.dslrig.packetcreator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.management.OperatingSystemMXBean;

import io.mosip.testrig.dslrig.dataprovider.util.CreatedPathRegistry;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

class CreatedPathCleanupMemoryTest {

	private static final int SCENARIOS = 20;
	private static final int PACKETS_PER_SCENARIO = 1000;
	private static final byte[] PACKET_PAYLOAD = new byte[64 * 1024];

	private Path suiteRoot;

	@AfterEach
	void tearDown() throws Exception {
		try {
			ContextUtils.clearAllCreatedData();
		} catch (Exception ignored) {
		}
		forceDelete(suiteRoot);
	}

	@Test
	void should_leaveZeroCreatedDataAfterEachDslRun_for20000Packets() throws Exception {
		OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

		warmupCaches();
		gcQuiet();
		long baselineHeap = heapUsed();
		printMemory("baseline", baselineHeap, os.getFreePhysicalMemorySize(), 0L, 0L);

		suiteRoot = Path.of("target", "dsl-memory-20000").toAbsolutePath();
		forceDelete(suiteRoot);
		Files.createDirectories(suiteRoot);

		Path resourceRoot = suiteRoot.resolve("resource");
		Files.createDirectories(resourceRoot);
		Path staticProps = resourceRoot.resolve("application.properties");
		Files.writeString(staticProps, "keep=true");
		Path defaultKeep = resourceRoot.resolve("Profile").resolve("Default").resolve("keep.txt");
		Files.createDirectories(defaultKeep.getParent());
		Files.writeString(defaultKeep, "static");

		Path sharedPkt = suiteRoot.resolve("pktcreator");
		Path sharedPrereg = suiteRoot.resolve("prereg");
		Files.createDirectories(sharedPkt);
		Files.createDirectories(sharedPrereg);
		Files.writeString(sharedPkt.resolve("v1.json"), "{}");
		Files.write(sharedPrereg.resolve("app.zip"), new byte[1024]);
		CreatedPathRegistry.registerShared(sharedPkt.toString());
		CreatedPathRegistry.registerShared(sharedPrereg.toString());

		Path keyFile = resourceRoot.resolve("privatekeys").resolve("api-internal.qaj21.10002.reg.key");
		Files.createDirectories(keyFile.getParent());
		Files.write(keyFile, new byte[2048]);
		CreatedPathRegistry.registerShared(keyFile.toString());

		List<List<Path>> perScenarioCreated = new ArrayList<>();
		long createdBytes = 0L;

		for (int s = 1; s <= SCENARIOS; s++) {
			String contextKey = "env_S" + s + "_context";
			List<Path> scenarioPaths = new ArrayList<>();

			Path packetDir = suiteRoot.resolve("packets").resolve("S" + s);
			Files.createDirectories(packetDir);
			for (int p = 1; p <= PACKETS_PER_SCENARIO; p++) {
				Files.write(packetDir.resolve("packet_" + p + ".bin"), PACKET_PAYLOAD);
				createdBytes += PACKET_PAYLOAD.length;
			}
			CreatedPathRegistry.register(contextKey, "packets_", packetDir.toString());
			scenarioPaths.add(packetDir);

			Path ridWork = sharedPkt.resolve("rid_S" + s);
			Files.createDirectories(ridWork);
			Files.writeString(ridWork.resolve("meta.json"), "{\"rid\":true}");
			CreatedPathRegistry.register(contextKey, CreatedPathRegistry.PKTCREATOR_FILES_KEY, ridWork.toString());
			scenarioPaths.add(ridWork);

			Path serverCtx = resourceRoot.resolve("server.context." + contextKey + ".properties");
			Files.writeString(serverCtx, "urlBase=http://example");
			CreatedPathRegistry.register(contextKey, CreatedPathRegistry.CREATED_PATHS_KEY, serverCtx.toString());
			scenarioPaths.add(serverCtx);

			Path mdsProfile = resourceRoot.resolve("Profile").resolve("res" + s);
			Files.createDirectories(mdsProfile.resolve("Registration"));
			Files.write(mdsProfile.resolve("Registration").resolve("Face.iso"), new byte[64 * 1024]);
			CreatedPathRegistry.register(contextKey, CreatedPathRegistry.CREATED_PATHS_KEY, mdsProfile.toString());
			scenarioPaths.add(mdsProfile);

			perScenarioCreated.add(scenarioPaths);
		}

		gcQuiet();
		printMemory("after create 20000 packets", heapUsed(), os.getFreePhysicalMemorySize(), createdBytes,
				createdBytes);
		assertTrue(createdBytes > 1000L * 1024 * 1024,
				"expected ~1250 MB of created packet data before cleanup, was " + createdBytes);

		for (int s = 1; s <= SCENARIOS; s++) {
			String result = ContextUtils.clearPacketGenFolders("env_S" + s + "_context");
			assertTrue(result.contains("successfully"));
			for (Path path : perScenarioCreated.get(s - 1)) {
				assertFalse(Files.exists(path), "leftover after scenario " + s + ": " + path);
			}
			assertTrue(Files.exists(sharedPkt));
			assertTrue(Files.exists(keyFile));
			assertTrue(Files.exists(defaultKeep));
			assertTrue(Files.exists(staticProps));
			printMemory("after DSL scenario " + s + " delete", heapUsed(), os.getFreePhysicalMemorySize(), 0L,
					createdBytes);
		}

		assertTrue(ContextUtils.clearAllCreatedData().contains("successfully"));
		perScenarioCreated.clear();
		perScenarioCreated = null;
		assertFalse(Files.exists(sharedPkt));
		assertFalse(Files.exists(sharedPrereg));
		assertFalse(Files.exists(keyFile));
		assertTrue(Files.exists(defaultKeep));
		assertTrue(Files.exists(staticProps));
		gcQuiet();
		long leftoverSuite = leftoverBytes(CreatedPathRegistry.snapshot(CreatedPathRegistry.GLOBAL_CONTEXT,
				CreatedPathRegistry.CREATED_PATHS_KEY));
		long afterSuiteHeap = heapUsed();
		printMemory("after suite overall delete", afterSuiteHeap, os.getFreePhysicalMemorySize(), leftoverSuite,
				createdBytes);
		assertEquals(0L, leftoverSuite, "suite leftover created bytes must be 0");
		assertFalse(Files.exists(sharedPkt));
		assertFalse(Files.exists(sharedPrereg));
		assertFalse(Files.exists(keyFile));
		assertTrue(Files.exists(defaultKeep));
		assertTrue(Files.exists(staticProps));
		System.out.println("DSL MEMORY SUMMARY createdBytes=" + mb(createdBytes)
				+ " MB leftoverAfterSuite=" + leftoverSuite + " bytes heapDeltaVsBaseline="
				+ (afterSuiteHeap - baselineHeap) + " bytes remainingContextKeys="
				+ VariableManager.getContextKeys());
		assertTrue(VariableManager.getContextKeys().stream()
				.allMatch(VariableManager.NS_DEFAULT::equals),
				"only default namespace should remain, was " + VariableManager.getContextKeys());
	}

	@Test
	void should_deleteCreatedPersonaResourceFiles_keepDefaultAndStatic() throws Exception {
		suiteRoot = Path.of("target", "created-path-cleanup").toAbsolutePath();
		Path resourceRoot = suiteRoot.resolve("resource");
		forceDelete(suiteRoot);
		Files.createDirectories(resourceRoot);

		Path staticProps = resourceRoot.resolve("application.properties");
		Files.writeString(staticProps, "keep=true");
		Path defaultKeep = resourceRoot.resolve("Profile").resolve("Default").resolve("keep.txt");
		Files.createDirectories(defaultKeep.getParent());
		Files.writeString(defaultKeep, "static");

		String contextKey = "resource_S1_context";
		Path serverCtx = resourceRoot.resolve("server.context." + contextKey + ".properties");
		Files.writeString(serverCtx, "urlBase=http://example");
		CreatedPathRegistry.register(contextKey, CreatedPathRegistry.CREATED_PATHS_KEY, serverCtx.toString());

		Path keyFile = resourceRoot.resolve("privatekeys").resolve("api-internal.qaj21.10002.reg.key");
		Files.createDirectories(keyFile.getParent());
		Files.write(keyFile, new byte[] { 1, 2, 3 });
		CreatedPathRegistry.registerShared(keyFile.toString());

		Path mdsProfile = resourceRoot.resolve("Profile").resolve("res999");
		Files.createDirectories(mdsProfile.resolve("Registration"));
		Files.write(mdsProfile.resolve("Registration").resolve("Face.iso"), new byte[16]);
		CreatedPathRegistry.register(contextKey, CreatedPathRegistry.CREATED_PATHS_KEY, mdsProfile.toString());
		CreatedPathRegistry.register(contextKey, CreatedPathRegistry.CREATED_PATHS_KEY, defaultKeep.getParent().toString());
		CreatedPathRegistry.registerShared(resourceRoot.toString());
		CreatedPathRegistry.registerShared(resourceRoot.resolve("privatekeys").toString());
		CreatedPathRegistry.registerShared(resourceRoot.resolve("Profile").toString());

		assertTrue(ContextUtils.clearPacketGenFolders(contextKey).contains("successfully"));
		assertFalse(Files.exists(serverCtx));
		assertFalse(Files.exists(mdsProfile));
		assertTrue(Files.exists(keyFile));
		assertTrue(Files.exists(defaultKeep));
		assertTrue(Files.exists(staticProps));

		assertTrue(ContextUtils.clearAllCreatedData().contains("successfully"));
		assertFalse(Files.exists(keyFile));
		assertTrue(Files.exists(defaultKeep));
		assertTrue(Files.exists(staticProps));
	}

	private static void warmupCaches() throws Exception {
		VariableManager.Init();
		Path warmup = Path.of("target", "dsl-memory-warmup");
		forceDelete(warmup);
		Files.createDirectories(warmup);
		Files.write(warmup.resolve("packet_1.bin"), PACKET_PAYLOAD);
		CreatedPathRegistry.register("warmup_ctx", "packets_", warmup.toString());
		ContextUtils.clearAllCreatedData();
		forceDelete(warmup);
	}

	private static long leftoverBytes(Iterable<String> paths) throws IOException {
		long total = 0L;
		for (String path : paths) {
			Path existing = Path.of(path);
			if (!Files.exists(existing)) {
				continue;
			}
			if (Files.isRegularFile(existing)) {
				total += Files.size(existing);
				continue;
			}
			try (Stream<Path> walk = Files.walk(existing)) {
				total += walk.filter(Files::isRegularFile).mapToLong(file -> {
					try {
						return Files.size(file);
					} catch (IOException e) {
						return 0L;
					}
				}).sum();
			}
		}
		return total;
	}

	private static long heapUsed() {
		return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
	}

	private static void printMemory(String phase, long heapUsed, long freePhysical, long leftoverBytes,
			long createdBytes) {
		System.out.println("DSL MEMORY [" + phase + "] heapUsed=" + mb(heapUsed) + " MB freePhysical="
				+ mb(freePhysical) + " MB leftoverCreated=" + leftoverBytes + " bytes createdData="
				+ mb(createdBytes) + " MB");
	}

	private static long mb(long bytes) {
		return bytes / (1024L * 1024L);
	}

	private static void gcQuiet() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			System.gc();
			System.runFinalization();
			Thread.sleep(200);
		}
		System.gc();
		Thread.sleep(400);
	}

	private static void forceDelete(Path root) throws IOException {
		if (root == null || !Files.exists(root)) {
			return;
		}
		try (Stream<Path> paths = Files.walk(root)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ignored) {
				}
			});
		}
	}
}
