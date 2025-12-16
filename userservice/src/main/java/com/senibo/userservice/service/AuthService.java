package com.senibo.userservice.service;

import java.util.UUID;

import com.senibo.userservice.dto.AuthResponse;
import com.senibo.userservice.dto.LoginRequest;
import com.senibo.userservice.dto.RegisterRequest;
import com.senibo.userservice.dto.UserResponse;

public interface AuthService {

  String register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  AuthResponse verifyEmail(String token);

  /**
   * Retrieves user information by user ID.
   * 
   * @param userId Unique identifier of the user
   * @return UserResponse containing user details
   * @throws NotFoundException if user is not found
   */
  UserResponse getUserById(UUID userId);
  
}
