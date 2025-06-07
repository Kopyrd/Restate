package com.example.restate.security;

import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext(); // Clear security context before each test

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
    }

    @Test
    void doFilterInternal_WithAuthEndpoints_ShouldSkipAuthentication() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        lenient().when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(ContentCachingResponseWrapper.class));
        verify(jwtUtil, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithNoAuthHeader_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/mieszkania");
        when(request.getHeader("Authorization")).thenReturn(null);
        lenient().when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(ContentCachingResponseWrapper.class));
        verify(jwtUtil, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithInvalidAuthHeaderFormat_ShouldContinueWithoutAuthentication() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/mieszkania");
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");
        lenient().when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(ContentCachingResponseWrapper.class));
        verify(jwtUtil, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldAuthenticateUser() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/mieszkania");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        lenient().when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

        when(jwtUtil.extractUsername("valid.token.here")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtUtil.validateToken("valid.token.here", "testuser")).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(ContentCachingResponseWrapper.class));
        verify(jwtUtil).extractUsername("valid.token.here");
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtUtil).validateToken("valid.token.here", "testuser");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotAuthenticateUser() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/mieszkania");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");
        lenient().when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

        when(jwtUtil.extractUsername("invalid.token.here")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtUtil.validateToken("invalid.token.here", "testuser")).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(ContentCachingResponseWrapper.class));
        verify(jwtUtil).extractUsername("invalid.token.here");
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtUtil).validateToken("invalid.token.here", "testuser");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithExceptionDuringAuthentication_ShouldClearContextAndContinue() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/mieszkania");
        when(request.getHeader("Authorization")).thenReturn("Bearer error.token.here");
        lenient().when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

        when(jwtUtil.extractUsername("error.token.here")).thenThrow(new RuntimeException("Token error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(any(HttpServletRequest.class), any(ContentCachingResponseWrapper.class));
        verify(jwtUtil).extractUsername("error.token.here");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
