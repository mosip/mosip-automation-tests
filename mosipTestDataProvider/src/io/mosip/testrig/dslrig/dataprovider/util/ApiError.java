package io.mosip.testrig.dslrig.dataprovider.util;

import org.springframework.http.HttpStatus;

public class ApiError {

    private String message;
    private String code;
    private String path;         

    public ApiError() {}

    /**
     * Constructor using errorKey (recommended).
     */
    public ApiError(HttpStatus httpStatus, String message, String code, String path) { 
        this.message = message;
        this.code = code;
        this.path = path;
    }


    /**
     * Full manual constructor (rarely needed).
     */
    public ApiError(int status, String error, String message, String code, String path, String timestamp) {
        this.message = message;
        this.code = code;
        this.path = path;
    }

    // ===== Getters / Setters =====

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

}
