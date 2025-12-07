package com.senibo.eventservice.service;

import java.util.UUID;

import com.senibo.eventservice.dto.CreateEventRequest;
import com.senibo.eventservice.dto.EventResponse;
import com.senibo.eventservice.dto.EventSearchRequest;
import com.senibo.eventservice.dto.PagedResponse;
import com.senibo.eventservice.dto.UpdateEventRequest;
import com.senibo.eventservice.enums.EventStatus;

/**
 * Service interface for event management operations.
 * Handles business logic for creating, updating, searching, and managing events.
 */
public interface EventService {

  /**
   * Create a new event.
   * Sets availableTickets equal to capacity and status to DRAFT by default.
   * 
   * @param request Event details
   * @param organizerId ID of the user creating the event (from JWT)
   * @return Created event details
   * @throws ValidationException if business rules are violated
   */
  EventResponse createEvent(CreateEventRequest request, UUID organizerId);

  /**
   * Get a single event by ID.
   * Public endpoint - anyone can view event details.
   * 
   * @param id Event ID
   * @return Event details
   * @throws NotFoundException if event not found
   */
  EventResponse getEventById(UUID id);

  /**
   * Update an event.
   * Only the organizer who created the event can update it.
   * 
   * @param eventId ID of the event to update
   * @param request Updated event details
   * @param organizerId ID of the organizer making the request
   * @return Updated event details
   * @throws NotFoundException if event not found
   * @throws UnauthorizedException if organizer doesn't own the event
   */
  EventResponse updateEvent(UUID eventId, UpdateEventRequest request, UUID organizerId);

  /**
   * Delete (cancel) an event.
   * Changes event status to CANCELLED (soft delete).
   * Only the organizer who created the event can delete it.
   * 
   * @param eventId ID of the event to delete
   * @param organizerId ID of the organizer making the request
   * @throws NotFoundException if event not found
   * @throws UnauthorizedException if organizer doesn't own the event
   */
  void deleteEvent(UUID eventId, UUID organizerId);

  /**
   * Search and filter events with pagination.
   * Public endpoint - anyone can search.
   * 
   * @param searchRequest Contains filters (category, city, status, etc.) and pagination
   * @return Paginated list of events matching the search criteria
   */
  PagedResponse<EventResponse> searchEvents(EventSearchRequest searchRequest);

  /**
   * Get all events created by a specific organizer.
   * Returns all statuses (DRAFT, PUBLISHED, CANCELLED, etc.).
   * 
   * @param organizerId ID of the organizer
   * @param page Page number (0-based)
   * @param size Number of items per page
   * @return Paginated list of organizer's events
   */
  PagedResponse<EventResponse> getMyEvents(UUID organizerId, int page, int size);

  /**
   * Get all published events (public endpoint).
   * Returns only events with PUBLISHED status.
   * 
   * @param page Page number (0-based)
   * @param size Number of items per page
   * @return Paginated list of published events
   */
  PagedResponse<EventResponse> getPublishedEvents(int page, int size);

  /**
   * Update event status (publish, cancel, complete).
   * Only the organizer who created the event can update its status.
   * 
   * @param eventId ID of the event
   * @param newStatus New status to set
   * @param organizerId ID of the organizer making the request
   * @return Updated event details
   * @throws NotFoundException if event not found
   * @throws UnauthorizedException if organizer doesn't own the event
   */
  EventResponse updateEventStatus(UUID eventId, EventStatus newStatus, UUID organizerId);
}