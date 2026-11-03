package io.mosip.testrig.dslrig.ivv.orchestrator;

/**
 * Thrown by Wait steps when park/resume is enabled so the worker can free itself
 * and resume the same {@link ScenarioExecutionState} after {@code waitTimeMs}.
 * Polling steps use {@link #repeatStep(long)} to resume by re-running the same step.
 */
public class ScenarioYieldException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final long waitTimeMs;
	private final boolean repeatStep;

	public ScenarioYieldException(long waitTimeMs) {
		this(waitTimeMs, false);
	}

	private ScenarioYieldException(long waitTimeMs, boolean repeatStep) {
		super("Yield for wait of " + waitTimeMs + " ms");
		this.waitTimeMs = waitTimeMs;
		this.repeatStep = repeatStep;
	}

	public static ScenarioYieldException repeatStep(long waitTimeMs) {
		return new ScenarioYieldException(waitTimeMs, true);
	}

	public long getWaitTimeMs() {
		return waitTimeMs;
	}

	public boolean isRepeatStep() {
		return repeatStep;
	}
}
