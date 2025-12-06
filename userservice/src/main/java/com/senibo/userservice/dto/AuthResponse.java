package com.senibo.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token and user details")
public record AuthResponse(
    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token,
    
    @Schema(description = "Token type", example = "Bearer")
    String type,
    
    @Schema(description = "Username", example = "johndoe")
    String username,
    
    @Schema(description = "Email address", example = "john@example.com")
    String email,
    
    @Schema(description = "User role", example = "ROLE_USER")
    String role
) {
    public static AuthResponse of(String token, com.senibo.userservice.entity.User user) {
        return new AuthResponse(
            token,
            "Bearer",
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
    }
}