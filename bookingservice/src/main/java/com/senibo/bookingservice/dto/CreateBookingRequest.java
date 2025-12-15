package com.senibo.bookingservice.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for creating new booking")
public record CreateBookingRequest(

  @Schema(description = "Event Id", example = "uiee22-43dd2-d33id-2229d-cncin")
  @NotNull(message = "event Id is required")
  UUID eventId,

  @Schema(description = "Number of Tickets", example = "2")
  @Positive(message = "This number cannot be less than Zero")
  @Max(value = 10 , message = "Max amount of booknigs is 10")
  Integer numberOfTickets
) {
  
}
