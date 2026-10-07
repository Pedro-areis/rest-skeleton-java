package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    private static final LocalDate VALID_BIRTH_DATE = LocalDate.of(2000, 1, 15);
    // O User só guarda o hash; quem gera o hash é o UserService (testado em UserServiceTest).
    private static final String PASSWORD_HASH = "hash-da-senha";

    private User newUser() {
        return new User("Pedro", "pedro@email.com", PASSWORD_HASH, VALID_BIRTH_DATE);
    }

    // ---------- criação ----------

    @Test
    void shouldGenerateIdAndCreatedAtWhenCreatingNewUser() {
        User user = newUser();

        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void shouldKeepGivenDataWhenCreatingNewUser() {
        User user = newUser();

        assertEquals("Pedro", user.getName());
        assertEquals("pedro@email.com", user.getEmail());
        assertEquals(PASSWORD_HASH, user.getPasswordHash());
        assertEquals(VALID_BIRTH_DATE, user.getBirthDate());
    }

    @Test
    void shouldNotValidateWhenRebuildingUserFromPersistence() {
        // O construtor de reconstituição não aplica regras: o dado já foi validado na criação.
        LocalDate futureDate = LocalDate.now().plusYears(1);

        assertDoesNotThrow(() -> new User(
                UUID.randomUUID(), "Pedro", "sem-arroba", PASSWORD_HASH, futureDate, LocalDateTime.now()));
    }

    // ---------- e-mail ----------

    @Test
    void shouldThrowInvalidEmailWhenEmailHasNoAtSign() {
        InvalidEmailException ex = assertThrows(InvalidEmailException.class,
                () -> new User("Pedro", "email-sem-arroba", PASSWORD_HASH, VALID_BIRTH_DATE));

        assertEquals("E-mail inválido", ex.getMessage());
    }

    @Test
    void shouldThrowInvalidEmailWhenEmailIsNull() {
        assertThrows(InvalidEmailException.class,
                () -> new User("Pedro", null, PASSWORD_HASH, VALID_BIRTH_DATE));
    }

    @Test
    void shouldCreateUserWithValidEmail() {
        assertDoesNotThrow(this::newUser);
    }

    @Test
    void shouldThrowInvalidEmailWhenUpdatingProfileWithInvalidEmail() {
        User user = newUser();

        assertThrows(InvalidEmailException.class, () -> user.updateProfile(null, "invalido", null));
    }

    @Test
    void shouldUpdateEmailWhenValid() {
        User user = newUser();

        user.updateProfile(null, "novo@email.com", null);

        assertEquals("novo@email.com", user.getEmail());
    }

    @Test
    void shouldKeepEmailWhenUpdatingWithBlank() {
        User user = newUser();

        user.updateProfile(null, "   ", null);

        assertEquals("pedro@email.com", user.getEmail());
    }

    // ---------- nome ----------

    @Test
    void shouldUpdateNameWhenFilled() {
        User user = newUser();

        user.updateProfile("Pedro Reis", null, null);

        assertEquals("Pedro Reis", user.getName());
    }

    @Test
    void shouldKeepNameWhenUpdatingWithNull() {
        User user = newUser();

        user.updateProfile(null, null, null);

        assertEquals("Pedro", user.getName());
    }

    @Test
    void shouldKeepNameWhenUpdatingWithBlank() {
        User user = newUser();

        user.updateProfile("   ", null, null);

        assertEquals("Pedro", user.getName());
    }

    // ---------- hash da senha ----------

    @Test
    void shouldUpdatePasswordHashWhenFilled() {
        User user = newUser();

        user.updatePasswordHash("novo-hash");

        assertEquals("novo-hash", user.getPasswordHash());
    }

    @Test
    void shouldKeepPasswordHashWhenUpdatingWithNull() {
        User user = newUser();

        user.updatePasswordHash(null);

        assertEquals(PASSWORD_HASH, user.getPasswordHash());
    }

    @Test
    void shouldKeepPasswordHashWhenUpdatingWithBlank() {
        User user = newUser();

        user.updatePasswordHash("   ");

        assertEquals(PASSWORD_HASH, user.getPasswordHash());
    }

    // ---------- data de nascimento ----------

    @Test
    void shouldThrowInvalidBirthDateWhenDateIsInTheFuture() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        InvalidBirthDateException ex = assertThrows(InvalidBirthDateException.class,
                () -> new User("Pedro", "pedro@email.com", PASSWORD_HASH, tomorrow));

        assertEquals("Data de nascimento inválida", ex.getMessage());
    }

    @Test
    void shouldThrowInvalidBirthDateWhenDateIsNull() {
        assertThrows(InvalidBirthDateException.class,
                () -> new User("Pedro", "pedro@email.com", PASSWORD_HASH, null));
    }

    @Test
    void shouldAcceptBirthDateOfToday() {
        assertDoesNotThrow(() -> new User("Pedro", "pedro@email.com", PASSWORD_HASH, LocalDate.now()));
    }

    @Test
    void shouldUpdateBirthDateWhenValid() {
        User user = newUser();
        LocalDate newDate = LocalDate.of(1995, 5, 20);

        user.updateProfile(null, null, newDate);

        assertEquals(newDate, user.getBirthDate());
    }

    @Test
    void shouldKeepBirthDateWhenUpdatingWithNull() {
        User user = newUser();

        user.updateProfile("Outro Nome", null, null);

        assertEquals(VALID_BIRTH_DATE, user.getBirthDate());
    }

    @Test
    void shouldThrowInvalidBirthDateWhenUpdatingWithFutureDate() {
        User user = newUser();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        assertThrows(InvalidBirthDateException.class, () -> user.updateProfile(null, null, tomorrow));
        assertEquals(VALID_BIRTH_DATE, user.getBirthDate());
    }
}
