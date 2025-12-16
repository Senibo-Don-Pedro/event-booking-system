package com.senibo.userservice.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.senibo.userservice.dto.AuthResponse;
import com.senibo.userservice.dto.EmailVerifiedEvent;
import com.senibo.userservice.dto.LoginRequest;
import com.senibo.userservice.dto.RegisterRequest;
import com.senibo.userservice.dto.UserRegisteredEvent;
import com.senibo.userservice.dto.UserResponse;
import com.senibo.userservice.entity.User;
import com.senibo.userservice.exception.AlreadyExistsException;
import com.senibo.userservice.exception.NotFoundException;
import com.senibo.userservice.exception.UnverifiedException;
import com.senibo.userservice.repository.UserRepository;
import com.senibo.userservice.security.JwtService;
import com.senibo.userservice.service.AuthService;
import com.senibo.userservice.service.KafkaProducerService;

import jakarta.transaction.Transactional;
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
  private final KafkaProducerService kafkaProducerService;

  /**
   * Registers a new user in the system.
   * 
   * @param request Registration details (username, email, password, etc.)
   * @return Success message prompting email verification
   * @throws AlreadyExistsException if username or email already exists
   */
  @Override
  @Transactional
  public String register(RegisterRequest request) {

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
        .isEmailVerified(false)
        .verificationToken(UUID.randomUUID().toString())
        .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
        .build();

    // Persist user to database
    User createdUser = userRepository.save(newUser);

    //Create object event to send to Kafka
    UserRegisteredEvent event = UserRegisteredEvent.from(createdUser);

    // Publish user registration event to Kafka
    kafkaProducerService.publishUserRegisteredEvent(event);

    // Return success message to user to verify email
    return "User registered successfully. Please verify your email to activate your account.";
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

    if (!user.getIsEmailVerified()) {
      throw new UnverifiedException("Please verify your email before logging in");
    }

    // Generate JWT token with userId as subject
    String jwtToken = jwtService.generateTokenWithUserId(user.getId(), user.getUsername());

    // // Extract user roles for logging
    log.info("Login success user={} roles={}", user.getUsername(), user.getRole());

    // Return authentication response
    return AuthResponse.of(jwtToken, user);
  }

  /**
   * Verifies a user's email using the provided token.
   * 
   * @param token Verification token sent to user's email
   * @return AuthResponse containing JWT token and user information
   * @throws NotFoundException if token is invalid or expired
   */
  @Override
  public AuthResponse verifyEmail(String token) {
    // Locate user by the provided verification token
    User user = userRepository.findByVerificationToken(token)
        .orElseThrow(() -> new NotFoundException("Invalid verification token"));

    // Validate token expiry
    if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new NotFoundException("Verification token has expired");
    }

    // Create JWT for the user
    String jwtToken = jwtService.generateTokenWithUserId(user.getId(), user.getUsername());

    // If already verified, return immediately
    if (user.getIsEmailVerified()) {
      return AuthResponse.of(jwtToken, user);
    }

    // Mark email as verified and clear token data
    user.setIsEmailVerified(true);
    user.setVerificationToken(null);
    user.setVerificationTokenExpiry(null);

    // Persist the updated user
    userRepository.save(user);

    // Create email verified event
    EmailVerifiedEvent event = EmailVerifiedEvent.from(user);

    // Publish email verification event to Kafka
    kafkaProducerService.publishEmailVerifiedEvent(event);

    // Return auth response with JWT
    return AuthResponse.of(jwtToken, user);
  }

  /**
   * Retrieves user information by user ID.
   * 
   * @param userId Unique identifier of the user
   * @return UserResponse containing user details
   * @throws NotFoundException if user is not found
   */
  @Override
  public UserResponse getUserById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    return UserResponse.from(user);
  }

}