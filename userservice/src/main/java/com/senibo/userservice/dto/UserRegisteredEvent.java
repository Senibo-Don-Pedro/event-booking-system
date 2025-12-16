package com.senibo.userservice.dto;

import java.util.UUID;

import com.senibo.userservice.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Event published when a user registers")
public record UserRegisteredEvent(

        UUID userId,

        String email,

        String username,

        String firstname,

        String verificationToken) {
    public static UserRegisteredEvent from(User user) {
        return new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstname(),
                user.getVerificationToken());
    }
}