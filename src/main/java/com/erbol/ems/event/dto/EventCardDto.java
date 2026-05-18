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
public class EventCardDto {
    private Long id;
    private String title;
    private String shortDescription;
    private String location;
    private LocalDateTime startAt;
    private BigDecimal price;
    private boolean free;
    private int capacity;
    private String categoryName;
    private String organizerName;
    private String coverImageUrl;
}