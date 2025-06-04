
package com.example.restate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMieszkanieDTO {
    
    private String developer;
    private String investment;
    private String number;

    @DecimalMin(value = "0.1", message = "Area must be positive")
    private BigDecimal area;

    @DecimalMin(value = "1", message = "Price must be positive")
    private BigDecimal price;

    private String voivodeship;
    private String city;
    private String district;

    @Min(value = 0, message = "Floor cannot be negative")
    private Integer floor;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}