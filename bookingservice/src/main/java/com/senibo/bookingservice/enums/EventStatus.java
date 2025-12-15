package com.senibo.bookingservice.enums;

public enum EventStatus {
  DRAFT("Event created but not visible to public"),
  PUBLISHED("Event is live and accepting bookings"),
  CANCELLED("Event was cancelled"),
  COMPLETED("Event has finished");

  private final String description;

  EventStatus(String description){
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
