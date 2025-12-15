package com.senibo.eventservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for updating the available Tickets")
public record UpdateTicketsRequest(
    @Schema(description = "Tickets to Book")
    @NotNull(message = "Tickets to book is required")
    // @Positive(message = "Tickets to book must be positive")
    Integer ticketsToBook
) {
}
