package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidBirthDateException;
import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    private static final LocalDate VALID_BIRTH_DATE = LocalDate.of(2000, 1, 15);

    private User newUser() {
        return new User("Pedro", "pedro@email.com", "123", VALID_BIRTH_DATE);
    }

    // ---------- e-mail ----------

    @Test
    void shouldThrowInvalidEmailWhenEmailHasNoAtSign() {
        InvalidEmailException ex = assertThrows(InvalidEmailException.class,
                () -> new User("Pedro", "email-sem-arroba", "123", VALID_BIRTH_DATE));

        assertEquals("E-mail inválido", ex.getMessage());
    }

    @Test
    void shouldThrowInvalidEmailWhenEmailIsNull() {
        assertThrows(InvalidEmailException.class,
                () -> new User("Pedro", null, "123", VALID_BIRTH_DATE));
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

    // ---------- data de nascimento ----------

    @Test
    void shouldThrowInvalidBirthDateWhenDateIsInTheFuture() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        InvalidBirthDateException ex = assertThrows(InvalidBirthDateException.class,
                () -> new User("Pedro", "pedro@email.com", "123", tomorrow));

        assertEquals("Data de nascimento inválida", ex.getMessage());
    }

    @Test
    void shouldThrowInvalidBirthDateWhenDateIsNull() {
        assertThrows(InvalidBirthDateException.class,
                () -> new User("Pedro", "pedro@email.com", "123", null));
    }

    @Test
    void shouldAcceptBirthDateOfToday() {
        assertDoesNotThrow(() -> new User("Pedro", "pedro@email.com", "123", LocalDate.now()));
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
