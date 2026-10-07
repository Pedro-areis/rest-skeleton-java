package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa as regras do caso de uso com um repositório falso: sem Spring, sem banco, sem Mockito.
 */
class UserServiceTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 15);

    private FakeUserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = new FakeUserRepository();
        service = new UserService(repository);
    }

    private User register(String name, String email) {
        return service.execute(name, email, "123", BIRTH_DATE);
    }

    // ---------- criar ----------

    @Test
    void shouldCreateUserAndPersistIt() {
        User created = service.execute("Pedro", "pedro@email.com", "123", BIRTH_DATE);

        assertNotNull(created.getId());
        assertEquals("Pedro", created.getName());
        assertEquals("pedro@email.com", created.getEmail());
        assertEquals(BIRTH_DATE, created.getBirthDate());
        assertTrue(repository.findById(created.getId()).isPresent());
    }

    @Test
    void shouldNotCreateUserWhenEmailAlreadyExists() {
        register("Pedro", "pedro@email.com");

        assertThrows(EmailAlreadyExistsException.class, () -> register("Outro Pedro", "pedro@email.com"));

        assertEquals(1, repository.count());
    }

    @Test
    void shouldNotCreateUserWhenEmailIsInvalid() {
        assertThrows(InvalidEmailException.class, () -> register("Pedro", "email-sem-arroba"));

        assertEquals(0, repository.count());
    }

    @Test
    void shouldNotCreateUserWhenBirthDateIsInTheFuture() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertThrows(InvalidBirthDateException.class,
                () -> service.execute("Pedro", "pedro@email.com", "123", tomorrow));

        assertEquals(0, repository.count());
    }

    // ---------- consultar ----------

    @Test
    void shouldFindUserById() {
        User created = register("Pedro", "pedro@email.com");

        Optional<User> found = service.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals("pedro@email.com", found.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        assertTrue(service.findById(UUID.randomUUID()).isEmpty());
    }

    // ---------- atualizar ----------

    @Test
    void shouldUpdateUserFoundById() {
        User created = register("Pedro", "pedro@email.com");
        LocalDate newBirthDate = LocalDate.of(1995, 5, 20);

        // Muda também o e-mail: é o caso que quebrava quando o update buscava por e-mail.
        User updated = service.update(created.getId(), "Pedro Reis", "novo@email.com", "nova-senha", newBirthDate);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Pedro Reis", updated.getName());
        assertEquals("novo@email.com", updated.getEmail());
        assertEquals(newBirthDate, updated.getBirthDate());

        // Conferindo o que ficou gravado, e não só o retorno.
        User stored = repository.findById(created.getId()).orElseThrow();
        assertEquals("Pedro Reis", stored.getName());
        assertEquals("novo@email.com", stored.getEmail());
        assertEquals(newBirthDate, stored.getBirthDate());
        assertEquals("nova-senha", stored.getPassword());
        assertTrue(repository.findByEmail("pedro@email.com").isEmpty());
    }

    @Test
    void shouldKeepCurrentDataWhenUpdatingWithNullFields() {
        User created = register("Pedro", "pedro@email.com");

        service.update(created.getId(), null, null, null, null);

        User stored = repository.findById(created.getId()).orElseThrow();
        assertEquals("Pedro", stored.getName());
        assertEquals("pedro@email.com", stored.getEmail());
        assertEquals("123", stored.getPassword());
        assertEquals(BIRTH_DATE, stored.getBirthDate());
    }

    @Test
    void shouldAllowUpdatingKeepingTheSameEmail() {
        User created = register("Pedro", "pedro@email.com");

        // Mandar o próprio e-mail não é conflito: o e-mail já é dele.
        assertDoesNotThrow(() -> service.update(created.getId(), "Novo Nome", "pedro@email.com", null, null));

        assertEquals("Novo Nome", repository.findById(created.getId()).orElseThrow().getName());
    }

    @Test
    void shouldThrowUserNotFoundWhenUpdatingUnknownId() {
        assertThrows(UserNotFoundException.class,
                () -> service.update(UUID.randomUUID(), "Nome", null, null, null));
    }

    @Test
    void shouldNotUpdateWhenNewEmailBelongsToAnotherUser() {
        register("Pedro", "pedro@email.com");
        User maria = register("Maria", "maria@email.com");

        assertThrows(EmailAlreadyExistsException.class,
                () -> service.update(maria.getId(), "Maria Nova", "pedro@email.com", null, null));

        // Nada foi gravado: nem o e-mail, nem o nome que vinha junto.
        User stored = repository.findById(maria.getId()).orElseThrow();
        assertEquals("maria@email.com", stored.getEmail());
        assertEquals("Maria", stored.getName());
    }

    @Test
    void shouldNotUpdateWhenNewEmailIsInvalid() {
        User created = register("Pedro", "pedro@email.com");

        assertThrows(InvalidEmailException.class,
                () -> service.update(created.getId(), null, "invalido", null, null));

        assertEquals("pedro@email.com", repository.findById(created.getId()).orElseThrow().getEmail());
    }

    @Test
    void shouldNotUpdateWhenBirthDateIsInTheFuture() {
        User created = register("Pedro", "pedro@email.com");
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertThrows(InvalidBirthDateException.class,
                () -> service.update(created.getId(), null, null, null, tomorrow));

        assertEquals(BIRTH_DATE, repository.findById(created.getId()).orElseThrow().getBirthDate());
    }

    // ---------- excluir ----------

    @Test
    void shouldDeleteExistingUser() {
        User created = register("Pedro", "pedro@email.com");

        service.deleteById(created.getId());

        assertTrue(repository.findById(created.getId()).isEmpty());
    }

    @Test
    void shouldThrowUserNotFoundWhenDeletingUnknownId() {
        register("Pedro", "pedro@email.com");

        assertThrows(UserNotFoundException.class, () -> service.deleteById(UUID.randomUUID()));

        assertEquals(1, repository.count());
    }
}
