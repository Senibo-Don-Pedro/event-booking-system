package com.senibo.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request payload for user registration")
public record RegisterRequest(
    @Schema(description = "Username for the account", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @Schema(description = "Email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @Schema(description = "Password (minimum 6 characters)", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @Schema(description = "First name", example = "John")
    String firstname,

    @Schema(description = "Last name", example = "Doe")
    String lastname
) {}