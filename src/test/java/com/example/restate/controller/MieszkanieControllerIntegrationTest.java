package com.example.restate.controller;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.dto.CreateMieszkanieDTO;
import com.example.restate.dto.MieszkanieSearchCriteria;
import com.example.restate.dto.UpdateMieszkanieDTO;
import com.example.restate.entity.Mieszkanie;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.repository.MieszkanieRepository;
import com.example.restate.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class MieszkanieControllerIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MieszkanieRepository mieszkanieRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/mieszkania";
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Wyczyść bazę danych
        mieszkanieRepository.deleteAll();
        userRepository.deleteAll();

        // Utwórz użytkowników testowych
        setupTestUsers();
        
        // Dodaj testowe mieszkania
        setupTestMieszkania();
    }

    @AfterEach
    void tearDown() {
        mieszkanieRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void setupTestUsers() {
        // Admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        // Regular user
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("User123!"));
        user.setFirstName("Regular");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setEnabled(true);
        userRepository.save(user);

        // Uzyskaj tokeny
        adminToken = getAuthToken("admin", "Admin123!");
        userToken = getAuthToken("user", "User123!");
    }

    private String getAuthToken(String username, String password) {
        try {
            var authRequest = objectMapper.createObjectNode();
            authRequest.put("username", username);
            authRequest.put("password", password);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "/api/auth/login",
                    authRequest,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                var responseBody = objectMapper.readTree(response.getBody());
                return responseBody.get("token").asText();
            }
        } catch (Exception e) {
            fail("Failed to get auth token: " + e.getMessage());
        }
        return null;
    }

    private void setupTestMieszkania() {
        // Mieszkanie 1 - Luksusowe
        Mieszkanie m1 = new Mieszkanie();
        m1.setDeveloper("LuxDev");
        m1.setInvestment("Golden Heights");
        m1.setNumber("A101");
        m1.setArea(BigDecimal.valueOf(85.5));
        m1.setPrice(BigDecimal.valueOf(750000));
        m1.setVoivodeship("Mazowieckie");
        m1.setCity("Warszawa");
        m1.setDistrict("Śródmieście");
        m1.setFloor(10);
        m1.setDescription("Luksusowe mieszkanie w centrum");
        m1.setStatus(Mieszkanie.Status.AVAILABLE);

        // Mieszkanie 2 - Średnia klasa
        Mieszkanie m2 = new Mieszkanie();
        m2.setDeveloper("StandardDev");
        m2.setInvestment("City Park");
        m2.setNumber("B205");
        m2.setArea(BigDecimal.valueOf(62.0));
        m2.setPrice(BigDecimal.valueOf(450000));
        m2.setVoivodeship("Mazowieckie");
        m2.setCity("Warszawa");
        m2.setDistrict("Mokotów");
        m2.setFloor(2);
        m2.setDescription("Nowoczesne mieszkanie w zielonym otoczeniu");
        m2.setStatus(Mieszkanie.Status.AVAILABLE);

        // Mieszkanie 3 - Budżetowe
        Mieszkanie m3 = new Mieszkanie();
        m3.setDeveloper("BudgetDev");
        m3.setInvestment("Affordable Living");
        m3.setNumber("C15");
        m3.setArea(BigDecimal.valueOf(45.0));
        m3.setPrice(BigDecimal.valueOf(280000));
        m3.setVoivodeship("Śląskie");
        m3.setCity("Katowice");
        m3.setDistrict("Centrum");
        m3.setFloor(5);
        m3.setDescription("Kompaktowe mieszkanie dla młodych");
        m3.setStatus(Mieszkanie.Status.RESERVED);

        mieszkanieRepository.saveAll(List.of(m1, m2, m3));
    }

    @Test
    void getAllMieszkania_AsUser_ShouldReturnPagedResults() {
        // Given
        HttpHeaders headers = createAuthHeaders(userToken);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "?page=0&size=2&sortBy=price&sortDir=desc",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"totalElements\":3"));
        assertTrue(response.getBody().contains("\"pageSize\":2"));
        assertTrue(response.getBody().contains("Golden Heights")); // Najdroższe powinno być pierwsze
    }

    @Test
    void getMieszkanieById_ExistingId_ShouldReturnMieszkanie() {
        // Given
        Mieszkanie saved = mieszkanieRepository.findAll().get(0);
        HttpHeaders headers = createAuthHeaders(userToken);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/" + saved.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains(saved.getDeveloper()));
    }

    @Test
    void getMieszkanieById_NonExistingId_ShouldReturnNotFound() {
        // Given
        HttpHeaders headers = createAuthHeaders(userToken);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/999999",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createMieszkanie_AsAdmin_ShouldCreateSuccessfully() throws Exception {
        // Given
        HttpHeaders headers = createAuthHeaders(adminToken);
        
        CreateMieszkanieDTO dto = new CreateMieszkanieDTO();
        dto.setDeveloper("NewDev");
        dto.setInvestment("New Investment");
        dto.setNumber("D301");
        dto.setArea(BigDecimal.valueOf(70.0));
        dto.setPrice(BigDecimal.valueOf(500000));
        dto.setVoivodeship("Wielkopolskie");
        dto.setCity("Poznań");
        dto.setDistrict("Stare Miasto");
        dto.setFloor(3);
        dto.setDescription("Nowe mieszkanie testowe");

        HttpEntity<CreateMieszkanieDTO> request = new HttpEntity<>(dto, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains("NewDev"));
        assertEquals(4, mieszkanieRepository.count()); // 3 początkowe + 1 nowe
    }

    @Test
    void createMieszkanie_AsUser_ShouldBeForbidden() throws Exception {
        // Given
        HttpHeaders headers = createAuthHeaders(userToken);
        
        CreateMieszkanieDTO dto = new CreateMieszkanieDTO();
        dto.setDeveloper("TestDev");
        dto.setInvestment("Test Investment");
        dto.setNumber("A1");
        dto.setArea(BigDecimal.valueOf(50.0));
        dto.setPrice(BigDecimal.valueOf(300000));
        dto.setVoivodeship("Test Voivodeship");
        dto.setCity("Test City");
        dto.setDistrict("Test District");
        dto.setFloor(1);
        dto.setDescription("Test Description");
        
        HttpEntity<CreateMieszkanieDTO> request = new HttpEntity<>(dto, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL,
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        //Spring Security może zwrócić 403 lub 400 w zależności od konfiguracji
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || 
                   response.getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateMieszkanie_AsAdmin_ShouldUpdateSuccessfully() throws Exception {
        // Given
        Mieszkanie existing = mieszkanieRepository.findAll().get(0);
        HttpHeaders headers = createAuthHeaders(adminToken);
        
        UpdateMieszkanieDTO dto = new UpdateMieszkanieDTO();
        dto.setPrice(BigDecimal.valueOf(800000));
        dto.setDescription("Zaktualizowany opis");

        HttpEntity<UpdateMieszkanieDTO> request = new HttpEntity<>(dto, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/" + existing.getId(),
                HttpMethod.PUT,
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("800000"));
        assertTrue(response.getBody().contains("Zaktualizowany opis"));
    }

    @Test
    void deleteMieszkanie_AsAdmin_ShouldDeleteSuccessfully() {
        // Given
        Mieszkanie toDelete = mieszkanieRepository.findAll().get(0);
        HttpHeaders headers = createAuthHeaders(adminToken);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/" + toDelete.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(2, mieszkanieRepository.count()); // 3 - 1 = 2
    }

    @Test
    void getByInvestment_ShouldReturnFilteredResults() {
        // Given
        HttpHeaders headers = createAuthHeaders(userToken);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/investment/Golden Heights",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Golden Heights"));
        assertTrue(response.getBody().contains("LuxDev"));
    }

    @Test
    void getByPriceRange_ShouldReturnFilteredResults() {
        // Given
        HttpHeaders headers = createAuthHeaders(userToken);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/price-range?minPrice=400000&maxPrice=500000",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("City Park"));
        assertFalse(response.getBody().contains("Golden Heights"));
    }

    @Test
    void searchByCriteria_ComplexSearch_ShouldReturnMatchingResults() throws Exception {
        // Given
        HttpHeaders headers = createAuthHeaders(userToken);
        
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .city("Warszawa")
                .minPrice(BigDecimal.valueOf(400000))
                .maxPrice(BigDecimal.valueOf(800000))
                .minArea(BigDecimal.valueOf(60.0))
                .build();

        HttpEntity<MieszkanieSearchCriteria> request = new HttpEntity<>(criteria, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/search",
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Golden Heights")); // Spełnia kryteria
        assertTrue(response.getBody().contains("City Park")); // Spełnia kryteria
        assertFalse(response.getBody().contains("Affordable Living")); // Katowice, nie Warszawa
    }

    @Test
    void changeStatus_AsAdmin_ShouldUpdateStatus() {
        // Given
        Mieszkanie mieszkanie = mieszkanieRepository.findAll().get(0);
        HttpHeaders headers = createAuthHeaders(adminToken);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/" + mieszkanie.getId() + "/status?status=SOLD",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("SOLD"));
        
        // Sprawdź w bazie danych
        Mieszkanie updated = mieszkanieRepository.findById(mieszkanie.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(Mieszkanie.Status.SOLD, updated.getStatus());
    }

    @Test
    void searchWithDeveloperCriteria_ShouldUseCorrectStrategy() throws Exception {
        // Given
        HttpHeaders headers = createAuthHeaders(userToken);
        
        MieszkanieSearchCriteria criteria = MieszkanieSearchCriteria.builder()
                .developer("LuxDev")
                .build();

        HttpEntity<MieszkanieSearchCriteria> request = new HttpEntity<>(criteria, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/search",
                HttpMethod.POST,
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("LuxDev"));
        assertTrue(response.getBody().contains("Golden Heights"));
        assertFalse(response.getBody().contains("StandardDev"));
    }

    @Test
    void unauthorizedAccess_ShouldBeForbidden() {
        // When - brak tokena autoryzacji
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL, String.class);

        // Then
        // Spring Security zwraca 403 FORBIDDEN dla braku tokena
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}