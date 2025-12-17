package com.senibo.notificationservice.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.senibo.notificationservice.event.BookingCancelledEvent;
import com.senibo.notificationservice.event.BookingConfirmedEvent;
import com.senibo.notificationservice.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
  // Inject JavaMailSender to handle email sending
  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Override
  @Async // Optional: Executes in a separate thread so it doesn't block the consumer
  public void sendVerificationEmail(String to, String username, String token) {
    String subject = "Verify your Email";
    String url = "http://localhost:8082/api/auth/verify?token=" + token;

    String html = String.format(
        """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #2c3e50;">Welcome to Senibo Events!</h2>
                <p>Hi %s,</p>
                <p>Thank you for joining us. Please click the button below to verify your account:</p>
                <a href="%s" style="background-color: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 10px;">Verify Account</a>
                <p style="margin-top: 20px; font-size: 12px; color: #7f8c8d;">Link expires in 24 hours.</p>
            </div>
            """,
        username, url);

    sendHtmlEmail(to, subject, html);
  }

  @Override
  @Async
  public void sendWelcomeEmail(String to, String username) {
    String subject = "Welcome to the Family!";
    String html = String.format(
        """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #27ae60;">Account Verified!</h2>
                <p>Hi %s,</p>
                <p>Your email has been successfully verified. You can now log in and start booking tickets for the hottest events.</p>
                <p>See you inside!</p>
            </div>
            """,
        username);

    sendHtmlEmail(to, subject, html);
  }

  @Override
  @Async
  public void sendBookingConfirmationEmail(String to, String username, BookingConfirmedEvent event) {
    String subject = "Booking Confirmed: " + event.eventTitle();
    String html = String.format(
        """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #2c3e50;">Booking Confirmed!</h2>
                <p>Hi %s,</p>
                <p>Your tickets are ready. Here are the details:</p>
                <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 10px 0;">
                    <p><strong>Event:</strong> %s</p>
                    <p><strong>Date:</strong> %s</p>
                    <p><strong>Ref:</strong> %s</p>
                    <p><strong>Tickets:</strong> %d</p>
                    <p><strong>Total Price:</strong> $%s</p>
                </div>
                <p>Please show this email at the entrance.</p>
            </div>
            """,
        username, event.eventTitle(), event.eventDate(), event.bookingReference(), event.numberOfTickets(),
        event.totalPrice());

    sendHtmlEmail(to, subject, html);
  }

  @Override
  @Async
  public void sendBookingCancellationEmail(String to, String username, BookingCancelledEvent event) {
    String subject = "Booking Cancelled: " + event.eventTitle();
    String html = String.format(
        """
             <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                <h2 style="color: #c0392b;">Booking Cancelled</h2>
                <p>Hi %s,</p>
                <p>We have processed your cancellation for <strong>%s</strong>.</p>
                <p>We hope to see you at another event soon.</p>
            </div>
            """,
        username, event.eventTitle());

    sendHtmlEmail(to, subject, html);
  }

  private void sendHtmlEmail(String to, String subject, String htmlBody) {
    // Implementation for sending HTML email
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true); // true indicates HTML

      mailSender.send(message);
      log.info("Email sent to {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send email to: {}", to, e);
    }
  }
}
