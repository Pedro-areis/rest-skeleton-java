package dev.pedroreis.rest_skeleton_java;

import dev.pedroreis.rest_skeleton_java.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Grupo B: o que só o PostgreSQL real consegue provar. */
class PersistenceIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldKeepTheIdGeneratedByTheDomainOnSave() throws Exception {
        String idFromResponse = registerAndGetId("Pedro", "pedro@email.com");

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT id, name FROM users WHERE email = ?", "pedro@email.com");

        assertEquals(UUID.fromString(idFromResponse), row.get("id"));
        assertEquals(1, countUsers());
    }

    @Test
    void shouldKeepTheSameIdAndRowAfterUpdate() throws Exception {
        String id = registerAndGetId("Pedro", "pedro@email.com");
        String accessToken = loginAndGetTokens("pedro@email.com")[0];

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Pedro Reis\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        assertEquals(1, countUsers());
        assertEquals("Pedro Reis", jdbc.queryForObject(
                "SELECT name FROM users WHERE id = ?", String.class, UUID.fromString(id)));
    }

    @Test
    void shouldStoreTheBCryptHashAndNeverThePlainPassword() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");

        String stored = jdbc.queryForObject(
                "SELECT password_hash FROM users WHERE email = ?", String.class, "pedro@email.com");

        assertNotEquals(PASSWORD, stored);
        assertTrue(stored.startsWith("$2"), "esperava um hash BCrypt, veio: " + stored);
    }

    @Test
    void shouldFillCreatedAtInTheDatabase() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");

        Object createdAt = jdbc.queryForObject(
                "SELECT created_at FROM users WHERE email = ?", Object.class, "pedro@email.com");

        assertNotNull(createdAt);
    }

    @Test
    void shouldReturn409OnRealUniqueViolationForDuplicateEmail() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");

        register("Outro Pedro", "pedro@email.com", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(1, countUsers());
    }

    @Test
    void shouldReturn409WhenUpdatingEmailToOneOwnedByAnotherUser() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");
        registerAndGetId("Maria", "maria@email.com");
        String accessToken = loginAndGetTokens("pedro@email.com")[0];

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"maria@email.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
