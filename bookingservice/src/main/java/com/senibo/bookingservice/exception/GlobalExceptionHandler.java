package com.senibo.bookingservice.exception;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.senibo.bookingservice.dto.ApiErrorResponse;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the Booking Service.
 * Centralizes error handling and provides consistent API error responses.
 * 
 * Handles:
 * 1. Custom business exceptions (BookingException, InsufficientTicketsException, etc.)
 * 2. Feign client exceptions (communication with other services)
 * 3. Validation exceptions (Jakarta Validation)
 * 4. General runtime exceptions
 * 5. Specific HTTP status code mappings
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // === CUSTOM BUSINESS EXCEPTIONS ===
    
    /**
     * Handles BookingException - general booking failures
     * Returns: 400 Bad Request
     */
    @ExceptionHandler(BookingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleBookingException(
            BookingException ex, WebRequest request) {
        
        log.warn("BookingException: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles InsufficientTicketsException - when not enough tickets available
     * Returns: 409 Conflict (resource state conflict)
     */
    @ExceptionHandler(InsufficientTicketsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponse> handleInsufficientTicketsException(
            InsufficientTicketsException ex, WebRequest request) {
        
        log.warn("InsufficientTicketsException: {} - Request: {}", 
                ex.getMessage(), request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    /**
     * Handles EventNotPublishedException - when trying to book unpublished events
     * Returns: 422 Unprocessable Entity (business rule violation)
     */
    @ExceptionHandler(EventNotPublishedException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ApiErrorResponse> handleEventNotPublishedException(
            EventNotPublishedException ex, WebRequest request) {
        
        log.warn("EventNotPublishedException: {} - Request: {}", 
                ex.getMessage(), request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // === FEIGN CLIENT EXCEPTIONS ===
    
    /**
     * Handles FeignException.NotFound - when event/user doesn't exist in other services
     * Returns: 404 Not Found
     */
    @ExceptionHandler(FeignException.NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiErrorResponse> handleFeignNotFoundException(
            FeignException.NotFound ex, WebRequest request) {
        
        String errorMessage = extractFeignErrorMessage(ex, "Resource not found");
        log.error("Feign 404 Error: {} - Request: {}", errorMessage, request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(errorMessage);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handles FeignException.BadRequest - when other service rejects our request
     * Returns: 400 Bad Request
     */
    @ExceptionHandler(FeignException.BadRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleFeignBadRequestException(
            FeignException.BadRequest ex, WebRequest request) {
        
        String errorMessage = extractFeignErrorMessage(ex, "Invalid request to external service");
        log.error("Feign 400 Error: {} - Request: {}", errorMessage, request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(errorMessage);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles FeignException.Unauthorized/Forbidden - authentication/authorization failures
     * Returns: 401/403 as appropriate
     */
    @ExceptionHandler({FeignException.Unauthorized.class, FeignException.Forbidden.class})
    public ResponseEntity<ApiErrorResponse> handleFeignAuthException(
            FeignException ex, WebRequest request) {
        
        HttpStatus status = ex instanceof FeignException.Unauthorized ? 
                HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        
        String errorMessage = extractFeignErrorMessage(ex, "Authentication/Authorization failed with external service");
        log.error("Feign Auth Error ({}): {} - Request: {}", 
                status.value(), errorMessage, request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(errorMessage);
        return new ResponseEntity<>(error, status);
    }
    
    /**
     * Handles generic FeignException (other 4xx/5xx errors from external services)
     * Returns: 502 Bad Gateway (we're acting as a gateway to other services)
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErrorResponse> handleFeignException(
            FeignException ex, WebRequest request) {
        
        String errorMessage = extractFeignErrorMessage(ex, "External service communication failed");
        log.error("Feign Communication Error ({}): {} - Request: {}", 
                ex.status(), errorMessage, request.getDescription(false));
        
        // Map Feign status to appropriate HTTP status
        HttpStatus status = mapFeignStatusToHttpStatus(ex.status());
        
        ApiErrorResponse error = ApiErrorResponse.of(errorMessage);
        return new ResponseEntity<>(error, status);
    }
    
    // === VALIDATION EXCEPTIONS ===
    
    /**
     * Handles MethodArgumentNotValidException - Jakarta Validation failures
     * Returns: 400 Bad Request with detailed field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        // Extract field errors with clear messages
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        
        log.warn("Validation failed with {} errors - Request: {}", 
                errors.size(), request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of("Validation failed", errors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // === DATABASE/OPTIMISTIC LOCKING EXCEPTIONS ===
    
    /**
     * Handles OptimisticLockingFailureException - concurrent modification conflicts
     * Returns: 409 Conflict
     */
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingException(
            org.springframework.dao.OptimisticLockingFailureException ex, WebRequest request) {
        
        log.warn("Concurrent modification detected: {} - Request: {}", 
                ex.getMessage(), request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(
                "The resource was modified by another request. Please try again.");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    /**
     * Handles DataIntegrityViolationException - database constraint violations
     * Returns: 409 Conflict
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
        
        log.error("Database integrity violation: {} - Request: {}", 
                ex.getMessage(), request.getDescription(false));
        
        // Try to extract meaningful message from the exception
        String userMessage = extractDataIntegrityMessage(ex);
        
        ApiErrorResponse error = ApiErrorResponse.of(userMessage);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    // === GENERAL EXCEPTIONS ===
    
    /**
     * Handles IllegalArgumentException - invalid arguments passed to methods
     * Returns: 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Illegal argument: {} - Request: {}", 
                ex.getMessage(), request.getDescription(false));
        
        ApiErrorResponse error = ApiErrorResponse.of(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Catch-all handler for any unhandled RuntimeException
     * Returns: 500 Internal Server Error
     * NEVER expose internal details in production!
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        log.error("Unhandled RuntimeException: {} - Request: {}", 
                ex.getMessage(), request.getDescription(false), ex);
        
        // In production, use a generic message
        String userMessage = "An unexpected error occurred. Please try again later.";
        
        ApiErrorResponse error = ApiErrorResponse.of(userMessage);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Ultimate catch-all for any Exception not caught above
     * Returns: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unhandled Exception: {} - Request: {}", 
                ex.getMessage(), request.getDescription(false), ex);
        
        String userMessage = "An internal server error occurred.";
        ApiErrorResponse error = ApiErrorResponse.of(userMessage);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // === HELPER METHODS ===
    
    /**
     * Extracts meaningful error message from FeignException.
     * Tries to parse JSON error response from other services.
     */
    private String extractFeignErrorMessage(FeignException ex, String defaultMessage) {
        try {
            // Feign exceptions often contain JSON response in the message
            String content = ex.contentUTF8();
            if (content != null && !content.isBlank()) {
                // Try to parse JSON error response (simplified)
                // In real implementation, you might want to parse the actual JSON
                if (content.contains("\"message\"")) {
                    // Extract message field from JSON
                    int start = content.indexOf("\"message\"") + 10;
                    int end = content.indexOf("\"", start);
                    if (start > 9 && end > start) {
                        return content.substring(start, end);
                    }
                }
                return content.length() > 200 ? content.substring(0, 200) + "..." : content;
            }
        } catch (Exception e) {
            log.debug("Failed to parse Feign error content", e);
        }
        return defaultMessage + " (Status: " + ex.status() + ")";
    }
    
    /**
     * Maps Feign HTTP status codes to appropriate HTTP status for our API.
     */
    private HttpStatus mapFeignStatusToHttpStatus(int feignStatus) {
        if (feignStatus >= 400 && feignStatus < 500) {
            // For 4xx errors from other services, return 502 (Bad Gateway)
            // because the error originated from an external service
            return HttpStatus.BAD_GATEWAY;
        } else if (feignStatus >= 500) {
            // For 5xx errors from other services, return 503 (Service Unavailable)
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        // Default fallback
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * Formats field validation errors for user-friendly response.
     */
    private String formatFieldError(FieldError fieldError) {
        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage();
        
        if (fieldError.getRejectedValue() != null) {
            return String.format("%s: %s (rejected value: '%s')", 
                    field, message, fieldError.getRejectedValue());
        }
        return String.format("%s: %s", field, message);
    }
    
    /**
     * Extracts user-friendly message from DataIntegrityViolationException.
     */
    private String extractDataIntegrityMessage(
            org.springframework.dao.DataIntegrityViolationException ex) {
        
        String message = ex.getMessage();
        
        // Check for common constraint violations
        if (message != null) {
            if (message.contains("unique constraint") || message.contains("Unique index")) {
                return "A record with the same information already exists.";
            }
            if (message.contains("foreign key constraint")) {
                return "Referenced record does not exist.";
            }
            if (message.contains("not-null") || message.contains("NOT NULL")) {
                return "Required field cannot be null.";
            }
        }
        
        return "Database constraint violation occurred.";
    }
}