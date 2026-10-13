package dev.pedroreis.rest_skeleton_java.auth.ports.outbound;

import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;

import java.util.UUID;

/**
 * Emissão e leitura de tokens. O domínio não conhece a biblioteca JWT (ADR 002).
 * Cada leitura exige o tipo certo: um refresh token não vale como access token, e o contrário.
 */
public interface TokenPort {
    String generateAccessToken(UUID userId);

    String generateRefreshToken(UUID userId);

    /** @return o id do usuário dono do token @throws InvalidTokenException se não for um access token válido */
    UUID parseAccessToken(String token);

    /** @return o id do usuário dono do token @throws InvalidTokenException se não for um refresh token válido */
    UUID parseRefreshToken(String token);
}
