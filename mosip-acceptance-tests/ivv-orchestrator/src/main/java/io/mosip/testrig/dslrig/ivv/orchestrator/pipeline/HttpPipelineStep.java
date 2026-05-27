package io.mosip.testrig.dslrig.ivv.orchestrator.pipeline;

import io.mosip.testrig.dslrig.ivv.core.base.StepInterface;
import io.mosip.testrig.dslrig.ivv.core.dtos.CallRecord;
import io.mosip.testrig.dslrig.ivv.core.dtos.RequestDataDTO;
import io.mosip.testrig.dslrig.ivv.core.dtos.ResponseDataDTO;
import io.mosip.testrig.dslrig.ivv.core.exceptions.FeatureNotSupportedError;
import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;
import io.mosip.testrig.dslrig.ivv.core.pipeline.StepBinder;
import io.mosip.testrig.dslrig.ivv.core.pipeline.StepContext;
import io.mosip.testrig.dslrig.ivv.core.pipeline.StepOutputs;
import io.mosip.testrig.dslrig.ivv.orchestrator.BaseTestCaseUtil;
import io.restassured.response.Response;

/**
 * HTTP-oriented pipeline for steps that need {@link BaseTestCaseUtil} helpers.
 * Subclasses implement {@link #prepareRequest()}, {@link #buildRequest()},
 * {@link #executeHttp(RequestDataDTO)}, and {@link #processResponse(ResponseDataDTO)}.
 */
public abstract class HttpPipelineStep extends BaseTestCaseUtil implements StepInterface {

    protected StepContext context;

    @Override
    public final void run() throws RigInternalError, FeatureNotSupportedError {
        context = StepBinder.bind(step, store);
        prepareRequest();
        RequestDataDTO request = buildRequest();
        if (request != null) {
            Response response = executeHttp(request);
            if (response != null) {
                callRecord = new CallRecord(request.getUrl(), "HTTP", request.getRequest(), response);
                processResponse(new ResponseDataDTO(response.getStatusCode(), response.getBody().asString(), null));
            }
        }
        StepOutputs.publish(context, step);
    }

    protected RequestDataDTO buildRequest() throws RigInternalError {
        return null;
    }

    protected abstract void prepareRequest() throws RigInternalError;

    protected abstract Response executeHttp(RequestDataDTO request) throws RigInternalError;

    protected abstract void processResponse(ResponseDataDTO response) throws RigInternalError;
}
