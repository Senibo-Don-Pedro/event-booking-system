package com.senibo.bookingservice.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.senibo.bookingservice.config.FeignConfig;
import com.senibo.bookingservice.dto.ApiSuccessResponse;
import com.senibo.bookingservice.dto.clientDTOs.UserResponse;

@FeignClient(
  name = "user-service", 
  url = "${user.service.url}", 
  configuration = FeignConfig.class // <--- Link the config here
)
public interface UserServiceClient {

  @GetMapping("/api/auth/{userId}")
  ApiSuccessResponse<UserResponse> getUserById(@PathVariable UUID userId);

}
