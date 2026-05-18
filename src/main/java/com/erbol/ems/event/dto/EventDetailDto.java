package com.erbol.ems.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDetailDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private int capacity;
    private BigDecimal price;
    private boolean free;
    private String categoryName;
    private String organizerName;
    private String coverImageUrl;
    private String status;
    private long seatsRemaining;
    private boolean soldOut;
}