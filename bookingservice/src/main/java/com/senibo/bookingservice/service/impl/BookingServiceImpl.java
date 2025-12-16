package com.senibo.bookingservice.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.senibo.bookingservice.client.EventServiceClient;
import com.senibo.bookingservice.client.UserServiceClient;
import com.senibo.bookingservice.dto.ApiSuccessResponse;
import com.senibo.bookingservice.dto.BookingResponse;
import com.senibo.bookingservice.dto.CreateBookingRequest;
import com.senibo.bookingservice.dto.PagedResponse;
import com.senibo.bookingservice.dto.clientDTOs.EventResponse;
import com.senibo.bookingservice.dto.clientDTOs.UpdateTicketsRequest;
import com.senibo.bookingservice.dto.clientDTOs.UserResponse;
import com.senibo.bookingservice.dto.kafka.BookingCancelledEvent;
import com.senibo.bookingservice.dto.kafka.BookingConfirmedEvent;
import com.senibo.bookingservice.entity.Booking;
import com.senibo.bookingservice.enums.BookingStatus;
import com.senibo.bookingservice.enums.EventStatus;
import com.senibo.bookingservice.exception.BookingException;
import com.senibo.bookingservice.exception.EventNotPublishedException;
import com.senibo.bookingservice.exception.InsufficientTicketsException;
import com.senibo.bookingservice.exception.NotFoundException;
import com.senibo.bookingservice.exception.UnauthorizedException;
import com.senibo.bookingservice.repository.BookingRepository;
import com.senibo.bookingservice.service.BookingService;
import com.senibo.bookingservice.service.KafkaProducerService;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final int MAX_BOOKING_REFERENCE_ATTEMPTS = 10;

    private final EventServiceClient eventServiceClient;
    private final BookingRepository bookingRepository;
    private final UserServiceClient userServiceClient;
    private final KafkaProducerService kafkaProducerService;

    @Value("${app.internal-service-key}")
    private String internalServiceKey; // Inject the secret

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public BookingResponse createBooking(CreateBookingRequest request, UUID userId) {
        // Validate inputs
        validateInputs(request, userId);

        // Fetch and validate event
        EventResponse event = fetchAndValidateEvent(request);

        //  Validate booking against event
        validateBookingAgainstEvent(request, event);

        //  Create and save booking entity
        Booking booking = createBookingEntity(request, userId, event);

        try {
            //  Update event service tickets
            updateEventTickets(request, event);

            //  Confirm booking
            confirmBooking(booking);

            //Get User Id from User service
            UserResponse user = getUserDetails(userId);

            //Get email from the user response
            String userEmail = user.email();

            //Build Booking confirmed event to publish to Kafka
            BookingConfirmedEvent bookingConfirmedEvent = new BookingConfirmedEvent(
                    booking.getId(),
                    booking.getUserId(),
                    userEmail,
                    event.title(),
                    booking.getNumberOfTickets(),
                    booking.getTotalPrice(),
                    booking.getBookingReference(),
                    event.startDateTime());

            //Publish Booking confirmed event to Kafka
            kafkaProducerService.publishBookingConfirmedEvent(bookingConfirmedEvent);

            log.info("Booking created successfully: bookingReference={}, eventId={}, userId={}",
                    booking.getBookingReference(), request.eventId(), userId);

        } catch (FeignException e) {
            // Handle event service failure
            handleEventServiceFailure(booking, e);
            throw new BookingException("Failed to reserve tickets. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error during booking creation for bookingReference={}: ",
                    booking.getBookingReference(), e);
        }

        return BookingResponse.from(booking);
    }

    @Override
    public PagedResponse<BookingResponse> getMyBookings(UUID userId, int page, int pageSize) {
        // Create Pageable object
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        // Query with specification and pagination
        Page<Booking> bookingsPage = bookingRepository.findByUserId(userId, pageable);

        // Convert entities to DTOs
        Page<BookingResponse> responsePage = bookingsPage.map(BookingResponse::from);

        return PagedResponse.of(responsePage);
    }

    @Override
    public BookingResponse getBookingById(UUID bookingId, UUID userId) {
        //Check if booking exists
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Booking Not Found"));

        //Check if booking belongs to the user
        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this booking");
        }

        //Return booking after passing the checks
        return BookingResponse.from(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(UUID bookingId, UUID userId) {
        //Check if booking exists
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Booking Not Found"));

        //Check if booking belongs to the user
        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("This booking cannot be cancelled");
        }

        //Cancel Booking
        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        //Get event details
        ApiSuccessResponse<EventResponse> eventResponse = eventServiceClient.getEvent(booking.getEventId());

        EventResponse event = eventResponse.data();

        //Get User details
        UserResponse user = getUserDetails(userId);

        // Build Event: Instantiate BookingCancelledEvent
        BookingCancelledEvent bookingCancelledEvent = new BookingCancelledEvent(
                booking.getId(),
                booking.getUserId(),
                user.email(),
                event.title(),
                booking.getNumberOfTickets(),
                booking.getTotalPrice(),
                booking.getBookingReference(),
                event.startDateTime());

        // Publish to Kafka
        kafkaProducerService.publishBookingCancelledEvent(bookingCancelledEvent);

        // ✅ NEW: Return tickets to Event Service
        try {
            returnTicketsToEvent(booking);
            log.info("Tickets returned to event: bookingId={}, eventId={}, tickets={}",
                    bookingId, booking.getEventId(), booking.getNumberOfTickets());
        } catch (FeignException e) {
            // Log error but don't fail the cancellation
            log.error("Failed to return tickets to event service: bookingId={}", bookingId, e);
            // Booking is still cancelled, but tickets weren't returned
            // This is acceptable - better to let user cancel than block them
        }

    }

    // ==============================
    // HELPER METHODS
    // ==============================
    private void validateInputs(CreateBookingRequest request, UUID userId) {
        if (userId == null) {
            throw new BookingException("User ID cannot be null");
        }

        if (request.numberOfTickets() <= 0) {
            throw new BookingException("Number of tickets must be positive");
        }

        // Optional: Add max tickets per booking limit
        if (request.numberOfTickets() > 10) {
            throw new BookingException("Cannot book more than 10 tickets in a single booking");
        }
    }

    private EventResponse fetchAndValidateEvent(CreateBookingRequest request) {
        try {
            ApiSuccessResponse<EventResponse> eventResponse = eventServiceClient.getEvent(request.eventId());
            EventResponse event = eventResponse.data();

            if (event == null) {
                throw new BookingException("Event not found with id: " + request.eventId());
            }

            return event;

        } catch (FeignException.NotFound e) {
            throw new BookingException("Event not found with id: " + request.eventId());
        } catch (FeignException e) {
            log.error("Failed to fetch event details for eventId: {}", request.eventId(), e);
            throw new BookingException("Failed to fetch event details. Please try again.");
        }
    }

    private void validateBookingAgainstEvent(CreateBookingRequest request, EventResponse event) {
        // Check event status first (fail fast)
        if (event.status() != EventStatus.PUBLISHED) {
            throw new EventNotPublishedException(
                    String.format("Event '%s' is not published. Current status: %s",
                            event.title(), event.status()));
        }

        // Check ticket availability
        if (event.availableTickets() <= 0) {
            throw new InsufficientTicketsException("Event is sold out");
        }

        if (event.availableTickets() < request.numberOfTickets()) {
            throw new InsufficientTicketsException(
                    String.format("Only %d tickets available. Requested: %d",
                            event.availableTickets(), request.numberOfTickets()));
        }

        // Validate event price
        if (event.price() == null || event.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BookingException("Event price is invalid");
        }
    }

    private Booking createBookingEntity(CreateBookingRequest request, UUID userId, EventResponse event) {
        BigDecimal totalPrice = calculateTotalPrice(request, event);
        String bookingReference = generateUniqueBookingReference();

        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(request.eventId())
                .numberOfTickets(request.numberOfTickets())
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .bookingReference(bookingReference)
                .build();

        return bookingRepository.save(booking);
    }

    private BigDecimal calculateTotalPrice(CreateBookingRequest request, EventResponse event) {
        return event.price().multiply(BigDecimal.valueOf(request.numberOfTickets()));
    }

    private String generateUniqueBookingReference() {
        int attempts = 0;

        while (attempts < MAX_BOOKING_REFERENCE_ATTEMPTS) {
            String shortUUID = UUID.randomUUID().toString().split("-")[0];
            String bookingReference = "BOOK-" + shortUUID.toUpperCase();

            if (!bookingRepository.existsByBookingReference(bookingReference)) {
                return bookingReference;
            }

            attempts++;
            log.warn("Booking reference collision detected. Attempt: {}", attempts);
        }

        throw new BookingException("Failed to generate unique booking reference after " +
                MAX_BOOKING_REFERENCE_ATTEMPTS + " attempts");
    }

    private void updateEventTickets(CreateBookingRequest request, EventResponse event) {
        UpdateTicketsRequest updateRequest = new UpdateTicketsRequest(request.numberOfTickets());

        try {
            eventServiceClient.updateAvailableTickets(
                    request.eventId(),
                    updateRequest,
                    internalServiceKey);

            log.debug("Successfully updated tickets for eventId: {}", request.eventId());

        } catch (FeignException e) {
            log.error("Failed to update tickets for eventId: {}", request.eventId(), e);
            throw e; // Re-throw to be handled by the caller
        }
    }

    private void returnTicketsToEvent(Booking booking) {
        // Create request to INCREASE tickets (negative number)
        UpdateTicketsRequest updateRequest = new UpdateTicketsRequest(
                -booking.getNumberOfTickets() // ← Negative to ADD tickets back
        );

        // Call Event Service
        // Note: This requires updating Event Service to accept negative numbers!
        eventServiceClient.updateAvailableTickets(
                booking.getEventId(),
                updateRequest,
                internalServiceKey);
    }

    private void confirmBooking(Booking booking) {
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    private void handleEventServiceFailure(Booking booking, FeignException e) {
        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);

        log.error("Event service call failed for booking: {}. Marked as FAILED.",
                booking.getBookingReference(), e);
    }

    private UserResponse getUserDetails(UUID userId) {
        try {
            ApiSuccessResponse<UserResponse> userResponse = userServiceClient.getUserById(userId);
            return userResponse.data();
        } catch (FeignException e) {
            log.error("Failed to fetch user details for userId: {}", userId, e);
            throw new BookingException("Failed to fetch user details. Please try again.");
        }
    }
}