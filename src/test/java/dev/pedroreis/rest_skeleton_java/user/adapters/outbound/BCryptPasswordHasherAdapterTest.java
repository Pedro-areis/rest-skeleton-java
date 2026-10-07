package dev.pedroreis.rest_skeleton_java.user.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Usa o BCrypt de verdade, que é lento de propósito (~100 ms por hash).
 * Por isso são poucos testes: o resto do sistema usa o FakePasswordHasher.
 */
class BCryptPasswordHasherAdapterTest {

    private final BCryptPasswordHasherAdapter hasher = new BCryptPasswordHasherAdapter();

    @Test
    void shouldNotReturnTheRawPasswordAsHash() {
        String hash = hasher.hash("minha-senha");

        assertNotEquals("minha-senha", hash);
    }

    @Test
    void shouldMatchCorrectPassword() {
        String hash = hasher.hash("minha-senha");

        assertTrue(hasher.matches("minha-senha", hash));
    }

    @Test
    void shouldNotMatchWrongPassword() {
        String hash = hasher.hash("minha-senha");

        assertFalse(hasher.matches("outra-senha", hash));
    }

    @Test
    void shouldGenerateDifferentHashesForTheSamePassword() {
        // Cada hash leva um "sal" aleatório: a mesma senha nunca gera o mesmo hash duas vezes.
        String first = hasher.hash("minha-senha");
        String second = hasher.hash("minha-senha");

        assertNotEquals(first, second);
        assertTrue(hasher.matches("minha-senha", first));
        assertTrue(hasher.matches("minha-senha", second));
    }

    @Test
    void shouldAcceptPasswordOfExactly72Bytes() {
        String password = "a".repeat(72);

        assertDoesNotThrow(() -> hasher.hash(password));
    }

    @Test
    void shouldRejectPasswordLongerThan72Bytes() {
        String password = "a".repeat(73);

        assertThrows(InvalidPasswordException.class, () -> hasher.hash(password));
    }

    @Test
    void shouldCountBytesAndNotCharacters() {
        // 40 caracteres, mas "ñ" ocupa 2 bytes em UTF-8: são 80 bytes.
        String password = "ñ".repeat(40);

        assertThrows(InvalidPasswordException.class, () -> hasher.hash(password));
    }

    @Test
    void shouldAcceptMultibytePasswordOfExactly72Bytes() {
        // 36 caracteres de 2 bytes = 72 bytes.
        String password = "ñ".repeat(36);

        assertDoesNotThrow(() -> hasher.hash(password));
    }

    @Test
    void shouldNotMatchWhenPasswordIsLongerThan72BytesEvenIfStartIsTheSame() {
        // O BCrypt ignora o que passa de 72 bytes. Sem a conferência, esta senha "bateria" com o hash.
        String stored = "a".repeat(72);
        String hash = hasher.hash(stored);

        assertFalse(hasher.matches(stored + "b", hash));
    }
}
