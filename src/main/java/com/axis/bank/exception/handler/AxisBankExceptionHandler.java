package com.axis.bank.exception.handler;

import com.axis.bank.exception.AxisBankException;
import com.axis.bank.exception.model.ErrorInfo;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static com.axis.bank.utility.Constants.TRACE_ID;

@RestControllerAdvice
public class AxisBankExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AxisBankException.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> exceptionHandler(Exception exception) {
        logCompleteTrace(exception);
        ErrorInfo errorInfo = ErrorInfo.builder()
                .uuid(MDC.get(TRACE_ID)).errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage(exception.getMessage()).timeStamp(LocalDateTime.now()).build();
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(errorInfo.toString());
        }
        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AxisBankException.class)
    public ResponseEntity<ErrorInfo> axisBankExceptionHandler(AxisBankException axisBankException) {
        logCompleteTrace(axisBankException);
        ErrorInfo errorInfo = ErrorInfo.builder().uuid(MDC.get(TRACE_ID))
                .errorCode(axisBankException.getStatus().value())
                .errorMessage(axisBankException.getMessage()).timeStamp(LocalDateTime.now()).build();
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(errorInfo.toString());
        }
        return new ResponseEntity<>(errorInfo, axisBankException.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException methodArgumentNotValidException) {
        logCompleteTrace(methodArgumentNotValidException);
        BindingResult result = methodArgumentNotValidException.getBindingResult();
        List<String> errors = result.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setUuid(MDC.get(TRACE_ID));
        errorInfo.setErrorMessage(String.join(", ", errors)); // Combine multiple validation errors
        errorInfo.setErrorCode(HttpStatus.BAD_REQUEST.value());
        errorInfo.setTimeStamp(LocalDateTime.now());
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(errorInfo.toString());
        }
        return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorInfo> contraintViolationExceptionHandler(ConstraintViolationException constraintViolationException) {
        logCompleteTrace(constraintViolationException);
        List<String> errors = constraintViolationException.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setUuid(MDC.get(TRACE_ID));
        errorInfo.setErrorMessage(String.join(", ", errors)); // Proper message formatting
        errorInfo.setErrorCode(HttpStatus.BAD_REQUEST.value());
        errorInfo.setTimeStamp(LocalDateTime.now());
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(errorInfo.toString());
        }
        return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorInfo> accessDeniedExceptionHandler(AccessDeniedException accessDeniedException) {
        logCompleteTrace(accessDeniedException);
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setUuid(MDC.get(TRACE_ID));
        errorInfo.setErrorMessage(accessDeniedException.getMessage());
        errorInfo.setErrorCode(HttpStatus.FORBIDDEN.value());
        errorInfo.setTimeStamp(LocalDateTime.now());
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(errorInfo.toString());
        }
        return new ResponseEntity<>(errorInfo, HttpStatus.FORBIDDEN);
    }

    private void logCompleteTrace(Exception exception) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Stack Trace : {}", exception.getStackTrace());
            LOGGER.error("Error Message : {}", exception.getMessage());
        }
    }
}
