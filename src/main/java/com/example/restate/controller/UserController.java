package com.example.restate.controller;

import com.example.restate.dto.RegisterRequest;
import com.example.restate.dto.UpdateUserDTO;
import com.example.restate.dto.UserProfileDTO;
import com.example.restate.entity.User;
import com.example.restate.exception.ResourceNotFoundException;
import com.example.restate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import com.example.restate.entity.Role;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Available for admin only")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Available for admin only")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Admin only")
    public ResponseEntity<UserProfileDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        User existingUser = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        User userToUpdate = updateUserDTO.toEntity(existingUser);
        User updated = userService.update(id, userToUpdate);

        return ResponseEntity.ok(UserProfileDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Admin only")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin")
    @Operation(summary = "Utwórz nowego administratora")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> createAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        User newAdmin = new User();
        newAdmin.setUsername(registerRequest.getUsername());
        newAdmin.setPassword(registerRequest.getPassword());
        newAdmin.setEmail(registerRequest.getEmail());
        newAdmin.setFirstName(registerRequest.getFirstName());
        newAdmin.setLastName(registerRequest.getLastName());
        newAdmin.setRole(Role.ADMIN);
        newAdmin.setEnabled(true);
        
        User savedAdmin = userService.save(newAdmin);

        UserProfileDTO responseDTO = UserProfileDTO.fromEntity(savedAdmin);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Get current user profile", description = "Available for all authenticated users")
    public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .map(UserProfileDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Get current user profile", description = "Available for all authenticated users")
    public ResponseEntity<UserProfileDTO> getCurrentUserMe(Authentication authentication) {
        return getCurrentUser(authentication);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Update current user profile", description = "Available for all authenticated users")
    public ResponseEntity<UserProfileDTO> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        String username = authentication.getName();
        User existingUser = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        User userToUpdate = updateUserDTO.toEntity(existingUser);
        User updated = userService.update(existingUser.getId(), userToUpdate);

        return ResponseEntity.ok(UserProfileDTO.fromEntity(updated));
    }
}