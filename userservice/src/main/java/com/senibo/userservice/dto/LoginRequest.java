package com.senibo.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request payload for user login")
public record LoginRequest(
    @Schema(description = "Username or Email", example = "john@example.com or johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username or Email is required")
    // @Email(message = "Email must be valid")
    String identifier,

    @Schema(description = "Password", example = "SecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    String password
) {}