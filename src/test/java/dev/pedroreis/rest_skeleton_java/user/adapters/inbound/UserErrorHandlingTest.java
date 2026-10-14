package dev.pedroreis.rest_skeleton_java.user.adapters.inbound;

import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.AuthExceptionHandler;
import dev.pedroreis.rest_skeleton_java.auth.ports.outbound.TokenPort;
import dev.pedroreis.rest_skeleton_java.config.SecurityConfig;
import dev.pedroreis.rest_skeleton_java.shared.web.GlobalExceptionHandler;
import dev.pedroreis.rest_skeleton_java.user.domain.User;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidPasswordException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.UserNotFoundException;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.CreateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.DeleteUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.FindUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.UpdateUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de fatia web com a cadeia de segurança real (ver ADR 006): controller, advices e filtro de token,
 * com o {@link TokenPort} e os casos de uso simulados. Sem banco.
 */
@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthExceptionHandler.class, UserExceptionHandler.class})
class UserErrorHandlingTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String BEARER = "Bearer token-valido";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenPort tokenPort;
    @MockitoBean
    private CreateUserUseCase createUserUseCase;
    @MockitoBean
    private FindUserUseCase findUserUseCase;
    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;
    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    @BeforeEach
    void setUp() {
        when(tokenPort.parseAccessToken("token-valido")).thenReturn(USER_ID);
    }

    private static final String VALID_BODY =
            "{\"name\":\"Pedro\",\"email\":\"pedro@email.com\",\"password\":\"123\",\"birthDate\":\"2000-01-15\"}";

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any())).thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void shouldReturn400WhenDomainRejectsEmail() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any())).thenThrow(new InvalidEmailException());

        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail inválido"));
    }

    @Test
    void shouldReturn400WhenDomainRejectsBirthDate() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any())).thenThrow(new InvalidBirthDateException());

        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Data de nascimento inválida"));
    }

    @Test
    void shouldReturn400WithFieldErrorsWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"nao-e-email\",\"password\":\"123\",\"birthDate\":\"2000-01-15\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Nome é obrigatório"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Formato de e-mail inválido"));
    }

    @Test
    void shouldReturn400WithFieldErrorWhenBirthDateIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pedro\",\"email\":\"pedro@email.com\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.fieldErrors.birthDate").value("Data de nascimento é obrigatória"));
    }

    @Test
    void shouldReturn400WhenBirthDateHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pedro\",\"email\":\"pedro@email.com\",\"password\":\"123\",\"birthDate\":\"31/12/2000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou malformado"));
    }

    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isto nao e json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou malformado"));
    }

    @Test
    void shouldReturn401WhenUpdatingUserThatNoLongerExists() throws Exception {
        when(updateUserUseCase.update(any(), any(), any(), any(), any())).thenThrow(new UserNotFoundException());

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novo Nome\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticação necessária ou token inválido"));
    }

    @Test
    void shouldReturn400WhenPasswordIsTooLong() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any())).thenThrow(new InvalidPasswordException());

        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Senha inválida: deve ter no mínimo 8 caracteres e no máximo 72 bytes"));
    }

    @Test
    void shouldReturn400WithFieldErrorWhenUpdatingWithInvalidEmailFormat() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-e-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Formato de e-mail inválido"));
    }

    @Test
    void shouldReturn409WhenUpdatingToEmailOfAnotherUser() throws Exception {
        when(updateUserUseCase.update(any(), any(), any(), any(), any())).thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"outro@email.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void shouldReturn401WhenDeletingUserThatNoLongerExists() throws Exception {
        doThrow(new UserNotFoundException()).when(deleteUserUseCase).deleteById(any());

        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", BEARER))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticação necessária ou token inválido"));
    }

    @Test
    void shouldReturnOwnDataOnGetMe() throws Exception {
        User user = new User("Pedro", "pedro@email.com", "hash", LocalDate.of(2000, 1, 15));
        when(findUserUseCase.findById(USER_ID)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("pedro@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldReturn401WhenGettingUserThatNoLongerExists() throws Exception {
        when(findUserUseCase.findById(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", BEARER))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticação necessária ou token inválido"));
    }

    @Test
    void shouldReturn401OnMeRoutesWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/v1/users/me").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUseIdFromTokenWhenUpdatingAndDeleting() throws Exception {
        User user = new User("Novo Nome", "pedro@email.com", "hash", LocalDate.of(2000, 1, 15));
        when(updateUserUseCase.update(eq(USER_ID), any(), any(), any(), any())).thenReturn(user);

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", BEARER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novo Nome\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/users/me").header("Authorization", BEARER))
                .andExpect(status().isNoContent());

        verify(deleteUserUseCase).deleteById(USER_ID);
    }

    @Test
    void shouldNotExposeRoutesWithUserIdInPath() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", UUID.randomUUID()).header("Authorization", BEARER))
                .andExpect(status().is4xxClientError());
        verify(deleteUserUseCase, org.mockito.Mockito.never()).deleteById(any());
    }

    @Test
    void shouldReturn500WithGenericMessageAndNotLeakDetails() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("senha do banco = segredo"));

        mockMvc.perform(post("/api/v1/public/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno. Tente novamente mais tarde"))
                .andExpect(jsonPath("$.message", not(containsString("segredo"))));
    }

    @Test
    void shouldKeepSpringStatusForUnsupportedMethod() throws Exception {
        // GET em /register não existe (só POST): o Spring responde 405 e o catch-all preserva o status.
        mockMvc.perform(get("/api/v1/public/users/register"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
