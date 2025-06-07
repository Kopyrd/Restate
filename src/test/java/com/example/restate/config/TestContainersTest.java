package com.example.restate.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple test to verify that TestContainers works correctly.
 */
@Testcontainers
public class TestContainersTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Test
    void testPostgresContainer() {
        System.out.println("[DEBUG_LOG] Testing PostgreSQL container...");
        System.out.println("[DEBUG_LOG] Container running: " + postgres.isRunning());
        System.out.println("[DEBUG_LOG] JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("[DEBUG_LOG] Username: " + postgres.getUsername());
        System.out.println("[DEBUG_LOG] Password: " + postgres.getPassword());
        
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
    }
}