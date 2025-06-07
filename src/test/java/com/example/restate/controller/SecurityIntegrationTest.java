package com.example.restate.controller;

import com.example.restate.config.IntegrationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class SecurityIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testAccessWithoutToken() {
        ResponseEntity<Object> response = restTemplate.getForEntity(
                "/api/users",
                Object.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testAccessWithInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid-token");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                requestEntity,
                Object.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}