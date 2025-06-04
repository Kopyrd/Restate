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
public class CreateMieszkanieDTO {
    
    @NotBlank(message = "Developer is required")
    private String developer;

    @NotBlank(message = "Investment is required")
    private String investment;

    @NotBlank(message = "Number is required")
    private String number;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "0.1", message = "Area must be positive")
    private BigDecimal area;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "1", message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Voivodeship is required")
    private String voivodeship;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "District is required")
    private String district;

    @NotNull(message = "Floor is required")
    @Min(value = 0, message = "Floor cannot be negative")
    private Integer floor;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}