package dev.pedroreis.rest_skeleton_java;

import com.jayway.jsonpath.JsonPath;
import dev.pedroreis.rest_skeleton_java.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Grupo A: o caminho feliz de ponta a ponta, com JWT real e PostgreSQL real. */
class FullFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldRunFullFlowRegisterLoginMeUpdateRefreshAndDelete() throws Exception {
        String id = registerAndGetId("Pedro", "pedro@email.com");

        String[] tokens = loginAndGetTokens("pedro@email.com");
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Pedro"))
                .andExpect(jsonPath("$.email").value("pedro@email.com"));

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Pedro Reis\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Pedro Reis"))
                .andExpect(jsonPath("$.email").value("pedro@email.com"));

        String refreshBody = postJson("/api/v1/public/auth/refresh",
                "{\"refreshToken\": \"%s\"}".formatted(refreshToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newAccessToken = JsonPath.read(refreshBody, "$.accessToken");

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(newAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pedro Reis"));

        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", bearer(newAccessToken)))
                .andExpect(status().isNoContent());

        assertEquals(0, countUsers());
    }

    @Test
    void shouldLoginWithNewPasswordAfterChangingIt() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");
        String accessToken = loginAndGetTokens("pedro@email.com")[0];

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\": \"outra-senha-456\"}"))
                .andExpect(status().isOk());

        login("pedro@email.com", PASSWORD).andExpect(status().isUnauthorized());
        login("pedro@email.com", "outra-senha-456").andExpect(status().isOk());
    }
}
