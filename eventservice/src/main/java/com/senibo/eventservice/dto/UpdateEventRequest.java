package com.senibo.eventservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.senibo.eventservice.enums.EventCategory;
import com.senibo.eventservice.enums.EventStatus;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for updating an event")
public record UpdateEventRequest(

    @Schema(description = "Updated title", example = "Updated Tech Conference")
    String title,

    @Schema(description = "Updated description")
    String description,

    @Schema(description = "Updated event category", example = "SPORTS")
    EventCategory category,

    @Schema(description = "Updated image URL")
    String imageUrl,

    @Schema(description = "Updated start date/time", example = "2025-04-20T12:00:00")
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("startDateTime")
    LocalDateTime startDateTime,

    @Schema(description = "Updated end date/time", example = "2025-04-20T18:00:00")
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("endDateTime")
    LocalDateTime endDateTime,

    @Schema(description = "Updated venue")
    String venue,

    @Schema(description = "Updated address")
    String address,

    @Schema(description = "Updated city")
    String city,

    @Schema(description = "Updated capacity", example = "350")
    @Min(1)
    Integer capacity,

    @Schema(description = "Updated ticket price", example = "20000.00")
    @Positive
    BigDecimal price,

    @Schema(description = "Updated event status", example = "CANCELLED")
    EventStatus status
) {}
