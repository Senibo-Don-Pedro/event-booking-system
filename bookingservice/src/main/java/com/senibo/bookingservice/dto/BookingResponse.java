package com.senibo.bookingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.senibo.bookingservice.entity.Booking;
import com.senibo.bookingservice.enums.BookingStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookingResponse(
    @Schema(description = "Unique booking identifier") UUID id,
    @Schema(description = "User who made the booking") UUID userId,
    @Schema(description = "Event being booked") UUID eventId,
    @Schema(description = "Number of tickets purchased") Integer numberOfTickets,
    @Schema(description = "Total cost of booking") BigDecimal totalPrice,
    @Schema(description = "Current booking status") BookingStatus status,
    @Schema(description = "Public reference number") String bookingReference,
    @Schema(description = "When booking was created") LocalDateTime createdAt) {

  public static BookingResponse from(Booking booking) {
    return new BookingResponse(
        booking.getId(),
        booking.getUserId(),
        booking.getEventId(),
        booking.getNumberOfTickets(),
        booking.getTotalPrice(),
        booking.getStatus(),
        booking.getBookingReference(),
        booking.getCreatedAt());
  }
}