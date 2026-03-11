package io.mosip.testrig.dslrig.packetcreator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.mosip.testrig.dslrig.dataprovider.util.ApiError;
import io.mosip.testrig.dslrig.dataprovider.util.ServiceException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiError> handleServiceException(
            ServiceException ex,
            HttpServletRequest request) {

        String path = ex.getApiUrl() != null
                ? ex.getApiUrl()
                : request.getRequestURI();

        ApiError err = new ApiError(
                ex.getStatus(),
                (String) ex.getMessage(),     
                (String) ex.getErrorCode(),
                (String) path
        );

        logger.error(
                "API FAILED | path={} | code={} | message={}",
                path,
                ex.getErrorCode(),
                ex.getMessage(),
                ex
        );

        return new ResponseEntity<>(err, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOtherExceptions(
            Exception ex,
            HttpServletRequest request) {

        ApiError err = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                "INTERNAL_ERROR",
                request.getRequestURI()
        );

        logger.error(
                "UNHANDLED EXCEPTION | path={} | message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
