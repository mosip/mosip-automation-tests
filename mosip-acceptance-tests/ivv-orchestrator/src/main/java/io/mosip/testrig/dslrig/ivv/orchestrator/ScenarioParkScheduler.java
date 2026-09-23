package io.mosip.testrig.dslrig.ivv.orchestrator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.testng.Reporter;

/**
 * Fixed worker pool that parks scenarios during long Wait steps and resumes the same
 * {@link ScenarioExecutionState} when the wait expires — freeing workers for other scenarios.
 */
public final class ScenarioParkScheduler {

	private static final Logger logger = Logger.getLogger(ScenarioParkScheduler.class);

	private static final AtomicInteger WORKER_SEQ = new AtomicInteger();
	private static final ThreadLocal<Boolean> ON_WORKER = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private static volatile ScenarioParkScheduler instance;

	private final LinkedBlockingQueue<ScenarioExecutionState> readyQueue = new LinkedBlockingQueue<>();
	private final DelayQueue<ScenarioExecutionState> parkQueue = new DelayQueue<>();
	private final ExecutorService workers;
	private final Thread unparker;
	private final AtomicBoolean shutdown = new AtomicBoolean(false);
	private final ScenarioStepExecutor.StepFactory stepFactory;

	private ScenarioParkScheduler(int workerCount, ScenarioStepExecutor.StepFactory stepFactory) {
		this.stepFactory = stepFactory;
		int n = Math.max(1, workerCount);
		ThreadFactory tf = r -> {
			Thread t = new Thread(r, "scenario-park-worker-" + WORKER_SEQ.incrementAndGet());
			t.setDaemon(true);
			return t;
		};
		this.workers = Executors.newFixedThreadPool(n, tf);
		for (int i = 0; i < n; i++) {
			workers.submit(this::workerLoop);
		}
		this.unparker = new Thread(this::unparkLoop, "scenario-park-unparker");
		this.unparker.setDaemon(true);
		this.unparker.start();
		logger.info("ScenarioParkScheduler started with " + n + " workers");
	}

	public static synchronized ScenarioParkScheduler init(int workerCount,
			ScenarioStepExecutor.StepFactory stepFactory) {
		if (instance == null) {
			instance = new ScenarioParkScheduler(workerCount, stepFactory);
		}
		return instance;
	}

	public static ScenarioParkScheduler get() {
		ScenarioParkScheduler s = instance;
		if (s == null) {
			throw new IllegalStateException("ScenarioParkScheduler not initialized");
		}
		return s;
	}

	public static boolean isInitialized() {
		return instance != null;
	}

	/** True when the current thread is a park-scheduler worker (safe to yield). */
	public static boolean isRunningOnWorker() {
		return Boolean.TRUE.equals(ON_WORKER.get());
	}

	public CompletableFuture<Void> execute(ScenarioExecutionState state) {
		CompletableFuture<Void> future = new CompletableFuture<>();
		state.setCompletion(future);
		readyQueue.offer(state);
		return future;
	}

	public synchronized void shutdown() {
		if (!shutdown.compareAndSet(false, true)) {
			return;
		}
		workers.shutdownNow();
		unparker.interrupt();
		instance = null;
		logger.info("ScenarioParkScheduler shut down");
	}

	private void workerLoop() {
		ON_WORKER.set(Boolean.TRUE);
		try {
			while (!shutdown.get() && !Thread.currentThread().isInterrupted()) {
				ScenarioExecutionState state;
				try {
					state = readyQueue.poll(500, TimeUnit.MILLISECONDS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
				if (state == null) {
					continue;
				}
				bindReporter(state);
				String scenarioId = state.getScenario() != null ? state.getScenario().getId() : "?";
				if (state.getStepIndex() > 0) {
					String resumeMsg = "Resuming scenario " + scenarioId + " at step index " + state.getStepIndex();
					logger.info(resumeMsg);
					if (state.getExtentTest() != null) {
						state.getExtentTest().info(resumeMsg);
					}
					Reporter.log(resumeMsg);
				}
				try {
					ScenarioStepExecutor.Outcome outcome = ScenarioStepExecutor.runUntilYieldOrDone(state,
							stepFactory);
					if (outcome == ScenarioStepExecutor.Outcome.YIELD) {
						parkQueue.offer(state);
					} else {
						state.getCompletion().complete(null);
					}
				} catch (Throwable t) {
					logger.error("Scenario " + scenarioId + " failed on park worker: " + t.getMessage(), t);
					state.getCompletion().completeExceptionally(t);
				}
			}
		} finally {
			ON_WORKER.remove();
		}
	}

	private void unparkLoop() {
		while (!shutdown.get() && !Thread.currentThread().isInterrupted()) {
			try {
				ScenarioExecutionState state = parkQueue.poll(500, TimeUnit.MILLISECONDS);
				if (state == null) {
					continue;
				}
				readyQueue.offer(state);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	private static void bindReporter(ScenarioExecutionState state) {
		if (state.getTestResult() != null) {
			Reporter.setCurrentTestResult(state.getTestResult());
		}
	}
}
