package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import io.mosip.testrig.dslrig.ivv.core.dtos.Scenario;
import io.mosip.testrig.dslrig.ivv.core.dtos.Store;

/**
 * Runs Scenario 0 (before-suite setup) in dependency-safe phases so independent
 * user/center/machine tracks execute concurrently.
 */
public final class Scenario0ParallelRunner {

	private static final Logger logger = Logger.getLogger(Scenario0ParallelRunner.class);

	/** User/center/machine track 1 through WritePreReq(1). */
	private static final int TRACK1_FROM = 2;
	private static final int TRACK1_TO = 13;
	/** User2 + center2 only (steps 16-18 run later after track1 WritePreReq). */
	private static final int TRACK2A_FROM = 14;
	private static final int TRACK2A_TO = 15;
	/** Machine + zone setup for track 2 (needs $$center2 from track2a). */
	private static final int TRACK2B_FROM = 19;
	private static final int TRACK2B_TO = 28;
	/** User3 + center3 (independent of track2; original order runs after step 29). */
	private static final int TRACK3A_FROM = 30;
	private static final int TRACK3A_TO = 31;
	/** Machine + zone setup for track 3 (needs $$center3 from track3a). */
	private static final int TRACK3B_FROM = 32;
	private static final int TRACK3B_TO = 41;

	private static final int PHASE2_FROM = 16;
	private static final int PHASE2_TO = 17;
	private static final int WAIT_STEP = 18;
	private static final int SET_CONTEXT_DETAILS2 = 29;
	private static final int SET_CONTEXT_DETAILS3 = 42;
	private static final int PHASE_FINAL_FROM = 43;
	private static final int PHASE_FINAL_TO = 48;

	private Scenario0ParallelRunner() {
	}

	@FunctionalInterface
	public interface StepRangeExecutor {
		Store execute(Scenario scenario, Store store, int fromStepIndex, int toStepIndex, boolean willRetry)
				throws Exception;
	}

	@FunctionalInterface
	public interface ScenarioScenarioCopier {
		Scenario copyForTrack(Scenario source);
	}

	public static Store run(Scenario masterScenario, Store initialStore, boolean willRetry,
			StepRangeExecutor stepRangeExecutor, ScenarioScenarioCopier scenarioCopier) throws Exception {

		int poolSize = Math.min(3, Math.max(1, Integer.parseInt(dslConfigManager.getThreadCount())));
		ExecutorService executor = Executors.newFixedThreadPool(poolSize);
		Store store = initialStore;

		try {
			logger.info("Scenario 0 parallel setup: running steps 0-1 sequentially");
			store = stepRangeExecutor.execute(masterScenario, store, 0, 1, willRetry);

			logger.info("Scenario 0 parallel setup: phase 1 — tracks 1 and 2a in parallel");
			List<CompletableFuture<Map<String, String>>> phase1Futures = new ArrayList<>();
			phase1Futures.add(runTrack(executor, "track1", masterScenario, TRACK1_FROM, TRACK1_TO, store, willRetry,
					stepRangeExecutor, scenarioCopier));
			phase1Futures.add(runTrack(executor, "track2a", masterScenario, TRACK2A_FROM, TRACK2A_TO, store, willRetry,
					stepRangeExecutor, scenarioCopier));
			for (CompletableFuture<Map<String, String>> future : phase1Futures) {
				mergeVariables(masterScenario, future.join());
			}

			logger.info("Scenario 0 parallel setup: phase 1b — track 3a sequential (user3 + center3; avoids masterdata API races)");
			store = stepRangeExecutor.execute(masterScenario, store, TRACK3A_FROM, TRACK3A_TO, willRetry);

			logger.info("Scenario 0 parallel setup: phase 2 — ReadPreReq(1) and setContext(details1)");
			store = stepRangeExecutor.execute(masterScenario, store, PHASE2_FROM, PHASE2_TO, willRetry);

			logger.info("Scenario 0 parallel setup: phase 3 — track2b in parallel with wait(60)");
			final Store storeForWait = store;
			AtomicReference<Exception> waitError = new AtomicReference<>();
			CompletableFuture<Void> waitFuture = CompletableFuture.runAsync(() -> {
				try {
					stepRangeExecutor.execute(masterScenario, storeForWait, WAIT_STEP, WAIT_STEP, willRetry);
				} catch (Exception e) {
					waitError.set(e);
				}
			}, executor);

			Map<String, String> track2bVars = runTrack(executor, "track2b", masterScenario, TRACK2B_FROM, TRACK2B_TO,
					store, willRetry, stepRangeExecutor, scenarioCopier).join();
			mergeVariables(masterScenario, track2bVars);

			waitFuture.join();
			if (waitError.get() != null) {
				throw waitError.get();
			}

			logger.info("Scenario 0 parallel setup: phase 3b — track3b on main thread (steps 32-41)");
			store = stepRangeExecutor.execute(masterScenario, store, TRACK3B_FROM, TRACK3B_TO, willRetry);

			logger.info("Scenario 0 parallel setup: phase 4 — setContext for details2 and details3");
			store = stepRangeExecutor.execute(masterScenario, store, SET_CONTEXT_DETAILS2, SET_CONTEXT_DETAILS2,
					willRetry);
			store = stepRangeExecutor.execute(masterScenario, store, SET_CONTEXT_DETAILS3, SET_CONTEXT_DETAILS3,
					willRetry);

			logger.info("Scenario 0 parallel setup: phase 5 — global cert and cleanup steps");
			store = stepRangeExecutor.execute(masterScenario, store, PHASE_FINAL_FROM, PHASE_FINAL_TO, willRetry);

			return store;
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	private static CompletableFuture<Map<String, String>> runTrack(ExecutorService executor, String trackName,
			Scenario masterScenario, int fromStep, int toStep, Store baseStore, boolean willRetry,
			StepRangeExecutor stepRangeExecutor, ScenarioScenarioCopier scenarioCopier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Scenario trackScenario = scenarioCopier.copyForTrack(masterScenario);
				Store trackStore = cloneStore(baseStore);
				stepRangeExecutor.execute(trackScenario, trackStore, fromStep, toStep, willRetry);
				logger.info("Scenario 0 parallel setup: " + trackName + " completed (steps " + fromStep + "-" + toStep
						+ ")");
				return new HashMap<>(trackScenario.getVariables());
			} catch (Exception e) {
				Throwable root = e;
				while (root.getCause() != null) {
					root = root.getCause();
				}
				throw new RuntimeException(trackName + " failed (steps " + fromStep + "-" + toStep + "): "
						+ root.getMessage(), e);
			}
		}, executor);
	}

	private static void mergeVariables(Scenario master, Map<String, String> trackVariables) {
		synchronized (master.getVariables()) {
			master.getVariables().putAll(trackVariables);
		}
	}

	private static Store cloneStore(Store source) {
		Store copy = new Store();
		copy.setConfigs(source.getConfigs());
		copy.setGlobals(source.getGlobals());
		copy.setPersona(source.getPersona());
		copy.setRegistrationUsers(source.getRegistrationUsers());
		copy.setPartners(source.getPartners());
		copy.setProperties(source.getProperties());
		return copy;
	}
}
