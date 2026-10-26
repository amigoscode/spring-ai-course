package com.amigoscode.bookstore;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts a real Postgres in Docker for the tests and points the app at it.
 * Docker must be running.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgres() {
		return new PostgreSQLContainer(DockerImageName.parse("pgvector/pgvector:pg17"));
	}

}
