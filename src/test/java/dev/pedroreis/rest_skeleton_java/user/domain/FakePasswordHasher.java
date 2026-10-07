package dev.pedroreis.rest_skeleton_java.user.domain;

import dev.pedroreis.rest_skeleton_java.user.ports.outbound.PasswordHasherPort;

/**
 * Hasher falso para testar o domínio sem BCrypt (que é lento de propósito).
 * O "hash" é só o prefixo + a senha: previsível, instantâneo e fácil de conferir.
 * Conta quantas vezes foi chamado, para provar que o serviço não gera hash à toa.
 */
public class FakePasswordHasher implements PasswordHasherPort {

    public static final String PREFIX = "hashed:";

    private int hashCount = 0;

    @Override
    public String hash(String rawPassword) {
        hashCount++;
        return PREFIX + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return (PREFIX + rawPassword).equals(passwordHash);
    }

    public int hashCount() {
        return hashCount;
    }
}
