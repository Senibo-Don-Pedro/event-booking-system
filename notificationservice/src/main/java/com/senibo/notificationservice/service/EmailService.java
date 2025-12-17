package com.senibo.notificationservice.service;

import com.senibo.notificationservice.event.BookingCancelledEvent;
import com.senibo.notificationservice.event.BookingConfirmedEvent;

public interface EmailService {
    void sendVerificationEmail(String to, String username, String verificationToken);
    void sendWelcomeEmail(String to, String username);
    void sendBookingConfirmationEmail(String to, String username, BookingConfirmedEvent event);
    void sendBookingCancellationEmail(String to, String username, BookingCancelledEvent event);
}