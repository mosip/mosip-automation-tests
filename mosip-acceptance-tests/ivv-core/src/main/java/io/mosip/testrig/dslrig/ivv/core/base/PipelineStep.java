package io.mosip.testrig.dslrig.ivv.core.base;

import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.core.pipeline.StepBinder;
import io.mosip.testrig.dslrig.ivv.core.pipeline.StepContext;
import io.mosip.testrig.dslrig.ivv.core.pipeline.StepOutputs;

/**
 * Template for the standard step pipeline:
 * <ol>
 *   <li>parse — Gherkin/JSON → {@link io.mosip.testrig.dslrig.ivv.core.dtos.Scenario.Step} (ivv-parser)</li>
 *   <li>bind — {@link #bind()}</li>
 *   <li>prepare — {@link #prepare()}</li>
 *   <li>execute — {@link #execute()}</li>
 *   <li>assert — orchestrator (HTTP status, error middleware, MOSIP body)</li>
 *   <li>publish — {@link #publishOutputs()}</li>
 * </ol>
 * MOSIP-specific steps should stay thin: bind parameters here, delegate domain work to helpers.
 */
public abstract class PipelineStep extends BaseStep implements StepInterface {

    protected StepContext context;

    @Override
    public final void run() throws RigInternalError, FeatureNotSupportedError {
        bind();
        prepare();
        execute();
        publishOutputs();
    }

    /** Resolve {@code $$} variables into {@link #context}. */
    protected void bind() throws RigInternalError {
        context = StepBinder.bind(step);
    }

    /** Build requests, load templates, validate parameters. */
    protected void prepare() throws RigInternalError {
    }

    /** Perform the action (HTTP, DB, packet utility, etc.). */
    protected abstract void execute() throws RigInternalError;

    /** Write {@link StepContext#getOutputs()} and {@code outVarName} into scenario variables. */
    protected void publishOutputs() {
        StepOutputs.publish(context, step);
    }
}
