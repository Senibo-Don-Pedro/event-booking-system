package com.senibo.bookingservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.senibo.bookingservice.enums.BookingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a ticket booking for an event.
 * Each booking is tied to a user and an event (via UUIDs).
 */
@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID of the user who made the booking.
     * References User Service - we don't have a foreign key constraint
     * because the user exists in a different database (microservices pattern).
     */
    @Column(nullable = false)
    private UUID userId;

    /**
     * ID of the event being booked.
     * References Event Service - no foreign key constraint
     * because the event exists in a different database.
     */
    @Column(nullable = false)
    private UUID eventId;

    /**
     * Number of tickets booked for this event.
     * Must be greater than 0.
     */
    @Column(nullable = false)
    private Integer numberOfTickets;

    /**
     * Total price for all tickets.
     * Calculated as: numberOfTickets × event.price
     * Stored in the booking for historical record (in case event price changes later).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Current status of the booking.
     * See BookingStatus enum for lifecycle details.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    /**
     * Unique reference code for this booking.
     * Used for customer support and ticket validation.
     * Format: BOOK-{UUID-first-8-chars} (e.g., BOOK-a1b2c3d4)
     */
    @Column(unique = true, nullable = false)
    private String bookingReference;

    /**
     * When the booking was created.
     * Set automatically by Spring Data JPA auditing.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When the booking was last updated.
     * Updated automatically by Spring Data JPA auditing.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Optimistic locking version field.
     * Prevents concurrent booking modifications (race conditions).
     * Automatically managed by JPA - increments on each update.
     */
    @Version
    private Long version;
}