package com.senibo.bookingservice.dto.clientDTOs;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.senibo.bookingservice.enums.EventCategory;
import com.senibo.bookingservice.enums.EventStatus;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Response model for event details")
public record EventResponse(

    @Schema(description = "Event ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Event title", example = "Tech Conference 2025")
    String title,

    @Schema(description = "Event description")
    String description,

    @Schema(description = "Event category", example = "TECH")
    EventCategory category,

    @Schema(description = "Cover image URL")
    String imageUrl,

    @Schema(description = "Start date/time", example = "2025-04-20T10:00:00")
    LocalDateTime startDateTime,

    @Schema(description = "End date/time", example = "2025-04-20T16:00:00")
    LocalDateTime endDateTime,

    @Schema(description = "Venue name")
    String venue,

    @Schema(description = "Address of the venue")
    String address,

    @Schema(description = "City where the event will take place")
    String city,

    @Schema(description = "Total capacity", example = "300")
    Integer capacity,

    @Schema(description = "Tickets currently available", example = "150")
    Integer availableTickets,

    @Schema(description = "Ticket price", example = "15000.00")
    BigDecimal price,

    @Schema(description = "Current event status", example = "ACTIVE")
    EventStatus status,

    @Schema(description = "Organizer ID")
    UUID organizerId,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt
) {

}
