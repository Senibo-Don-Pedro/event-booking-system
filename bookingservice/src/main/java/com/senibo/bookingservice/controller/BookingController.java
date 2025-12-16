package com.senibo.bookingservice.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.senibo.bookingservice.dto.ApiSuccessResponse;
import com.senibo.bookingservice.dto.BookingResponse;
import com.senibo.bookingservice.dto.CreateBookingRequest;
import com.senibo.bookingservice.dto.PagedResponse;
import com.senibo.bookingservice.exception.UnauthorizedException;
import com.senibo.bookingservice.service.BookingService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
  private final BookingService bookingService;

  // ==================== HELPER METHOD ====================

  /**
   * Extract organizerId from JWT token in SecurityContext.
   * The subject of the JWT contains the user's UUID.
   */
  private UUID getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("User not authenticated");
    }

    String userId = authentication.getName(); // From SecurityContext

    try {
      return UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      throw new UnauthorizedException("Invalid user ID in token");
    }
  }

  // ==================== ENDPOINTS ====================

  // ✅ Using the helper in endpoints
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiSuccessResponse<BookingResponse> createBooking(
      @Valid @RequestBody CreateBookingRequest request
      ) {

    UUID userId = getAuthenticatedUserId(); // ← Easy!

    BookingResponse booking = bookingService.createBooking(request, userId);

    return ApiSuccessResponse.of(booking, "Booking created successfully");
  }

  @GetMapping("/my-bookings")
  public ApiSuccessResponse<PagedResponse<BookingResponse>> getMyBookings(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int pageSize) {

    UUID userId = getAuthenticatedUserId(); // ✅ Get from JWT

    PagedResponse<BookingResponse> bookings = bookingService.getMyBookings(userId, page, pageSize);

    return ApiSuccessResponse.of(bookings, "Your bookings retrieved successfully");
  };

  @GetMapping("/{bookingId}")
  public ApiSuccessResponse<BookingResponse> getBookingById(
      @Parameter(description = "Booking ID", required = true) @PathVariable UUID bookingId) {
    UUID userId = getAuthenticatedUserId(); // ✅ Get from JWT

    BookingResponse booking = bookingService.getBookingById(bookingId, userId);

    return ApiSuccessResponse.of(booking, "Your booking retrieved successfully");
  }

  @DeleteMapping("/{bookingId}")
  public ApiSuccessResponse<String> deleteBooking(
      @Parameter(description = "Booking ID", required = true) @PathVariable UUID bookingId
    ) {
    UUID userId = getAuthenticatedUserId(); // ✅ Get from JWT

    bookingService.deleteBooking(bookingId, userId);

    String message = "Booking cancelled successfully";

    return ApiSuccessResponse.of(message);
  }

}
