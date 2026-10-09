package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.EmailAlreadyExistsException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidPasswordException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa as regras do caso de uso com repositório e hasher falsos: sem Spring, sem banco, sem Mockito.
 */
class UserServiceTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 15);
    private static final String RAW_PASSWORD = "senha-valida";
    private static final String HASHED_PASSWORD = FakePasswordHasher.PREFIX + RAW_PASSWORD;

    private FakeUserRepository repository;
    private FakePasswordHasher hasher;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = new FakeUserRepository();
        hasher = new FakePasswordHasher();
        service = new UserService(repository, hasher);
    }

    private User register(String name, String email) {
        return service.execute(name, email, RAW_PASSWORD, BIRTH_DATE);
    }

    // ---------- criar ----------

    @Test
    void shouldCreateUserAndPersistIt() {
        User created = service.execute("Pedro", "pedro@email.com", RAW_PASSWORD, BIRTH_DATE);

        assertNotNull(created.getId());
        assertEquals("Pedro", created.getName());
        assertEquals("pedro@email.com", created.getEmail());
        assertEquals(BIRTH_DATE, created.getBirthDate());
        assertTrue(repository.findById(created.getId()).isPresent());
    }

    @Test
    void shouldStoreHashedPasswordInsteadOfRawPassword() {
        User created = register("Pedro", "pedro@email.com");

        User stored = repository.findById(created.getId()).orElseThrow();
        assertEquals(HASHED_PASSWORD, stored.getPasswordHash());
        assertNotEquals(RAW_PASSWORD, stored.getPasswordHash());
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
                () -> service.execute("Pedro", "pedro@email.com", RAW_PASSWORD, tomorrow));

        assertEquals(0, repository.count());
    }

    // ---------- regra de senha (cadastro) ----------

    @Test
    void shouldNotCreateUserWhenPasswordIsShorterThanMinimum() {
        // 7 caracteres: um a menos que o mínimo.
        assertThrows(InvalidPasswordException.class,
                () -> service.execute("Pedro", "pedro@email.com", "1234567", BIRTH_DATE));

        assertEquals(0, repository.count());
        // Senha rejeitada nunca chega ao hash (que é caro de propósito).
        assertEquals(0, hasher.hashCount());
    }

    @Test
    void shouldNotCreateUserWhenPasswordIsNull() {
        assertThrows(InvalidPasswordException.class,
                () -> service.execute("Pedro", "pedro@email.com", null, BIRTH_DATE));

        assertEquals(0, repository.count());
    }

    @Test
    void shouldCreateUserWhenPasswordHasExactlyMinimumLength() {
        User created = service.execute("Pedro", "pedro@email.com", "12345678", BIRTH_DATE);

        assertEquals(FakePasswordHasher.PREFIX + "12345678",
                repository.findById(created.getId()).orElseThrow().getPasswordHash());
    }

    @Test
    void shouldNotRequireUppercaseDigitsOrSymbols() {
        // Só letras minúsculas: a regra é apenas o tamanho (RF01).
        assertDoesNotThrow(() -> service.execute("Pedro", "pedro@email.com", "aaaaaaaa", BIRTH_DATE));
    }

    @Test
    void shouldCountSpacesAsCharactersAndKeepThemUntouched() {
        // 4 letras + 4 espaços = 8 caracteres. Os espaços contam e a senha segue sem trim.
        String passwordWithTrailingSpaces = "abcd    ";

        User created = service.execute("Pedro", "pedro@email.com", passwordWithTrailingSpaces, BIRTH_DATE);

        assertEquals(FakePasswordHasher.PREFIX + passwordWithTrailingSpaces,
                repository.findById(created.getId()).orElseThrow().getPasswordHash());
    }

    @Test
    void shouldNotCreateUserWhenPasswordWithSpacesIsShorterThanMinimum() {
        // 3 letras + 4 espaços = 7 caracteres.
        assertThrows(InvalidPasswordException.class,
                () -> service.execute("Pedro", "pedro@email.com", "abc    ", BIRTH_DATE));

        assertEquals(0, repository.count());
    }

    @Test
    void shouldCountCharactersByStringLength() {
        // Decisão da Task 5.1: conta-se com String.length() (unidades UTF-16).
        // Cada emoji ocupa 2 unidades, então 4 emojis somam 8 e atingem o mínimo.
        String fourEmojis = "😀😀😀😀";

        assertDoesNotThrow(() -> service.execute("Pedro", "pedro@email.com", fourEmojis, BIRTH_DATE));
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
        assertEquals(FakePasswordHasher.PREFIX + "nova-senha", stored.getPasswordHash());
        assertTrue(repository.findByEmail("pedro@email.com").isEmpty());
    }

    @Test
    void shouldKeepCurrentDataWhenUpdatingWithNullFields() {
        User created = register("Pedro", "pedro@email.com");
        int hashesBefore = hasher.hashCount();

        service.update(created.getId(), null, null, null, null);

        User stored = repository.findById(created.getId()).orElseThrow();
        assertEquals("Pedro", stored.getName());
        assertEquals("pedro@email.com", stored.getEmail());
        assertEquals(HASHED_PASSWORD, stored.getPasswordHash());
        assertEquals(BIRTH_DATE, stored.getBirthDate());
        // Sem senha nova, não há por que gerar outro hash.
        assertEquals(hashesBefore, hasher.hashCount());
    }

    @Test
    void shouldKeepPasswordHashWhenUpdatingWithBlankPassword() {
        User created = register("Pedro", "pedro@email.com");
        int hashesBefore = hasher.hashCount();

        service.update(created.getId(), null, null, "   ", null);

        assertEquals(HASHED_PASSWORD, repository.findById(created.getId()).orElseThrow().getPasswordHash());
        assertEquals(hashesBefore, hasher.hashCount());
    }

    @Test
    void shouldNotUpdateWhenNewPasswordIsShorterThanMinimum() {
        User created = register("Pedro", "pedro@email.com");
        int hashesBefore = hasher.hashCount();

        // O nome vem junto: se a senha for rejeitada, nada do que veio na requisição pode ser gravado.
        assertThrows(InvalidPasswordException.class,
                () -> service.update(created.getId(), "Pedro Reis", null, "1234567", null));

        User stored = repository.findById(created.getId()).orElseThrow();
        assertEquals("Pedro", stored.getName());
        assertEquals(HASHED_PASSWORD, stored.getPasswordHash());
        assertEquals(hashesBefore, hasher.hashCount());
    }

    @Test
    void shouldUpdatePasswordWhenNewPasswordHasExactlyMinimumLength() {
        User created = register("Pedro", "pedro@email.com");

        service.update(created.getId(), null, null, "12345678", null);

        assertEquals(FakePasswordHasher.PREFIX + "12345678",
                repository.findById(created.getId()).orElseThrow().getPasswordHash());
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
