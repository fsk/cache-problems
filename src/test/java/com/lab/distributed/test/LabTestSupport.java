package com.lab.distributed.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Optional Testcontainers bootstrap for level integration tests.
 * Levels extend this when they add @SpringBootTest.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class LabTestSupport {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("lab")
            .withUsername("lab")
            .withPassword("lab");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("lab.postgres.url", postgres::getJdbcUrl);
        registry.add("lab.postgres.username", postgres::getUsername);
        registry.add("lab.postgres.password", postgres::getPassword);
        registry.add("lab.redis.host", redis::getHost);
        registry.add("lab.redis.port", redis::getFirstMappedPort);
    }
}
