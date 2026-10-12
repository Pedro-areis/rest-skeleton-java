package dev.pedroreis.rest_skeleton_java.auth.domain;

import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidCredentialsException;
import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;
import dev.pedroreis.rest_skeleton_java.user.domain.FakePasswordHasher;
import dev.pedroreis.rest_skeleton_java.user.domain.FakeUserRepository;
import dev.pedroreis.rest_skeleton_java.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Testa o domínio de auth sem Spring, sem banco e sem JWT (fakes de repositório, hasher e token). */
class AuthServiceTest {

    private static final String EMAIL = "pedro@email.com";
    private static final String PASSWORD = "senha-segura-123";

    private FakeUserRepository userRepository;
    private FakeTokenPort tokenPort;
    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        FakePasswordHasher passwordHasher = new FakePasswordHasher();
        tokenPort = new FakeTokenPort();
        authService = new AuthService(userRepository, passwordHasher, tokenPort);

        user = userRepository.save(new User(
                "Pedro", EMAIL, passwordHasher.hash(PASSWORD), LocalDate.of(2000, 1, 15)));
    }

    // ---------- login ----------

    @Test
    void loginShouldReturnAccessAndRefreshTokensOfTheUser() {
        AuthTokens tokens = authService.login(EMAIL, PASSWORD);

        assertEquals(user.getId(), tokenPort.parseAccessToken(tokens.accessToken()));
        assertEquals(user.getId(), tokenPort.parseRefreshToken(tokens.refreshToken()));
    }

    @Test
    void loginShouldRejectWrongPassword() {
        assertThrows(InvalidCredentialsException.class, () -> authService.login(EMAIL, "senha-errada-123"));
    }

    @Test
    void loginShouldRejectUnknownEmail() {
        assertThrows(InvalidCredentialsException.class, () -> authService.login("outro@email.com", PASSWORD));
    }

    @Test
    void loginShouldGiveTheSameMessageForUnknownEmailAndWrongPassword() {
        InvalidCredentialsException wrongPassword = assertThrows(
                InvalidCredentialsException.class, () -> authService.login(EMAIL, "senha-errada-123"));
        InvalidCredentialsException unknownEmail = assertThrows(
                InvalidCredentialsException.class, () -> authService.login("outro@email.com", PASSWORD));

        assertEquals(wrongPassword.getMessage(), unknownEmail.getMessage());
    }

    @Test
    void loginShouldRejectNullPassword() {
        assertThrows(InvalidCredentialsException.class, () -> authService.login(EMAIL, null));
    }

    // ---------- renovação ----------

    @Test
    void refreshShouldReturnANewAccessTokenOfTheSameUser() {
        AuthTokens tokens = authService.login(EMAIL, PASSWORD);

        String newAccessToken = authService.refresh(tokens.refreshToken());

        assertEquals(user.getId(), tokenPort.parseAccessToken(newAccessToken));
    }

    @Test
    void refreshShouldRejectAnAccessToken() {
        AuthTokens tokens = authService.login(EMAIL, PASSWORD);

        assertThrows(InvalidTokenException.class, () -> authService.refresh(tokens.accessToken()));
    }

    @Test
    void refreshShouldRejectGarbage() {
        assertThrows(InvalidTokenException.class, () -> authService.refresh("lixo"));
        assertThrows(InvalidTokenException.class, () -> authService.refresh(null));
    }

    @Test
    void refreshShouldRejectTokenOfAUserThatNoLongerExists() {
        AuthTokens tokens = authService.login(EMAIL, PASSWORD);
        userRepository.deleteById(user.getId());

        assertThrows(InvalidTokenException.class, () -> authService.refresh(tokens.refreshToken()));
    }

    @Test
    void refreshShouldRejectTokenOfAnUnknownUser() {
        String tokenOfSomeoneElse = tokenPort.generateRefreshToken(UUID.randomUUID());

        assertThrows(InvalidTokenException.class, () -> authService.refresh(tokenOfSomeoneElse));
    }
}
