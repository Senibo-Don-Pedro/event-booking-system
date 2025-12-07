package com.senibo.eventservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response format")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
    @Schema(description = "Indicates failure", example = "false")
    boolean success,
    
    @Schema(description = "Error message", example = "Invalid email or password")
    String message,
    
    @Schema(description = "Timestamp of the error", example = "2024-12-06T10:30:00")
    LocalDateTime timestamp,
    
    @Schema(description = "List of detailed error messages (for validation errors)", example = "[\"email: Email must be valid\", \"password: Password is required\"]")
    List<String> errors
) {

    // Factory method for single error message
    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(false, message, LocalDateTime.now(), null);
    }

    // Factory method for validation errors
    public static ApiErrorResponse of(String message, List<String> errors) {
        return new ApiErrorResponse(false, message, LocalDateTime.now(), errors);
    }

    // Factory method for multiple validation errors
    public static ApiErrorResponse ofValidationErrors(List<String> errors) {
        return new ApiErrorResponse(false, "Validation failed", LocalDateTime.now(), errors);
    }
}