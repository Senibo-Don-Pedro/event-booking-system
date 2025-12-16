package com.senibo.bookingservice.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.senibo.bookingservice.dto.ApiSuccessResponse;
import com.senibo.bookingservice.dto.clientDTOs.EventResponse;
import com.senibo.bookingservice.dto.clientDTOs.UpdateTicketsRequest;

@FeignClient(name = "event-service", url = "${event.service.url}")
public interface EventServiceClient {

  // Method 1: Get event details (no JWT needed - public endpoint)
  @GetMapping("/api/events/{eventId}")
  ApiSuccessResponse<EventResponse> getEvent(@PathVariable UUID eventId);

  // Method 2: Update tickets (JWT required)
  // ✅ Change 1: Remove @RequestHeader("Authorization")
  // ✅ Change 2: Add @RequestHeader("x-internal-secret")
  @PatchMapping("/api/events/{eventId}/tickets")
  ApiSuccessResponse<EventResponse> updateAvailableTickets(
      @PathVariable UUID eventId,
      @RequestBody UpdateTicketsRequest request,
      @RequestHeader("x-internal-secret") String internalSecret);

}
