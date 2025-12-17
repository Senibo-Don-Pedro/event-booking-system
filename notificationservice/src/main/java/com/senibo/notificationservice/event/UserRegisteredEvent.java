package com.senibo.notificationservice.event;

import java.util.UUID;

public record UserRegisteredEvent(

        UUID userId,

        String email,

        String username,

        String firstname,

        String verificationToken) {

}