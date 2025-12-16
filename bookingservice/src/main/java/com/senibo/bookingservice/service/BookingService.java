package com.senibo.bookingservice.service;

import java.util.UUID;

import com.senibo.bookingservice.dto.BookingResponse;
import com.senibo.bookingservice.dto.CreateBookingRequest;
import com.senibo.bookingservice.dto.PagedResponse;

public interface BookingService {

  BookingResponse createBooking(CreateBookingRequest request, UUID userId);

  PagedResponse<BookingResponse> getMyBookings(UUID userId, int page, int pageSize);

  BookingResponse getBookingById(UUID bookingId, UUID userId);

  void deleteBooking(UUID bookingId, UUID userId);
  
}
