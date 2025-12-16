package com.senibo.bookingservice.dto.clientDTOs;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User profile information (excluding password)")
public record UserResponse(
    @Schema(description = "User unique identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,

    @Schema(description = "Username", example = "johndoe") String username,

    @Schema(description = "Email address", example = "john@example.com") String email,

    @Schema(description = "First name", example = "John") String firstname,

    @Schema(description = "Last name", example = "Doe") String lastname,

    @Schema(description = "User role", example = "ROLE_USER") String role,

    @Schema(description = "Account creation timestamp", example = "2024-12-06T10:30:00") LocalDateTime createdAt) {

}