package com.senibo.notificationservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingCancelledEvent(
  UUID bookingId,
  UUID userId,
  String email,
  String eventTitle,
  Integer numberOfTickets,
  BigDecimal totalPrice,
  String bookingReference,
  LocalDateTime eventDate
) {

}
