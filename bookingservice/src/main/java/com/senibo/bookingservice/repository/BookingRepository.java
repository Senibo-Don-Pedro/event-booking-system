package com.senibo.bookingservice.repository;

import java.util.Optional;
import java.util.UUID;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.senibo.bookingservice.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
  Page<Booking> findByUserId(UUID userId, Pageable pageable);

  Page<Booking> findByEventId(UUID eventId, Pageable pageable);

  Optional<Booking> findByBookingReference(String bookingReference);

  Boolean existsByBookingReference(String bookingReference);
}
