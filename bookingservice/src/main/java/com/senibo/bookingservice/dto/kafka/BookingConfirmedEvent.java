package com.senibo.bookingservice.dto.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingConfirmedEvent(
    UUID bookingId,
    UUID userId,
    String email,
    String eventTitle,
    Integer numberOfTickets,
    BigDecimal totalPrice,
    String bookingReference,
    LocalDateTime eventDate) {

}
