package dev.pedroreis.rest_skeleton_java.auth.domain;

import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;
import dev.pedroreis.rest_skeleton_java.auth.ports.outbound.TokenPort;

import java.util.UUID;

/**
 * Token falso para testar o domínio sem JWT: o "token" é o tipo + o id ("access:&lt;uuid&gt;"),
 * previsível e fácil de conferir. Só o teste do adaptador usa o JJWT de verdade.
 */
public class FakeTokenPort implements TokenPort {

    private static final String ACCESS_PREFIX = "access:";
    private static final String REFRESH_PREFIX = "refresh:";

    @Override
    public String generateAccessToken(UUID userId) {
        return ACCESS_PREFIX + userId;
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        return REFRESH_PREFIX + userId;
    }

    @Override
    public UUID parseAccessToken(String token) {
        return parse(token, ACCESS_PREFIX);
    }

    @Override
    public UUID parseRefreshToken(String token) {
        return parse(token, REFRESH_PREFIX);
    }

    private UUID parse(String token, String prefix) {
        if (token == null || !token.startsWith(prefix)) {
            throw new InvalidTokenException();
        }
        try {
            return UUID.fromString(token.substring(prefix.length()));
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }
}
