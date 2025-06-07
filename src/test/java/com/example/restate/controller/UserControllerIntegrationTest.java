package com.example.restate.controller;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.*;
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

        userRepository.deleteAll();


        adminUser = new User();
        adminUser.setUsername(ADMIN_USERNAME);
        adminUser.setEmail(ADMIN_EMAIL);
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);


        regularUser = new User();
        regularUser.setUsername(USER_USERNAME);
        regularUser.setEmail(USER_EMAIL);
        regularUser.setPassword(passwordEncoder.encode(USER_PASSWORD));
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRole(Role.USER);
        userRepository.save(regularUser);


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


        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/me",
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );


        System.out.println("[DEBUG_LOG] Final response status: " + response.getStatusCode());
        System.out.println("[DEBUG_LOG] Final response body: " + response.getBody());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_USERNAME, response.getBody().getUsername());
        assertEquals(USER_EMAIL, response.getBody().getEmail());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals("Name", response.getBody().getLastName());
        assertEquals("ROLE_" + Role.USER.name(), response.getBody().getRole());


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


        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.GET,
                requestEntity,
                UserProfileDTO.class
        );


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_USERNAME, response.getBody().getUsername());
        assertEquals(USER_EMAIL, response.getBody().getEmail());
    }

    @Test
    void testGetUserById_AsUser_ShouldBeForbidden() {
        // Given
        HttpEntity<Void> requestEntity = new HttpEntity<>(userHeaders);


        ResponseEntity<Object> response = restTemplate.exchange(
                BASE_URL + "/" + adminUser.getId(),
                HttpMethod.GET,
                requestEntity,
                Object.class
        );


        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteUser_AsAdmin() {
        HttpEntity<Void> requestEntity = new HttpEntity<>(adminHeaders);
        
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(userRepository.existsById(regularUser.getId()));
    }

    @Test
    void testDeleteUser_AsUser_ShouldBeForbidden() {
        HttpEntity<Void> requestEntity = new HttpEntity<>(userHeaders);
        
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + adminUser.getId(),
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUpdateUser_AsAdmin() {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");
        updateDTO.setEmail("updated@example.com");

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);
        
        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals("Name", response.getBody().getLastName());
    }

    @Test
    void testCreateAdmin() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newadmin");
        registerRequest.setEmail("newadmin@example.com");
        registerRequest.setPassword("NewAdmin123!");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("Admin");

        HttpEntity<RegisterRequest> requestEntity = new HttpEntity<>(registerRequest, adminHeaders);
        
        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/admin",
                HttpMethod.POST,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("newadmin", response.getBody().getUsername());
        

        User newAdmin = userRepository.findByUsername("newadmin").orElse(null);
        assertNotNull(newAdmin);
        assertEquals(Role.ADMIN, newAdmin.getRole());
    }

    @Test
    void testGetCurrentUserProfile() {
        HttpEntity<Void> requestEntity = new HttpEntity<>(userHeaders);
        
        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/profile",
                HttpMethod.GET,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(USER_USERNAME, response.getBody().getUsername());
    }

    @Test
    void testGetCurrentUserMe() {
        HttpEntity<Void> requestEntity = new HttpEntity<>(userHeaders);
        
        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/me",
                HttpMethod.GET,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(USER_USERNAME, response.getBody().getUsername());
    }

    @Test
    void testUpdateNonExistentUser_AsAdmin() {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setFirstName("Updated");
        
        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);
        
        ResponseEntity<Object> response = restTemplate.exchange(
                BASE_URL + "/999999",
                HttpMethod.PUT,
                requestEntity,
                Object.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetNonExistentUser_AsAdmin() {
        HttpEntity<Void> requestEntity = new HttpEntity<>(adminHeaders);
        
        ResponseEntity<Object> response = restTemplate.exchange(
                BASE_URL + "/999999",
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateUserWithAllFields_AsAdmin() {
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .username("newUsername")
                .email("newemail@example.com")
                .firstName("NewFirstName")
                .lastName("NewLastName")
                .password("NewPassword123!")
                .role(Role.ADMIN)
                .enabled(false)
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newUsername", response.getBody().getUsername());
        assertEquals("newemail@example.com", response.getBody().getEmail());
        assertEquals("NewFirstName", response.getBody().getFirstName());
        assertEquals("NewLastName", response.getBody().getLastName());
        assertEquals("ROLE_ADMIN", response.getBody().getRole());

        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals("NewFirstName", updatedUser.getFirstName());
        assertEquals("NewLastName", updatedUser.getLastName());
        assertEquals(Role.ADMIN, updatedUser.getRole());
        assertFalse(updatedUser.isEnabled());
        // Hasło powinno zostać zaktualizowane
        assertNotEquals(USER_PASSWORD, updatedUser.getPassword());
    }

    @Test
    void testUpdateUserWithPartialFields_AsAdmin() {
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .firstName("OnlyFirstName")
                .email("onlyemail@example.com")
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        assertEquals("OnlyFirstName", response.getBody().getFirstName());
        assertEquals("onlyemail@example.com", response.getBody().getEmail());
        
        assertEquals(USER_USERNAME, response.getBody().getUsername());
        assertEquals("User", response.getBody().getLastName()); // Oryginalne nazwisko
        assertEquals("ROLE_USER", response.getBody().getRole()); // Oryginalna rola

        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertEquals("OnlyFirstName", updatedUser.getFirstName());
        assertEquals("onlyemail@example.com", updatedUser.getEmail());
        assertEquals(USER_USERNAME, updatedUser.getUsername());
        assertEquals("User", updatedUser.getLastName());
        assertEquals(Role.USER, updatedUser.getRole());
    }

    @Test
    void testUpdateUserWithNullFields_AsAdmin() {
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .firstName("UpdatedName")
                .username(null)
                .email(null)
                .lastName(null)
                .password(null)
                .role(null)
                .enabled(null)
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);

        // When
        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        assertEquals("UpdatedName", response.getBody().getFirstName());
        
        assertEquals(USER_USERNAME, response.getBody().getUsername());
        assertEquals(USER_EMAIL, response.getBody().getEmail());
        assertEquals("User", response.getBody().getLastName());
        assertEquals("ROLE_USER", response.getBody().getRole());

        // Weryfikuj w bazie danych
        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertEquals("UpdatedName", updatedUser.getFirstName());
        assertEquals(USER_USERNAME, updatedUser.getUsername());
        assertEquals(USER_EMAIL, updatedUser.getEmail());
        assertEquals("User", updatedUser.getLastName());
        assertEquals(Role.USER, updatedUser.getRole());
        assertTrue(updatedUser.isEnabled()); // Powinno pozostać enabled
    }

    @Test
    void testUpdateUserWithEmptyPassword_AsAdmin() {
        // Given - UpdateUserDTO z pustym hasłem (nie powinno być aktualizowane)
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .firstName("TestName")
                .password("") // Puste hasło
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);
        String originalPassword = userRepository.findById(regularUser.getId()).get().getPassword();

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TestName", response.getBody().getFirstName());

        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();

        assertNotEquals(originalPassword, updatedUser.getPassword());
    }

    @Test
    void testUpdateCurrentUserWithRoleChange_ShouldNotChangeRole() {
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .firstName("UpdatedByUser")
                .role(Role.ADMIN)
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, userHeaders);

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/me",
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UpdatedByUser", response.getBody().getFirstName());
        
        assertEquals("ROLE_ADMIN", response.getBody().getRole());

        User updatedUser = userRepository.findByUsername(USER_USERNAME).orElseThrow();
        assertEquals("UpdatedByUser", updatedUser.getFirstName());
        assertEquals(Role.ADMIN, updatedUser.getRole()); // toEntity aktualizuje wszystkie pola
    }

    @Test
    void testUpdateUserDisabling_AsAdmin() {
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .enabled(false)
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertFalse(updatedUser.isEnabled());
    }

    @Test
    void testUpdateUserEnabling_AsAdmin() {
        regularUser.setEnabled(false);
        userRepository.save(regularUser);

        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .enabled(true)
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertTrue(updatedUser.isEnabled());
    }

    @Test
    void testUpdateUserWithWhitespacePassword_AsAdmin() {
        UpdateUserDTO updateDTO = UpdateUserDTO.builder()
                .firstName("TestName")
                .password("   ") // Tylko białe znaki
                .build();

        HttpEntity<UpdateUserDTO> requestEntity = new HttpEntity<>(updateDTO, adminHeaders);
        String originalPassword = userRepository.findById(regularUser.getId()).get().getPassword();

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                BASE_URL + "/" + regularUser.getId(),
                HttpMethod.PUT,
                requestEntity,
                UserProfileDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertNotEquals(originalPassword, updatedUser.getPassword());
    }
}