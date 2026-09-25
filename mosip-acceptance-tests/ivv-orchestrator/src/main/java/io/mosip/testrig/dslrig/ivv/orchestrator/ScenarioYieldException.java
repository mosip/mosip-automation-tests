package io.mosip.testrig.dslrig.ivv.orchestrator;

/**
 * Thrown by Wait steps when park/resume is enabled so the worker can free itself
 * and resume the same {@link ScenarioExecutionState} after {@code waitTimeMs}.
 */
public class ScenarioYieldException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final long waitTimeMs;

	public ScenarioYieldException(long waitTimeMs) {
		super("Yield for wait of " + waitTimeMs + " ms");
		this.waitTimeMs = waitTimeMs;
	}

	public long getWaitTimeMs() {
		return waitTimeMs;
	}
}
