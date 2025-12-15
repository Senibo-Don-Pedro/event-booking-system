package com.senibo.eventservice.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.senibo.eventservice.dto.CreateEventRequest;
import com.senibo.eventservice.dto.EventResponse;
import com.senibo.eventservice.dto.EventSearchRequest;
import com.senibo.eventservice.dto.PagedResponse;
import com.senibo.eventservice.dto.UpdateEventRequest;
import com.senibo.eventservice.dto.UpdateTicketsRequest;
import com.senibo.eventservice.entity.Event;
import com.senibo.eventservice.enums.EventStatus;
import com.senibo.eventservice.exception.InsufficientTicketsException;
import com.senibo.eventservice.exception.NotFoundException;
import com.senibo.eventservice.exception.UnauthorizedException;
import com.senibo.eventservice.exception.ValidationException;
import com.senibo.eventservice.repository.EventRepository;
import com.senibo.eventservice.service.EventService;
import com.senibo.eventservice.util.EventSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

  private final EventRepository eventRepository;

  @Override
  public EventResponse createEvent(CreateEventRequest request, UUID organizerId) {

    // 1. Validate business rules
    validateEventDates(request.startDateTime(), request.endDateTime());

    // 2. Build Event entity from DTO
    var event = Event.builder()
        .title(request.title())
        .description(request.description())
        .category(request.category())
        .imageUrl(request.imageUrl())
        .startDateTime(request.startDateTime())
        .endDateTime(request.endDateTime())
        .venue(request.venue())
        .address(request.address())
        .city(request.city())
        .capacity(request.capacity())
        .availableTickets(request.capacity())
        .price(request.price())
        .status(EventStatus.DRAFT)
        .organizerId(organizerId)
        .build();

    // 3. Save to database
    Event newEvent = eventRepository.save(event);

    // 4. Convert to DTO and return
    return EventResponse.from(newEvent);

  }

  @Override
  public EventResponse getEventById(UUID id) {
    Event event = eventRepository.findById(id).orElseThrow(
        () -> new NotFoundException(String.format("Event with id %s not found", id)));

    return EventResponse.from(event);
  }

  @Override
  public EventResponse updateEvent(UUID eventId, UpdateEventRequest request, UUID organizerId) {

    // 1. Find event
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

    // 2. Check ownership
    if (!event.getOrganizerId().equals(organizerId)) {
      throw new UnauthorizedException("You are not authorized to update this event");
    }

    // 3. Update basic fields if they are non-null & non-empty
    if (request.title() != null && !request.title().isBlank()) {
      event.setTitle(request.title());
    }

    if (request.description() != null && !request.description().isBlank()) {
      event.setDescription(request.description());
    }

    if (request.category() != null) {
      event.setCategory(request.category());
    }

    if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
      event.setImageUrl(request.imageUrl());
    }

    // 4. Dates - validate using current values as fallback
    if (request.startDateTime() != null || request.endDateTime() != null) {
      LocalDateTime newStartDate = request.startDateTime() != null
          ? request.startDateTime()
          : event.getStartDateTime();

      LocalDateTime newEndDate = request.endDateTime() != null
          ? request.endDateTime()
          : event.getEndDateTime();

      validateEventDates(newStartDate, newEndDate);

      if (request.startDateTime() != null) {
        event.setStartDateTime(newStartDate);
      }

      if (request.endDateTime() != null) {
        event.setEndDateTime(newEndDate);
      }
    }

    // 5. Location fields
    if (request.venue() != null && !request.venue().isBlank()) {
      event.setVenue(request.venue());
    }

    if (request.address() != null && !request.address().isBlank()) {
      event.setAddress(request.address());
    }

    if (request.city() != null && !request.city().isBlank()) {
      event.setCity(request.city());
    }

    // 6. Capacity
    if (request.capacity() != null) {
      int ticketsSold = event.getCapacity() - event.getAvailableTickets();

      // Prevent reducing capacity below tickets already sold
      if (request.capacity() < ticketsSold) {
        throw new ValidationException(
            "Cannot reduce capacity to " + request.capacity() +
                ". Already sold " + ticketsSold + " tickets");
      }

      int diff = request.capacity() - event.getCapacity();
      event.setCapacity(request.capacity());
      event.setAvailableTickets(event.getAvailableTickets() + diff);
    }

    // 7. Pricing
    if (request.price() != null) {
      event.setPrice(request.price());
    }

    // 8. Status
    if (request.status() != null) {
      event.setStatus(request.status());
    }

    // 4. Save updated event
    Event updatedEvent = eventRepository.save(event);

    // 5. Return DTO
    return EventResponse.from(updatedEvent);

  }

  @Override
  public EventResponse updateAvailableTickets(UUID eventId, UpdateTicketsRequest ticketsToBook) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException("Event not found"));

    // Calculate new ticket count
    Integer ticketsChange = ticketsToBook.ticketsToBook();
    Integer newAvailableTickets = event.getAvailableTickets() - ticketsChange;

    // Validate result is not negative
    if (newAvailableTickets < 0) {
      if (ticketsChange > 0) {
        // Booking attempt
        throw new InsufficientTicketsException(
            String.format("Cannot book %d tickets. Only %d available.",
                ticketsChange, event.getAvailableTickets()));
      } else {
        // Should never happen for returns, but just in case
        throw new InsufficientTicketsException("Invalid ticket update operation");
      }
    }

    // Validate not exceeding total capacity when returning tickets
    if (newAvailableTickets > event.getCapacity()) {
      throw new InsufficientTicketsException("Cannot return more tickets than total capacity");
    }

    // Update
    event.setAvailableTickets(newAvailableTickets);
    Event updatedEvent = eventRepository.save(event);
    return EventResponse.from(updatedEvent);
  }

  @Override
  public void deleteEvent(UUID eventId, UUID organizerId) {
    // 1. Find event
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

    // 2. Check ownership
    if (!event.getOrganizerId().equals(organizerId)) {
      throw new UnauthorizedException("You are not authorized to delete this event");
    }

    // 3. Set status to cancelled
    event.setStatus(EventStatus.CANCELLED);

    // 4. Save event
    eventRepository.save(event);
  }

  @Override
  public PagedResponse<EventResponse> searchEvents(EventSearchRequest searchRequest) {
    // 1. Build dynamic specification from search filters
    Specification<Event> spec = EventSpecification.buildSearchSpec(
        searchRequest.category(),
        searchRequest.city(),
        searchRequest.status(),
        searchRequest.titleKeyword(),
        searchRequest.startDateAfter(),
        searchRequest.organizerId());

    // 2. Build Pageable with sorting
    Sort.Direction direction = searchRequest.sortDirection().equalsIgnoreCase("DESC")
        ? Sort.Direction.DESC
        : Sort.Direction.ASC;

    Pageable pageable = PageRequest.of(
        searchRequest.page(),
        searchRequest.size(),
        Sort.by(direction, searchRequest.sortBy()));

    // 3. Query
    Page<Event> eventPage = eventRepository.findAll(spec, pageable);

    // 4. Convert and wrap
    Page<EventResponse> responsePage = eventPage.map(EventResponse::from);

    return PagedResponse.of(responsePage);
  }

  @Override
  public PagedResponse<EventResponse> getMyEvents(UUID organizerId, int page, int size) {
    // 1. Create Pageable object
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    // 2. Create Specification for PUBLISHED status
    Specification<Event> spec = EventSpecification.hasOrganizer(organizerId);

    // 3. Query with specification and pagination
    Page<Event> eventPage = eventRepository.findAll(spec, pageable);

    // 4. Convert Event entities to EventResponse DTOs
    Page<EventResponse> responsePage = eventPage.map(EventResponse::from);

    //Same as this
    // Page<EventResponse> responsePage = eventPage.map(event -> EventResponse.from(event));

    // //And also Same as this
    // Page<EventResponse> responsePage = eventsPage.map(event -> new EventResponse(
    //     event.getId(),
    //     event.getTitle(),
    //     event.getCategory(),
    //     event.getStartDateTime(),
    //     event.getEndDateTime(),
    //     event.getStatus()));

    return PagedResponse.of(responsePage);
  }

  @Override
  public PagedResponse<EventResponse> getPublishedEvents(int page, int size) {

    // 1. Create Pageable object
    Pageable pageable = PageRequest.of(page, size, Sort.by("startDateTime").ascending());

    // 2. Create Specification for PUBLISHED status
    Specification<Event> spec = EventSpecification.hasStatus(EventStatus.PUBLISHED);

    // 3. Query with specification and pagination
    Page<Event> eventPage = eventRepository.findAll(spec, pageable);

    // 4. Convert Event entities to EventResponse DTOs
    Page<EventResponse> responsePage = eventPage.map(EventResponse::from);

    //Same as this
    // Page<EventResponse> responsePage = eventPage.map(event -> EventResponse.from(event));

    // //And also Same as this
    // Page<EventResponse> responsePage = eventsPage.map(event -> new EventResponse(
    //     event.getId(),
    //     event.getTitle(),
    //     event.getCategory(),
    //     event.getStartDateTime(),
    //     event.getEndDateTime(),
    //     event.getStatus()));

    return PagedResponse.of(responsePage);

  }

  @Override
  public EventResponse updateEventStatus(UUID eventId, EventStatus newStatus, UUID organizerId) {
    // 1. Find event
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

    // 2. Check ownership
    if (!event.getOrganizerId().equals(organizerId)) {
      throw new UnauthorizedException("You are not authorized to update this event");
    }

    switch (event.getStatus()) {
      case DRAFT -> {
        if (newStatus == EventStatus.COMPLETED) {
          throw new ValidationException("You cannot set the event to completed directly");
        }

        event.setStatus(newStatus);
      }

      case PUBLISHED -> {
        if (newStatus == EventStatus.DRAFT) {
          throw new ValidationException("You cannot set an already Published event to draft");
        }

        event.setStatus(newStatus);
      }

      case COMPLETED -> {
        if (newStatus != EventStatus.COMPLETED) {
          throw new ValidationException("This event has already been completed");
        }
      }

      case CANCELLED -> {
        if (newStatus != EventStatus.CANCELLED) {
          throw new ValidationException("This event has already been cancelled");
        }
      }
    }

    Event updatedEvent = eventRepository.save(event);

    return EventResponse.from(updatedEvent);
  }

  // Helper method for validation
  private void validateEventDates(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    var now = LocalDateTime.now();

    // Start date must be in the future
    if (startDateTime.isBefore(now)) {
      throw new ValidationException("Event start date must be in the future");
    }

    // End date must be after start date
    if (endDateTime.isBefore(startDateTime)) {
      throw new ValidationException("Event end date must be after start date");
    }
  }
}
