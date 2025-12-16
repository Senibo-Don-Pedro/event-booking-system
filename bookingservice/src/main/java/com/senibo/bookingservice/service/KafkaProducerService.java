package com.senibo.bookingservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.senibo.bookingservice.dto.kafka.BookingCancelledEvent;
import com.senibo.bookingservice.dto.kafka.BookingConfirmedEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaProducerService {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishBookingConfirmedEvent(BookingConfirmedEvent event) {
    log.info("Publishing Booking Confirmed Event to Kafka: {} for user {}", event, event.userId());
    kafkaTemplate.send("booking-events", event);
  }

  public void publishBookingCancelledEvent(BookingCancelledEvent event) {
    log.info("Publishing Booking Cancelled Event to Kafka: {} for user {}", event, event.userId());
    kafkaTemplate.send("booking-events", event);
  }
}
