package com.example.demo.exception;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  private final Environment env;

  public GlobalExceptionHandler(Environment env) {
    this.env = env;
  }

  // Handle generic exceptions
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex);
  }

  // Handle resource not found (e.g., booking not found)
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
  }

  // Handle validation errors (e.g., missing fields in request body)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    Map<String, Object> response = new HashMap<>();
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation Failed");
    response.put("details", errors);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Build JSON error response
  private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, Exception ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("status", status.value());
    response.put("error", status.getReasonPhrase());
    response.put("message", message);

    // Include stack trace only in development mode
    if (isDevelopmentMode()) {
      response.put("stackTrace", ex.getStackTrace());
    }

    return new ResponseEntity<>(response, status);
  }

  // Check if the application is in development mode
  private boolean isDevelopmentMode() {
    String activeProfile = env.getProperty("spring.profiles.active", "production");
    return "development".equals(activeProfile);
  }
}

