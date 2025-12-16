package com.senibo.userservice.controller;

import com.senibo.userservice.dto.*;
import com.senibo.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for user registration and login operations.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // For Swagger documentation
    public ResponseEntity<ApiSuccessResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {

        String response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED) // ✅ Actually returns 201
                .body(ApiSuccessResponse.of(null, response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user credentials and returns a JWT token")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiSuccessResponse.of(response, "Login successful"));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> verifyEmail(
            @RequestParam String token) {

        AuthResponse response = authService.verifyEmail(token);

        return ResponseEntity.ok(ApiSuccessResponse.of(response, "Email verified successfully"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> getUserById(
            @PathVariable UUID userId) {

        UserResponse response = authService.getUserById(userId);

        return ResponseEntity.ok(ApiSuccessResponse.of(response, "User retrieved successfully"));
    }
}