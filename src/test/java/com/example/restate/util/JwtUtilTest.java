package com.example.restate.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_USERNAME = "testuser";
    private static final String SECRET = "my-super-secret-key-which-is-long-enough-to-be-secure";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Manually call init() since @PostConstruct won't be triggered in tests
        jwtUtil.init();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // When
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.string";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.extractUsername(invalidToken));
    }

    @Test
    void validateToken_WithValidTokenAndUsername_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // When
        boolean isValid = jwtUtil.validateToken(token, TEST_USERNAME);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithValidTokenAndWrongUsername_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(TEST_USERNAME);
        String wrongUsername = "wronguser";

        // When
        boolean isValid = jwtUtil.validateToken(token, wrongUsername);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken, TEST_USERNAME);

        // Then
        assertFalse(isValid);
    }
}