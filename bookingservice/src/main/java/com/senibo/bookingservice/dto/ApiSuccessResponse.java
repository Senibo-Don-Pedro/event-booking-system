package com.senibo.bookingservice.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard success response format")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiSuccessResponse<T>(
    @Schema(description = "Indicates success", example = "true")
    boolean success,
    
    @Schema(description = "Success message", example = "Booking Created Successfully")
    String message,
    
    @Schema(description = "Timestamp of the response", example = "2024-12-06T10:30:00")
    LocalDateTime timestamp,
    
    @Schema(description = "Response payload data")
    T data
) {

    // Factory method with custom message
    public static <T> ApiSuccessResponse<T> of(T data, String message) {
        return new ApiSuccessResponse<>(true, message, LocalDateTime.now(), data);
    }

    // Factory method with default message
    public static <T> ApiSuccessResponse<T> of(T data) {
        return new ApiSuccessResponse<>(true, "Operation completed successfully", LocalDateTime.now(), data);
    }

    // Factory method for success without data (for operations like delete)
    public static <T> ApiSuccessResponse<T> of(String message) {
        return new ApiSuccessResponse<>(true, message, LocalDateTime.now(), null);
    }
}