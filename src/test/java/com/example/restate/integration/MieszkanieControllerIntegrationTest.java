package com.example.restate.integration;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.*;
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
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for the MieszkanieController.
 * Tests all endpoints and functionality.
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
    private static final String ADMIN_PASSWORD = "Admin123!";

    private String adminToken;
    private HttpHeaders headers;
    private Mieszkanie mieszkanie1;
    private Mieszkanie mieszkanie2;
    private Mieszkanie mieszkanie3;

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

        // Create test mieszkania
        mieszkanie1 = new Mieszkanie();
        mieszkanie1.setDeveloper("Developer A");
        mieszkanie1.setInvestment("Investment X");
        mieszkanie1.setNumber("A1");
        mieszkanie1.setArea(BigDecimal.valueOf(75.5));
        mieszkanie1.setPrice(BigDecimal.valueOf(500000));
        mieszkanie1.setVoivodeship("Mazowieckie");
        mieszkanie1.setCity("Warsaw");
        mieszkanie1.setDistrict("Mokotów");
        mieszkanie1.setFloor(2);
        mieszkanie1.setDescription("Test Description 1");
        mieszkanie1.setStatus(Mieszkanie.Status.AVAILABLE);

        mieszkanie2 = new Mieszkanie();
        mieszkanie2.setDeveloper("Developer B");
        mieszkanie2.setInvestment("Investment Y");
        mieszkanie2.setNumber("B2");
        mieszkanie2.setArea(BigDecimal.valueOf(100.0));
        mieszkanie2.setPrice(BigDecimal.valueOf(750000));
        mieszkanie2.setVoivodeship("Mazowieckie");
        mieszkanie2.setCity("Warsaw");
        mieszkanie2.setDistrict("Śródmieście");
        mieszkanie2.setFloor(3);
        mieszkanie2.setDescription("Test Description 2");
        mieszkanie2.setStatus(Mieszkanie.Status.RESERVED);

        mieszkanie3 = new Mieszkanie();
        mieszkanie3.setDeveloper("Developer A");
        mieszkanie3.setInvestment("Investment Z");
        mieszkanie3.setNumber("C3");
        mieszkanie3.setArea(BigDecimal.valueOf(120.0));
        mieszkanie3.setPrice(BigDecimal.valueOf(900000));
        mieszkanie3.setVoivodeship("Małopolskie");
        mieszkanie3.setCity("Kraków");
        mieszkanie3.setDistrict("Stare Miasto");
        mieszkanie3.setFloor(4);
        mieszkanie3.setDescription("Test Description 3");
        mieszkanie3.setStatus(Mieszkanie.Status.SOLD);

        mieszkanie1 = mieszkanieRepository.save(mieszkanie1);
        mieszkanie2 = mieszkanieRepository.save(mieszkanie2);
        mieszkanie3 = mieszkanieRepository.save(mieszkanie3);
    }

    @AfterEach
    void tearDown() {
        // Clean up the database
        mieszkanieRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testGetAllMieszkania() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<PageResponse<MieszkanieDTO>> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<PageResponse<MieszkanieDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getContent().size());
    }

    @Test
    void testGetMieszkanieById() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<MieszkanieDTO> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanie1.getId(),
                HttpMethod.GET,
                requestEntity,
                MieszkanieDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mieszkanie1.getId(), response.getBody().getId());
        assertEquals("Developer A", response.getBody().getDeveloper());
    }

    @Test
    void testCreateMieszkanie() {
        // Given
        CreateMieszkanieDTO createDTO = CreateMieszkanieDTO.builder()
                .developer("New Developer")
                .investment("New Investment")
                .number("N1")
                .area(BigDecimal.valueOf(80.0))
                .price(BigDecimal.valueOf(600000))
                .voivodeship("Pomorskie")
                .city("Gdańsk")
                .district("Wrzeszcz")
                .floor(1)
                .description("New Description")
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
        assertEquals("New Developer", response.getBody().getDeveloper());
        assertEquals("New Investment", response.getBody().getInvestment());
    }

    @Test
    void testUpdateMieszkanie() {
        // Given
        UpdateMieszkanieDTO updateDTO = UpdateMieszkanieDTO.builder()
                .developer("Updated Developer")
                .price(BigDecimal.valueOf(550000))
                .description("Updated Description")
                .build();

        HttpEntity<UpdateMieszkanieDTO> requestEntity = new HttpEntity<>(updateDTO, headers);

        // When
        ResponseEntity<MieszkanieDTO> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanie1.getId(),
                HttpMethod.PUT,
                requestEntity,
                MieszkanieDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mieszkanie1.getId(), response.getBody().getId());
        assertEquals("Updated Developer", response.getBody().getDeveloper());
        assertEquals(0, BigDecimal.valueOf(550000).compareTo(response.getBody().getPrice()));
        assertEquals("Updated Description", response.getBody().getDescription());
    }

    @Test
    void testDeleteMieszkanie() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanie1.getId(),
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify it's deleted
        assertFalse(mieszkanieRepository.existsById(mieszkanie1.getId()));
    }

    @Test
    void testChangeStatus() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<MieszkanieDTO> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanie1.getId() + "/status?status=SOLD",
                HttpMethod.PATCH,
                requestEntity,
                MieszkanieDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mieszkanie1.getId(), response.getBody().getId());
        assertEquals(Mieszkanie.Status.SOLD, response.getBody().getStatus());
    }

    @Test
    void testGetByDeveloper() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<PageResponse<MieszkanieDTO>> response = restTemplate.exchange(
                BASE_URL + "/developer/Developer A",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<PageResponse<MieszkanieDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("Developer A", response.getBody().getContent().get(0).getDeveloper());
    }

    @Test
    void testGetByInvestment() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<PageResponse<MieszkanieDTO>> response = restTemplate.exchange(
                BASE_URL + "/investment/Investment X",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<PageResponse<MieszkanieDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Investment X", response.getBody().getContent().get(0).getInvestment());
    }

    @Test
    void testGetByPriceRange() {
        // When
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<PageResponse<MieszkanieDTO>> response = restTemplate.exchange(
                BASE_URL + "/price-range?min=700000&max=950000",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<PageResponse<MieszkanieDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
    }


    @Test
    void testSearchByCriteria() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("Developer A")
                .build();

        HttpEntity<MieszkanieSearchCriteria> requestEntity = new HttpEntity<>(criteria, headers);

        // When
        ResponseEntity<PageResponse<MieszkanieDTO>> response = restTemplate.exchange(
                BASE_URL + "/search",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PageResponse<MieszkanieDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("Developer A", response.getBody().getContent().get(0).getDeveloper());
    }

    @Test
    void testSearchByAdvancedCriteria() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .minPrice(BigDecimal.valueOf(700000))
                .maxPrice(BigDecimal.valueOf(950000))
                .minArea(BigDecimal.valueOf(90.0))
                .build();

        HttpEntity<MieszkanieSearchCriteria> requestEntity = new HttpEntity<>(criteria, headers);

        // When
        ResponseEntity<PageResponse<MieszkanieDTO>> response = restTemplate.exchange(
                BASE_URL + "/search",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PageResponse<MieszkanieDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    void testSearchByLocationCriteria() {
        // Given
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Kraków")
                .build();

        HttpEntity<MieszkanieSearchCriteria> requestEntity = new HttpEntity<>(criteria, headers);

        // When
        ResponseEntity<PageResponse<MieszkanieDTO>> response = restTemplate.exchange(
                BASE_URL + "/search",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<PageResponse<MieszkanieDTO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Kraków", response.getBody().getContent().get(0).getCity());
    }
}
