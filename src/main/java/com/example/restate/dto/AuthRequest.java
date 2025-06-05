package com.example.restate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication request")
public class AuthRequest {
    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "admin", defaultValue = "admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "password123", defaultValue = "password123")
    private String password;
}