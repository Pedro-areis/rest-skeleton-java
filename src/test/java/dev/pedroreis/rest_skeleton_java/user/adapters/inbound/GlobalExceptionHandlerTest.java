package dev.pedroreis.rest_skeleton_java.user.adapters.inbound;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.UserNotFoundException;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.CreateUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.DeleteUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.FindUserUseCase;
import dev.pedroreis.rest_skeleton_java.user.ports.inbound.UpdateUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste web "standalone": sobe só o controller + o advice, sem contexto Spring,
 * sem Spring Security e sem banco.
 */
class GlobalExceptionHandlerTest {

    private CreateUserUseCase createUserUseCase;
    private UpdateUserUseCase updateUserUseCase;
    private DeleteUserUseCase deleteUserUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createUserUseCase = mock(CreateUserUseCase.class);
        updateUserUseCase = mock(UpdateUserUseCase.class);
        deleteUserUseCase = mock(DeleteUserUseCase.class);
        FindUserUseCase findUserUseCase = mock(FindUserUseCase.class);

        UserController controller = new UserController(
                createUserUseCase, findUserUseCase, updateUserUseCase, deleteUserUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static final String VALID_BODY =
            "{\"name\":\"Pedro\",\"email\":\"pedro@email.com\",\"password\":\"123\",\"birthDate\":\"2000-01-15\"}";

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any())).thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void shouldReturn400WhenDomainRejectsEmail() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any())).thenThrow(new InvalidEmailException());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail inválido"));
    }

    @Test
    void shouldReturn400WhenDomainRejectsBirthDate() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any())).thenThrow(new InvalidBirthDateException());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Data de nascimento inválida"));
    }

    @Test
    void shouldReturn400WithFieldErrorsWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"nao-e-email\",\"password\":\"123\",\"birthDate\":\"2000-01-15\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Nome é obrigatório"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Formato de e-mail inválido"));
    }

    @Test
    void shouldReturn400WithFieldErrorWhenBirthDateIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pedro\",\"email\":\"pedro@email.com\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.fieldErrors.birthDate").value("Data de nascimento é obrigatória"));
    }

    @Test
    void shouldReturn400WhenBirthDateHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pedro\",\"email\":\"pedro@email.com\",\"password\":\"123\",\"birthDate\":\"31/12/2000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou malformado"));
    }

    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isto nao e json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou malformado"));
    }

    @Test
    void shouldReturn404WhenUpdatingUnknownUser() throws Exception {
        when(updateUserUseCase.update(any(), any(), any(), any(), any())).thenThrow(new UserNotFoundException());

        mockMvc.perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Novo Nome\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    @Test
    void shouldReturn404WhenDeletingUnknownUser() throws Exception {
        doThrow(new UserNotFoundException()).when(deleteUserUseCase).deleteById(any());

        mockMvc.perform(delete("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }

    @Test
    void shouldReturn500WithGenericMessageAndNotLeakDetails() throws Exception {
        when(createUserUseCase.execute(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("senha do banco = segredo"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno. Tente novamente mais tarde"))
                .andExpect(jsonPath("$.message", not(containsString("segredo"))));
    }

    @Test
    void shouldKeepSpringStatusForUnsupportedMethod() throws Exception {
        // GET em /register não existe (só POST): o Spring responde 405 e o catch-all preserva o status.
        mockMvc.perform(get("/api/v1/users/register"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
