package io.mosip.ivv.core.exceptions;

public class FeatureNotSupportedError extends Exception {
    public FeatureNotSupportedError(String errorMessage) {
        super(errorMessage);
    }
}
