package io.mosip.testrig.dslrig.ivv.core.base;

import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

/**
 * @deprecated Use {@link PipelineStep} ({@code prepare()} / {@code execute()} / {@code publishOutputs()})
 *             or {@link io.mosip.testrig.dslrig.ivv.orchestrator.pipeline.HttpPipelineStep} for HTTP steps.
 */
@Deprecated
public interface StepAPITestInterface {
    void prepareRequest() throws RigInternalError;
    void processResponse() throws RigInternalError;
}
