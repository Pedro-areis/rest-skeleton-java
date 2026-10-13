package dev.pedroreis.rest_skeleton_java.auth.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;
import dev.pedroreis.rest_skeleton_java.auth.ports.outbound.TokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Tokens JWT assinados com HS256 (ADR 002). O {@code sub} é o id do usuário e o claim
 * {@code type} separa access de refresh token. O tempo vem do {@link Clock} injetado.
 */
@Component
public class JwtTokenAdapter implements TokenPort {
    static final String TYPE_CLAIM = "type";
    static final String ACCESS = "access";
    static final String REFRESH = "refresh";
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final Clock clock;

    /**
     * @throws IllegalArgumentException se o segredo tiver menos de 32 bytes (exigência do HS256)
     */
    public JwtTokenAdapter(
            @Value("${api.security.token.secret}") String secret,
            @Value("${api.security.token.access-expiration}") Duration accessTtl,
            @Value("${api.security.token.refresh-expiration}") Duration refreshTtl,
            Clock clock) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("O segredo do token precisa ter pelo menos 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.clock = clock;
    }

    @Override
    public String generateAccessToken(UUID userId) {
        return generate(userId, ACCESS, accessTtl);
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        return generate(userId, REFRESH, refreshTtl);
    }

    @Override
    public UUID parseAccessToken(String token) {
        return parse(token, ACCESS);
    }

    @Override
    public UUID parseRefreshToken(String token) {
        return parse(token, REFRESH);
    }

    private String generate(UUID userId, String type, Duration ttl) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(TYPE_CLAIM, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                // HS256 explícito: sem isso, o JJWT escolheria HS384/HS512 para chaves maiores.
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private UUID parse(String token, String expectedType) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .require(TYPE_CLAIM, expectedType)
                    .build()
                    .parseSignedClaims(token);
            if (!Jwts.SIG.HS256.getId().equals(jws.getHeader().getAlgorithm())) {
                throw new InvalidTokenException();
            }
            return UUID.fromString(jws.getPayload().getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            // Inclui token nulo/vazio, malformado, expirado, adulterado, de tipo errado ou com sub inválido.
            throw new InvalidTokenException();
        }
    }
}
