package io.mosip.testrig.dslrig.dataprovider.util;

import org.springframework.http.HttpStatus;
import io.mosip.testrig.apirig.utils.ErrorCodes;

/**
 * Application-level runtime exception.
 * - Internal errors use ErrorCodes
 * - External (MOSIP) errors use raw message + code
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String errorCode;
    private final String apiUrl;

    public ServiceException(HttpStatus status, String errorKey) {
        super(ErrorCodes.message(errorKey));
        this.status = status;
        this.errorCode = ErrorCodes.code(errorKey);
        this.apiUrl = null;
    }

    public ServiceException(HttpStatus status, String errorKey, Object... args) {
        super(ErrorCodes.message(errorKey, args));
        this.status = status;
        this.errorCode = ErrorCodes.code(errorKey);
        this.apiUrl = null;
    }

    public ServiceException(HttpStatus status, String errorKey, String apiUrl, Object... args) {
        super(ErrorCodes.message(errorKey, args));
        this.status = status;
        this.errorCode = ErrorCodes.code(errorKey);
        this.apiUrl = apiUrl;
    }

    public ServiceException(HttpStatus status, String errorKey, String apiUrl, Throwable cause, Object... args) {
        super(ErrorCodes.message(errorKey, args), cause);
        this.status = status;
        this.errorCode = ErrorCodes.code(errorKey);
        this.apiUrl = apiUrl;
    }

    public ServiceException(HttpStatus status, String code, String apiUrl) {
        this.status = status;
        this.errorCode = code;
        this.apiUrl = apiUrl;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
