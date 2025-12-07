package com.senibo.eventservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import com.senibo.eventservice.dto.ApiErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the User Service.
 * Catches and handles exceptions across all controllers, returning standardized error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles resource not found exceptions.
   * Returns 404 NOT FOUND status.
   */
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ApiErrorResponse> handleNotFoundException(
      NotFoundException ex,
      WebRequest request) {
    log.error("Resource not found: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handles validation errors from @Valid annotations.
   * Returns 400 BAD REQUEST status with detailed field errors.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    log.error("Validation failed: {}", ex.getMessage());

    // Extract field error messages
    List<String> errors = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .map(error -> {
          String fieldName = ((FieldError) error).getField();
          String errorMessage = error.getDefaultMessage();
          return fieldName + ": " + errorMessage;
        })
        .collect(Collectors.toList());

    ApiErrorResponse error = ApiErrorResponse.ofValidationErrors(errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
  * Handle JSON parsing errors (invalid enum values, date formats, etc.)
  */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    String message = "Invalid request body";

    // Extract more specific error message for enum validation
    Throwable cause = ex.getCause();
    if (cause instanceof InvalidFormatException invalidFormatEx) {
      String fieldName = invalidFormatEx.getPath().get(0).getFieldName();
      Object value = invalidFormatEx.getValue();
      Class<?> targetType = invalidFormatEx.getTargetType();

      if (targetType.isEnum()) {
        // Get valid enum values
        Object[] enumConstants = targetType.getEnumConstants();
        String validValues = String.join(", ",
            java.util.Arrays.stream(enumConstants)
                .map(Object::toString)
                .toArray(String[]::new));

        message = String.format(
            "Invalid value '%s' for field '%s'. Valid values are: %s",
            value, fieldName, validValues);
      } else {
        message = String.format(
            "Invalid value '%s' for field '%s'",
            value, fieldName);
      }
    }

    log.error("JSON parse error: {}", ex.getMessage());
    ApiErrorResponse errorResponse = ApiErrorResponse.of(message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handles Date validation
   */
  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiErrorResponse> handleValidationException(
      Exception ex,
      WebRequest request) {
    log.error(ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handles authentication failures (invalid credentials).
   * Returns 401 UNAUTHORIZED status.
   */
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      Exception ex,
      WebRequest request) {
    log.error(ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.of("Unauthorized");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
   * Handles all other unhandled exceptions.
   * Returns 500 INTERNAL SERVER ERROR status.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiErrorResponse> handleGlobalException(
      Exception ex,
      WebRequest request) {
    log.error("Unexpected error occurred: ", ex);

    ApiErrorResponse error = ApiErrorResponse.of("An unexpected error occurred. Please try again later.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}