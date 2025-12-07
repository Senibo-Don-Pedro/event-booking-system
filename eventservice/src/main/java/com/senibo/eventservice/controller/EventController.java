package com.senibo.eventservice.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.senibo.eventservice.dto.*;
import com.senibo.eventservice.enums.EventCategory;
import com.senibo.eventservice.enums.EventStatus;
import com.senibo.eventservice.exception.UnauthorizedException;
import com.senibo.eventservice.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for event management operations.
 * Handles creating, reading, updating, and deleting events.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "APIs for managing events")
public class EventController {

  private final EventService eventService;

  // ==================== HELPER METHOD ====================

  /**
   * Extract organizerId from JWT token in SecurityContext.
   * The subject of the JWT contains the user's UUID.
   */
  private UUID getAuthenticatedOrganizerId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("User not authenticated");
    }

    // The subject (stored in getName()) is the userId from JWT
    String userId = authentication.getName();

    try {
      return UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      throw new UnauthorizedException("Invalid user ID in token");
    }
  }

  // ==================== ENDPOINTS ====================

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new event", description = "Create a new event (authenticated organizers only)")
  public ApiSuccessResponse<EventResponse> createEvent(
      @Valid @RequestBody CreateEventRequest request) {

    UUID organizerId = getAuthenticatedOrganizerId(); // ✅ Get from JWT

    EventResponse createdEvent = eventService.createEvent(request, organizerId);

    return ApiSuccessResponse.of(createdEvent, "Event created successfully");
  }

  @GetMapping("/{eventId}")
  @Operation(summary = "Get event by ID", description = "Retrieve event details by ID (public - no auth required)")
  public ApiSuccessResponse<EventResponse> getEventById(
      @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId) {

    EventResponse event = eventService.getEventById(eventId);

    return ApiSuccessResponse.of(event);
  }

  @PutMapping("/{eventId}")
  @Operation(summary = "Update event", description = "Update event details (owner only - JWT required)")
  public ApiSuccessResponse<EventResponse> updateEvent(
      @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId,
      @Valid @RequestBody UpdateEventRequest request) {

    UUID organizerId = getAuthenticatedOrganizerId(); // ✅ Get from JWT

    EventResponse updatedEvent = eventService.updateEvent(eventId, request, organizerId);

    return ApiSuccessResponse.of(updatedEvent, "Event updated successfully");
  }

  @DeleteMapping("/{eventId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete event", description = "Soft delete event by changing status to CANCELLED (owner only - JWT required)")
  public void deleteEvent(
      @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId) {

    UUID organizerId = getAuthenticatedOrganizerId(); // ✅ Get from JWT

    eventService.deleteEvent(eventId, organizerId);
  }

  @GetMapping("/search")
  @Operation(summary = "Search events", description = "Search and filter events with pagination (public - no auth required)")
  public ApiSuccessResponse<PagedResponse<EventResponse>> searchEvents(
      @Parameter(description = "Event category") @RequestParam(required = false) String category,
      @Parameter(description = "City") @RequestParam(required = false) String city,
      @Parameter(description = "Event status") @RequestParam(required = false) String status,
      @Parameter(description = "Search keyword in title") @RequestParam(required = false) String titleKeyword,
      @Parameter(description = "Events starting after this date (ISO format)") @RequestParam(required = false) String startDateAfter,
      @Parameter(description = "Organizer ID") @RequestParam(required = false) UUID organizerId,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort by field") @RequestParam(defaultValue = "startDateTime") String sortBy,
      @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "ASC") String sortDirection) {

    // Build EventSearchRequest from query params
    EventSearchRequest searchRequest = new EventSearchRequest(
        category != null ? EventCategory.valueOf(category.toUpperCase()) : null,
        city,
        status != null ? EventStatus.valueOf(status.toUpperCase()) : null,
        titleKeyword,
        startDateAfter != null ? LocalDateTime.parse(startDateAfter) : null,
        organizerId,
        page,
        size,
        sortBy,
        sortDirection);

    PagedResponse<EventResponse> events = eventService.searchEvents(searchRequest);

    return ApiSuccessResponse.of(events);
  }

  @GetMapping("/my-events")
  @Operation(summary = "Get my events", description = "Get all events created by the authenticated organizer (JWT required)")
  public ApiSuccessResponse<PagedResponse<EventResponse>> getMyEvents(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

    UUID organizerId = getAuthenticatedOrganizerId(); // ✅ Get from JWT

    PagedResponse<EventResponse> events = eventService.getMyEvents(organizerId, page, size);

    return ApiSuccessResponse.of(events, "Your events retrieved successfully");
  }

  @GetMapping("/published")
  @Operation(summary = "Get published events", description = "Get all published events (public - no auth required)")
  public ApiSuccessResponse<PagedResponse<EventResponse>> getPublishedEvents(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

    PagedResponse<EventResponse> events = eventService.getPublishedEvents(page, size);

    return ApiSuccessResponse.of(events);
  }

  @PatchMapping("/{eventId}/status")
  @Operation(summary = "Update event status", description = "Change event status: DRAFT/PUBLISHED/CANCELLED/COMPLETED (owner only - JWT required)")
  public ApiSuccessResponse<EventResponse> updateEventStatus(
      @Parameter(description = "Event ID", required = true) @PathVariable UUID eventId,
      @Parameter(description = "New status", required = true) @RequestParam EventStatus status) {

    UUID organizerId = getAuthenticatedOrganizerId(); // ✅ Get from JWT

    EventResponse updatedEvent = eventService.updateEventStatus(eventId, status, organizerId);

    return ApiSuccessResponse.of(updatedEvent, "Event status updated successfully");
  }
}