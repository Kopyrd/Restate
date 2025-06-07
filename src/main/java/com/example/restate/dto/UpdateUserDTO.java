package com.example.restate.dto;

import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserDTO {
    private Long id;
    private String username;
    private String password;

    @Email
    private String email;

    private String firstName;
    private String lastName;
    private Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;

    public User toEntity(User existingUser) {
        if (username != null) existingUser.setUsername(username);
        if (email != null) existingUser.setEmail(email);
        if (firstName != null) existingUser.setFirstName(firstName);
        if (lastName != null) existingUser.setLastName(lastName);
        if (role != null) existingUser.setRole(role);
        if (enabled != null) existingUser.setEnabled(enabled);
        if (password != null && !password.isEmpty()) existingUser.setPassword(password);

        return existingUser;
    }
}
