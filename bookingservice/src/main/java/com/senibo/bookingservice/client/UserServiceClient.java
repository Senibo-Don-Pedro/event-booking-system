package com.senibo.bookingservice.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.senibo.bookingservice.dto.ApiSuccessResponse;
import com.senibo.bookingservice.dto.clientDTOs.UserResponse;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

  @GetMapping("/api/users/{userId}")
  ApiSuccessResponse<UserResponse> getUserById(@PathVariable UUID userId);

}
