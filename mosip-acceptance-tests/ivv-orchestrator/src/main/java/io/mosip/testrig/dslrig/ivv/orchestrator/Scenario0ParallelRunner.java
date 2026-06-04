package io.mosip.testrig.dslrig.ivv.orchestrator;



import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.Properties;

import java.util.concurrent.CompletableFuture;

import java.util.concurrent.CompletionException;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import java.util.concurrent.TimeUnit;



import org.apache.log4j.Logger;



import io.mosip.testrig.dslrig.ivv.core.dtos.Persona;

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

	private static final int TRACK1_TO = 12;

	/** User2 + center2 only (ReadPreReq/setContext run later after track1 WritePreReq). */

	private static final int TRACK2A_FROM = 13;

	private static final int TRACK2A_TO = 14;

	/** Machine + zone setup for track 2 (needs $$center2 from track2a). */

	private static final int TRACK2B_FROM = 17;

	private static final int TRACK2B_TO = 25;

	/** User3 + center3 (independent of track2; original order runs after step 29). */

	private static final int TRACK3A_FROM = 27;

	private static final int TRACK3A_TO = 28;

	/** Machine + zone setup for track 3 (needs $$center3 from track3a). */

	private static final int TRACK3B_FROM = 29;

	private static final int TRACK3B_TO = 37;



	private static final int PHASE2_FROM = 15;

	private static final int PHASE2_TO = 16;

	/** setContext($$details2) — must run immediately after track2 WritePreReq, before track3 overwrites shared keys. */
	private static final int SET_CONTEXT_DETAILS2 = 26;

	/** setContext($$details3) — must run immediately after track3 WritePreReq. */
	private static final int SET_CONTEXT_DETAILS3 = 38;

	/** External-packet user and WritePreReq(4). */

	private static final int PHASE_FINAL_SETUP_FROM = 39;

	private static final int PHASE_FINAL_SETUP_TO = 40;

	private static final int STEP_CLEAR_DEVICE_CERT_CACHE = 41;

	private static final int STEP_GENERATE_AUTH_CERTS = 42;

	private static final int STEP_UPLOAD_DEVICE_CERT = 43;

	private static final int STEP_WARM_RUN_CACHE = 44;



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



			logger.info("Scenario 0 parallel setup: phase 1 - tracks 1 and 2a in parallel");

			List<CompletableFuture<Map<String, String>>> phase1Futures = new ArrayList<>();

			phase1Futures.add(runTrack(executor, "track1", masterScenario, TRACK1_FROM, TRACK1_TO, store, willRetry,

					stepRangeExecutor, scenarioCopier));

			phase1Futures.add(runTrack(executor, "track2a", masterScenario, TRACK2A_FROM, TRACK2A_TO, store, willRetry,

					stepRangeExecutor, scenarioCopier));

			for (CompletableFuture<Map<String, String>> future : phase1Futures) {

				mergeVariables(masterScenario, future.join());

			}



			logger.info(

					"Scenario 0 parallel setup: phase 1b - track 3a sequential (user3 + center3; avoids masterdata API races)");

			store = stepRangeExecutor.execute(masterScenario, store, TRACK3A_FROM, TRACK3A_TO, willRetry);



			logger.info("Scenario 0 parallel setup: phase 2 - ReadPreReq(1) and setContext(details1)");

			store = stepRangeExecutor.execute(masterScenario, store, PHASE2_FROM, PHASE2_TO, willRetry);



			logger.info("Scenario 0 parallel setup: phase 3 - track2b (sequential; must finish before track3b)");

			Map<String, String> track2bVars = runTrack(executor, "track2b", masterScenario, TRACK2B_FROM, TRACK2B_TO,

					store, willRetry, stepRangeExecutor, scenarioCopier).join();

			mergeVariables(masterScenario, track2bVars);

			logger.info("Scenario 0 parallel setup: setContext($$details2) after track2b (step " + SET_CONTEXT_DETAILS2
					+ "; before track3 overwrites machineid/appendedkey)");

			store = stepRangeExecutor.execute(masterScenario, store, SET_CONTEXT_DETAILS2, SET_CONTEXT_DETAILS2,
					willRetry);

			logger.info("Scenario 0 parallel setup: phase 3b - track3b on main thread (steps 29-37)");

			store = stepRangeExecutor.execute(masterScenario, store, TRACK3B_FROM, TRACK3B_TO, willRetry);

			logger.info("Scenario 0 parallel setup: setContext($$details3) after track3b (step " + SET_CONTEXT_DETAILS3
					+ ")");

			store = stepRangeExecutor.execute(masterScenario, store, SET_CONTEXT_DETAILS3, SET_CONTEXT_DETAILS3,
					willRetry);

			final Store storeForWarmRunCache = store;
			CompletableFuture<Void> warmFuture = CompletableFuture.runAsync(() -> {
				try {
					Scenario warmScenario = scenarioCopier.copyForTrack(masterScenario);
					Store warmStore = cloneStore(storeForWarmRunCache);
					stepRangeExecutor.execute(warmScenario, warmStore, STEP_WARM_RUN_CACHE, STEP_WARM_RUN_CACHE,
							willRetry);
					logger.info("Scenario 0 parallel setup: WarmRunCache completed (step " + STEP_WARM_RUN_CACHE + ")");
				} catch (Exception e) {
					throw new RuntimeException("WarmRunCache failed (step " + STEP_WARM_RUN_CACHE + "): "
							+ rootCauseMessage(e), e);
				}
			}, executor);

			logger.info(
					"Scenario 0 parallel setup: phase 5 - external user, WritePreReq(4), clear (WarmRunCache already running)");

			store = stepRangeExecutor.execute(masterScenario, store, PHASE_FINAL_SETUP_FROM, PHASE_FINAL_SETUP_TO,
					willRetry);

			store = stepRangeExecutor.execute(masterScenario, store, STEP_CLEAR_DEVICE_CERT_CACHE,
					STEP_CLEAR_DEVICE_CERT_CACHE, willRetry);

			logger.info(
					"Scenario 0 parallel setup: phase 5b - GenerateAuthCertifcates and WarmRunCache in parallel (upload after auth)");

			final Store storeForAuthAndWarm = store;
			CompletableFuture<Void> authCertsFuture = CompletableFuture.runAsync(() -> {
				try {
					Scenario authScenario = scenarioCopier.copyForTrack(masterScenario);
					Store authStore = cloneStore(storeForAuthAndWarm);
					stepRangeExecutor.execute(authScenario, authStore, STEP_GENERATE_AUTH_CERTS, STEP_GENERATE_AUTH_CERTS,
							willRetry);
					logger.info("Scenario 0 parallel setup: GenerateAuthCertifcates completed (step "
							+ STEP_GENERATE_AUTH_CERTS + ")");
				} catch (Exception e) {
					throw new RuntimeException("GenerateAuthCertifcates failed (step " + STEP_GENERATE_AUTH_CERTS + "): "
							+ rootCauseMessage(e), e);
				}
			}, executor);

			joinAsyncStep(authCertsFuture, "GenerateAuthCertifcates");

			store = stepRangeExecutor.execute(masterScenario, store, STEP_UPLOAD_DEVICE_CERT, STEP_UPLOAD_DEVICE_CERT,
					willRetry);

			joinAsyncStep(warmFuture, "WarmRunCache");



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



	private static String rootCauseMessage(Exception e) {
		Throwable root = e;
		while (root.getCause() != null) {
			root = root.getCause();
		}
		return root.getMessage();
	}



	private static void joinAsyncStep(CompletableFuture<Void> future, String stepLabel) {
		try {
			future.join();
		} catch (CompletionException e) {
			Throwable root = e.getCause() != null ? e.getCause() : e;
			if (root instanceof RuntimeException) {
				throw (RuntimeException) root;
			}
			throw new RuntimeException(stepLabel + " failed: " + root.getMessage(), root);
		}
	}



	private static void mergeVariables(Scenario master, Map<String, String> trackVariables) {

		synchronized (master.getVariables()) {

			master.getVariables().putAll(trackVariables);

		}

	}



	private static Store cloneStore(Store source) {

		Store copy = new Store();

		if (source.getConfigs() != null) {
			copy.setConfigs(new HashMap<>(source.getConfigs()));
		}
		if (source.getGlobals() != null) {
			copy.setGlobals(new HashMap<>(source.getGlobals()));
		}
		if (source.getProperties() != null) {
			copy.setProperties((Properties) source.getProperties().clone());
		}
		if (source.getPersona() != null) {
			Persona srcPersona = source.getPersona();
			Persona personaCopy = new Persona();
			personaCopy.setId(srcPersona.getId());
			personaCopy.setGroupName(srcPersona.getGroupName());
			personaCopy.setPersonaClass(srcPersona.getPersonaClass());
			if (srcPersona.getPersons() != null) {
				personaCopy.setPersons(new ArrayList<>(srcPersona.getPersons()));
			}
			copy.setPersona(personaCopy);
		}
		if (source.getRegistrationUsers() != null) {
			copy.setRegistrationUsers(new ArrayList<>(source.getRegistrationUsers()));
		}
		if (source.getPartners() != null) {
			copy.setPartners(new ArrayList<>(source.getPartners()));
		}
		if (source.getHttpData() != null) {
			Store.HTTPDataObject httpCopy = copy.new HTTPDataObject();
			httpCopy.setCookie(source.getHttpData().getCookie());
			copy.setHttpData(httpCopy);
		}
		copy.setRegApplicationContext(source.getRegApplicationContext());
		copy.setRegLocalContext(source.getRegLocalContext());
		copy.setRegistrationDto(source.getRegistrationDto());
		copy.setCurrentPerson(source.getCurrentPerson());
		copy.setCurrentIntroducer(source.getCurrentIntroducer());
		copy.setCurrentRegistrationUSer(source.getCurrentRegistrationUSer());
		copy.setCurrentPartner(source.getCurrentPartner());
		return copy;

	}

}

