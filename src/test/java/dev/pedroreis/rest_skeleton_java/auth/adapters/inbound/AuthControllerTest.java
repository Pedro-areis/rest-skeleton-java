package dev.pedroreis.rest_skeleton_java.auth.adapters.inbound;

import dev.pedroreis.rest_skeleton_java.auth.domain.AuthTokens;
import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidCredentialsException;
import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;
import dev.pedroreis.rest_skeleton_java.auth.ports.inbound.LoginUseCase;
import dev.pedroreis.rest_skeleton_java.auth.ports.inbound.RefreshTokenUseCase;
import dev.pedroreis.rest_skeleton_java.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste web "standalone": controller + advices, sem contexto Spring, sem Spring Security e sem banco.
 * O advice genérico vai primeiro na lista de propósito: quem decide é o {@code @Order} (ADR 005).
 */
class AuthControllerTest {

    private LoginUseCase loginUseCase;
    private RefreshTokenUseCase refreshTokenUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        loginUseCase = mock(LoginUseCase.class);
        refreshTokenUseCase = mock(RefreshTokenUseCase.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(loginUseCase, refreshTokenUseCase))
                .setControllerAdvice(new GlobalExceptionHandler(), new AuthExceptionHandler())
                .build();
    }

    // ---------- login ----------

    @Test
    void loginShouldReturn200WithBothTokens() throws Exception {
        when(loginUseCase.login("pedro@email.com", "senha-segura-123"))
                .thenReturn(new AuthTokens("access-123", "refresh-456"));

        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pedro@email.com\",\"password\":\"senha-segura-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-456"));
    }

    @Test
    void loginShouldReturn401WithGenericMessageWhenCredentialsAreInvalid() throws Exception {
        when(loginUseCase.login("pedro@email.com", "errada-123456"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pedro@email.com\",\"password\":\"errada-123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }

    @Test
    void loginShouldReturn400WithFieldErrorsWhenBodyIsIncomplete() throws Exception {
        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.fieldErrors.email").value("E-mail é obrigatório"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Senha é obrigatória"));
    }

    @Test
    void loginShouldReturn500WithoutDetailsOnUnexpectedError() throws Exception {
        when(loginUseCase.login("pedro@email.com", "senha-segura-123"))
                .thenThrow(new RuntimeException("detalhe interno"));

        mockMvc.perform(post("/api/v1/public/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pedro@email.com\",\"password\":\"senha-segura-123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno. Tente novamente mais tarde"));
    }

    // ---------- renovação ----------

    @Test
    void refreshShouldReturn200WithOnlyANewAccessToken() throws Exception {
        when(refreshTokenUseCase.refresh("refresh-456")).thenReturn("novo-access-789");

        mockMvc.perform(post("/api/v1/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("novo-access-789"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void refreshShouldReturn401WhenTokenIsInvalid() throws Exception {
        when(refreshTokenUseCase.refresh("ruim")).thenThrow(new InvalidTokenException());

        mockMvc.perform(post("/api/v1/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"ruim\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido ou expirado"));
    }

    @Test
    void refreshShouldReturn400WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/public/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.refreshToken").value("Refresh token é obrigatório"));
    }
}
