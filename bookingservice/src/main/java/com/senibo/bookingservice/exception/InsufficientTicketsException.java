package com.senibo.bookingservice.exception;

public class InsufficientTicketsException extends RuntimeException {
  public InsufficientTicketsException(String message) {
    super(message);
  }
}
