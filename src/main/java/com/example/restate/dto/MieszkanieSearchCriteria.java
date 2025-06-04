package com.example.restate.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MieszkanieSearchCriteria {
    private String developer;
    private String investment;
    private String city;
    private String district;
    private String voivodeship;
    private Integer floor;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minArea;
    private BigDecimal maxArea;
    private String status;
}
