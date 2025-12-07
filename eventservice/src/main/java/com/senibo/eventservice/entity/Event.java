package com.senibo.eventservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.senibo.eventservice.enums.EventCategory;
import com.senibo.eventservice.enums.EventStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  //Basic Information
  @Column(nullable = false, length = 200)
  private String title;

  @Column
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventCategory category;

  @Column
  private String imageUrl;

  //Date & Location
  @Column(nullable = false)
  private LocalDateTime startDateTime;

  @Column(nullable = false)
  private LocalDateTime endDateTime;

  @Column(nullable = false)
  private String venue;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String city;

  // Capacity & Pricing
  @Column(nullable = false)
  private Integer capacity;

  @Column(nullable = false)
  private Integer availableTickets;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  // Status & Ownership
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventStatus status;

  @Column(nullable = false)
  private UUID organizerId;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  // Good practice to make it non-nullable and non-updatable
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false) // Good practice to make it non-nullable
  private LocalDateTime updatedAt;
}
