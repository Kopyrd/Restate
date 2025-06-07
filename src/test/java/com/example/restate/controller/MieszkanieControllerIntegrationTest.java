package com.example.restate.controller;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.AuthRequest;
import com.example.restate.dto.AuthResponse;
import com.example.restate.dto.CreateMieszkanieDTO;
import com.example.restate.dto.MieszkanieDTO;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.repository.MieszkanieRepository;
import com.example.restate.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the MieszkanieController using a real PostgreSQL database in a Docker container.
 */
public class MieszkanieControllerIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MieszkanieRepository mieszkanieRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_URL = "/api/auth/login";
    private static final String BASE_URL = "/api/mieszkania";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";
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
        adminUser.setEmail(ADMIN_EMAIL);
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);
        
        // Login as admin and get token
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(ADMIN_USERNAME);
        authRequest.setPassword(ADMIN_PASSWORD);
        
        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                AUTH_URL,
                authRequest,
                AuthResponse.class
        );
        
        adminToken = Objects.requireNonNull(authResponse.getBody()).getToken();
        
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
    void testCreateMieszkanie() {
        // Given
        CreateMieszkanieDTO createDTO = CreateMieszkanieDTO.builder()
                .developer("Test Developer")
                .investment("Test Investment")
                .number("A-123")
                .area(new BigDecimal("75.50"))
                .price(new BigDecimal("500000.00"))
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotów")
                .floor(3)
                .description("Test apartment description")
                .build();

        HttpEntity<CreateMieszkanieDTO> requestEntity = new HttpEntity<>(createDTO, headers);

        // When
        ResponseEntity<MieszkanieDTO> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                requestEntity,
                MieszkanieDTO.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Test Developer", response.getBody().getDeveloper());
        assertEquals("Test Investment", response.getBody().getInvestment());
        assertEquals("A-123", response.getBody().getNumber());
        assertEquals(0, new BigDecimal("75.50").compareTo(response.getBody().getArea()));
        assertEquals(0, new BigDecimal("500000.00").compareTo(response.getBody().getPrice()));
        assertEquals("Mazowieckie", response.getBody().getVoivodeship());
        assertEquals("Warsaw", response.getBody().getCity());
        assertEquals("Mokotów", response.getBody().getDistrict());
        assertEquals(3, response.getBody().getFloor());
        assertEquals("Test apartment description", response.getBody().getDescription());
        assertEquals(Mieszkanie.Status.AVAILABLE, response.getBody().getStatus());
    }

    @Test
    void testGetMieszkanieById() {
        // Given
        // Create a mieszkanie first
        CreateMieszkanieDTO createDTO = CreateMieszkanieDTO.builder()
                .developer("Test Developer")
                .investment("Test Investment")
                .number("A-123")
                .area(new BigDecimal("75.50"))
                .price(new BigDecimal("500000.00"))
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotów")
                .floor(3)
                .description("Test apartment description")
                .build();

        HttpEntity<CreateMieszkanieDTO> createRequestEntity = new HttpEntity<>(createDTO, headers);
        ResponseEntity<MieszkanieDTO> createResponse = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                createRequestEntity,
                MieszkanieDTO.class
        );
        
        Integer mieszkanieId = Objects.requireNonNull(createResponse.getBody()).getId();

        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<MieszkanieDTO> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanieId,
                HttpMethod.GET,
                requestEntity,
                MieszkanieDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mieszkanieId, response.getBody().getId());
        assertEquals("Test Developer", response.getBody().getDeveloper());
    }

    @Test
    void testUpdateMieszkanie() {
        // Given
        // Create a mieszkanie first
        CreateMieszkanieDTO createDTO = CreateMieszkanieDTO.builder()
                .developer("Test Developer")
                .investment("Test Investment")
                .number("A-123")
                .area(new BigDecimal("75.50"))
                .price(new BigDecimal("500000.00"))
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotów")
                .floor(3)
                .description("Test apartment description")
                .build();

        HttpEntity<CreateMieszkanieDTO> createRequestEntity = new HttpEntity<>(createDTO, headers);
        ResponseEntity<MieszkanieDTO> createResponse = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                createRequestEntity,
                MieszkanieDTO.class
        );
        
        Integer mieszkanieId = Objects.requireNonNull(createResponse.getBody()).getId();

        // Update DTO
        UpdateMieszkanieDTO updateDTO = UpdateMieszkanieDTO.builder()
                .developer("Updated Developer")
                .price(new BigDecimal("550000.00"))
                .description("Updated description")
                .build();

        HttpEntity<UpdateMieszkanieDTO> updateRequestEntity = new HttpEntity<>(updateDTO, headers);

        // When
        ResponseEntity<MieszkanieDTO> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanieId,
                HttpMethod.PUT,
                updateRequestEntity,
                MieszkanieDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mieszkanieId, response.getBody().getId());
        assertEquals("Updated Developer", response.getBody().getDeveloper());
        assertEquals(0, new BigDecimal("550000.00").compareTo(response.getBody().getPrice()));
        assertEquals("Updated description", response.getBody().getDescription());
        // Fields not updated should remain the same
        assertEquals("Test Investment", response.getBody().getInvestment());
    }

    @Test
    void testDeleteMieszkanie() {
        // Given
        // Create a mieszkanie first
        CreateMieszkanieDTO createDTO = CreateMieszkanieDTO.builder()
                .developer("Test Developer")
                .investment("Test Investment")
                .number("A-123")
                .area(new BigDecimal("75.50"))
                .price(new BigDecimal("500000.00"))
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotów")
                .floor(3)
                .description("Test apartment description")
                .build();

        HttpEntity<CreateMieszkanieDTO> createRequestEntity = new HttpEntity<>(createDTO, headers);
        ResponseEntity<MieszkanieDTO> createResponse = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                createRequestEntity,
                MieszkanieDTO.class
        );
        
        Integer mieszkanieId = Objects.requireNonNull(createResponse.getBody()).getId();

        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanieId,
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verify it's deleted
        ResponseEntity<MieszkanieDTO> getResponse = restTemplate.exchange(
                BASE_URL + "/" + mieszkanieId,
                HttpMethod.GET,
                requestEntity,
                MieszkanieDTO.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void testChangeStatus() {
        // Given
        // Create a mieszkanie first
        CreateMieszkanieDTO createDTO = CreateMieszkanieDTO.builder()
                .developer("Test Developer")
                .investment("Test Investment")
                .number("A-123")
                .area(new BigDecimal("75.50"))
                .price(new BigDecimal("500000.00"))
                .voivodeship("Mazowieckie")
                .city("Warsaw")
                .district("Mokotów")
                .floor(3)
                .description("Test apartment description")
                .build();

        HttpEntity<CreateMieszkanieDTO> createRequestEntity = new HttpEntity<>(createDTO, headers);
        ResponseEntity<MieszkanieDTO> createResponse = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                createRequestEntity,
                MieszkanieDTO.class
        );
        
        Integer mieszkanieId = Objects.requireNonNull(createResponse.getBody()).getId();

        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<MieszkanieDTO> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanieId + "/status?status=SOLD",
                HttpMethod.PATCH,
                requestEntity,
                MieszkanieDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mieszkanieId, response.getBody().getId());
        assertEquals(Mieszkanie.Status.SOLD, response.getBody().getStatus());
    }
}