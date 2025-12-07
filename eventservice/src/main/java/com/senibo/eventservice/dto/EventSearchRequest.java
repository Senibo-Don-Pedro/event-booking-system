package com.senibo.eventservice.dto;

import com.senibo.eventservice.enums.EventCategory;
import com.senibo.eventservice.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Search filters for events")
public record EventSearchRequest(
    
    @Schema(description = "Filter by category", example = "CONFERENCE")
    EventCategory category,
    
    @Schema(description = "Filter by city", example = "Lagos")
    String city,
    
    @Schema(description = "Filter by status", example = "PUBLISHED")
    EventStatus status,
    
    @Schema(description = "Search by title keyword", example = "Spring")
    String titleKeyword,
    
    @Schema(description = "Filter events starting after this date")
    LocalDateTime startDateAfter,
    
    @Schema(description = "Filter by organizer ID")
    UUID organizerId,
    
    @Schema(description = "Page number (0-based)", example = "0")
    Integer page,
    
    @Schema(description = "Page size", example = "20")
    Integer size,
    
    @Schema(description = "Sort by field", example = "startDateTime")
    String sortBy,
    
    @Schema(description = "Sort direction (ASC or DESC)", example = "ASC")
    String sortDirection
    
) {
    // Default values
    public EventSearchRequest {
        page = (page != null && page >= 0) ? page : 0;
        size = (size != null && size > 0 && size <= 100) ? size : 20;
        sortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy : "startDateTime";
        sortDirection = (sortDirection != null && !sortDirection.isBlank()) ? sortDirection : "ASC";
    }
}