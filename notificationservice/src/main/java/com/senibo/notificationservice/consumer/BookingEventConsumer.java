package com.senibo.notificationservice.consumer;

import com.senibo.notificationservice.event.BookingCancelledEvent;
import com.senibo.notificationservice.event.BookingConfirmedEvent;
import com.senibo.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = "booking-events", groupId = "notification-service")
public class BookingEventConsumer {

    private final EmailService emailService;

    @KafkaHandler
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        log.info("Received BookingConfirmedEvent for bookingId: {}", event.bookingId());

        try {
            emailService.sendBookingConfirmationEmail(
                    event.email(),
                    "Valued Customer", // Ideally you'd fetch the name or add it to the event
                    event);
            log.info("✅ Booking confirmation email sent successfully to: {}", event.email());
        } catch (Exception e) {
            log.error("❌ FAILED to send booking confirmation email to: {}", event.email(), e);
            throw e; // Re-throw so Kafka knows it failed
        }

    }

    @KafkaHandler
    public void handleBookingCancelledEvent(BookingCancelledEvent event) {
        log.info("Received BookingCancelledEvent for bookingId: {}", event.bookingId());

        try {
            emailService.sendBookingCancellationEmail(
                    event.email(),
                    "Valued Customer",
                    event);
            log.info("✅ Booking cancellation email sent successfully to: {}", event.email());
        } catch (Exception e) {
            log.error("❌ FAILED to send booking cancellation email to: {}", event.email(), e);
            throw e; // Re-throw so Kafka knows it failed
        }

    }
}