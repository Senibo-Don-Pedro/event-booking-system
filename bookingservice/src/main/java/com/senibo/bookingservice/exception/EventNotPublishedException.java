package com.senibo.bookingservice.exception;

public class EventNotPublishedException extends RuntimeException {
    public EventNotPublishedException(String message) {
        super(message);
    }
}