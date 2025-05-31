package com.example.restate.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MieszkanieSearchCriteria {
    private String developer;
    private String investment;
    private Integer rooms;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minArea;
    private BigDecimal maxArea;
    private Double lat;
    private Double lng;
    private Double radius; // w kilometrach
}