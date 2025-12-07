package com.senibo.userservice.service.impl;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.senibo.userservice.dto.AuthResponse;
import com.senibo.userservice.dto.LoginRequest;
import com.senibo.userservice.dto.RegisterRequest;
import com.senibo.userservice.entity.User;
import com.senibo.userservice.exception.AlreadyExistsException;
import com.senibo.userservice.repository.UserRepository;
import com.senibo.userservice.security.JwtService;
import com.senibo.userservice.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of authentication services including user registration and login.
 * Handles password encryption, JWT token generation, and user validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  /**
   * Registers a new user in the system.
   * 
   * @param request Registration details (username, email, password, etc.)
   * @return AuthResponse containing JWT token and user information
   * @throws AlreadyExistsException if username or email already exists
   */
  @Override
  public AuthResponse register(RegisterRequest request) {

    // Validate username uniqueness
    if (userRepository.existsByUsername(request.username())) {
      throw new AlreadyExistsException("This username already exists");
    }

    // Validate email uniqueness
    if (userRepository.existsByEmail(request.email())) {
      throw new AlreadyExistsException("This email already exists");
    }

    // Build new user with encrypted password
    User newUser = User.builder()
        .username(request.username())
        .email(request.email())
        .password(passwordEncoder.encode(request.password())) // Hash password with BCrypt
        .firstname(request.firstname())
        .lastname(request.lastname())
        .build();

    // Persist user to database
    User createdUser = userRepository.save(newUser);

    // // Generate JWT token for immediate authentication
    // String token = jwtService.generateToken(createdUser);

    // Generate JWT token with userId as subject
    String jwtToken = jwtService.generateTokenWithUserId(newUser.getId(), newUser.getUsername());

    // Return authentication response with token
    return AuthResponse.of(jwtToken, createdUser);
  }

  /**
   * Authenticates a user and generates a JWT token.
   * 
   * @param request Login credentials (email/username and password)
   * @return AuthResponse containing JWT token and user information
   * @throws BadCredentialsException if credentials are invalid
   */
  @Override
  public AuthResponse login(LoginRequest request) {
    // Load user details
    User user = userRepository.findByEmailOrUsername(request.identifier(), request.identifier())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    // TODO: Uncomment when Notification Service is ready
    // if (!principal.getIsEmailVerified()) {
    //     throw new UnverifiedException("Please verify your email before logging in");
    // }

    // // Generate JWT token for the authenticated user
    // var token = jwtService.generateToken(principal);

    // Generate JWT token with userId as subject
    String jwtToken = jwtService.generateTokenWithUserId(user.getId(), user.getUsername());

    // // Extract user roles for logging
    log.info("Login success user={} roles={}", user.getUsername(), user.getRole());

    // Return authentication response
    return AuthResponse.of(jwtToken, user);
  }
}