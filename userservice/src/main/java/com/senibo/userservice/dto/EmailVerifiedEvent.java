package com.senibo.userservice.dto;

import java.util.UUID;

import com.senibo.userservice.entity.User;

public record EmailVerifiedEvent(
    UUID userId,
    String email,
    String username,
    String firstname) {
  public static EmailVerifiedEvent from(User user) {
    return new EmailVerifiedEvent(user.getId(), user.getEmail(), user.getUsername(), user.getFirstname());
  }
}
