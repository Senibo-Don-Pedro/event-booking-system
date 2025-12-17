package com.senibo.notificationservice.event;

import java.util.UUID;

public record EmailVerifiedEvent(
    UUID userId,
    String email,
    String username,
    String firstname) {

}
