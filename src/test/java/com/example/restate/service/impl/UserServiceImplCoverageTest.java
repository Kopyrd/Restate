package com.example.restate.service.impl;

import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplCoverageTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
    }

    @Test
    void update_WithPartialFields_ShouldUpdateOnlyProvidedFields() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("encodedExistingPassword");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setRole(Role.USER);

        User partialUpdate = new User();
        // Only set email and firstName, leave other fields null
        partialUpdate.setEmail("newemail@example.com");
        partialUpdate.setFirstName("NewFirstName");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.update(1L, partialUpdate);

        // Then
        // Fields that were updated
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("NewFirstName", result.getFirstName());

        // Fields that should remain unchanged
        assertEquals("existinguser", result.getUsername());
        assertEquals("encodedExistingPassword", result.getPassword());
        assertEquals("User", result.getLastName());
        assertEquals(Role.USER, result.getRole());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        // Password encoder should not be called since password was not updated
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void update_WithEmptyPassword_ShouldNotUpdatePassword() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("encodedExistingPassword");

        User updateWithEmptyPassword = new User();
        updateWithEmptyPassword.setUsername("newusername");
        updateWithEmptyPassword.setPassword(""); // Empty password

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.update(1L, updateWithEmptyPassword);

        // Then
        assertEquals("newusername", result.getUsername());
        assertEquals("encodedExistingPassword", result.getPassword()); // Password should not change

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        // Password encoder should not be called
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void update_WithAllFieldsNull_ShouldNotUpdateAnyField() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("encodedExistingPassword");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setRole(Role.USER);

        User emptyUpdate = new User();
        // All fields are null

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.update(1L, emptyUpdate);

        // Then
        // All fields should remain unchanged
        assertEquals("existinguser", result.getUsername());
        assertEquals("existing@example.com", result.getEmail());
        assertEquals("encodedExistingPassword", result.getPassword());
        assertEquals("Existing", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals(Role.USER, result.getRole());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void update_WithNullPassword_ShouldNotUpdatePassword() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("encodedExistingPassword");

        User updateWithNullPassword = new User();
        updateWithNullPassword.setUsername("newusername");
        // Password is null

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.update(1L, updateWithNullPassword);

        // Then
        assertEquals("newusername", result.getUsername());
        assertEquals("encodedExistingPassword", result.getPassword()); // Password should not change

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void update_WithAllFields_ShouldUpdateAllFields() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("encodedExistingPassword");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setRole(Role.USER);

        User completeUpdate = new User();
        completeUpdate.setUsername("newusername");
        completeUpdate.setEmail("new@example.com");
        completeUpdate.setPassword("newpassword");
        completeUpdate.setFirstName("New");
        completeUpdate.setLastName("Name");
        completeUpdate.setRole(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.update(1L, completeUpdate);

        // Then
        assertEquals("newusername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedNewPassword", result.getPassword());
        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals(Role.ADMIN, result.getRole());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("newpassword");
    }
}
