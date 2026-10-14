package dev.pedroreis.rest_skeleton_java;

import com.jayway.jsonpath.JsonPath;
import dev.pedroreis.rest_skeleton_java.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Grupo C: erros de segurança e de validação, com o JwtTokenAdapter real. */
class SecurityAndValidationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldReturn401OnProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturn401WhenTokenIsTampered() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");
        String accessToken = loginAndGetTokens("pedro@email.com")[0];
        String tampered = accessToken.substring(0, accessToken.length() - 2) + "xx";

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(tampered)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenUsingRefreshTokenAsAccessToken() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");
        String refreshToken = loginAndGetTokens("pedro@email.com")[1];

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenUsingAccessTokenToRefresh() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");
        String accessToken = loginAndGetTokens("pedro@email.com")[0];

        postJson("/api/v1/public/auth/refresh", "{\"refreshToken\": \"%s\"}".formatted(accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForValidTokenOfDeletedUser() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");
        String accessToken = loginAndGetTokens("pedro@email.com")[0];

        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", bearer(accessToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(accessToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WithSameMessageForWrongPasswordAndUnknownEmail() throws Exception {
        registerAndGetId("Pedro", "pedro@email.com");

        String wrongPassword = login("pedro@email.com", "senha-errada-999")
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknownEmail = login("ninguem@email.com", PASSWORD)
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertEquals(
                JsonPath.<String>read(wrongPassword, "$.message"),
                JsonPath.<String>read(unknownEmail, "$.message"));
    }

    @Test
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        register("Pedro", "pedro@email.com", "curta")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertEquals(0, countUsers());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        register("Pedro", "isso-nao-e-email", PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        assertEquals(0, countUsers());
    }
}
