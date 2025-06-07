package com.example.restate.controller;

import com.example.restate.dto.AuthRequest;
import com.example.restate.dto.AuthResponse;
import com.example.restate.dto.RegisterRequest;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.service.UserService;
import com.example.restate.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.example.restate.config.WebMvcTestConfig;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(WebMvcTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest authRequest;
    private RegisterRequest registerRequest;
    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup test data
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setEnabled(true);

        authentication = mock(Authentication.class);
    }

    @Test
    void authenticate_WhenCredentialsAreValid_ShouldReturnToken() throws Exception {
        // Given
        when(userService.existsByUsername("testuser")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.loadUserByUsername("testuser")).thenReturn(user);
        when(jwtUtil.generateToken("testuser")).thenReturn("test.jwt.token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.token").value("test.jwt.token"));

        verify(userService, times(1)).existsByUsername("testuser");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).loadUserByUsername("testuser");
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void authenticate_WhenUserDoesNotExist_ShouldReturnBadRequest() throws Exception {
        // Given
        when(userService.existsByUsername("testuser")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).existsByUsername("testuser");
    }

    @Test
    void authenticate_WhenCredentialsAreInvalid_ShouldReturnBadRequest() throws Exception {
        // Given
        when(userService.existsByUsername("testuser")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).existsByUsername("testuser");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerUser_WhenDataIsValid_ShouldRegisterAndReturnToken() throws Exception {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);

        User registeredUser = new User();
        registeredUser.setId(2L);
        registeredUser.setUsername("newuser");
        registeredUser.setEmail("newuser@example.com");
        registeredUser.setRole(Role.USER);

        when(userService.registerUser(any(User.class))).thenReturn(registeredUser);
        when(jwtUtil.generateToken("newuser")).thenReturn("new.jwt.token");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.token").value("new.jwt.token"));

        verify(userService, times(1)).existsByUsername("newuser");
        verify(userService, times(1)).existsByEmail("newuser@example.com");
        verify(userService, times(1)).registerUser(any(User.class));
        verify(jwtUtil, times(1)).generateToken("newuser");
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerUser_WhenUsernameExists_ShouldReturnBadRequest() throws Exception {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).existsByUsername("newuser");
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerUser_WhenEmailExists_ShouldReturnBadRequest() throws Exception {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).existsByUsername("newuser");
        verify(userService, times(1)).existsByEmail("newuser@example.com");
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerUser_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.registerUser(any(User.class))).thenThrow(new RuntimeException("Registration failed"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).existsByUsername("newuser");
        verify(userService, times(1)).existsByEmail("newuser@example.com");
        verify(userService, times(1)).registerUser(any(User.class));
    }
}
