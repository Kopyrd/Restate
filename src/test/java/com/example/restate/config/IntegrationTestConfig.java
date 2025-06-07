package com.example.restate.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base configuration for integration tests using TestContainers.
 * This class sets up a PostgreSQL container for integration tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class IntegrationTestConfig {

    /**
     * PostgreSQL container for integration tests.
     * This container will be started before tests and stopped after tests.
     * Using a singleton pattern to ensure only one container is created for all tests.
     */
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    // Static initialization block to ensure container is started only once for all tests
    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("integration-tests-db")
                .withUsername("testuser")
                .withPassword("testpass");
        POSTGRES_CONTAINER.start();

        // Add shutdown hook to stop container when JVM shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (POSTGRES_CONTAINER.isRunning()) {
                System.out.println("[DEBUG_LOG] Stopping PostgreSQL container");
                POSTGRES_CONTAINER.stop();
            }
        }));
    }

    /**
     * Dynamically sets the database properties for the test context.
     * This method is called before each test class.
     */
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        System.out.println("[DEBUG_LOG] Initializing test context with PostgreSQL container");
        System.out.println("[DEBUG_LOG] Container running: " + POSTGRES_CONTAINER.isRunning());
        System.out.println("[DEBUG_LOG] JDBC URL: " + POSTGRES_CONTAINER.getJdbcUrl());

        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "false");

        // Add connection pool settings to avoid timeout issues
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "10");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "5");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "600000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "1800000");
        registry.add("spring.datasource.hikari.auto-commit", () -> "true");

        System.out.println("[DEBUG_LOG] Test context initialized");
    }
}
