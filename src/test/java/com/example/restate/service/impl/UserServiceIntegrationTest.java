package com.example.restate.service.impl;

import com.example.restate.config.IntegrationTestConfig;
import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clear database
        userRepository.deleteAll();
        
        // Add test data
        setupTestUsers();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private void setupTestUsers() {
        // Admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword("Admin123!"); // Will be encoded by the service
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userService.save(admin);

        // Regular user
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@test.com");
        user.setPassword("User123!"); // Will be encoded by the service
        user.setFirstName("Regular");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setEnabled(true);
        userService.save(user);
    }

    @Test
    void loadUserByUsername_ExistingUsername_ShouldReturnUserDetails() {
        // When
        UserDetails userDetails = userService.loadUserByUsername("admin");

        // Then
        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_NonExistingUsername_ShouldThrowException() {
        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void findByUsername_ExistingUsername_ShouldReturnUser() {
        // When
        Optional<User> result = userService.findByUsername("admin");

        // Then
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals("Admin", result.get().getFirstName());
    }

    @Test
    void findByUsername_NonExistingUsername_ShouldReturnEmpty() {
        // When
        Optional<User> result = userService.findByUsername("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        // When
        Optional<User> result = userService.findByEmail("admin@test.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals("Admin", result.get().getFirstName());
    }

    @Test
    void findByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<User> result = userService.findByEmail("nonexistent@test.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void save_NewUser_ShouldSaveAndEncodePassword() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@test.com");
        newUser.setPassword("Password123!");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole(Role.USER);
        newUser.setEnabled(true);

        // When
        User saved = userService.save(newUser);

        // Then
        assertNotNull(saved.getId());
        assertEquals("newuser", saved.getUsername());
        
        // Password should be encoded
        assertNotEquals("Password123!", saved.getPassword());
        assertTrue(passwordEncoder.matches("Password123!", saved.getPassword()));
        
        // Verify in database
        assertEquals(3, userRepository.count()); // 2 initial + 1 new
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // When
        List<User> result = userService.findAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("admin")));
        assertTrue(result.stream().anyMatch(u -> u.getUsername().equals("user")));
    }

    @Test
    void findById_ExistingId_ShouldReturnUser() {
        // Given
        User saved = userRepository.findByUsername("admin").orElseThrow();

        // When
        Optional<User> result = userService.findById(saved.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
    }

    @Test
    void findById_NonExistingId_ShouldReturnEmpty() {
        // When
        Optional<User> result = userService.findById(999999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void update_ExistingUser_ShouldUpdateFields() {
        // Given
        User existing = userRepository.findByUsername("admin").orElseThrow();
        Long id = existing.getId();
        
        User updated = new User();
        updated.setFirstName("Updated");
        updated.setLastName("Admin");
        updated.setEmail("updated.admin@test.com");

        // When
        User result = userService.update(id, updated);

        // Then
        assertEquals(id, result.getId());
        assertEquals("Updated", result.getFirstName());
        assertEquals("Admin", result.getLastName());
        assertEquals("updated.admin@test.com", result.getEmail());
        // Username should remain unchanged
        assertEquals("admin", result.getUsername());
    }

    @Test
    void update_WithPassword_ShouldEncodePassword() {
        // Given
        User existing = userRepository.findByUsername("admin").orElseThrow();
        Long id = existing.getId();
        
        User updated = new User();
        updated.setPassword("NewPassword123!");

        // When
        User result = userService.update(id, updated);

        // Then
        // Password should be encoded
        assertNotEquals("NewPassword123!", result.getPassword());
        assertTrue(passwordEncoder.matches("NewPassword123!", result.getPassword()));
    }

    @Test
    void update_NonExistingUser_ShouldThrowException() {
        // Given
        User updated = new User();
        updated.setFirstName("Updated");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userService.update(999999L, updated);
        });
    }

    @Test
    void deleteById_ExistingId_ShouldDeleteUser() {
        // Given
        User toDelete = userRepository.findByUsername("user").orElseThrow();
        Long id = toDelete.getId();

        // When
        userService.deleteById(id);

        // Then
        assertEquals(1, userRepository.count());
        assertFalse(userRepository.findById(id).isPresent());
    }

    @Test
    void registerUser_ShouldSaveUserWithEncodedPassword() {
        // Given
        User newUser = new User();
        newUser.setUsername("registered");
        newUser.setEmail("registered@test.com");
        newUser.setPassword("Register123!");
        newUser.setFirstName("Registered");
        newUser.setLastName("User");
        newUser.setRole(Role.USER);
        newUser.setEnabled(true);

        // When
        User registered = userService.registerUser(newUser);

        // Then
        assertNotNull(registered.getId());
        assertEquals("registered", registered.getUsername());
        
        // Password should be encoded
        assertNotEquals("Register123!", registered.getPassword());
        assertTrue(passwordEncoder.matches("Register123!", registered.getPassword()));
    }

    @Test
    void createAdmin_ShouldSaveAdminWithEncodedPassword() {
        // Given
        User newAdmin = new User();
        newAdmin.setUsername("newadmin");
        newAdmin.setEmail("newadmin@test.com");
        newAdmin.setPassword("Admin123!");
        newAdmin.setFirstName("New");
        newAdmin.setLastName("Admin");
        newAdmin.setRole(Role.ADMIN);
        newAdmin.setEnabled(true);

        // When
        User created = userService.createAdmin(newAdmin);

        // Then
        assertNotNull(created.getId());
        assertEquals("newadmin", created.getUsername());
        assertEquals(Role.ADMIN, created.getRole());
        
        // Password should be encoded
        assertNotEquals("Admin123!", created.getPassword());
        assertTrue(passwordEncoder.matches("Admin123!", created.getPassword()));
    }

    @Test
    void existsByUsername_ExistingUsername_ShouldReturnTrue() {
        // When
        boolean result = userService.existsByUsername("admin");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByUsername_NonExistingUsername_ShouldReturnFalse() {
        // When
        boolean result = userService.existsByUsername("nonexistent");

        // Then
        assertFalse(result);
    }

    @Test
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        // When
        boolean result = userService.existsByEmail("admin@test.com");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // When
        boolean result = userService.existsByEmail("nonexistent@test.com");

        // Then
        assertFalse(result);
    }
}