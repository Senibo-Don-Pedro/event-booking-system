package com.senibo.userservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.senibo.userservice.dto.EmailVerifiedEvent;
import com.senibo.userservice.dto.UserRegisteredEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaProducerService {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishUserRegisteredEvent(UserRegisteredEvent event) {
    log.info("Publishing User Registered Event to Kafka: {} for user {}", event, event.username());
    kafkaTemplate.send("user-events", event);

  }

  public void publishEmailVerifiedEvent(EmailVerifiedEvent event) {
    log.info("Publishing Email Verified Event to Kafka: {} for user {}", event, event.username());
    kafkaTemplate.send("user-events", event);
  }
}
