package dev.pedroreis.rest_skeleton_java.auth.ports.inbound;

import dev.pedroreis.rest_skeleton_java.auth.domain.AuthTokens;

public interface LoginUseCase {
    /** @throws dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidCredentialsException e-mail inexistente ou senha errada */
    AuthTokens login(String email, String password);
}
