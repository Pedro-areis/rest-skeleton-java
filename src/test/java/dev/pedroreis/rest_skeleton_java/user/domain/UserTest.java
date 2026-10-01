package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    void shouldThrowInvalidEmailWhenEmailHasNoAtSign() {
        InvalidEmailException ex = assertThrows(InvalidEmailException.class,
                () -> new User("Pedro", "email-sem-arroba", "123"));

        assertEquals("E-mail inválido", ex.getMessage());
    }

    @Test
    void shouldThrowInvalidEmailWhenEmailIsNull() {
        assertThrows(InvalidEmailException.class, () -> new User("Pedro", null, "123"));
    }

    @Test
    void shouldCreateUserWithValidEmail() {
        assertDoesNotThrow(() -> new User("Pedro", "pedro@email.com", "123"));
    }

    @Test
    void shouldThrowInvalidEmailWhenUpdatingProfileWithInvalidEmail() {
        User user = new User("Pedro", "pedro@email.com", "123");

        assertThrows(InvalidEmailException.class, () -> user.updateProfile(null, "invalido"));
    }
}
