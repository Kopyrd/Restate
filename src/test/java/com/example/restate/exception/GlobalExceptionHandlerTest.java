package com.example.restate.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFoundException_ShouldReturnNotFoundStatus() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource Not Found", response.getBody().getError());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestStatus() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "field1", "Field1 error"));
        fieldErrors.add(new FieldError("object", "field2", "Field2 error"));

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals("Invalid input parameters", response.getBody().getMessage());

        Map<String, String> validationErrors = response.getBody().getValidationErrors();
        assertNotNull(validationErrors);
        assertEquals(2, validationErrors.size());
        assertEquals("Field1 error", validationErrors.get("field1"));
        assertEquals("Field2 error", validationErrors.get("field2"));
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnConflictStatus() {
        // Given
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Throwable cause = mock(Throwable.class);
        when(ex.getMostSpecificCause()).thenReturn(cause);
        when(cause.getMessage()).thenReturn("Duplicate entry");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Data Integrity Violation", response.getBody().getError());
        assertEquals("Database constraint violation: Duplicate entry", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequestStatus() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid Argument", response.getBody().getError());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbiddenStatus() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().getError());
        assertEquals("You don't have permission to access this resource", response.getBody().getMessage());
    }

    @Test
    void handleAuthenticationException_ShouldReturnBadRequestStatus() {
        // Given
        AuthenticationException ex = mock(AuthenticationException.class);
        when(ex.getMessage()).thenReturn("Authentication failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Authentication Failed", response.getBody().getError());
        assertEquals("Authentication failed", response.getBody().getMessage());
    }

    @Test
    void handleBadCredentialsException_ShouldReturnBadRequestStatus() {
        // Given
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Authentication Failed", response.getBody().getError());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    void handleCustomAuthenticationException_ShouldReturnBadRequestStatus() {
        // Given
        com.example.restate.exception.AuthenticationException ex = 
            new com.example.restate.exception.AuthenticationException("Custom auth error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomAuthenticationException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Authentication Failed", response.getBody().getError());
        assertEquals("Custom auth error", response.getBody().getMessage());
    }

    @Test
    void handleMissingServletRequestParameterException_ShouldReturnBadRequestStatus() {
        // Given
        MissingServletRequestParameterException ex = mock(MissingServletRequestParameterException.class);
        when(ex.getMessage()).thenReturn("Required parameter is missing");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMissingServletRequestParameterException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing Parameter", response.getBody().getError());
        assertEquals("Required parameter is missing", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentTypeMismatchException_ShouldReturnBadRequestStatus() {
        // Given
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("paramName");
        when(ex.getRequiredType()).thenReturn((Class)Integer.class);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatchException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Type Mismatch", response.getBody().getError());
        assertEquals("Parameter 'paramName' should be of type Integer", response.getBody().getMessage());
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerErrorStatus() {
        // Given
        Exception ex = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Unexpected error", response.getBody().getMessage());
    }
}
