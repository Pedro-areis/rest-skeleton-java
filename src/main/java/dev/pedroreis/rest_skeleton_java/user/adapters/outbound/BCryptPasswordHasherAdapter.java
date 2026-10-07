package dev.pedroreis.rest_skeleton_java.user.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.user.domain.exception.InvalidPasswordException;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {
    /** O BCrypt só considera os primeiros 72 BYTES da senha (não caracteres: "ñ" ocupa 2 bytes). */
    private static final int MAX_PASSWORD_BYTES = 72;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        if (exceedsBcryptLimit(rawPassword)) {
            throw new InvalidPasswordException();
        }
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        // Senha acima do limite nunca pode ter sido gravada, então não corresponde a nenhum hash.
        if (rawPassword == null || passwordHash == null || exceedsBcryptLimit(rawPassword)) {
            return false;
        }
        return encoder.matches(rawPassword, passwordHash);
    }

    private boolean exceedsBcryptLimit(String rawPassword) {
        return rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
    }
}
