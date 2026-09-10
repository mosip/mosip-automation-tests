package io.mosip.testrig.dslrig.ivv.core.exceptions;

public class RigInternalError extends Exception {
    public RigInternalError(String errorMessage) {
        super(errorMessage);
    }

    public RigInternalError(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
