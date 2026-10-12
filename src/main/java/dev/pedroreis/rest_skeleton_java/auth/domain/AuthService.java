package dev.pedroreis.rest_skeleton_java.auth.domain;

import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidCredentialsException;
import dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException;
import dev.pedroreis.rest_skeleton_java.auth.ports.inbound.LoginUseCase;
import dev.pedroreis.rest_skeleton_java.auth.ports.inbound.RefreshTokenUseCase;
import dev.pedroreis.rest_skeleton_java.auth.ports.outbound.TokenPort;
import dev.pedroreis.rest_skeleton_java.user.domain.User;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.PasswordHasherPort;
import dev.pedroreis.rest_skeleton_java.user.ports.outbound.UserRepositoryPort;

import java.util.UUID;

/** Depende de {@code user} (auth → user), nunca o contrário (ADR 001). */
public class AuthService implements LoginUseCase, RefreshTokenUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final TokenPort tokenPort;

    public AuthService(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort, TokenPort tokenPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenPort = tokenPort;
    }

    @Override
    public AuthTokens login(String email, String password) {
        // Mesma exceção para e-mail inexistente e senha errada (ADR 002).
        User user = userRepositoryPort.findByEmail(email)
                .filter(found -> passwordHasherPort.matches(password, found.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return new AuthTokens(
                tokenPort.generateAccessToken(user.getId()),
                tokenPort.generateRefreshToken(user.getId()));
    }

    @Override
    public String refresh(String refreshToken) {
        UUID userId = tokenPort.parseRefreshToken(refreshToken);

        // Usuário excluído depois do login: o refresh token ainda é válido, mas não deve renovar nada.
        userRepositoryPort.findById(userId)
                .orElseThrow(InvalidTokenException::new);

        return tokenPort.generateAccessToken(userId);
    }
}
