package com.biobac.warehouse.exception;

import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return ResponseEntity.status(status).body(ResponseUtil.error(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage();

            field = field.replaceAll("\\[\\d+\\]", "[]");

            String friendlyField;
            if ("username".equals(field)) {
                friendlyField = "Username";
            } else if ("email".equals(field)) {
                friendlyField = "Email";
            } else if ("password".equals(field)) {
                friendlyField = "Password";
            } else if (field.startsWith("roles[].roleName")) {
                friendlyField = "Roles";
            } else if (field.startsWith("roles[].permissions")) {
                friendlyField = "Permissions";
            } else if (field.startsWith("roles[].permissions[].permissionName")) {
                friendlyField = "Permissions";
            } else {
                friendlyField = field;
            }

            errors.merge(friendlyField, message, (existing, newMsg) -> existing + "; " + newMsg);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtil.error("Validation failed", errors));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseUtil.error(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateException(DuplicateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseUtil.error(ex.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(ex.getMessage()));
    }

    @ExceptionHandler(NotEnoughException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotEnoughException(NotEnoughException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidDataException(InvalidDataException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(ex.getMessage()));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleExternalServiceException(ExternalServiceException ex) {
        return ResponseEntity.status((HttpStatus.INTERNAL_SERVER_ERROR)).body(ResponseUtil.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtil.error("Internal server error"));
    }
}
