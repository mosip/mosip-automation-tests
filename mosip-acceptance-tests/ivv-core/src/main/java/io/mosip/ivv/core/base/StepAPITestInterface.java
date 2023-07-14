package io.mosip.ivv.core.base;

import io.mosip.ivv.core.exceptions.RigInternalError;

public interface StepAPITestInterface {
    void prepareRequest() throws RigInternalError;
    void processResponse() throws RigInternalError;
}
