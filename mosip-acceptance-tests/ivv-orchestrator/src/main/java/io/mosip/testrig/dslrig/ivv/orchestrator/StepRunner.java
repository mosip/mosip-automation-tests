package io.mosip.testrig.dslrig.ivv.orchestrator;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

/**
 * Runs the post-parse step lifecycle for a scenario step instance.
 * Legacy steps implement {@link StepInterface#run()} directly; {@link io.mosip.testrig.dslrig.ivv.core.base.PipelineStep}
 * uses bind → prepare → execute → publish inside {@code run()}.
 */
public final class StepRunner {

    private StepRunner() {
    }

  /**
   * setup → validate → run → assert (HTTP status, error middleware, MOSIP body).
   */
    public static void runLifecycle(StepInterface step) throws RigInternalError, FeatureNotSupportedError {
        step.setup();
        step.validateStep();
        step.run();
        step.assertHttpStatus();
        if (step.hasError()) {
            return;
        }
        if (step.getErrorsForAssert().size() > 0) {
            step.errorHandler();
            if (step.hasError()) {
                return;
            }
        } else {
            step.assertNoError();
        }
    }

    public static boolean lifecycleFailed(StepInterface step) {
        return Boolean.TRUE.equals(step.hasError());
    }
}
