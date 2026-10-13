package dev.pedroreis.rest_skeleton_java.auth.adapters.outbound;

import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Usa o JJWT de verdade, mas com um relógio controlado: nenhum teste espera o tempo passar. */
class JwtTokenAdapterTest {

    private static final String SECRET = "segredo-de-teste-com-mais-de-32-bytes-ok!";
    private static final Duration ACCESS_TTL = Duration.ofHours(1);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);
    private static final Instant START = Instant.parse("2026-10-12T12:00:00Z");

    private MutableClock clock;
    private JwtTokenAdapter adapter;
    private UUID userId;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        adapter = new JwtTokenAdapter(SECRET, ACCESS_TTL, REFRESH_TTL, clock);
        userId = UUID.randomUUID();
    }

    // ---------- emissão e leitura ----------

    @Test
    void accessTokenShouldCarryTheUserId() {
        String token = adapter.generateAccessToken(userId);

        assertEquals(userId, adapter.parseAccessToken(token));
    }

    @Test
    void refreshTokenShouldCarryTheUserId() {
        String token = adapter.generateRefreshToken(userId);

        assertEquals(userId, adapter.parseRefreshToken(token));
    }

    @Test
    void accessAndRefreshTokensShouldBeDifferent() {
        assertNotEquals(adapter.generateAccessToken(userId), adapter.generateRefreshToken(userId));
    }

    // ---------- tipo ----------

    @Test
    void refreshTokenShouldNotWorkAsAccessToken() {
        String refresh = adapter.generateRefreshToken(userId);

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(refresh));
    }

    @Test
    void accessTokenShouldNotWorkAsRefreshToken() {
        String access = adapter.generateAccessToken(userId);

        assertThrows(InvalidTokenException.class, () -> adapter.parseRefreshToken(access));
    }

    @Test
    void tokenWithoutTypeShouldBeRejected() {
        String semTipo = Jwts.builder().subject(userId.toString()).signWith(key(SECRET), Jwts.SIG.HS256).compact();

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(semTipo));
        assertThrows(InvalidTokenException.class, () -> adapter.parseRefreshToken(semTipo));
    }

    // ---------- expiração (relógio controlado) ----------

    @Test
    void accessTokenShouldBeValidJustBeforeOneHour() {
        String token = adapter.generateAccessToken(userId);
        clock.advance(ACCESS_TTL.minusSeconds(1));

        assertDoesNotThrow(() -> adapter.parseAccessToken(token));
    }

    @Test
    void accessTokenShouldExpireAfterOneHour() {
        String token = adapter.generateAccessToken(userId);
        clock.advance(ACCESS_TTL.plusSeconds(1));

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(token));
    }

    @Test
    void refreshTokenShouldOutliveTheAccessToken() {
        String access = adapter.generateAccessToken(userId);
        String refresh = adapter.generateRefreshToken(userId);
        clock.advance(Duration.ofHours(2));

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(access));
        assertEquals(userId, adapter.parseRefreshToken(refresh));
    }

    @Test
    void refreshTokenShouldBeValidJustBeforeSevenDays() {
        String token = adapter.generateRefreshToken(userId);
        clock.advance(REFRESH_TTL.minusSeconds(1));

        assertDoesNotThrow(() -> adapter.parseRefreshToken(token));
    }

    @Test
    void refreshTokenShouldExpireAfterSevenDays() {
        String token = adapter.generateRefreshToken(userId);
        clock.advance(REFRESH_TTL.plusSeconds(1));

        assertThrows(InvalidTokenException.class, () -> adapter.parseRefreshToken(token));
    }

    // ---------- assinatura e formato ----------

    @Test
    void tokenSignedWithAnotherSecretShouldBeRejected() {
        JwtTokenAdapter other = new JwtTokenAdapter(
                "outro-segredo-tambem-com-mais-de-32-bytes!", ACCESS_TTL, REFRESH_TTL, clock);
        String token = other.generateAccessToken(userId);

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(token));
    }

    @Test
    void tokenWithTamperedPayloadShouldBeRejected() {
        String mine = adapter.generateAccessToken(userId);
        String someoneElses = adapter.generateAccessToken(UUID.randomUUID());
        // Cabeçalho e assinatura do meu token + payload do token de outro usuário.
        String forged = mine.split("\\.")[0] + "." + someoneElses.split("\\.")[1] + "." + mine.split("\\.")[2];

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(forged));
    }

    @Test
    void unsignedTokenShouldBeRejected() {
        String unsigned = Jwts.builder()
                .subject(userId.toString())
                .claim("type", "access")
                .compact();

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(unsigned));
    }

    @Test
    void tokenSignedWithAnotherAlgorithmShouldBeRejected() {
        // Mesma chave, mas HS512: a assinatura é válida, só que o sistema aceita apenas HS256.
        SecretKey longKey = key("segredo-de-teste-bem-maior-com-pelo-menos-64-bytes-para-o-hs512-ok!!");
        JwtTokenAdapter longKeyAdapter = new JwtTokenAdapter(
                "segredo-de-teste-bem-maior-com-pelo-menos-64-bytes-para-o-hs512-ok!!", ACCESS_TTL, REFRESH_TTL, clock);
        String hs512 = Jwts.builder()
                .subject(userId.toString())
                .claim("type", "access")
                .expiration(java.util.Date.from(START.plus(ACCESS_TTL)))
                .signWith(longKey, Jwts.SIG.HS512)
                .compact();

        assertThrows(InvalidTokenException.class, () -> longKeyAdapter.parseAccessToken(hs512));
    }

    @Test
    void tokenWithSubjectThatIsNotAUuidShouldBeRejected() {
        String token = Jwts.builder()
                .subject("nao-e-um-uuid")
                .claim("type", "access")
                .expiration(java.util.Date.from(START.plus(ACCESS_TTL)))
                .signWith(key(SECRET), Jwts.SIG.HS256)
                .compact();

        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(token));
    }

    @Test
    void malformedNullAndBlankTokensShouldBeRejected() {
        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken("isto.nao.e-jwt"));
        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken("lixo"));
        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(""));
        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken("   "));
        assertThrows(InvalidTokenException.class, () -> adapter.parseAccessToken(null));
    }

    // ---------- configuração ----------

    @Test
    void shouldRefuseASecretShorterThan32Bytes() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtTokenAdapter("curto", ACCESS_TTL, REFRESH_TTL, clock));
    }

    @Test
    void shouldAcceptADefaultLengthSecretOfMoreThan48BytesAndStillSignWithHs256() {
        // O segredo padrão do application.properties tem mais de 48 bytes (o JJWT escolheria HS384 sozinho).
        String longSecret = "chave_secreta_super_segura_padrao_para_ambiente_local";
        JwtTokenAdapter longAdapter = new JwtTokenAdapter(longSecret, ACCESS_TTL, REFRESH_TTL, clock);

        String token = longAdapter.generateAccessToken(userId);

        assertEquals(userId, longAdapter.parseAccessToken(token));
        assertEquals("HS256", Jwts.parser().verifyWith(key(longSecret)).clock(() -> java.util.Date.from(START))
                .build().parseSignedClaims(token).getHeader().getAlgorithm());
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Relógio que só anda quando o teste manda. */
    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
