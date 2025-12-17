package com.senibo.notificationservice.consumer;

import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.senibo.notificationservice.event.EmailVerifiedEvent;
import com.senibo.notificationservice.event.UserRegisteredEvent;
import com.senibo.notificationservice.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = "user-events", groupId = "notification-service")
public class UserEventConsumer {
  private final EmailService emailService;

  @KafkaHandler
  public void handleUserRegisteredEvent(UserRegisteredEvent event) {
    log.info("Received UserRegisteredEvent for email: {}", event.email());
    emailService.sendVerificationEmail(
        event.email(),
        event.username(),
        event.verificationToken());
  }

  @KafkaHandler
  public void handleEmailVerifiedEvent(EmailVerifiedEvent event) {
    log.info("Received EmailVerifiedEvent for email: {}", event.email());
    emailService.sendWelcomeEmail(event.email(), event.username());
  }
}
