package com.senibo.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated response wrapper")
public record PagedResponse<T>(

    @Schema(description = "List of items") List<T> content,

    @Schema(description = "Current page number (0-based)") int pageNumber,

    @Schema(description = "Number of items per page") int pageSize,

    @Schema(description = "Total number of items") long totalElements,

    @Schema(description = "Total number of pages") int totalPages,

    @Schema(description = "Is this the first page?") boolean first,

    @Schema(description = "Is this the last page?") boolean last,

    @Schema(description = "Is the page empty?") boolean empty

) {
  // Factory method to create from Spring Page
  public static <T> PagedResponse<T> of(Page<T> page) {
    return new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast(),
        page.isEmpty());
  }
}
