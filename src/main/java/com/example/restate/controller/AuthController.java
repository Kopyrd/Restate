package com.example.restate.controller;

import com.example.restate.dto.AuthRequest;
import com.example.restate.dto.AuthResponse;
import com.example.restate.util.JwtUtil;
import com.example.restate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
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
}