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
    private String internalServiceKey;

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public BookingResponse createBooking(CreateBookingRequest request, UUID userId) {
        // --- STEP 1: VALIDATION (Read Only) ---
        validateInputs(request, userId);
        EventResponse event = fetchAndValidateEvent(request);
        validateBookingAgainstEvent(request, event);

        // --- STEP 2: INVENTORY RESERVATION (Critical - External Call) ---
        // We do this BEFORE saving to our DB. If this fails, the method exits,
        // nothing is saved, and we don't need manual rollbacks.
        try {
            updateEventTickets(request, event);
        } catch (FeignException e) {
            log.error("Failed to reserve tickets for event: {}", request.eventId(), e);
            throw new BookingException("Failed to reserve tickets. Please try again.");
        }

        // --- STEP 3: PERSISTENCE (Critical - Database) ---
        // Inventory is reserved, so we save the booking as CONFIRMED immediately.
        Booking booking = createBookingEntity(request, userId, event);

        // --- STEP 4: NOTIFICATIONS (Non-Critical / Best Effort) ---
        // We wrap this in a separate try-catch so it DOES NOT rollback the transaction.
        // If the email server fails, the user still has a valid booking.
        try {
            // Fetch User Email (Token Relay handles authentication)
            UserResponse user = getUserDetails(userId);

            BookingConfirmedEvent bookingConfirmedEvent = new BookingConfirmedEvent(
                    booking.getId(),
                    booking.getUserId(),
                    user.email(),
                    event.title(),
                    booking.getNumberOfTickets(),
                    booking.getTotalPrice(),
                    booking.getBookingReference(),
                    event.startDateTime());

            kafkaProducerService.publishBookingConfirmedEvent(bookingConfirmedEvent);

            log.info("Booking created and notification sent: bookingReference={}, userId={}",
                    booking.getBookingReference(), userId);

        } catch (Exception e) {
            // Log and swallow exception. Do NOT fail the booking.
            log.error("Booking confirmed but failed to send notification for bookingRef: {}",
                    booking.getBookingReference(), e);
        }

        return BookingResponse.from(booking);
    }

    @Override
    public PagedResponse<BookingResponse> getMyBookings(UUID userId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Booking> bookingsPage = bookingRepository.findByUserId(userId, pageable);
        return PagedResponse.of(bookingsPage.map(BookingResponse::from));
    }

    @Override
    public BookingResponse getBookingById(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Booking Not Found"));

        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this booking");
        }

        return BookingResponse.from(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new NotFoundException("Booking Not Found"));

        if (!booking.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to modify this booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("This booking cannot be cancelled");
        }

        // 1. Cancel in DB
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // 2. Return Tickets (Best Effort - Log on failure)
        try {
            returnTicketsToEvent(booking);
            log.info("Tickets returned to event: bookingId={}, eventId={}, tickets={}",
                    bookingId, booking.getEventId(), booking.getNumberOfTickets());
        } catch (FeignException e) {
            log.error("Failed to return tickets to event service: bookingId={}", bookingId, e);
            // We still proceed with cancellation because the user shouldn't be blocked
        }

        // 3. Send Notification (Best Effort)
        try {
            // We need event details for the email title/date
            EventResponse event = fetchEventDetailsSafe(booking.getEventId());
            UserResponse user = getUserDetails(userId);

            if (event != null && user != null) {
                BookingCancelledEvent bookingCancelledEvent = new BookingCancelledEvent(
                        booking.getId(),
                        booking.getUserId(),
                        user.email(),
                        event.title(),
                        booking.getNumberOfTickets(),
                        booking.getTotalPrice(),
                        booking.getBookingReference(),
                        event.startDateTime());

                kafkaProducerService.publishBookingCancelledEvent(bookingCancelledEvent);
            }
        } catch (Exception e) {
            log.error("Booking cancelled but failed to send notification for bookingRef: {}",
                    booking.getBookingReference(), e);
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

    // A safe version of event fetching that doesn't throw exceptions (used in cancellation)
    private EventResponse fetchEventDetailsSafe(UUID eventId) {
        try {
            ApiSuccessResponse<EventResponse> eventResponse = eventServiceClient.getEvent(eventId);
            return eventResponse.data();
        } catch (Exception e) {
            log.error("Could not fetch event details for notification. eventId={}", eventId, e);
            return null; // Return null so we skip the specific email fields
        }
    }

    private void validateBookingAgainstEvent(CreateBookingRequest request, EventResponse event) {
        if (event.status() != EventStatus.PUBLISHED) {
            throw new EventNotPublishedException(
                    String.format("Event '%s' is not published. Current status: %s",
                            event.title(), event.status()));
        }

        if (event.availableTickets() <= 0) {
            throw new InsufficientTicketsException("Event is sold out");
        }

        if (event.availableTickets() < request.numberOfTickets()) {
            throw new InsufficientTicketsException(
                    String.format("Only %d tickets available. Requested: %d",
                            event.availableTickets(), request.numberOfTickets()));
        }

        if (event.price() == null || event.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new BookingException("Event price is invalid");
        }
    }

    private Booking createBookingEntity(CreateBookingRequest request, UUID userId, EventResponse event) {
        BigDecimal totalPrice = calculateTotalPrice(request, event);
        String bookingReference = generateUniqueBookingReference();

        // Optimized: Save directly as CONFIRMED since we already reserved tickets
        Booking booking = Booking.builder()
                .userId(userId)
                .eventId(request.eventId())
                .numberOfTickets(request.numberOfTickets())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED) 
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
        }
        throw new BookingException("Failed to generate unique booking reference");
    }

    private void updateEventTickets(CreateBookingRequest request, EventResponse event) {
        UpdateTicketsRequest updateRequest = new UpdateTicketsRequest(request.numberOfTickets());
        eventServiceClient.updateAvailableTickets(
                request.eventId(),
                updateRequest,
                internalServiceKey);
    }

    private void returnTicketsToEvent(Booking booking) {
        // Negative number adds tickets back
        UpdateTicketsRequest updateRequest = new UpdateTicketsRequest(-booking.getNumberOfTickets());
        eventServiceClient.updateAvailableTickets(
                booking.getEventId(),
                updateRequest,
                internalServiceKey);
    }

    private UserResponse getUserDetails(UUID userId) {
        ApiSuccessResponse<UserResponse> userResponse = userServiceClient.getUserById(userId);
        return userResponse.data();
    }
}