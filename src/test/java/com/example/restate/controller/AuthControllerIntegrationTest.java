package com.example.restate.controller;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.AuthRequest;
import com.example.restate.dto.AuthResponse;
import com.example.restate.dto.RegisterRequest;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the AuthController using a real PostgreSQL database in a Docker container.
 */
public class AuthControllerIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BASE_URL = "/api/auth";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "Password123!";

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser() {
        // Given
        // First create an admin user and get a token
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("Admin123!"));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        // Login as admin
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("Admin123!");

        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                BASE_URL + "/login",
                authRequest,
                AuthResponse.class
        );

        String adminToken = authResponse.getBody().getToken();

        // Set up headers with admin token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        HttpEntity<RegisterRequest> requestEntity = new HttpEntity<>(registerRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/register",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Print debug information
        System.out.println("[DEBUG_LOG] Register response status: " + response.getStatusCode());
        System.out.println("[DEBUG_LOG] Register response body: " + response.getBody());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify user was created in the database
        User user = userRepository.findByUsername(TEST_USERNAME).orElse(null);
        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals(Role.USER, user.getRole());
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, user.getPassword()));
    }

    @Test
    void testLoginUser() {
        // Given
        // Create a user first
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        userRepository.save(user);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(TEST_USERNAME);
        authRequest.setPassword(TEST_PASSWORD);

        // When
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                authRequest,
                AuthResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        // Given
        // Create a user first
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        userRepository.save(user);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(TEST_USERNAME);
        authRequest.setPassword("WrongPassword123!");

        // When
        ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                authRequest,
                Object.class
        );

        // Then
        // The AuthController returns BAD_REQUEST (400) for invalid credentials, not UNAUTHORIZED (401)
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
