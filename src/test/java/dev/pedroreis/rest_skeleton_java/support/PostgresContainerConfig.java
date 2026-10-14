package dev.pedroreis.rest_skeleton_java.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL real em container para os testes de integração (ADR 007).
 * O container é estático e sobe uma vez por execução da suíte, compartilhado por todas as classes de teste.
 * O Testcontainers o remove ao fim da execução. O {@link ServiceConnection} aponta o datasource para ele.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }
}
