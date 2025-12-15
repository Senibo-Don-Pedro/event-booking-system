package com.senibo.bookingservice.enums;

/**
 * Represents the lifecycle status of a booking.
 * 
 * Status Flow:
 * PENDING → CONFIRMED (success path)
 * PENDING → FAILED (event service call failed)
 * CONFIRMED → CANCELLED (user cancels booking)
 */
public enum BookingStatus {
  /**
   * Initial state after booking is created.
   * Waiting for Event Service to confirm ticket availability.
   */
  PENDING,

  /**
   * Event Service successfully updated available tickets.
   * Booking is confirmed and user has reserved tickets.
   */
  CONFIRMED,

  /**
   * Event Service call failed (timeout, conflict, or service down).
   * Booking exists in DB but no tickets were reserved.
   */
  FAILED,

  /**
   * User or admin cancelled a confirmed booking.
   * Tickets should be returned to available pool.
   */
  CANCELLED
}