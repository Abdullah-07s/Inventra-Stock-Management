package com.inventory.wareflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handler for the whole application. Without this,
 * 
 * @Valid failures on DTOs and custom auth/authorization exceptions would
 *        surface as bare 403s or empty response bodies - a documented gotcha
 *        from
 *        earlier projects in this portfolio. Every handler here returns a
 *        consistent JSON shape: { timestamp, status, error, message } or, for
 *        validation failures, { timestamp, status, error, fieldErrors: {...} }.
 */
@RestControllerAdvice
// @RestControllerAdvice applies these @ExceptionHandler methods globally,
// across every @RestController in the application, and serializes return
// values as JSON automatically (combines @ControllerAdvice + @ResponseBody).
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Triggered when a @Valid-annotated DTO fails Bean Validation (@NotBlank,
    // @Email, etc.)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AuthException.class)
    // Custom exception for auth failures (bad credentials, duplicate
    // username/email, etc.)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        return buildErrorResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    // Custom exception for authorization failures (valid user, but lacks the
    // required activity/role).
    public ResponseEntity<Map<String, Object>> handleForbiddenException(ForbiddenException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    // Custom exception for lookups against a nonexistent entity (bad ID in a path
    // variable, etc.)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    // Catch-all fallback - anything unhandled becomes a 500 with a generic
    // message rather than leaking a stack trace to the client.
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}