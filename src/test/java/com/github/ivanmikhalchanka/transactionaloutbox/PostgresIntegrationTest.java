package com.github.ivanmikhalchanka.transactionaloutbox;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface PostgresIntegrationTest {
  @Container
  @SuppressWarnings("rawtypes")
  PostgreSQLContainer postgresTestContainer =
      new PostgreSQLContainer<>("postgres:11.1")
          .withDatabaseName("test-sample-db")
          .withUsername("sample-app-user")
          .withPassword("sample-app-password");

  @BeforeAll
  static void initPostgresDbProperties() {
    System.setProperty("spring.datasource.url", postgresTestContainer.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgresTestContainer.getUsername());
    System.setProperty("spring.datasource.password", postgresTestContainer.getPassword());
  }
}
