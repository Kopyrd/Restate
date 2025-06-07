package com.example.restate.integration;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.AuthRequest;
import com.example.restate.dto.CreateMieszkanieDTO;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.repository.MieszkanieRepository;
import com.example.restate.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for exception handling.
 * Tests the GlobalExceptionHandler with real HTTP requests.
 */
public class ExceptionHandlingIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MieszkanieRepository mieszkanieRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth/login";
    private static final String MIESZKANIE_URL = "/api/mieszkania";
    private static final String USER_URL = "/api/users";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin123!";

    private String adminToken;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        // Clean up the database
        mieszkanieRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user
        User adminUser = new User();
        adminUser.setUsername(ADMIN_USERNAME);
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        // Login as admin and get token
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(ADMIN_USERNAME);
        authRequest.setPassword(ADMIN_PASSWORD);

        ResponseEntity<Object> authResponse = restTemplate.postForEntity(
                AUTH_URL,
                authRequest,
                Object.class
        );

        // Extract token from response
        if (authResponse.getStatusCode() == HttpStatus.OK) {
            @SuppressWarnings("unchecked")
            var body = (java.util.Map<String, String>) authResponse.getBody();
            adminToken = body.get("token");
        }

        // Set up headers with token
        headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @AfterEach
    void tearDown() {
        // Clean up the database
        mieszkanieRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testResourceNotFoundException() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Object> response = restTemplate.exchange(
                MIESZKANIE_URL + "/999",
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertEquals("Resource not found", body.get("message"));
        assertTrue(body.get("details").toString().contains("Mieszkanie not found with id: 999"));
    }

    @Test
    void testMethodArgumentNotValidException() {
        // Given
        CreateMieszkanieDTO invalidDto = new CreateMieszkanieDTO();
        // Missing required fields

        HttpEntity<CreateMieszkanieDTO> requestEntity = new HttpEntity<>(invalidDto, headers);

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                MIESZKANIE_URL,
                HttpMethod.POST,
                requestEntity,
                Object.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertEquals("Validation failed", body.get("message"));
    }

    @Test
    void testHttpMessageNotReadableException() {
        // Given
        String invalidJson = "{\"price\": \"not-a-number\"}";

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setBearerAuth(adminToken);
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(invalidJson, jsonHeaders);

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                MIESZKANIE_URL,
                HttpMethod.POST,
                requestEntity,
                Object.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertEquals("Invalid request format", body.get("message"));
    }

    @Test
    void testAccessDeniedException() {
        // Given - create a regular user without admin rights
        User regularUser = new User();
        regularUser.setUsername("regular");
        regularUser.setEmail("regular@example.com");
        regularUser.setPassword(passwordEncoder.encode("Regular123!"));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRole(Role.USER);
        userRepository.save(regularUser);

        // Login as regular user
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("regular");
        authRequest.setPassword("Regular123!");

        ResponseEntity<Object> authResponse = restTemplate.postForEntity(
                AUTH_URL,
                authRequest,
                Object.class
        );

        // Extract token
        String regularToken = null;
        if (authResponse.getStatusCode() == HttpStatus.OK) {
            @SuppressWarnings("unchecked")
            var body = (java.util.Map<String, String>) authResponse.getBody();
            regularToken = body.get("token");
        }

        // Set up headers with regular user token
        HttpHeaders regularHeaders = new HttpHeaders();
        regularHeaders.setBearerAuth(regularToken);
        regularHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Try to access admin-only endpoint
        HttpEntity<Void> requestEntity = new HttpEntity<>(regularHeaders);

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                USER_URL + "/all",
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        // Then
        // The response could be FORBIDDEN (403) or UNAUTHORIZED (401) depending on security configuration
        assertTrue(
            response.getStatusCode() == HttpStatus.FORBIDDEN || 
            response.getStatusCode() == HttpStatus.UNAUTHORIZED,
            "Expected status code to be either FORBIDDEN or UNAUTHORIZED, but was " + response.getStatusCode()
        );
    }

    @Test
    void testAuthenticationException() {
        // Given - invalid credentials
        AuthRequest invalidAuthRequest = new AuthRequest();
        invalidAuthRequest.setUsername(ADMIN_USERNAME);
        invalidAuthRequest.setPassword("WrongPassword");

        // When
        ResponseEntity<Object> response = restTemplate.postForEntity(
                AUTH_URL,
                invalidAuthRequest,
                Object.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String, Object>) response.getBody();
        assertEquals("Authentication failed", body.get("message"));
    }

    @Test
    void testConstraintViolationException() {
        // Given
        CreateMieszkanieDTO invalidDto = new CreateMieszkanieDTO();
        invalidDto.setDeveloper("Developer");
        invalidDto.setInvestment("Investment");
        invalidDto.setNumber("A1");
        invalidDto.setArea(BigDecimal.valueOf(-10)); // Negative area should trigger constraint violation
        invalidDto.setPrice(BigDecimal.valueOf(500000));
        invalidDto.setVoivodeship("Mazowieckie");
        invalidDto.setCity("Warsaw");

        HttpEntity<CreateMieszkanieDTO> requestEntity = new HttpEntity<>(invalidDto, headers);

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                MIESZKANIE_URL,
                HttpMethod.POST,
                requestEntity,
                Object.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
