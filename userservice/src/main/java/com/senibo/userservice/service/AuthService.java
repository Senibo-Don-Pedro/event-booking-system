package com.senibo.userservice.service;

import com.senibo.userservice.dto.AuthResponse;
import com.senibo.userservice.dto.LoginRequest;
import com.senibo.userservice.dto.RegisterRequest;

public interface AuthService {

  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);
  
}
