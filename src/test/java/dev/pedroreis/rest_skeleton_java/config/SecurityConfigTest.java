package dev.pedroreis.rest_skeleton_java.config;

import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.AuthExceptionHandler;
import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;
import dev.pedroreis.rest_skeleton_java.auth.ports.outbound.TokenPort;
import dev.pedroreis.rest_skeleton_java.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de fatia web com a cadeia de segurança real (ver ADR 006). O {@link TokenPort} é simulado
 * e as rotas vêm de um controller de teste, sem depender dos controllers de {@code user} e {@code auth}.
 * Sem banco.
 */
@WebMvcTest(controllers = SecurityConfigTest.ProbeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthExceptionHandler.class,
        SecurityConfigTest.ProbeController.class})
class SecurityConfigTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @RestController
    static class ProbeController {
        @GetMapping("/api/v1/public/ping")
        String publicPing() {
            return "pong";
        }

        @GetMapping("/api/v1/protected/whoami")
        String whoAmI(@AuthenticationPrincipal UUID userId) {
            return userId.toString();
        }

        @GetMapping("/api/v1/protected/forbidden")
        String forbidden() {
            throw new AccessDeniedException("sem permissão");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenPort tokenPort;

    @Test
    void publicRouteShouldOpenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/public/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void protectedRouteShouldReturn401WithErrorResponseWhenThereIsNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/protected/whoami"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Autenticação necessária ou token inválido"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void protectedRouteShouldReturn401WhenTokenIsInvalid() throws Exception {
        when(tokenPort.parseAccessToken(anyString())).thenThrow(new InvalidTokenException());

        mockMvc.perform(get("/api/v1/protected/whoami").header("Authorization", "Bearer ruim"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void protectedRouteShouldReturn401WhenHeaderIsNotBearer() throws Exception {
        mockMvc.perform(get("/api/v1/protected/whoami").header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteShouldPassWithValidAccessTokenAndExposeUserIdAsPrincipal() throws Exception {
        when(tokenPort.parseAccessToken("token-valido")).thenReturn(USER_ID);

        mockMvc.perform(get("/api/v1/protected/whoami").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isOk())
                .andExpect(content().string(USER_ID.toString()));
    }

    @Test
    void publicRouteShouldStillOpenWhenClientSendsAnInvalidToken() throws Exception {
        when(tokenPort.parseAccessToken(anyString())).thenThrow(new InvalidTokenException());

        mockMvc.perform(get("/api/v1/public/ping").header("Authorization", "Bearer vencido"))
                .andExpect(status().isOk());
    }

    @Test
    void accessDeniedThrownInsideControllerShouldReturn403NotCatchAll500() throws Exception {
        when(tokenPort.parseAccessToken("token-valido")).thenReturn(USER_ID);

        mockMvc.perform(get("/api/v1/protected/forbidden").header("Authorization", "Bearer token-valido"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acesso negado"));
    }

    @Test
    void corsShouldNotAllowAnyOriginByDefault() throws Exception {
        mockMvc.perform(options("/api/v1/public/ping")
                        .header("Origin", "http://outro-site.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
