package com.example.restate.dto;

import com.example.restate.entity.Mieszkanie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode; // ✅ DODAJ IMPORT

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MieszkanieDTO {
    private Integer id;
    private String developer;
    private String investment;
    private String number;
    private BigDecimal area;
    private BigDecimal price;
    private String voivodeship;
    private String city;
    private String district;
    private Integer floor;
    private Mieszkanie.Status status;
    private String description;
    private BigDecimal pricePerMeter;

    // Factory Method Pattern
    public static MieszkanieDTO fromEntity(Mieszkanie mieszkanie) {
        return MieszkanieDTO.builder()
                .id(mieszkanie.getId())
                .developer(mieszkanie.getDeveloper())
                .investment(mieszkanie.getInvestment())
                .number(mieszkanie.getNumber())
                .area(mieszkanie.getArea())
                .price(mieszkanie.getPrice())
                .voivodeship(mieszkanie.getVoivodeship())
                .city(mieszkanie.getCity())
                .district(mieszkanie.getDistrict())
                .floor(mieszkanie.getFloor())
                .status(mieszkanie.getStatus())
                .description(mieszkanie.getDescription())
                .pricePerMeter(calculatePricePerMeter(mieszkanie))
                .build();
    }

    private static BigDecimal calculatePricePerMeter(Mieszkanie mieszkanie) {
        if (mieszkanie.getPrice() != null && mieszkanie.getArea() != null
                && mieszkanie.getArea().compareTo(BigDecimal.ZERO) > 0) {
            return mieszkanie.getPrice().divide(mieszkanie.getArea(), 2, RoundingMode.HALF_UP);
        }
        return null;
    }
}