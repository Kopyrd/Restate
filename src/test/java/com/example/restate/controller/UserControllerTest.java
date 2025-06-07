package com.example.restate.controller;

import com.example.restate.dto.RegisterRequest;
import com.example.restate.dto.UpdateUserDTO;
import com.example.restate.dto.UserProfileDTO;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.exception.ResourceNotFoundException;
import com.example.restate.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.example.restate.config.WebMvcTestConfig;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(WebMvcTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserProfileDTO userProfileDTO;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        // Setup test data
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);

        userProfileDTO = UserProfileDTO.fromEntity(user);

        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(Role.ADMIN);

        userList = Arrays.asList(user, admin);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {

        when(userService.findAll()).thenReturn(userList);


        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("admin")));

        verify(userService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).findById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() throws Exception {
        // Given
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");
        updateDTO.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("Name");
        updatedUser.setRole(Role.USER);

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userService.update(eq(1L), any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Name")));

        verify(userService, times(1)).findById(1L);
        verify(userService, times(1)).update(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");

        when(userService.findById(999L)).thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/users/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldDeleteUser() throws Exception {

        doNothing().when(userService).deleteById(1L);
        
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAdmin_ShouldCreateAndReturnAdmin() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newadmin");
        registerRequest.setPassword("password");
        registerRequest.setEmail("newadmin@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("Admin");

        User createdAdmin = new User();
        createdAdmin.setId(3L);
        createdAdmin.setUsername("newadmin");
        createdAdmin.setEmail("newadmin@example.com");
        createdAdmin.setFirstName("New");
        createdAdmin.setLastName("Admin");
        createdAdmin.setRole(Role.ADMIN);

        when(userService.save(any(User.class))).thenReturn(createdAdmin);

        mockMvc.perform(post("/api/users/admin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.username", is("newadmin")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")));


        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getCurrentUser_WhenUserExists_ShouldReturnUserProfile() throws Exception {

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));


        mockMvc.perform(get("/api/users/profile")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("User")));

        verify(userService, times(1)).findByUsername("testuser");
    }

    @Test
    @WithMockUser(username = "nonexistent", roles = "USER")
    void getCurrentUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/profile")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByUsername("nonexistent");
    }
}