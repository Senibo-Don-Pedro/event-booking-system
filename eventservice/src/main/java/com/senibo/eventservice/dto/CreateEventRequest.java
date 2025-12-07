package com.senibo.eventservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.senibo.eventservice.enums.EventCategory;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a new event")
public record CreateEventRequest(

    @Schema(description = "Event title", example = "Tech Conference 2025")
    @NotBlank(message = "Title is required")
    String title,

    @Schema(description = "Event description", example = "A conference about the future of technology")
    String description,

    @Schema(description = "Event category", example = "MUSIC")
    @NotNull(message = "Category is required")
    EventCategory category,

    @Schema(description = "Image URL for the event", example = "https://example.com/banner.jpg")
    String imageUrl,

    @Schema(description = "Event start date and time", example = "2025-04-20T10:00:00")
    @NotNull @Future
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("startDateTime")
    LocalDateTime startDateTime,

    @Schema(description = "Event end date and time", example = "2025-04-20T16:00:00")
    @NotNull @Future
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("endDateTime")
    LocalDateTime endDateTime,

    @Schema(description = "Venue name", example = "Eko Convention Center")
    @NotBlank
    String venue,

    @Schema(description = "Event address", example = "Plot 1415 Adetokunbo Ademola Street")
    @NotBlank
    String address,

    @Schema(description = "City where the event will take place", example = "Lagos")
    @NotBlank
    String city,

    @Schema(description = "Total number of seats available", example = "300")
    @NotNull @Min(1)
    Integer capacity,

    @Schema(description = "Ticket price", example = "15000.00")
    @NotNull @Positive
    BigDecimal price
) {}
