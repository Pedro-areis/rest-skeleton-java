package dev.pedroreis.rest_skeleton_java.support;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integração: aplicação completa (segurança, JWT real, JPA, Flyway) sobre PostgreSQL
 * real, chamada via MockMvc. A tabela {@code users} é esvaziada antes de cada teste (ADR 007).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresContainerConfig.class)
public abstract class AbstractIntegrationTest {

    protected static final String PASSWORD = "senha-forte-123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE users");
    }

    protected ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions register(String name, String email, String password) throws Exception {
        return postJson("/api/v1/public/users/register", """
                {"name": "%s", "email": "%s", "password": "%s", "birthDate": "2000-01-15"}
                """.formatted(name, email, password));
    }

    /** Cadastra um usuário e devolve o id gerado. */
    protected String registerAndGetId(String name, String email) throws Exception {
        String body = register(name, email, PASSWORD)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    protected ResultActions login(String email, String password) throws Exception {
        return postJson("/api/v1/public/auth/login", """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password));
    }

    /** Faz login e devolve {accessToken, refreshToken}. */
    protected String[] loginAndGetTokens(String email) throws Exception {
        String body = login(email, PASSWORD)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new String[]{JsonPath.read(body, "$.accessToken"), JsonPath.read(body, "$.refreshToken")};
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected int countUsers() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return total == null ? 0 : total;
    }
}
