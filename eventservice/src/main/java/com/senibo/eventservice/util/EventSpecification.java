package com.senibo.eventservice.util;

import com.senibo.eventservice.entity.Event;
import com.senibo.eventservice.enums.EventCategory;
import com.senibo.eventservice.enums.EventStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventSpecification {

  // Filter by category
  public static Specification<Event> hasCategory(EventCategory category) {
    return (root, query, criteriaBuilder) -> {
      if (category == null) {
        return null; // No filter
      }
      return criteriaBuilder.equal(root.get("category"), category);
    };
  }

  // Filter by city
  public static Specification<Event> hasCity(String city) {
    return (root, query, criteriaBuilder) -> {
      if (city == null || city.isBlank()) {
        return null;
      }
      return criteriaBuilder.equal(root.get("city"), city);
    };
  }

  // Filter by status
  public static Specification<Event> hasStatus(EventStatus status) {
    return (root, query, criteriaBuilder) -> {
      if (status == null) {
        return null;
      }
      return criteriaBuilder.equal(root.get("status"), status);
    };
  }

  // Search by title (contains text)
  public static Specification<Event> titleContains(String title) {
    return (root, query, criteriaBuilder) -> {
      if (title == null || title.isBlank()) {
        return null;
      }
      return criteriaBuilder.like(
          criteriaBuilder.lower(root.get("title")),
          "%" + title.toLowerCase() + "%");
    };
  }

  // Filter by start date after
  public static Specification<Event> startDateAfter(LocalDateTime date) {
    return (root, query, criteriaBuilder) -> {
      if (date == null) {
        return null;
      }
      return criteriaBuilder.greaterThanOrEqualTo(root.get("startDateTime"), date);
    };
  }

  // Filter by organizer
  public static Specification<Event> hasOrganizer(UUID organizerId) {
    return (root, query, criteriaBuilder) -> {
      if (organizerId == null) {
        return null;
      }
      return criteriaBuilder.equal(root.get("organizerId"), organizerId);
    };
  }

  // Combine multiple filters (you'll use this in service)
  public static Specification<Event> buildSearchSpec(
      EventCategory category,
      String city,
      EventStatus status,
      String titleKeyword,
      LocalDateTime startDateAfter,
      UUID organizerId) {
    return Specification
        .allOf(
            hasCategory(category),
            hasCity(city),
            hasStatus(status),
            titleContains(titleKeyword),
            startDateAfter(startDateAfter),
            hasOrganizer(organizerId));
  }
}

// Understanding Specifications:
// java(root, query, criteriaBuilder) -> {
//     // root = Event entity (access fields)
//     // query = The SQL query being built
//     // criteriaBuilder = Build conditions (equal, like, greaterThan, etc.)
// }