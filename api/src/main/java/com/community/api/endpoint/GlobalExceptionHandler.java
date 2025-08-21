package com.community.api.endpoint;

import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingService;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.ContentTooLongException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.social.NotAuthorizedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)

public class GlobalExceptionHandler {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    public static ResponseEntity<ErrorResponse> generateErrorResponse(String message, HttpStatus status, String trace) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setStatus_code(status.value());
        errorResponse.setStatus(status);
        errorResponse.setTrace(trace);
        return new ResponseEntity<>(errorResponse, status);
    }


    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<Object> handleNotAuthorizedException(NotAuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", "ERROR",
                "status_code", 403,
                "message", ex.getMessage()
        ));
    }


    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundRequests(Exception ex, WebRequest request) {
        return generateErrorResponse("Invalid request method", HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        // Collect all violation messages
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        ex.getConstraintViolations().forEach(violation -> {
            errorMessage.append(violation.getPropertyPath())
                    .append(": ")
                    .append(violation.getMessage())
                    .append("; ");
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("message", errorMessage.toString()));
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageConversionException(HttpMessageConversionException ex, WebRequest request) {
        return generateErrorResponse("Invalid type for ", HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        return generateErrorResponse("Invalid request body", HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    public ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Internal Server Error");
        errorResponse.setStatus_code(status.value());
        errorResponse.setStatus(status);
        return new ResponseEntity<>(errorResponse, headers, status);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errorMessages = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            errorMessages.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        });
        return generateErrorResponse("Invalid request parameters: " + errorMessages, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = {BindException.class})
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        return generateErrorResponse("Invalid request body", HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = {HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, WebRequest request) {
        String message = "Unsupported media type: " + ex.getContentType();
        System.out.println(ex.getMessage());
        return generateErrorResponse(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    @ExceptionHandler(value = {NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex, WebRequest request) {
        return generateErrorResponse("Null pointer exception", HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return generateErrorResponse("Invalid argument", HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = {ContentTooLongException.class})
    public ResponseEntity<ErrorResponse> handleContentTooLongException(ContentTooLongException ex, WebRequest request) {
        return generateErrorResponse("Content is TooLongE", HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        exceptionHandlingService.handleException(ex);
        return generateErrorResponse("Runtime exception : ", HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex, WebRequest request) {
        String message = "Missing required parameter: " + ex.getParameterName();
        return generateErrorResponse(message, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldName = "";
            if (!ife.getPath().isEmpty()) {
                fieldName = ife.getPath().get(0).getFieldName(); // Get the field name causing the issue
            }
            String message = "Invalid format for field: " + fieldName;
            return generateErrorResponse(message, HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        return generateErrorResponse("Malformed JSON request", HttpStatus.BAD_REQUEST, ex.getMessage());
    }


}

@Getter
@Setter
class ErrorResponse {
    private String message;
    private int status_code;
    private HttpStatus status;
    private String trace;

}