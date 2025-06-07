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


        ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                authRequest,
                Object.class
        );


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testRegisterWithExistingUsername() {
        // Utwórz użytkownika
        User existingUser = new User();
        existingUser.setUsername(TEST_USERNAME);
        existingUser.setEmail("different@example.com");
        existingUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setRole(Role.USER);
        userRepository.save(existingUser);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME); // Ten sam username
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                registerRequest,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Username already exists"));
    }

    @Test
    void testRegisterWithExistingEmail() {
        // Podobny test dla istniejącego emaila
        User existingUser = new User();
        existingUser.setUsername("different");
        existingUser.setEmail(TEST_EMAIL);
        existingUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setRole(Role.USER);
        userRepository.save(existingUser);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail(TEST_EMAIL); // Ten sam email
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                registerRequest,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Email already exists"));
    }

    @Test
    void testLoginWithNonExistentUser() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("nonexistent");
        authRequest.setPassword("password");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                authRequest,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testRegisterWithInvalidData() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(""); // Pusty username
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("123"); // Za krótkie hasło
        registerRequest.setFirstName("");
        registerRequest.setLastName("");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                registerRequest,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testRegisterWithNullData() {
        RegisterRequest registerRequest = new RegisterRequest();
        // Wszystkie pola null

        ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                registerRequest,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testLoginWithEmptyCredentials() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("");
        authRequest.setPassword("");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                authRequest,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testLoginWithNullCredentials() {
        AuthRequest authRequest = new AuthRequest();
        // username i password pozostają null

        ResponseEntity<Object> response = restTemplate.postForEntity(
                BASE_URL + "/login",
                authRequest,
                Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}