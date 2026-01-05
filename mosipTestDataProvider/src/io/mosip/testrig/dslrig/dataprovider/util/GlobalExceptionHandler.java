//package io.mosip.testrig.dslrig.dataprovider.util;
//
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.http.ResponseEntity;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//	@ExceptionHandler(ServiceException.class)
//	public ResponseEntity<ApiError> handleServiceException(ServiceException ex) {
//	    ApiError err = new ApiError(ex.getStatus(), ex.getMessage(), ex.getErrorCode());
//	    return new ResponseEntity<>(err, ex.getStatus());
//	}
//
//
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> handleOther(Exception ex) {
//        ApiError err = new ApiError(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
//                                    ex.getMessage(), "UNEXPECTED_ERROR");
//        return new ResponseEntity<>(err, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}
