package io.mosip.testrig.dslrig.ivv.core.base;

import io.mosip.testrig.dslrig.ivv.core.exceptions.RigInternalError;

public interface StepAPITestInterface {
    void prepareRequest() throws RigInternalError;
    void processResponse() throws RigInternalError;
}
