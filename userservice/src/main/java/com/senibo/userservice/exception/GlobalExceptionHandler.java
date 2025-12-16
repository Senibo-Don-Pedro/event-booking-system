package com.senibo.userservice.exception;

import com.senibo.userservice.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

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
   * Handles resource already exists exceptions.
   * Returns 409 CONFLICT status.
   */
  @ExceptionHandler(AlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<ApiErrorResponse> handleAlreadyExistsException(
      AlreadyExistsException ex,
      WebRequest request) {
    log.error("Resource already exists: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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
   * Handles authentication failures (invalid credentials).
   * Returns 401 UNAUTHORIZED status.
   */
  @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      Exception ex,
      WebRequest request) {
    log.error("Authentication failed: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.of("Invalid email or password");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  /**
  * Handles unverified account exceptions.
  * Returns 403 FORBIDDEN status.
  */
  @ExceptionHandler(UnverifiedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ApiErrorResponse> handleUnverifiedException(
      UnverifiedException ex,
      WebRequest request) {

    log.warn("Unverified account access attempt: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
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