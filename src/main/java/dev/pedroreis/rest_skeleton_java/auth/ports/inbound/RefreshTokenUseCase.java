package dev.pedroreis.rest_skeleton_java.auth.ports.inbound;

public interface RefreshTokenUseCase {
    /**
     * Troca um refresh token válido por um novo access token. O refresh token continua o mesmo.
     *
     * @throws dev.pedroreis.rest_skeleton_java.auth.domain.exception.InvalidTokenException token inválido, expirado,
     *         de tipo errado ou de um usuário que não existe mais
     */
    String refresh(String refreshToken);
}
