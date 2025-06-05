package com.example.restate.controller;

import com.example.restate.dto.AuthRequest;
import com.example.restate.dto.AuthResponse;
import com.example.restate.dto.RegisterRequest;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.util.JwtUtil;
import com.example.restate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    @Operation(summary = "Login with username and password", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        try {
            // Sprawdź czy użytkownik istnieje
            if (!userService.existsByUsername(request.getUsername())) {
                log.warn("User not found: {}", request.getUsername());
                return ResponseEntity.badRequest().build();
            }

            // Autentyfikacja przez AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            log.info("Authentication successful for user: {}", request.getUsername());

            // Pobranie UserDetails po udanej autentyfikacji
            final UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(new AuthResponse(userDetails.getUsername(), jwt));

        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", request.getUsername());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Authentication error for user: {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with USER role")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        try {
            // Check if username already exists
            if (userService.existsByUsername(request.getUsername())) {
                log.warn("Username already exists: {}", request.getUsername());
                return ResponseEntity.badRequest().body("Username already exists");
            }

            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                log.warn("Email already exists: {}", request.getEmail());
                return ResponseEntity.badRequest().body("Email already exists");
            }

            // Create new user entity
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword()); // Will be encoded by the service
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRole(Role.USER); // Default role is USER
            user.setEnabled(true);

            // Register the user
            User registeredUser = userService.registerUser(user);
            log.info("User registered successfully: {}", registeredUser.getUsername());

            // Generate token for the new user
            final String jwt = jwtUtil.generateToken(registeredUser.getUsername());

            // Return the authentication response
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(registeredUser.getUsername(), jwt));

        } catch (Exception e) {
            log.error("Registration error for user: {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }
}
