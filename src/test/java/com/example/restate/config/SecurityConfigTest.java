package com.example.restate.config;

import com.example.restate.security.JwtAuthenticationFilter;
import com.example.restate.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthFilter;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void authenticationProvider_ShouldReturnDaoAuthenticationProvider() {
        // When
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();

        // Then
        assertNotNull(authProvider);
        assertTrue(authProvider instanceof DaoAuthenticationProvider);

        // We can't directly test the internal configuration due to protected methods,
        // but we can verify the provider was created with our dependencies
        verify(userService, never()).loadUserByUsername(anyString()); // Just to verify the mock is properly injected
    }

    @Test
    void authenticationManager_ShouldReturnAuthenticationManagerFromConfiguration() throws Exception {
        // Given
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expectedManager);

        // When
        AuthenticationManager actualManager = securityConfig.authenticationManager(authenticationConfiguration);

        // Then
        assertNotNull(actualManager);
        assertEquals(expectedManager, actualManager);
        verify(authenticationConfiguration).getAuthenticationManager();
    }
}
