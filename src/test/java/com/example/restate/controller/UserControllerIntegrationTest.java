package com.example.restate.controller;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.AuthRequest;
import com.example.restate.dto.AuthResponse;
import com.example.restate.dto.UpdateUserDTO;
import com.example.restate.dto.UserProfileDTO;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the UserController using a real PostgreSQL database in a Docker container.
 */
public class UserControllerIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth/login";
    private static final String BASE_URL = "/api/users";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String USER_USERNAME = "user";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_PASSWORD = "User123!";

    private String adminToken;
    private String userToken;
    private HttpHeaders adminHeaders;
    private HttpHeaders userHeaders;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        // Clean up the database
        userRepository.deleteAll();

        // Create admin user
        adminUser = new User();
        adminUser.setUsername(ADMIN_USERNAME);
        adminUser.setEmail(ADMIN_EMAIL);
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);

        // Create regular user
        regularUser = new User();
        regularUser.setUsername(USER_USERNAME);
        regularUser.setEmail(USER_EMAIL);
        regularUser.setPassword(passwordEncoder.encode(USER_PASSWORD));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRole(Role.USER);
        userRepository.save(regularUser);

        // Login as admin and get token
        AuthRequest adminAuthRequest = new AuthRequest();
        adminAuthRequest.setUsername(ADMIN_USERNAME);
        adminAuthRequest.setPassword(ADMIN_PASSWORD);

        ResponseEntity<AuthResponse> adminAuthResponse = restTemplate.postForEntity(
                AUTH_URL,
                adminAuthRequest,
                AuthResponse.class
        );

        adminToken = Objects.requireNonNull(adminAuthResponse.getBody()).getToken();

        // Login as regular user and get token
        AuthRequest userAuthRequest = new AuthRequest();
        userAuthRequest.setUsername(USER_USERNAME);
        userAuthRequest.setPassword(USER_PASSWORD);

        ResponseEntity<AuthResponse> userAuthResponse = restTemplate.postForEntity(
                AUTH_URL,
                userAuthRequest,
                AuthResponse.class
        );

        userToken = Objects.requireNonNull(userAuthResponse.getBody()).getToken();

        // Set up headers with tokens
        adminHeaders = new HttpHeaders();
        adminHeaders.setBearerAuth(adminToken);
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);

        userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(userToken);
        userHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @AfterEach
    void tearDown() {
        // Clean up the database
        userRepository.deleteAll();
    }

    @Test
    void testGetCurrentUser() {
        // Given
        HttpEntity<Void> requestEntity = new HttpEntity<>(userHeaders);

        // When
        System.out.println("[DEBUG_LOG] Making GET request to " + BASE_URL + "/me");
        System.out.println("[DEBUG_LOG] Headers: " + userHeaders);

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/me",
                HttpMethod.GET,
                requestEntity,
                UserProfileDTO.class
        );

        // Then
        System.out.println("[DEBUG_LOG] Response status: " + response.getStatusCode());
        System.out.println("[DEBUG_LOG] Response body: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_USERNAME, response.getBody().getUsername());
        assertEquals(USER_EMAIL, response.getBody().getEmail());
        assertEquals("Regular", response.getBody().getFirstName());
        assertEquals("User", response.getBody().getLastName());
        assertEquals("ROLE_" + Role.USER.name(), response.getBody().getRole());
    }

    @Test
    void testUpdateCurrentUser() {
        // Given
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        System.out.println("[DEBUG_LOG] UpdateDTO: " + updateDTO);
        System.out.println("[DEBUG_LOG] Headers: " + userHeaders);

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, userHeaders);

        // When
        System.out.println("[DEBUG_LOG] Making PUT request to " + BASE_URL + "/me");

        ResponseEntity<String> errorResponse = restTemplate.exchange(
                BASE_URL + "/me",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        System.out.println("[DEBUG_LOG] Response status: " + errorResponse.getStatusCode());
        System.out.println("[DEBUG_LOG] Response body: " + errorResponse.getBody());

        // Try again with the correct response type
        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/me",
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        // Then
        System.out.println("[DEBUG_LOG] Final response status: " + response.getStatusCode());
        System.out.println("[DEBUG_LOG] Final response body: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_USERNAME, response.getBody().getUsername());
        assertEquals(USER_EMAIL, response.getBody().getEmail());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals("Name", response.getBody().getLastName());
        assertEquals("ROLE_" + Role.USER.name(), response.getBody().getRole());

        // Verify changes in database
        User updatedUser = userRepository.findByUsername(USER_USERNAME).orElseThrow();
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }

    @Test
    void testGetAllUsers_AsAdmin() {
        // Given
        HttpEntity<Void> requestEntity = new HttpEntity<>(adminHeaders);

        // When
        ResponseEntity<List<UserProfileDTO>> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UserProfileDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // Verify both users are in the list
        boolean foundAdmin = false;
        boolean foundUser = false;

        for (UserProfileDTO dto : response.getBody()) {
            if (dto.getUsername().equals(ADMIN_USERNAME)) {
                foundAdmin = true;
            } else if (dto.getUsername().equals(USER_USERNAME)) {
                foundUser = true;
            }
        }

        assertTrue(foundAdmin, "Admin user should be in the list");
        assertTrue(foundUser, "Regular user should be in the list");
    }

    @Test
    void testGetAllUsers_AsUser_ShouldBeForbidden() {
        // Given
        HttpEntity<Void> requestEntity = new HttpEntity<>(userHeaders);

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetUserById_AsAdmin() {
        // Given
        HttpEntity<Void> requestEntity = new HttpEntity<>(adminHeaders);

        // When
        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.GET,
                requestEntity,
                UserProfileDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_USERNAME, response.getBody().getUsername());
        assertEquals(USER_EMAIL, response.getBody().getEmail());
    }

    @Test
    void testGetUserById_AsUser_ShouldBeForbidden() {
        // Given
        HttpEntity<Void> requestEntity = new HttpEntity<>(userHeaders);

        // When
        ResponseEntity<Object> response = restTemplate.exchange(
                BASE_URL + "/" + adminUser.getId(),
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
